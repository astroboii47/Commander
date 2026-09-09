package com.astroboii47.commander

import android.app.Notification
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.graphics.drawable.toBitmap
import android.os.Build
import android.graphics.Bitmap

data class HubItem(
    val key: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val conversationTitle: String?,
    val text: String,
    val expandedText: String,
    val textLines: List<String>,
    val messages: List<HubMessage>,
    val displayImage: Bitmap?,
    val displayImageIsMonochrome: Boolean,
    val time: Long,
    val category: String?,
    val contentIntent: PendingIntent?,
    val replyAction: HubReplyAction?,
    val notificationActions: List<HubNotificationAction>,
    val hasAttachment: Boolean,
    val isSummary: Boolean,
    val isGroupHeader: Boolean,
    val groupKey: String?,
    val channelId: String?,
    val shortcutId: String?,
    val sourceKeys: Set<String>,
    val detached: Boolean = false,
)

data class HubNotificationAction(
    val title: String,
    val pendingIntent: PendingIntent,
    val semanticAction: Int,
    val destructive: Boolean,
)

data class HubMessage(
    val text: String,
    val sender: String?,
    val outgoing: Boolean,
    val time: Long? = null,
)

data class HubReplyAction(
    val pendingIntent: PendingIntent,
    val remoteInputs: Array<RemoteInput>,
)

object HubRepository {
    var items by mutableStateOf<List<HubItem>>(emptyList())
        private set
    private var dismissNotification: ((String) -> Unit)? = null
    private var snoozeNotification: ((String, Long) -> Unit)? = null
    private var refreshNotifications: (() -> Unit)? = null
    private var archiveNotification: ((HubItem) -> Unit)? = null
    private data class PendingReply(
        val message: HubMessage,
        val sentAt: Long,
        var acknowledged: Boolean = false,
    )
    private val sentReplies = mutableMapOf<String, MutableList<PendingReply>>()
    private val hiddenSummaryKeys = mutableSetOf<String>()
    private data class PendingNotificationAction(val packageName: String, val phrases: Set<String>, val time: Long)
    private val pendingNotificationActions = mutableListOf<PendingNotificationAction>()
    private val suppressedActionConfirmationKeys = mutableMapOf<String, Long>()
    private data class PendingExternalMutation(val packageName: String, val selectedKeys: Set<String>, val time: Long)
    private var pendingExternalMutation: PendingExternalMutation? = null

    fun prepareExternalMutation(item: HubItem) {
        pendingExternalMutation = PendingExternalMutation(item.packageName, item.sourceKeys.ifEmpty { setOf(item.key) }, System.currentTimeMillis())
    }

    fun recordNotificationAction(packageName: String, title: String) {
        val action = title.trim().lowercase()
        val phrases = when {
            "archive" in action -> setOf("archived", "message archived", "conversation archived")
            "delete" in action || "trash" in action -> setOf("deleted", "message deleted", "moved to trash")
            "mark" in action && "read" in action -> setOf("marked as read", "message read")
            "complete" in action || "done" in action -> setOf("completed", "marked complete", "task completed")
            "snooze" in action -> setOf("snoozed")
            else -> emptySet()
        }
        if (phrases.isNotEmpty()) {
            val now = System.currentTimeMillis()
            pendingNotificationActions.removeAll { now - it.time > ACTION_CONFIRMATION_WINDOW_MS }
            pendingNotificationActions += PendingNotificationAction(packageName, phrases, now)
        }
    }

    private fun consumeActionConfirmation(item: HubItem): Boolean {
        val now = System.currentTimeMillis()
        pendingNotificationActions.removeAll { now - it.time > ACTION_CONFIRMATION_WINDOW_MS }
        suppressedActionConfirmationKeys.entries.removeAll { now - it.value > SUPPRESSED_CONFIRMATION_RETENTION_MS }
        if (item.key in suppressedActionConfirmationKeys) return true
        val content = "${item.title} ${item.text}".trim().lowercase()
        val index = pendingNotificationActions.indexOfFirst { pending ->
            pending.packageName == item.packageName && pending.phrases.any { it in content }
        }
        if (index < 0) return false
        pendingNotificationActions.removeAt(index)
        suppressedActionConfirmationKeys[item.key] = now
        return true
    }

