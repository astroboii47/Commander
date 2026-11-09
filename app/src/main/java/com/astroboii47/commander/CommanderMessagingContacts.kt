package com.astroboii47.commander

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.Manifest
import android.content.pm.PackageManager
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.runtime.mutableIntStateOf
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class MessagingDiscovery(
    val identity: String,
    val packageName: String,
    val platform: String,
    val title: String,
    val shortcutId: String?,
    val lastSeen: Long,
    val address: String? = null,
)

data class CommanderMessagingContact(
    val id: String,
    val name: String,
    val identities: List<String>,
    val defaultIdentity: String? = null,
)

data class MessagingPlatformOption(
    val key: String,
    val label: String,
    val packageNames: Set<String>,
    val aliases: Set<String> = emptySet(),
)

data class SmsContactCandidate(val name: String, val number: String, val label: String)

object CommanderMessagingContacts {
    private const val PREFS = "commander_messaging_contacts"
    private const val DISCOVERIES = "discoveries"
    private const val CONTACTS = "contacts"
    private const val RETENTION_MS = 30L * 24L * 60L * 60L * 1000L
    private const val REPLY_ACTION_RETENTION_MS = 24L * 60L * 60L * 1000L
    private const val MAX_RETAINED_REPLY_ACTIONS = 64
    val revision = mutableIntStateOf(0)
    private data class RetainedReplyAction(
        val action: HubReplyAction,
        val capturedAt: Long,
    )
    private val retainedReplyActions = linkedMapOf<String, RetainedReplyAction>()
    private val builtInPlatforms = listOf(
        MessagingPlatformOption("sms", "Messages", emptySet(), setOf("text", "messages")),
        MessagingPlatformOption("whatsapp", "WhatsApp", setOf("com.whatsapp"), setOf("wa")),
        MessagingPlatformOption("messenger", "Messenger", setOf("com.facebook.orca"), setOf("msg", "fb")),
        MessagingPlatformOption("signal", "Signal", setOf("org.thoughtcrime.securesms")),
        MessagingPlatformOption("telegram", "Telegram", setOf("org.telegram.messenger"), setOf("tg")),
        MessagingPlatformOption("discord", "Discord", setOf("com.discord")),
    )

    fun platformOptions(context: Context): List<MessagingPlatformOption> {
        val knownPackages = discoveries(context).map(MessagingDiscovery::packageName).toSet()
        val installed = builtInPlatforms.filter { option ->
            option.key == "sms" || option.packageNames.any { packageName ->
                packageName in knownPackages || runCatching {
                    context.packageManager.getApplicationInfo(packageName, 0)
                }.isSuccess
            }
        }
        val builtInPackages = builtInPlatforms.flatMap(MessagingPlatformOption::packageNames).toSet()
        val discovered = discoveries(context)
            .filterNot { it.packageName in builtInPackages }
            .distinctBy(MessagingDiscovery::packageName)
            .map { discovery ->
                MessagingPlatformOption(
                    discovery.platform.lowercase().replace(Regex("[^a-z0-9]+"), "").ifBlank { "messages" },
                    discovery.platform,
                    setOf(discovery.packageName),
                )
            }
        return (installed + discovered).distinctBy(MessagingPlatformOption::key)
    }

    fun platformForInput(context: Context, input: String): MessagingPlatformOption? {
        val token = input.trim().substringBefore(' ').lowercase()
        return platformOptions(context).firstOrNull { token == it.key || token in it.aliases }
    }

    fun platformSuggestions(context: Context, input: String): List<ContactResult> {
        if (input.contains(' ')) return emptyList()
        val needle = input.trim().lowercase()
        return platformOptions(context).filter { option ->
            needle.isBlank() || option.key.startsWith(needle) || option.label.startsWith(needle, true) ||
                option.aliases.any { it.startsWith(needle) }
        }.map { option ->
            ContactResult(
                name = option.label,
                number = "",
                label = "search ${option.label} conversations",
                messagingPlatform = option.label,
                messagingPlatformScope = option.key,
                messagingPlatformIcon = option.packageNames.firstNotNullOfOrNull { loadCachedPlatformIcon(context, it) },
            )
        }
    }

    fun platformIcon(context: Context, option: MessagingPlatformOption): android.graphics.Bitmap? {
        val packageName = option.packageNames.firstOrNull() ?: if (option.key == "sms") {
            Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:")).resolveActivity(context.packageManager)?.packageName
        } else null
        return packageName?.let { platformIcon(context, it) }
    }

    private fun platformIcon(context: Context, packageName: String): android.graphics.Bitmap? = runCatching {
        context.packageManager.getApplicationIcon(packageName).toBitmap(72, 72)
    }.getOrNull()

