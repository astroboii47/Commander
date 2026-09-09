package com.astroboii47.commander

import android.content.Context
import androidx.compose.runtime.mutableStateOf

object AppSearchSettings {
    private const val PREFS = "app_search"
    private const val KEY_OPEN_SINGLE_RESULT = "open_single_result"
    private const val KEY_WEB_FALLBACK = "web_fallback"
    private const val KEY_ALIAS_SUGGESTIONS = "alias_suggestions"
    private const val KEY_APP_SHORTCUT_CATEGORY = "app_shortcut_category"
    private const val KEY_QUICK_CONTROLS_CATEGORY = "quick_controls_category"

    val openSingleResult = mutableStateOf(false)
    val webFallback = mutableStateOf(false)
    val aliasSuggestions = mutableStateOf(true)
    val appShortcutCategory = mutableStateOf(false)
    val quickControlsCategory = mutableStateOf(false)

    fun initialize(context: Context) {
        openSingleResult.value = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_OPEN_SINGLE_RESULT, false)
        webFallback.value = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_WEB_FALLBACK, false)
        aliasSuggestions.value = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_ALIAS_SUGGESTIONS, true)
        appShortcutCategory.value = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_APP_SHORTCUT_CATEGORY, false)
        quickControlsCategory.value = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_QUICK_CONTROLS_CATEGORY, false)
    }

    fun saveOpenSingleResult(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_OPEN_SINGLE_RESULT, enabled)
            .apply()
        openSingleResult.value = enabled
    }

    fun saveWebFallback(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_WEB_FALLBACK, enabled)
            .apply()
        webFallback.value = enabled
    }

    fun saveAliasSuggestions(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_ALIAS_SUGGESTIONS, enabled).apply()
        aliasSuggestions.value = enabled
    }

    fun saveAppShortcutCategory(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_APP_SHORTCUT_CATEGORY, enabled).apply()
        appShortcutCategory.value = enabled
    }

    fun saveQuickControlsCategory(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_QUICK_CONTROLS_CATEGORY, enabled).apply()
        quickControlsCategory.value = enabled
    }
}
