package com.astroboii47.commander

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import org.json.JSONArray
import org.json.JSONObject

enum class HubTabVisibility(val storedValue: String, val label: String) {
    Auto("auto", "auto"),
    Always("always", "always"),
    Hidden("hidden", "hidden");

    fun next(): HubTabVisibility = entries[(ordinal + 1) % entries.size]

    companion object {
        fun fromStored(value: String?): HubTabVisibility = entries.firstOrNull { it.storedValue == value } ?: Auto
    }
}

object HubSettings {
    private const val PREFS = "hub_settings"
    private const val KEY_SHOW_SUMMARIES = "show_summaries"
    private const val KEY_QUICK_KEYBOARD_NAVIGATION = "quick_keyboard_navigation"
    private const val KEY_CLOSE_REPLY_AFTER_SENDING = "close_reply_after_sending"
    private const val TAB_PREFIX = "tab_visibility_"
    val optionalTabs = listOf("messages", "calls", "email", "finance", "tasks", "apps", "flagged", "summaries")
    val tabVisibility = mutableStateOf<Map<String, HubTabVisibility>>(emptyMap())
    val quickKeyboardNavigation = mutableStateOf(false)
    val closeReplyAfterSending = mutableStateOf(false)

    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val legacySummaries = prefs.getBoolean(KEY_SHOW_SUMMARIES, false)
        tabVisibility.value = optionalTabs.associateWith { tab ->
            val stored = prefs.getString(TAB_PREFIX + tab, null)
            if (stored == null && tab == "summaries") {
                if (legacySummaries) HubTabVisibility.Always else HubTabVisibility.Hidden
            } else HubTabVisibility.fromStored(stored)
        }
        quickKeyboardNavigation.value = prefs.getBoolean(KEY_QUICK_KEYBOARD_NAVIGATION, false)
        closeReplyAfterSending.value = prefs.getBoolean(KEY_CLOSE_REPLY_AFTER_SENDING, false)
    }

    fun visibility(tab: String): HubTabVisibility = tabVisibility.value[tab] ?: HubTabVisibility.Auto

    fun saveTabVisibility(context: Context, tab: String, visibility: HubTabVisibility) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(TAB_PREFIX + tab, visibility.storedValue).apply()
        tabVisibility.value = tabVisibility.value + (tab to visibility)
    }

    fun saveQuickKeyboardNavigation(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_QUICK_KEYBOARD_NAVIGATION, enabled).apply()
        quickKeyboardNavigation.value = enabled
    }

    fun saveCloseReplyAfterSending(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_CLOSE_REPLY_AFTER_SENDING, enabled).apply()
        closeReplyAfterSending.value = enabled
    }
}

data class HubIgnoreRule(
    val id: String,
    val packageName: String,
    val appName: String,
    val scopeLabel: String?,
)

object HubIgnoreSettings {
    private const val PREFS = "hub_settings"
    private const val KEY_RULES = "ignored_notification_rules"
    val rules = mutableStateOf<List<HubIgnoreRule>>(emptyList())

    fun initialize(context: Context) {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_RULES, "[]") ?: "[]"
        rules.value = runCatching {
            val array = JSONArray(raw)
            List(array.length()) { index ->
                val item = array.getJSONObject(index)
                HubIgnoreRule(
                    item.getString("id"),
                    item.getString("package"),
                    item.getString("app"),
                    item.optString("scope").takeIf(String::isNotBlank),
                )
            }
        }.getOrDefault(emptyList())
    }

    fun ignoreApp(context: Context, item: HubItem) = add(
        context,
        HubIgnoreRule("app:${item.packageName}", item.packageName, item.appName, null),
    )

    fun ignoreScope(context: Context, item: HubItem): Boolean {
        val scope = item.ignoreScope() ?: return false
        add(context, HubIgnoreRule("scope:${item.packageName}:${scope.first}", item.packageName, item.appName, scope.second))
        return true
    }

    fun matches(item: HubItem): Boolean {
        val scopeId = item.ignoreScope()?.first
        return rules.value.any { rule ->
            rule.id == "app:${item.packageName}" ||
                (scopeId != null && rule.id == "scope:${item.packageName}:$scopeId")
        }
    }

    fun remove(context: Context, id: String) {
        rules.value = rules.value.filterNot { it.id == id }
        save(context)
    }

    private fun add(context: Context, rule: HubIgnoreRule) {
        rules.value = (rules.value.filterNot { it.id == rule.id } + rule).sortedBy { it.appName.lowercase() }
        save(context)
    }

    private fun save(context: Context) {
        val array = JSONArray().apply {
            rules.value.forEach { rule ->
                put(JSONObject().put("id", rule.id).put("package", rule.packageName)
                    .put("app", rule.appName).put("scope", rule.scopeLabel.orEmpty()))
            }
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_RULES, array.toString()).apply()
    }
}

private fun HubItem.ignoreScope(): Pair<String, String>? = when {
    !shortcutId.isNullOrBlank() -> "shortcut:$shortcutId" to (conversationTitle ?: title)
    !conversationTitle.isNullOrBlank() -> "conversation:${conversationTitle.lowercase()}" to conversationTitle
    !channelId.isNullOrBlank() -> "channel:$channelId" to "${channelId} notifications"
    else -> null
}