    fun cachePlatformIcon(context: Context, packageName: String, bitmap: Bitmap?) {
        if (packageName.isBlank() || bitmap == null) return
        runCatching {
            FileOutputStream(platformIconFile(context, packageName)).use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        }
    }

    fun loadCachedPlatformIcon(context: Context, packageName: String): Bitmap? = runCatching {
        val persistent = platformIconFile(context, packageName)
        val legacy = legacyPlatformIconFile(context, packageName)
        val source = persistent.takeIf(File::exists) ?: legacy.takeIf(File::exists)
        source?.let { BitmapFactory.decodeFile(it.absolutePath) }
    }.getOrNull()

    private fun platformIconFile(context: Context, packageName: String) =
        File(context.filesDir, "message_platform_${packageName.hashCode().toUInt().toString(16)}.png")

    private fun legacyPlatformIconFile(context: Context, packageName: String) =
        File(context.cacheDir, "message_platform_${packageName.hashCode().toUInt().toString(16)}.png")

    fun isMessagingPackage(context: Context, packageName: String): Boolean =
        builtInPlatforms.any { packageName in it.packageNames } || discoveries(context).any { it.packageName == packageName }

    fun discover(context: Context, item: HubItem) {
        if (item.replyAction == null || item.isSummary) return
        val title = item.conversationTitle?.takeIf(String::isNotBlank) ?: item.title
        if (title.isBlank()) return
        val identity = identity(item.packageName, item.shortcutId, title)
        val current = discoveries(context).associateBy(MessagingDiscovery::identity).toMutableMap()
        current[identity] = MessagingDiscovery(
            identity,
            item.packageName,
            item.appName,
            title,
            item.shortcutId,
            System.currentTimeMillis(),
            null,
        )
        saveDiscoveries(context, current.values.toList())
        item.replyAction?.let { retainReplyAction(identity, it) }
    }

    @Synchronized
    private fun retainReplyAction(identity: String, action: HubReplyAction) {
        val now = System.currentTimeMillis()
        retainedReplyActions.entries.removeAll { now - it.value.capturedAt > REPLY_ACTION_RETENTION_MS }
        retainedReplyActions.remove(identity)
        retainedReplyActions[identity] = RetainedReplyAction(action, now)
        while (retainedReplyActions.size > MAX_RETAINED_REPLY_ACTIONS) {
            retainedReplyActions.remove(retainedReplyActions.keys.first())
        }
    }

    @Synchronized
    fun retainedReply(context: Context, contactId: String): Pair<String, HubReplyAction>? {
        val now = System.currentTimeMillis()
        retainedReplyActions.entries.removeAll { now - it.value.capturedAt > REPLY_ACTION_RETENTION_MS }
        val identities = contacts(context).firstOrNull { it.id == contactId }?.identities.orEmpty()
        return identities.mapNotNull { identity ->
            retainedReplyActions[identity]?.let { identity to it }
        }.maxByOrNull { it.second.capturedAt }?.let { it.first to it.second.action }
    }

    @Synchronized
    fun forgetRetainedReply(identity: String) {
        retainedReplyActions.remove(identity)
    }

    fun discoveries(context: Context): List<MessagingDiscovery> {
        val now = System.currentTimeMillis()
        val linked = contacts(context).flatMap(CommanderMessagingContact::identities).toSet()
        val parsed = parseArray(context, DISCOVERIES).mapNotNull { value ->
            runCatching {
                MessagingDiscovery(
                    value.getString("identity"),
                    value.getString("package"),
                    value.getString("platform"),
                    value.getString("title"),
                    value.optString("shortcut").takeIf(String::isNotBlank),
                    value.getLong("seen"),
                    value.optString("address").takeIf(String::isNotBlank),
                )
            }.getOrNull()
        }.filter { it.identity in linked || now - it.lastSeen <= RETENTION_MS }
        if (parsed.size != parseArray(context, DISCOVERIES).size) saveDiscoveries(context, parsed)
        return parsed.sortedByDescending(MessagingDiscovery::lastSeen)
    }

    fun recent(context: Context): List<MessagingDiscovery> {
        val linked = contacts(context).flatMap(CommanderMessagingContact::identities).toSet()
        return discoveries(context).filterNot { it.identity in linked }
    }

    fun contacts(context: Context): List<CommanderMessagingContact> =
        parseArray(context, CONTACTS).mapNotNull { value ->
            runCatching {
                val links = value.getJSONArray("identities")
                CommanderMessagingContact(
                    value.getString("id"),
                    value.getString("name"),
                    List(links.length()) { links.getString(it) },
                    value.optString("defaultIdentity").takeIf(String::isNotBlank),
                )
            }.getOrNull()
        }.sortedBy { it.name.lowercase() }