    fun attachDismissHandler(handler: ((String) -> Unit)?) {
        dismissNotification = handler
    }

    fun attachSnoozeHandler(handler: ((String, Long) -> Unit)?) {
        snoozeNotification = handler
    }

    fun attachRefreshHandler(handler: (() -> Unit)?) {
        refreshNotifications = handler
    }

    fun attachArchiveHandler(handler: ((HubItem) -> Unit)?) {
        archiveNotification = handler
    }

    fun requestRefresh() {
        refreshNotifications?.invoke()
    }

    fun dismiss(key: String) {
        val item = items.firstOrNull { it.key == key || key in it.sourceKeys }
        item?.let { archiveNotification?.invoke(it) }
        val keys = item?.sourceKeys.orEmpty().ifEmpty { setOf(key) }
        items = items.filterNot { it === item || it.key == key }
        keys.forEach {
            sentReplies.remove(it)
            dismissNotification?.invoke(it)
        }
    }

    fun dismiss(item: HubItem) {
        if (item.isSummary) {
            // Cancelling an Android group summary commonly cancels every child
            // notification too. Hide only Commander's aggregate row so the
            // individual notifications remain intact and actionable.
            hiddenSummaryKeys += item.sourceKeys
            items = items.filterNot { it.key == item.key }
        } else {
            dismiss(item.key)
        }
    }

    fun snooze(item: HubItem, durationMillis: Long) {
        val keys = item.sourceKeys.ifEmpty { setOf(item.key) }
        items = items.filterNot { it.key == item.key || it.sourceKeys.any(keys::contains) }
        keys.forEach { key ->
            sentReplies.remove(key)
            snoozeNotification?.invoke(key, durationMillis)
        }
    }

    fun applyIgnoreRules() {
        items = items.filterNot(HubIgnoreSettings::matches)
    }

    fun recordSentReply(key: String, text: String) {
        val now = System.currentTimeMillis()
        val reply = HubMessage(text, null, true, now)
        val source = items.firstOrNull { it.key == key || key in it.sourceKeys }
        val historyKey = source?.replyHistoryKey() ?: key
        sentReplies.getOrPut(historyKey) { mutableListOf() }.add(PendingReply(reply, now))
        items = items.map { item ->
            if (item.key == key || key in item.sourceKeys) item.copy(messages = item.messages + reply) else item
        }
    }

    private fun mergeSentReplies(item: HubItem): HubItem {
        val historyKey = item.replyHistoryKey()
        val pending = sentReplies[historyKey] ?: sentReplies[item.key] ?: return item
        val now = System.currentTimeMillis()
        pending.removeAll { now - it.sentAt > SENT_REPLY_RETENTION_MS }
        if (pending.isEmpty()) {
            sentReplies.remove(historyKey)
            sentReplies.remove(item.key)
            return item
        }

        val merged = item.messages.toMutableList()
        val claimed = mutableSetOf<Int>()
        pending.forEach { local ->
            val normalized = local.message.text.normalizedReplyText()
            val matchIndex = merged.indices.reversed().firstOrNull { index ->
                val candidate = merged[index]
                val closeInTime = candidate.time == null ||
                    kotlin.math.abs(candidate.time - local.sentAt) <= SENT_REPLY_MATCH_WINDOW_MS
                index !in claimed && closeInTime && candidate.text.normalizedReplyText() == normalized
            }
            if (matchIndex != null) {
                claimed += matchIndex
                merged[matchIndex] = merged[matchIndex].copy(sender = null, outgoing = true)
                local.acknowledged = true
            } else {
                // Some messaging apps acknowledge an outgoing reply once and
                // omit it again from later notification payloads. Keep the
                // Commander copy in the conversation for the retention window.
                merged += local.message
            }
        }
        return item.copy(messages = merged)
    }

