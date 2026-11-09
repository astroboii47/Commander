package com.astroboii47.commander

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream
import android.app.PendingIntent
import android.app.ActivityOptions
import android.os.Build

object MessengerSettings {
    private const val PREFS = "messenger_settings"
    private const val PHOTOS = "profile_photos"
    private const val COMMAND_BAR_GLYPHS = "command_bar_message_glyphs"
    fun profilePhotos(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(PHOTOS, false)
    fun setProfilePhotos(context: Context, enabled: Boolean) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(PHOTOS, enabled).apply()
    fun commandBarMessageGlyphs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(COMMAND_BAR_GLYPHS, true)
    fun setCommandBarMessageGlyphs(context: Context, enabled: Boolean) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(COMMAND_BAR_GLYPHS, enabled).apply()
}

object MessengerChatStore {
    private const val PACKAGE = "com.facebook.orca"
    private const val PREFS = "messenger_chats"
    private const val CONTACT_PREFS = "messenger_contact_settings"
    private const val SAVED = "saved"
    private const val HIDDEN = "hidden"
    private const val SEEN_PREFIX = "seen_"
    private const val NAME_PREFIX = "name_"
    private const val DISCOVERY_RETENTION_MS = 30L * 24L * 60L * 60L * 1000L
    private val liveContentIntents = mutableMapOf<String, PendingIntent>()

    fun discover(context: Context, shortcutId: String?, label: String?, avatar: Bitmap? = null) {
        val id = shortcutId?.trim().orEmpty()
        val name = label?.trim().orEmpty()
        if (id.isBlank() || name.isBlank()) return
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(id, name).apply()
        context.getSharedPreferences(CONTACT_PREFS, Context.MODE_PRIVATE).edit()
            .putLong(SEEN_PREFIX + id, System.currentTimeMillis()).apply()
        if (MessengerSettings.profilePhotos(context) && avatar != null) runCatching {
            FileOutputStream(avatarFile(context, id)).use { avatar.compress(Bitmap.CompressFormat.PNG, 90, it) }
        }
    }

    fun attachLiveIntent(shortcutId: String?, contentIntent: PendingIntent?) {
        val id = shortcutId?.takeIf(String::isNotBlank) ?: return
        if (contentIntent != null) liveContentIntents[id] = contentIntent
    }

    fun search(context: Context, query: String, limit: Int): List<ContactResult> {
        val needle = query.trim()
        if (needle.isBlank() || !isInstalled(context)) return emptyList()
        val settings = context.getSharedPreferences(CONTACT_PREFS, Context.MODE_PRIVATE)
        val saved = settings.getStringSet(SAVED, emptySet()).orEmpty()
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).all
            .mapNotNull { (id, value) ->
                if (id !in saved) return@mapNotNull null
                val name = value as? String ?: return@mapNotNull null
                val displayName = settings.getString(NAME_PREFIX + id, null)?.takeIf(String::isNotBlank) ?: name
                if (!displayName.contains(needle, ignoreCase = true)) return@mapNotNull null
                ContactResult(displayName, "", "Messenger", id, loadAvatar(context, id))
            }
            .sortedWith(compareBy<ContactResult> { !it.name.startsWith(needle, true) }.thenBy { it.name.lowercase() })
            .take(limit)
    }

    data class DiscoveredConversation(
        val id: String,
        val originalName: String,
        val displayName: String,
        val saved: Boolean,
        val hidden: Boolean,
    )