    fun create(context: Context, discovery: MessagingDiscovery, name: String = discovery.title): CommanderMessagingContact {
        val contact = CommanderMessagingContact(
            UUID.randomUUID().toString(),
            name.trim().ifBlank { discovery.title },
            listOf(discovery.identity),
            discovery.identity,
        )
        saveContacts(context, contacts(context) + contact)
        return contact
    }

    fun addTo(context: Context, contactId: String, identity: String) {
        saveContacts(context, contacts(context).map {
            if (it.id == contactId) it.copy(
                identities = (it.identities + identity).distinct(),
                defaultIdentity = it.defaultIdentity ?: identity,
            ) else it
        })
    }

    fun setDefault(context: Context, contactId: String, identity: String) {
        saveContacts(context, contacts(context).map { contact ->
            if (contact.id == contactId && identity in contact.identities) contact.copy(defaultIdentity = identity) else contact
        })
    }

    fun addSmsTo(context: Context, contactId: String, candidate: SmsContactCandidate) {
        val normalized = PhoneNumberUtils.normalizeNumber(candidate.number)
        if (normalized.isBlank()) return
        val identity = "sms::$normalized"
        val current = discoveries(context).associateBy(MessagingDiscovery::identity).toMutableMap()
        current[identity] = MessagingDiscovery(
            identity = identity,
            packageName = "sms",
            platform = "SMS",
            title = candidate.name,
            shortcutId = null,
            lastSeen = System.currentTimeMillis(),
            address = candidate.number,
        )
        saveDiscoveries(context, current.values.toList())
        addTo(context, contactId, identity)
    }