    fun replace(notifications: Array<StatusBarNotification>, service: NotificationListenerService) {
        items = notifications
            .asSequence()
            .filter { !it.isOngoing }
            .onEach { it.cacheMessagingPlatformIcon(service) }
            .onEach { it.discoverMessengerConversation(service) }
            .mapNotNull { it.toHubItem(service) }
            .filterNot(::consumeActionConfirmation)
            .filterNot(HubIgnoreSettings::matches)
            .filterNot { it.isSummary && it.key in hiddenSummaryKeys }
            .map(::mergeSentReplies)
            .toList()
            .let(::deduplicateNotifications)
            .sortedByDescending { it.time }
        items.forEach { CommanderMessagingContacts.discover(service.applicationContext, it) }
    }

    fun upsert(notification: StatusBarNotification, service: NotificationListenerService) {
        if (notification.isOngoing) {
            remove(notification.key)
            return
        }
        notification.discoverMessengerConversation(service)
        notification.cacheMessagingPlatformIcon(service)
        val converted = notification.toHubItem(service)?.takeUnless(::consumeActionConfirmation)
            ?.takeUnless(HubIgnoreSettings::matches)?.let(::mergeSentReplies)
            ?.takeUnless { it.isSummary && it.key in hiddenSummaryKeys }
        converted?.let { CommanderMessagingContacts.discover(service.applicationContext, it) }
        items = if (converted == null) {
            items.filterNot { it.key == notification.key }
        } else {
            deduplicateNotifications(items.filterNot { it.key == notification.key || notification.key in it.sourceKeys } + converted)
                .sortedByDescending(HubItem::time)
        }
    }

    fun remove(key: String) {
        val mutation = pendingExternalMutation?.takeIf {
            System.currentTimeMillis() - it.time <= EXTERNAL_MUTATION_WINDOW_MS
        }.also { if (it == null) pendingExternalMutation = null }
        val affected = items.firstOrNull { it.key == key || key in it.sourceKeys }
        if (affected != null && mutation?.packageName == affected.packageName && key !in mutation.selectedKeys) {
            // Some apps clear every notification in a group after opening or
            // acting on one child. Preserve collateral siblings in Hub, but
            // remove actions whose PendingIntents are no longer live.
            items = items.map { item ->
                if (item !== affected) item else item.copy(
                    contentIntent = null,
                    replyAction = null,
                    notificationActions = emptyList(),
                    sourceKeys = item.sourceKeys - key,
                    detached = true,
                )
            }
            return
        }
        items = items.mapNotNull { item ->
            if (key !in item.sourceKeys) item else {
                val remaining = item.sourceKeys - key
                when {
                    remaining.isEmpty() -> null
                    item.key == key -> item.copy(key = remaining.first(), sourceKeys = remaining)
                    else -> item.copy(sourceKeys = remaining)
                }
            }
        }
        // Notification reply commonly removes the live notification even
        // though the conversation continues. Conversation-keyed sent history
        // must survive that removal so the next notification can merge it.
        hiddenSummaryKeys.remove(key)
        suppressedActionConfirmationKeys.remove(key)
    }
}

private const val SENT_REPLY_RETENTION_MS = 24 * 60 * 60 * 1000L
private const val SENT_REPLY_MATCH_WINDOW_MS = 2 * 60 * 1000L
private const val ACTION_CONFIRMATION_WINDOW_MS = 5_000L
private const val SUPPRESSED_CONFIRMATION_RETENTION_MS = 60_000L
private const val EXTERNAL_MUTATION_WINDOW_MS = 5_000L
private fun String.normalizedReplyText(): String = trim().replace(Regex("\\s+"), " ")

private fun HubItem.replyHistoryKey(): String = buildString {
    append(packageName)
    append('|')
    append(shortcutId?.takeIf(String::isNotBlank) ?: channelId?.takeIf(String::isNotBlank) ?: "notification")
    append('|')
    append((conversationTitle ?: title).trim().lowercase())
}

