package com.astroboii47.commander

import android.content.Context
import androidx.compose.runtime.mutableStateOf

object HubSettings {
    private const val PREFS = "hub_settings"
    private const val KEY_SHOW_SUMMARIES = "show_summaries"
    private const val KEY_QUICK_KEYBOARD_NAVIGATION = "quick_keyboard_navigation"
    val showSummaries = mutableStateOf(false)
    val quickKeyboardNavigation = mutableStateOf(false)

    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        showSummaries.value = prefs.getBoolean(KEY_SHOW_SUMMARIES, false)
        quickKeyboardNavigation.value = prefs.getBoolean(KEY_QUICK_KEYBOARD_NAVIGATION, false)
    }

    fun saveShowSummaries(context: Context, value: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_SHOW_SUMMARIES, value).apply()
        showSummaries.value = value
    }

    fun saveQuickKeyboardNavigation(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_QUICK_KEYBOARD_NAVIGATION, enabled).apply()
        quickKeyboardNavigation.value = enabled
    }
}