    fun smsContacts(context: Context, query: String, limit: Int = 30): List<SmsContactCandidate> {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) return emptyList()
        val needle = query.trim()
        val selection = if (needle.isBlank()) null else "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?"
        val args = if (needle.isBlank()) null else arrayOf("%$needle%")
        return context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.TYPE,
            ),
            selection,
            args,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} COLLATE NOCASE ASC",
        )?.use { cursor ->
            val nameColumn = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberColumn = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val typeColumn = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.TYPE)
            buildList {
                while (cursor.moveToNext() && size < limit) {
                    val number = cursor.getString(numberColumn)
                    add(SmsContactCandidate(
                        cursor.getString(nameColumn),
                        number,
                        ContactsContract.CommonDataKinds.Phone.getTypeLabel(
                            context.resources,
                            cursor.getInt(typeColumn),
                            "phone",
                        ).toString().lowercase(),
                    ))
                }
            }.distinctBy { PhoneNumberUtils.normalizeNumber(it.number) }
        }.orEmpty()
    }

    fun removeFrom(context: Context, contactId: String, identity: String) {
        saveContacts(context, contacts(context).map { contact ->
            if (contact.id == contactId) {
                val remaining = contact.identities.filterNot { it == identity }
                contact.copy(
                    identities = remaining,
                    defaultIdentity = if (contact.defaultIdentity == identity) remaining.firstOrNull() else contact.defaultIdentity,
                )
            }
            else contact
        })
    }

    fun rename(context: Context, contactId: String, name: String) {
        val clean = name.trim()
        if (clean.isBlank()) return
        saveContacts(context, contacts(context).map { if (it.id == contactId) it.copy(name = clean) else it })
    }

    fun deleteContact(context: Context, contactId: String) {
        saveContacts(context, contacts(context).filterNot { it.id == contactId })
    }

    fun deleteDiscovery(context: Context, identity: String) {
        saveDiscoveries(context, discoveries(context).filterNot { it.identity == identity })
    }

    fun clearRecent(context: Context) {
        val linked = contacts(context).flatMap(CommanderMessagingContact::identities).toSet()
        saveDiscoveries(context, discoveries(context).filter { it.identity in linked })
    }

    fun search(
        context: Context,
        query: String,
        limit: Int,
        platform: MessagingPlatformOption? = null,
    ): List<ContactResult> {
        val needle = query.trim()
        val discoveries = discoveries(context).associateBy(MessagingDiscovery::identity)
        return contacts(context).asSequence()
            .filter { contact ->
                contact.identities.any { identity ->
                    val route = discoveries[identity]
                    route != null && (platform == null || route.packageName in platform.packageNames)
                } && (needle.isBlank() || contact.name.contains(needle, true) || contact.identities.any { identity ->
                    discoveries[identity]?.title?.contains(needle, true) == true
                })
            }
            .sortedWith(
                compareBy<CommanderMessagingContact> {
                    val name = it.name.trim().lowercase()
                    when {
                        name == needle.lowercase() -> 0
                        name.startsWith(needle, true) -> 1
                        name.split(Regex("\\s+")).any { word -> word.startsWith(needle, true) } -> 2
                        else -> 3
                    }
                }.thenBy { it.name.length }.thenBy { it.name.lowercase() },
            )
            .flatMap { contact ->
                contact.identities.sortedByDescending { it == contact.defaultIdentity }.asSequence().mapNotNull { identity ->
                    val route = discoveries[identity] ?: return@mapNotNull null
                    if (platform != null && route.packageName !in platform.packageNames) return@mapNotNull null
                    if (needle.isNotBlank() && !contact.name.contains(needle, true) && !route.title.contains(needle, true)) {
                        return@mapNotNull null
                    }
                    val liveItem = HubRepository.items.firstOrNull { item ->
                        item.replyAction != null && identity == CommanderMessagingContacts.identity(
                            item.packageName,
                            item.shortcutId,
                            item.conversationTitle ?: item.title,
                        )
                    }
                    ContactResult(
                        name = contact.name,
                        number = route.address.orEmpty(),
                        label = listOf(
                            route.platform,
                            liveItem?.text?.take(72)?.takeIf(String::isNotBlank)
                                ?: route.title.takeIf { !it.equals(contact.name, true) },
                            if (isReplyAvailable(route)) "reply available"
                            else if (route.packageName == "com.facebook.orca" && route.shortcutId != null) "open conversation"
                            else "notification required",
                        )
                            .filterNotNull().joinToString(" · "),
                        commanderMessagingContactId = contact.id,
                        messagingIdentity = route.identity,
                        messagingPlatform = route.platform,
                        messagingRouteTitle = route.title,
                        messagingPackageName = route.packageName,
                        messagingPlatformIcon = loadCachedPlatformIcon(context, route.packageName),
                        messagingIsDefault = identity == contact.defaultIdentity,
                    )
                }
            }
            .take(limit)
            .toList()
    }

    fun bestRoute(context: Context, contactId: String, preferredIdentity: String? = null): Pair<MessagingDiscovery?, HubItem?> {
        val contact = contacts(context).firstOrNull { it.id == contactId } ?: return null to null
        val known = discoveries(context).associateBy(MessagingDiscovery::identity)
        val available = contact.identities.mapNotNull(known::get)
        // Once the user selects a platform identity, never silently fall back
        // to another app belonging to the same Commander person.
        val routes = preferredIdentity?.let { preferred ->
            available.firstOrNull { it.identity == preferred }?.let(::listOf)
        } ?: available.sortedByDescending { it.identity == contact.defaultIdentity }
        val preferredLive = preferredIdentity?.let { preferred ->
            HubRepository.items.firstOrNull { item ->
                item.replyAction != null && preferred == identity(
                    item.packageName,
                    item.shortcutId,
                    item.conversationTitle ?: item.title,
                )
            }
        }
        val live = preferredLive ?: HubRepository.items.firstOrNull { item ->
            item.replyAction != null && routes.any { it.identity == identity(item.packageName, item.shortcutId, item.conversationTitle ?: item.title) }
        }
        if (live != null) {
            val route = routes.firstOrNull { it.identity == identity(live.packageName, live.shortcutId, live.conversationTitle ?: live.title) }
            return route to live
        }
        return routes.firstOrNull { it.packageName == "com.facebook.orca" && it.shortcutId != null }
            ?.let { it to null } ?: (routes.firstOrNull() to null)
    }

    fun isReplyAvailable(discovery: MessagingDiscovery): Boolean = HubRepository.items.any { item ->
        item.replyAction != null && discovery.identity == identity(item.packageName, item.shortcutId, item.conversationTitle ?: item.title)
    }

    fun identity(packageName: String, shortcutId: String?, title: String): String {
        val stable = shortcutId?.takeIf(String::isNotBlank)
            ?: title.trim().lowercase().replace(Regex("\\s+"), " ")
        return "$packageName::$stable"
    }

    private fun parseArray(context: Context, key: String): List<JSONObject> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(key, "[]") ?: "[]"
        return runCatching {
            val array = JSONArray(raw)
            List(array.length()) { array.getJSONObject(it) }
        }.getOrDefault(emptyList())
    }

    private fun saveDiscoveries(context: Context, values: List<MessagingDiscovery>) {
        val array = JSONArray()
        values.forEach { value -> array.put(JSONObject().apply {
            put("identity", value.identity)
            put("package", value.packageName)
            put("platform", value.platform)
            put("title", value.title)
            put("shortcut", value.shortcutId.orEmpty())
            put("seen", value.lastSeen)
            put("address", value.address.orEmpty())
        }) }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(DISCOVERIES, array.toString()).apply()
        revision.intValue++
    }

    private fun saveContacts(context: Context, values: List<CommanderMessagingContact>) {
        val array = JSONArray()
        values.forEach { value -> array.put(JSONObject().apply {
            put("id", value.id)
            put("name", value.name)
            put("identities", JSONArray(value.identities))
            put("defaultIdentity", value.defaultIdentity.orEmpty())
        }) }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(CONTACTS, array.toString()).apply()
        revision.intValue++
    }
}
