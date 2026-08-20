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
    val isSummary: Boolean,
)

data class HubMessage(val text: String, val sender: String?, val outgoing: Boolean)

data class HubReplyAction(
    val pendingIntent: PendingIntent,
    val remoteInputs: Array<RemoteInput>,
)

object HubRepository {
    var items by mutableStateOf<List<HubItem>>(emptyList())
        private set
    private var dismissNotification: ((String) -> Unit)? = null
    private val sentReplies = mutableMapOf<String, MutableList<HubMessage>>()
    private val hiddenSummaryKeys = mutableSetOf<String>()

    fun attachDismissHandler(handler: ((String) -> Unit)?) {
        dismissNotification = handler
    }

    fun dismiss(key: String) {
        items = items.filterNot { it.key == key }
        sentReplies.remove(key)
        dismissNotification?.invoke(key)
    }

    fun dismiss(item: HubItem) {
        if (item.isSummary) {
            // Cancelling an Android group summary commonly cancels every child
            // notification too. Hide only Commander's aggregate row so the
            // individual notifications remain intact and actionable.
            hiddenSummaryKeys += item.key
            items = items.filterNot { it.key == item.key }
        } else {
            dismiss(item.key)
        }
    }

    fun recordSentReply(key: String, text: String) {
        val reply = HubMessage(text, null, true)
        sentReplies.getOrPut(key) { mutableListOf() }.add(reply)
        items = items.map { item ->
            if (item.key == key) item.copy(messages = item.messages + reply) else item
        }
    }

    fun replace(notifications: Array<StatusBarNotification>, service: NotificationListenerService) {
        items = notifications
            .asSequence()
            .filter { !it.isOngoing }
            .onEach { it.discoverMessengerConversation(service) }
            .mapNotNull { it.toHubItem(service) }
            .filterNot { it.isSummary && it.key in hiddenSummaryKeys }
            .map { item -> item.copy(messages = item.messages + sentReplies[item.key].orEmpty()) }
            .sortedByDescending { it.time }
            .toList()
    }

    fun upsert(notification: StatusBarNotification, service: NotificationListenerService) {
        if (notification.isOngoing) {
            remove(notification.key)
            return
        }
        notification.discoverMessengerConversation(service)
        val converted = notification.toHubItem(service)?.let { item ->
            item.copy(messages = item.messages + sentReplies[item.key].orEmpty())
        }?.takeUnless { it.isSummary && it.key in hiddenSummaryKeys }
        items = if (converted == null) {
            items.filterNot { it.key == notification.key }
        } else {
            (items.filterNot { it.key == notification.key } + converted).sortedByDescending(HubItem::time)
        }
    }

    fun remove(key: String) {
        items = items.filterNot { it.key == key }
        sentReplies.remove(key)
        hiddenSummaryKeys.remove(key)
    }
}

class HubNotificationService : NotificationListenerService() {
    override fun onListenerConnected() {
        HubRepository.attachDismissHandler(::cancelNotification)
        refresh()
    }
    override fun onListenerDisconnected() {
        HubRepository.attachDismissHandler(null)
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
                HubMessage(body, if (outgoing) null else sender, outgoing)
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
        // Some apps, including Shopify, promote the newest ordinary
        // notification to Android's group header and set GROUP_SUMMARY on it.
        // Keep those useful rows in their normal category. The summaries tab
        // is reserved for rows whose content is actually aggregate.
        isSummary = isAggregateSummary,
    )
}

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
    }.onFailure { Log.e("MinimalHubReply", "pendingIntent failed key=$key", it) }.getOrDefault(false)
}