class HubNotificationService : NotificationListenerService() {
    override fun onListenerConnected() {
        HubRepository.attachDismissHandler(::cancelNotification)
        HubRepository.attachSnoozeHandler { key, durationMillis ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) snoozeNotification(key, durationMillis)
        }
        HubRepository.attachRefreshHandler(::refresh)
        HubRepository.attachArchiveHandler { HubHistoryStore.archive(applicationContext, it) }
        refresh()
    }
    override fun onListenerDisconnected() {
        HubRepository.attachDismissHandler(null)
        HubRepository.attachSnoozeHandler(null)
        HubRepository.attachRefreshHandler(null)
        HubRepository.attachArchiveHandler(null)
    }
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        ChatGptNotificationBridge.onNotification(applicationContext, sbn)
        runCatching { HubRepository.upsert(sbn, this) }
    }
    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        sbn?.let {
            HubRepository.items.firstOrNull { item -> item.key == it.key || it.key in item.sourceKeys }
                ?.let { item -> HubHistoryStore.archive(applicationContext, item) }
            HubRepository.remove(it.key)
        }
    }

    private fun refresh() {
        runCatching { HubRepository.replace(activeNotifications ?: emptyArray(), this) }
    }
}

private fun StatusBarNotification.cacheMessagingPlatformIcon(service: NotificationListenerService) {
    val messageLike = notification.category == Notification.CATEGORY_MESSAGE ||
        notification.actions.orEmpty().any { action -> action.remoteInputs?.isNotEmpty() == true }
    if (!messageLike && !CommanderMessagingContacts.isMessagingPackage(service.applicationContext, packageName)) return
    val icon = runCatching { notification.smallIcon?.loadDrawable(service)?.toBitmap(72, 72) }.getOrNull()
    CommanderMessagingContacts.cachePlatformIcon(service.applicationContext, packageName, icon)
}

private fun StatusBarNotification.discoverMessengerConversation(service: NotificationListenerService) {
    if (packageName != "com.facebook.orca" || notification.flags and Notification.FLAG_GROUP_SUMMARY != 0) return
    val extras = notification.extras
    val label = extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)?.toString()
        ?: extras.getCharSequence("android.hiddenConversationTitle")?.toString()
        ?: extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
    val avatar = if (MessengerSettings.profilePhotos(service.applicationContext)) runCatching {
        notification.getLargeIcon()?.loadDrawable(service)?.toBitmap(96, 96)
    }.getOrNull() else null
    MessengerChatStore.discover(service.applicationContext, notification.shortcutId, label, avatar)
    MessengerChatStore.attachLiveIntent(notification.shortcutId, notification.contentIntent)
}

private val appLabelCache = mutableMapOf<String, String>()