    fun discovered(context: Context, limit: Int = Int.MAX_VALUE, includeHidden: Boolean = false): List<DiscoveredConversation> {
        val now = System.currentTimeMillis()
        val settings = context.getSharedPreferences(CONTACT_PREFS, Context.MODE_PRIVATE)
        val saved = settings.getStringSet(SAVED, emptySet()).orEmpty()
        val hidden = settings.getStringSet(HIDDEN, emptySet()).orEmpty()
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).all
            .mapNotNull { (id, value) ->
                val original = value as? String ?: return@mapNotNull null
                val isHidden = id in hidden
                if (isHidden && !includeHidden) return@mapNotNull null
                val seenKey = SEEN_PREFIX + id
                var seen = settings.getLong(seenKey, 0L)
                if (seen == 0L) {
                    seen = now
                    settings.edit().putLong(seenKey, seen).apply()
                }
                if (id !in saved && now - seen > DISCOVERY_RETENTION_MS) return@mapNotNull null
                DiscoveredConversation(
                    id,
                    original,
                    settings.getString(NAME_PREFIX + id, null)?.takeIf(String::isNotBlank) ?: original,
                    id in saved,
                    isHidden,
                ) to seen
            }
            .sortedWith(compareByDescending<Pair<DiscoveredConversation, Long>> { it.first.saved }.thenByDescending { it.second })
            .take(limit)
            .map { it.first }
    }

    fun save(context: Context, id: String, displayName: String? = null) {
        val prefs = context.getSharedPreferences(CONTACT_PREFS, Context.MODE_PRIVATE)
        val saved = prefs.getStringSet(SAVED, emptySet()).orEmpty().toMutableSet().apply { add(id) }
        prefs.edit().putStringSet(SAVED, saved).apply {
            displayName?.trim()?.takeIf(String::isNotBlank)?.let { putString(NAME_PREFIX + id, it) }
        }.apply()
    }

    fun unsave(context: Context, id: String) {
        val prefs = context.getSharedPreferences(CONTACT_PREFS, Context.MODE_PRIVATE)
        val saved = prefs.getStringSet(SAVED, emptySet()).orEmpty().toMutableSet().apply { remove(id) }
        prefs.edit().putStringSet(SAVED, saved).remove(NAME_PREFIX + id).apply()
    }

    fun rename(context: Context, id: String, displayName: String) {
        val name = displayName.trim()
        if (name.isBlank()) return
        context.getSharedPreferences(CONTACT_PREFS, Context.MODE_PRIVATE).edit()
            .putString(NAME_PREFIX + id, name).apply()
    }

    fun hide(context: Context, id: String) {
        val prefs = context.getSharedPreferences(CONTACT_PREFS, Context.MODE_PRIVATE)
        val hidden = prefs.getStringSet(HIDDEN, emptySet()).orEmpty().toMutableSet().apply { add(id) }
        val saved = prefs.getStringSet(SAVED, emptySet()).orEmpty().toMutableSet().apply { remove(id) }
        prefs.edit().putStringSet(HIDDEN, hidden).putStringSet(SAVED, saved).remove(NAME_PREFIX + id).apply()
    }

    fun restore(context: Context, id: String) {
        val prefs = context.getSharedPreferences(CONTACT_PREFS, Context.MODE_PRIVATE)
        val hidden = prefs.getStringSet(HIDDEN, emptySet()).orEmpty().toMutableSet().apply { remove(id) }
        prefs.edit().putStringSet(HIDDEN, hidden).apply()
    }

    fun delete(context: Context, id: String) {
        val settings = context.getSharedPreferences(CONTACT_PREFS, Context.MODE_PRIVATE)
        val saved = settings.getStringSet(SAVED, emptySet()).orEmpty().toMutableSet().apply { remove(id) }
        val hidden = settings.getStringSet(HIDDEN, emptySet()).orEmpty().toMutableSet().apply { remove(id) }
        settings.edit()
            .putStringSet(SAVED, saved)
            .putStringSet(HIDDEN, hidden)
            .remove(SEEN_PREFIX + id)
            .remove(NAME_PREFIX + id)
            .apply()
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(id).apply()
        liveContentIntents.remove(id)
        avatarFile(context, id).delete()
    }

    fun clearUnlinked(context: Context) {
        val settings = context.getSharedPreferences(CONTACT_PREFS, Context.MODE_PRIVATE)
        val saved = settings.getStringSet(SAVED, emptySet()).orEmpty()
        val discoveries = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val editor = discoveries.edit()
        discoveries.all.keys.filterNot(saved::contains).forEach(editor::remove)
        editor.apply()
    }

    fun openDraft(context: Context, target: ContactResult, body: String): String? {
        val shortcutId = target.messengerShortcutId ?: return "Messenger conversation is unavailable"
        liveContentIntents[shortcutId]?.let { pendingIntent ->
            MessengerDraftBridge.arm(context, body)
            return runCatching {
                val options = ActivityOptions.makeBasic().apply {
                    if (Build.VERSION.SDK_INT >= 34) {
                        pendingIntentBackgroundActivityStartMode = ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                    }
                }
                pendingIntent.send(context, 0, null, null, null, null, options.toBundle())
                null
            }.getOrElse {
                MessengerDraftBridge.clear(context)
                "Messenger notification action expired · wait for a new message"
            }
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            setClassName(PACKAGE, "com.facebook.messenger.intents.ShareIntentHandler")
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, body)
            // Messenger's share activity parses this extra as a Long even
            // though its published encrypted/group shortcut IDs contain a
            // textual prefix. Passing the full published ID crashes Messenger.
            shortcutId.substringAfterLast(':').takeIf { it.all(Char::isDigit) }?.let {
                putExtra(Intent.EXTRA_SHORTCUT_ID, it)
            }
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        MessengerShareSearchBridge.arm(context, target.name)
        return runCatching { context.startActivity(intent); null }.getOrElse {
            MessengerShareSearchBridge.clear(context)
            MessengerDraftBridge.clear(context)
            "Messenger could not prepare this message"
        }
    }

    private fun isInstalled(context: Context) = runCatching {
        context.packageManager.getPackageInfo(PACKAGE, 0)
    }.isSuccess

    private fun avatarFile(context: Context, id: String) = File(context.cacheDir, "messenger_${id.hashCode()}.png")
    private fun loadAvatar(context: Context, id: String): Bitmap? {
        if (!MessengerSettings.profilePhotos(context)) return null
        return runCatching { BitmapFactory.decodeFile(avatarFile(context, id).absolutePath) }.getOrNull()
    }
}

