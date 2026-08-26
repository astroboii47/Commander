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
import android.util.Log
import android.os.Build

data class HubItem(
    val key: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val conversationTitle: String?,
    val text: String,
    val messages: List<HubMessage>,
    val time: Long,
    val category: String?,
    val contentIntent: PendingIntent?,
    val replyAction: HubReplyAction?,
    val notificationActions: List<HubNotificationAction>,
    val isSummary: Boolean,
    val isGroupHeader: Boolean,
    val channelId: String?,
    val shortcutId: String?,
    val sourceKeys: Set<String>,
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
    private var refreshNotifications: (() -> Unit)? = null
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

    fun attachRefreshHandler(handler: (() -> Unit)?) {
        refreshNotifications = handler
    }

    fun requestRefresh() {
        refreshNotifications?.invoke()
    }

    fun dismiss(key: String) {
        val item = items.firstOrNull { it.key == key || key in it.sourceKeys }
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

    fun applyIgnoreRules() {
        items = items.filterNot(HubIgnoreSettings::matches)
    }

    fun recordSentReply(key: String, text: String) {
        val now = System.currentTimeMillis()
        val reply = HubMessage(text, null, true, now)
        sentReplies.getOrPut(key) { mutableListOf() }.add(PendingReply(reply, now))
        items = items.map { item ->
            if (item.key == key) item.copy(messages = item.messages + reply) else item
        }
    }

    private fun mergeSentReplies(item: HubItem): HubItem {
        val pending = sentReplies[item.key] ?: return item
        val now = System.currentTimeMillis()
        pending.removeAll { now - it.sentAt > SENT_REPLY_RETENTION_MS }
        if (pending.isEmpty()) {
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
            } else if (!local.acknowledged) {
                merged += local.message
            }
        }
        return item.copy(messages = merged)
    }

    fun replace(notifications: Array<StatusBarNotification>, service: NotificationListenerService) {
        items = notifications
            .asSequence()
            .filter { !it.isOngoing }
            .onEach { it.discoverMessengerConversation(service) }
            .mapNotNull { it.toHubItem(service) }
            .filterNot(::consumeActionConfirmation)
            .filterNot(HubIgnoreSettings::matches)
            .filterNot { it.isSummary && it.key in hiddenSummaryKeys }
            .map(::mergeSentReplies)
            .toList()
            .let(::deduplicateNotifications)
            .sortedByDescending { it.time }
    }

    fun upsert(notification: StatusBarNotification, service: NotificationListenerService) {
        if (notification.isOngoing) {
            remove(notification.key)
            return
        }
        notification.discoverMessengerConversation(service)
        val converted = notification.toHubItem(service)?.takeUnless(::consumeActionConfirmation)
            ?.takeUnless(HubIgnoreSettings::matches)?.let(::mergeSentReplies)
            ?.takeUnless { it.isSummary && it.key in hiddenSummaryKeys }
        items = if (converted == null) {
            items.filterNot { it.key == notification.key }
        } else {
            deduplicateNotifications(items.filterNot { notification.key in it.sourceKeys } + converted)
                .sortedByDescending(HubItem::time)
        }
    }

    fun remove(key: String) {
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
        sentReplies.remove(key)
        hiddenSummaryKeys.remove(key)
        suppressedActionConfirmationKeys.remove(key)
    }
}

private const val SENT_REPLY_RETENTION_MS = 10 * 60 * 1000L
private const val SENT_REPLY_MATCH_WINDOW_MS = 2 * 60 * 1000L
private const val ACTION_CONFIRMATION_WINDOW_MS = 5_000L
private const val SUPPRESSED_CONFIRMATION_RETENTION_MS = 60_000L
private fun String.normalizedReplyText(): String = trim().replace(Regex("\\s+"), " ")

class HubNotificationService : NotificationListenerService() {
    override fun onListenerConnected() {
        HubRepository.attachDismissHandler(::cancelNotification)
        HubRepository.attachRefreshHandler(::refresh)
        refresh()
    }
    override fun onListenerDisconnected() {
        HubRepository.attachDismissHandler(null)
        HubRepository.attachRefreshHandler(null)
    }
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        ChatGptNotificationBridge.onNotification(applicationContext, sbn)
        runCatching { HubRepository.upsert(sbn, this) }
    }
    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        sbn?.let { HubRepository.remove(it.key) }
    }

    private fun refresh() {
        runCatching { HubRepository.replace(activeNotifications ?: emptyArray(), this) }
    }
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
    val conversationTitle = (
        extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)?.toString()
            ?: extras.getCharSequence("android.hiddenConversationTitle")?.toString()
        )?.replace(Regex("\\s*\\(\\d+\\s+messages?\\)\\s*$", RegexOption.IGNORE_CASE), "")
        ?.trim()?.takeIf(String::isNotBlank)
    val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()
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
                val prefixed = if (conversationTitle != null) GROUP_SENDER_PREFIX.find(rawBody) else null
                val embeddedSender = prefixed?.groupValues?.getOrNull(1)?.cleanHubSender()
                val senderIsPlaceholder = rawSender == null || rawSender.equals("Mentioned all", true) ||
                    rawSender.equals("All", true) || rawSender.equals(conversationTitle, true)
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
        text = text,
        messages = messages,
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
        // Some apps, including Shopify, promote the newest ordinary
        // notification to Android's group header and set GROUP_SUMMARY on it.
        // Keep those useful rows in their normal category. The summaries tab
        // is reserved for rows whose content is actually aggregate.
        isSummary = isAggregateSummary,
        isGroupHeader = hasSummaryFlag,
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
    val sameGroupedEvent = isGroupHeader != other.isGroupHeader && channelId == other.channelId &&
        kotlin.math.abs(time - other.time) <= SEMANTIC_POST_TIME_WINDOW_MS &&
        listOf(title.semanticIdentity(), text.semanticIdentity()).sorted() ==
        listOf(other.title.semanticIdentity(), other.text.semanticIdentity()).sorted()
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
    val preferred = if (duplicateQuality() >= other.duplicateQuality()) this else other
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
    val reply = replyAction ?: run {
        Log.w("MinimalHubReply", "no reply action key=$key pkg=$packageName")
        return false
    }
    if (text.isBlank()) return false
    if (!HubReplySendGuard.acquire(key, text)) {
        Log.i("MinimalHubReply", "ignored repeated send key=$key")
        return true
    }
    Log.i("MinimalHubReply", "send key=$key pkg=$packageName inputs=${reply.remoteInputs.joinToString { it.resultKey }} length=${text.length}")
    return runCatching {
        val results = android.os.Bundle().apply {
            reply.remoteInputs.forEach { putCharSequence(it.resultKey, text) }
        }
        val fillIn = Intent()
        RemoteInput.addResultsToIntent(reply.remoteInputs, fillIn, results)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            RemoteInput.setResultsSource(fillIn, RemoteInput.SOURCE_FREE_FORM_INPUT)
        }
        reply.pendingIntent.send(context, 0, fillIn)
        Log.i("MinimalHubReply", "pendingIntent accepted key=$key")
        HubRepository.recordSentReply(key, text)
        true
    }.onFailure {
        HubReplySendGuard.release(key, text)
        Log.e("MinimalHubReply", "pendingIntent failed key=$key", it)
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