private fun StatusBarNotification.toHubItem(service: NotificationListenerService): HubItem? {
    val extras = notification.extras
    val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
    val suppliedConversationTitle = (
        extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)?.toString()
            ?: extras.getCharSequence("android.hiddenConversationTitle")?.toString()
        )?.replace(Regex("\\s*\\(\\d+\\s+messages?\\)\\s*$", RegexOption.IGNORE_CASE), "")
        ?.trim()?.takeIf(String::isNotBlank)
    val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()
    val expandedText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()
    val textLines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES).orEmpty()
        .mapNotNull { it?.toString()?.trim()?.takeIf(String::isNotBlank) }
    if (title.isBlank() && text.isBlank()) return null
    val appName = appLabelCache.getOrPut(packageName) {
        runCatching {
            val info = service.packageManager.getApplicationInfo(packageName, 0)
            service.packageManager.getApplicationLabel(info).toString()
        }.getOrDefault(packageName.substringAfterLast('.'))
    }
    val messages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) runCatching {
        val selfDisplayName = extras.getCharSequence("android.selfDisplayName")?.toString()?.trim()
        val messagingUser = extras.getParcelable<android.app.Person>("android.messagingUser")
            ?.name?.toString()?.trim()
        val selfNames = setOfNotNull(selfDisplayName, messagingUser).filter(String::isNotBlank)
        Notification.MessagingStyle.Message
            .getMessagesFromBundleArray(extras.getParcelableArray(Notification.EXTRA_MESSAGES))
            .mapNotNull { message ->
                val rawBody = message.text?.toString()?.takeIf(String::isNotBlank) ?: return@mapNotNull null
                val rawSender = message.senderPerson?.name?.toString()?.trim()?.takeIf(String::isNotBlank)
                val prefixed = if (suppliedConversationTitle != null) GROUP_SENDER_PREFIX.find(rawBody) else null
                val embeddedSender = prefixed?.groupValues?.getOrNull(1)?.cleanHubSender()
                val senderIsPlaceholder = rawSender == null || rawSender.equals("Mentioned all", true) ||
                    rawSender.equals("All", true) || rawSender.equals(suppliedConversationTitle, true)
                val sender = (if (senderIsPlaceholder) embeddedSender ?: rawSender else rawSender)?.cleanHubSender()
                val body = if (senderIsPlaceholder && embeddedSender != null && prefixed != null) {
                    rawBody.substring(prefixed.range.last + 1).trimStart()
                } else rawBody
                // MessagingStyle uses the app's configured user identity for
                // outgoing history. Exact self-name matches are reliable, and
                // a missing sender is treated as outgoing only when the app
                // supplied an explicit self identity.
                val outgoing = selfNames.any { it.equals(rawSender, true) } ||
                    (rawSender == null && selfNames.isNotEmpty())
                HubMessage(
                    text = body,
                    sender = if (outgoing) null else sender,
                    outgoing = outgoing,
                    time = message.timestamp.takeIf { it > 0L },
                )
            }
    }.getOrDefault(emptyList()) else emptyList()
    val instagramTextSender = if (packageName == "com.instagram.android" && notification.channelId == "ig_direct") {
        Regex("^\\s*([^:\n]{1,80}):\\s+.+$").find(text)?.groupValues?.getOrNull(1)?.cleanHubSender()
    } else null
    val conversationTitle = if (packageName == "com.instagram.android" && notification.channelId == "ig_direct") {
        messages.asReversed().firstNotNullOfOrNull { message ->
            message.sender?.takeIf(String::isNotBlank)
        } ?: instagramTextSender ?: suppliedConversationTitle
    } else suppliedConversationTitle
    val displayText = if (instagramTextSender != null && conversationTitle.equals(instagramTextSender, true)) {
        text.substringAfter(':').trimStart()
    } else text
    var displayImageIsMonochrome = false
    val displayImage = runCatching {
        val latestPersonIcon = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Notification.MessagingStyle.Message
                .getMessagesFromBundleArray(extras.getParcelableArray(Notification.EXTRA_MESSAGES))
                .asReversed()
                .firstNotNullOfOrNull { it.senderPerson?.icon }
        } else null
        val conversationLike = conversationTitle != null || messages.isNotEmpty() ||
            notification.category == Notification.CATEGORY_MESSAGE
        val preferredIcon = if (conversationLike) {
            latestPersonIcon ?: notification.getLargeIcon()
        } else {
            notification.smallIcon?.also { displayImageIsMonochrome = true }
                ?: notification.getLargeIcon()
        }
        preferredIcon?.loadDrawable(service)?.toBitmap(96, 96)
            ?: service.packageManager.getApplicationIcon(packageName).also { displayImageIsMonochrome = false }.toBitmap(96, 96)
    }.getOrNull()
    val hasSummaryFlag = notification.flags and Notification.FLAG_GROUP_SUMMARY != 0
    val isAggregateSummary = hasSummaryFlag && (
        notification.flags and FLAG_AUTOGROUP_SUMMARY != 0 ||
            !extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT).isNullOrBlank() ||
            extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES).orEmpty().size > 1 ||
            AGGREGATE_SUMMARY_TEXT.containsMatchIn(title) ||
            AGGREGATE_SUMMARY_TEXT.containsMatchIn(text)
        )
    return HubItem(
        key = key,
        packageName = packageName,
        appName = appName,
        title = title.ifBlank { appName },
        conversationTitle = conversationTitle,
        text = displayText,
        expandedText = expandedText,
        textLines = textLines,
        messages = messages,
        displayImage = displayImage,
        displayImageIsMonochrome = displayImageIsMonochrome,
        time = postTime,
        category = notification.category,
        contentIntent = notification.contentIntent,
        replyAction = notification.actions.orEmpty()
            .sortedByDescending {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
                    it.semanticAction == Notification.Action.SEMANTIC_ACTION_REPLY
            }
            .firstNotNullOfOrNull { action ->
            val inputList: List<RemoteInput> = action.remoteInputs?.filter { it.allowFreeFormInput }.orEmpty()
            if (inputList.isEmpty()) null else HubReplyAction(action.actionIntent, inputList.toTypedArray())
        },
        notificationActions = notification.actions.orEmpty().mapNotNull { action ->
            val label = action.title?.toString()?.trim().orEmpty()
            val hasReplyInput = action.remoteInputs?.any { it.allowFreeFormInput } == true
            if (label.isBlank() || hasReplyInput) null else HubNotificationAction(
                title = label,
                pendingIntent = action.actionIntent,
                semanticAction = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) action.semanticAction else 0,
                destructive = label.contains(Regex("delete|trash|remove", RegexOption.IGNORE_CASE)) ||
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && action.semanticAction == Notification.Action.SEMANTIC_ACTION_DELETE),
            )
        }.distinctBy { it.title.lowercase() },
        hasAttachment = extras.getBoolean("has_attachment", false),
        // Some apps, including Shopify, promote the newest ordinary
        // notification to Android's group header and set GROUP_SUMMARY on it.
        // Keep those useful rows in their normal category. The summaries tab
        // is reserved for rows whose content is actually aggregate.
        isSummary = isAggregateSummary,
        isGroupHeader = hasSummaryFlag,
        groupKey = groupKey,
        channelId = notification.channelId,
        shortcutId = notification.shortcutId,
        sourceKeys = setOf(key),
    )
}