object MessengerShareSearchBridge {
    private const val PREFS = "messenger_share_search"
    private const val TARGET = "target"
    private const val ARMED_AT = "armed_at"
    private const val STAGE = "stage"
    private const val READY = "ready"
    private const val TIMEOUT_MS = 20_000L

    data class Pending(val target: String, val stage: Int, val ready: Boolean)

    fun arm(context: Context, target: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(TARGET, target.trim())
            .putLong(ARMED_AT, System.currentTimeMillis())
            .putInt(STAGE, 0)
            .putBoolean(READY, false)
            .apply()
        HomeTypingAccessibilityService.monitorAutomation(TIMEOUT_MS)
    }

    fun pending(context: Context): Pending? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (System.currentTimeMillis() - prefs.getLong(ARMED_AT, 0L) > TIMEOUT_MS) {
            clear(context)
            return null
        }
        val target = prefs.getString(TARGET, null)?.takeIf(String::isNotBlank) ?: return null
        return Pending(target, prefs.getInt(STAGE, 0), prefs.getBoolean(READY, false))
    }

    fun setStage(context: Context, stage: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(STAGE, stage).apply()
    }

    fun markReady(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(READY, true).apply()
        HomeTypingAccessibilityService.automationFinished()
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
        HomeTypingAccessibilityService.automationFinished()
    }
}

object MessengerDraftBridge {
    private const val PREFS = "messenger_draft"
    private const val TEXT = "text"
    private const val ARMED_AT = "armed_at"

    fun arm(context: Context, text: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(TEXT, text).putLong(ARMED_AT, System.currentTimeMillis()).apply()
        HomeTypingAccessibilityService.monitorAutomation(15_000L)
    }

    fun pending(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (System.currentTimeMillis() - prefs.getLong(ARMED_AT, 0L) > 15_000L) {
            prefs.edit().clear().apply()
            return null
        }
        return prefs.getString(TEXT, null)
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
        HomeTypingAccessibilityService.automationFinished()
    }
}
