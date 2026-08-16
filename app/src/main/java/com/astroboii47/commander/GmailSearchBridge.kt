package com.astroboii47.commander

import android.content.Context
import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo

object GmailSearchBridge {
    private const val PREFS = "gmail_search_bridge"
    private const val KEY_QUERY = "pending_query"
    private const val KEY_SINCE = "pending_since"
    private const val MAX_WAIT_MS = 15_000L

    fun arm(context: Context, query: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_QUERY, query)
            .putLong(KEY_SINCE, System.currentTimeMillis())
            .apply()
        HomeTypingAccessibilityService.monitorAutomation(MAX_WAIT_MS)
    }

    fun tryFill(context: Context, root: AccessibilityNodeInfo?): Boolean {
        if (root == null) return false
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val query = prefs.getString(KEY_QUERY, null)?.takeIf(String::isNotBlank) ?: return false
        val since = prefs.getLong(KEY_SINCE, 0L)
        if (since == 0L || System.currentTimeMillis() - since > MAX_WAIT_MS) {
            prefs.edit().clear().apply()
            return false
        }

        findEditable(root)?.let { editor ->
            val arguments = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, query)
            }
            if (editor.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)) {
                editor.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
                prefs.edit().clear().apply()
                return true
            }
        }

        findSearchControl(root)?.let { search ->
            clickableAncestor(search)?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }
        return false
    }

    private fun findEditable(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isEditable && node.isEnabled) return node
        for (index in 0 until node.childCount) {
            node.getChild(index)?.let(::findEditable)?.let { return it }
        }
        return null
    }

    private fun findSearchControl(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val label = listOfNotNull(node.text, node.contentDescription, node.hintText)
            .joinToString(" ").trim()
        val resource = node.viewIdResourceName.orEmpty()
        if (node.isEnabled &&
            (label.startsWith("Search in", ignoreCase = true) || resource.endsWith(":id/open_search"))
        ) return node
        for (index in 0 until node.childCount) {
            node.getChild(index)?.let(::findSearchControl)?.let { return it }
        }
        return null
    }

    private fun clickableAncestor(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        var candidate: AccessibilityNodeInfo? = node
        repeat(5) {
            if (candidate?.isEnabled == true && candidate?.isClickable == true) return candidate
            candidate = candidate?.parent
        }
        return null
    }
}