private fun deduplicateNotifications(source: List<HubItem>): List<HubItem> {
    val result = mutableListOf<HubItem>()
    source.sortedByDescending(HubItem::time).forEach { candidate ->
        val matchIndex = result.indexOfFirst { it.isSemanticDuplicateOf(candidate) }
        if (matchIndex < 0) result += candidate
        else result[matchIndex] = result[matchIndex].mergeDuplicate(candidate)
    }
    return result
}

private fun HubItem.isSemanticDuplicateOf(other: HubItem): Boolean {
    if (packageName != other.packageName || isSummary != other.isSummary) return false
    val exactSameContent = title.semanticIdentity() == other.title.semanticIdentity() &&
        text.semanticIdentity() == other.text.semanticIdentity()
    val sameAndroidGroup = !groupKey.isNullOrBlank() && groupKey == other.groupKey
    val sameGroupedEvent = isGroupHeader != other.isGroupHeader && exactSameContent && (
        sameAndroidGroup || (
            channelId == other.channelId &&
                kotlin.math.abs(time - other.time) <= SEMANTIC_POST_TIME_WINDOW_MS
            )
        )
    if (sameGroupedEvent) return true
    val thisShortcut = shortcutId?.takeIf(String::isNotBlank)
    val otherShortcut = other.shortcutId?.takeIf(String::isNotBlank)
    if (thisShortcut != null && otherShortcut != null && thisShortcut != otherShortcut) return false
    val sameShortcut = thisShortcut != null && thisShortcut == otherShortcut
    val sameConversationLabel = (conversationTitle ?: title).semanticIdentity() ==
        (other.conversationTitle ?: other.title).semanticIdentity()
    if (!sameShortcut && !sameConversationLabel) return false

    val thisLatest = messages.lastOrNull()
    val otherLatest = other.messages.lastOrNull()
    val thisBody = (thisLatest?.text ?: text).normalizedReplyText()
    val otherBody = (otherLatest?.text ?: other.text).normalizedReplyText()
    if (thisBody.isBlank() || thisBody != otherBody) return false
    if (thisLatest != null && otherLatest != null && thisLatest.outgoing != otherLatest.outgoing) return false

    val thisMessageTime = thisLatest?.time
    val otherMessageTime = otherLatest?.time
    return if (thisMessageTime != null && otherMessageTime != null) {
        kotlin.math.abs(thisMessageTime - otherMessageTime) <= SEMANTIC_MESSAGE_TIME_WINDOW_MS
    } else {
        kotlin.math.abs(time - other.time) <= SEMANTIC_POST_TIME_WINDOW_MS
    }
}

