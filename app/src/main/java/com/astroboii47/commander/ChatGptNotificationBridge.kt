package com.astroboii47.commander

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.service.notification.StatusBarNotification
import android.view.accessibility.AccessibilityNodeInfo

object ChatGptNotificationBridge {
    private const val PREFS = "chatgpt_notification_bridge"
    private const val KEY_PROMPT = "pending_prompt"
    private const val KEY_SINCE = "pending_since"
    private const val KEY_RESPONSE = "response"
    private const val KEY_LAST_PROMPT = "last_prompt"
    private const val KEY_SUBMITTED = "submitted"
    private const val MAX_WAIT_MS = 10 * 60 * 1000L

    fun arm(context: Context, prompt: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_PROMPT, prompt)
            .putString(KEY_LAST_PROMPT, prompt)
            .putLong(KEY_SINCE, System.currentTimeMillis())
            .remove(KEY_RESPONSE)
            .putBoolean(KEY_SUBMITTED, false)
            .apply()
        HomeTypingAccessibilityService.monitorAutomation(20_000L)
    }

    fun onNotification(context: Context, sbn: StatusBarNotification) {
        if (sbn.packageName != "com.openai.chatgpt") return
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val prompt = prefs.getString(KEY_PROMPT, null)?.trim().orEmpty()
        val since = prefs.getLong(KEY_SINCE, 0L)
        if (prompt.isBlank() || since == 0L || sbn.postTime < since ||
            System.currentTimeMillis() - since > MAX_WAIT_MS
        ) return

        val extras = sbn.notification.extras
        val response = sequenceOf(
            extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString(),
            extras.getCharSequence(Notification.EXTRA_TEXT)?.toString(),
            extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString(),
        ).firstOrNull { candidate ->
            !candidate.isNullOrBlank() &&
                !candidate.trim().equals(prompt, ignoreCase = true) &&
                !candidate.contains("thinking", ignoreCase = true)
        }?.trim() ?: return

        prefs.edit().putString(KEY_RESPONSE, response).remove(KEY_PROMPT).remove(KEY_SINCE).apply()
        runCatching {
            context.startActivity(
                Intent(context, OverlayActivity::class.java)
                    .setAction(OverlayActivity.ACTION_OPEN_OVERLAY)
                    .putExtra(OverlayActivity.EXTRA_ASK_PREVIEW, response)
                    .putExtra(OverlayActivity.EXTRA_ASK_PROMPT, prompt)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            )
        }
    }

    fun consumeResponse(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val response = prefs.getString(KEY_RESPONSE, null)?.takeIf(String::isNotBlank)
        if (response != null) prefs.edit().remove(KEY_RESPONSE).apply()
        return response
    }

    fun lastPrompt(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_LAST_PROMPT, "question").orEmpty()

    fun trySubmitFromAccessibility(context: Context, root: AccessibilityNodeInfo?): Boolean {
        if (root == null) return false
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val since = prefs.getLong(KEY_SINCE, 0L)
        if (prefs.getString(KEY_PROMPT, null).isNullOrBlank() ||
            prefs.getBoolean(KEY_SUBMITTED, false) || since == 0L ||
            System.currentTimeMillis() - since > 20_000L
        ) return false
        val send = findSendNode(root) ?: return false
        val clicked = send.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        if (clicked) {
            prefs.edit().putBoolean(KEY_SUBMITTED, true).apply()
            HomeTypingAccessibilityService.automationFinished()
        }
        return clicked
    }

    private fun findSendNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val label = listOfNotNull(node.contentDescription, node.text)
            .joinToString(" ").trim().lowercase()
        if (node.isEnabled &&
            (label == "send" || label == "send message" || label.startsWith("send "))
        ) {
            var target: AccessibilityNodeInfo? = node
            repeat(4) {
                if (target?.isEnabled == true && target?.isClickable == true) return target
                target = target?.parent
            }
        }
        for (index in 0 until node.childCount) {
            val found = node.getChild(index)?.let(::findSendNode)
            if (found != null) return found
        }
        return null
    }
}
