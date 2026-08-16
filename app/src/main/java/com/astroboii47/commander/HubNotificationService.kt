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

    fun attachDismissHandler(handler: ((String) -> Unit)?) {
        dismissNotification = handler
    }

    fun dismiss(key: String) {
        items = items.filterNot { it.key == key }
        sentReplies.remove(key)
        dismissNotification?.invoke(key)
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
        }
        items = if (converted == null) {
            items.filterNot { it.key == notification.key }
        } else {
            (items.filterNot { it.key == notification.key } + converted).sortedByDescending(HubItem::time)
        }
    }

    fun remove(key: String) {
        items = items.filterNot { it.key == key }
        sentReplies.remove(key)
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
                // Notification histories are incoming data. Apps disagree on
                // MessagingStyle user identity, so never infer outgoing here.
                HubMessage(body, sender, false)
            }
    }.getOrDefault(emptyList()) else emptyList()
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
        isSummary = notification.flags and Notification.FLAG_GROUP_SUMMARY != 0,
    )
}

private val GROUP_SENDER_PREFIX = Regex("^[~～]?\\s*([^:\\n]{1,60}):\\s+")
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