private fun HubItem.mergeDuplicate(other: HubItem): HubItem {
    val preferred = when {
        isGroupHeader != other.isGroupHeader -> if (isGroupHeader) other else this
        duplicateQuality() >= other.duplicateQuality() -> this
        else -> other
    }
    val richerMessages = if (messages.size >= other.messages.size) messages else other.messages
    return preferred.copy(
        messages = richerMessages,
        time = maxOf(time, other.time),
        notificationActions = (preferred.notificationActions + other.notificationActions)
            .distinctBy { it.title.lowercase() },
        sourceKeys = sourceKeys + other.sourceKeys,
    )
}

private fun HubItem.duplicateQuality(): Int =
    (if (replyAction != null) 8 else 0) +
        notificationActions.size.coerceAtMost(3) * 2 +
        (if (contentIntent != null) 4 else 0) +
        (if (!shortcutId.isNullOrBlank()) 2 else 0) +
        messages.size.coerceAtMost(3)

private fun String.semanticIdentity(): String = trim().lowercase().replace(Regex("\\s+"), " ")
private const val SEMANTIC_MESSAGE_TIME_WINDOW_MS = 1_000L
private const val SEMANTIC_POST_TIME_WINDOW_MS = 2_000L

private val GROUP_SENDER_PREFIX = Regex("^[~～]?\\s*([^:\\n]{1,60}):\\s+")
private const val FLAG_AUTOGROUP_SUMMARY = 0x400
private val AGGREGATE_SUMMARY_TEXT = Regex(
    "\\b\\d+\\s+(?:new\\s+)?(?:messages?|notifications?|emails?|conversations?|chats?|orders?|reminders?)\\b|" +
        "\\b(?:messages?|notifications?|emails?|conversations?|chats?|orders?|reminders?)\\s+from\\s+\\d+\\b",
    RegexOption.IGNORE_CASE,
)
private fun String.cleanHubSender(): String = trim().trimStart('~', '～', '\u202f', '\u00a0').trim()

fun HubItem.sendReply(context: android.content.Context, text: String): Boolean {
    val reply = replyAction ?: return false
    return reply.sendReply(context, key, packageName, text)
}

fun HubReplyAction.sendReply(
    context: android.content.Context,
    guardKey: String,
    packageName: String,
    text: String,
): Boolean {
    if (text.isBlank()) return false
    if (!HubReplySendGuard.acquire(guardKey, text)) {
        return true
    }
    return runCatching {
        val results = android.os.Bundle().apply {
            remoteInputs.forEach { putCharSequence(it.resultKey, text) }
        }
        val fillIn = Intent()
        RemoteInput.addResultsToIntent(remoteInputs, fillIn, results)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            RemoteInput.setResultsSource(fillIn, RemoteInput.SOURCE_FREE_FORM_INPUT)
        }
        pendingIntent.send(context, 0, fillIn)
        HubRepository.recordSentReply(guardKey, text)
        true
    }.onFailure {
        HubReplySendGuard.release(guardKey, text)
    }.getOrDefault(false)
}

private object HubReplySendGuard {
    private const val RepeatWindowMs = 900L
    private val recent = mutableMapOf<String, Pair<String, Long>>()

    @Synchronized
    fun acquire(key: String, text: String): Boolean {
        val now = System.currentTimeMillis()
        val normalized = text.normalizedReplyText()
        val previous = recent[key]
        if (previous != null && previous.first == normalized && now - previous.second < RepeatWindowMs) return false
        recent[key] = normalized to now
        return true
    }

    @Synchronized
    fun release(key: String, text: String) {
        val normalized = text.normalizedReplyText()
        if (recent[key]?.first == normalized) recent.remove(key)
    }
}
