package com.astroboii47.commander

import android.content.Context
import androidx.compose.runtime.mutableStateOf

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
    private const val TAB_PREFIX = "tab_visibility_"
    val optionalTabs = listOf("messages", "calls", "email", "finance", "tasks", "apps", "flagged", "summaries")
    val tabVisibility = mutableStateOf<Map<String, HubTabVisibility>>(emptyMap())
    val quickKeyboardNavigation = mutableStateOf(false)

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
}
