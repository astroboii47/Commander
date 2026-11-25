package com.astroboii47.commander

import android.content.Context
import androidx.compose.runtime.mutableStateOf

enum class WebSearchMode(val title: String, val subtitle: String) {
    QuickPopup("Quick popup", "Open a browser-backed popup over Commander"),
    InbuiltPopup("Inbuilt popup", "Keep Commander and its keyboard open beneath a compact browser"),
    DefaultBrowser("Default browser", "Open searches directly in your normal browser"),
}

object AppSearchSettings {
    private const val PREFS = "app_search"
    private const val KEY_OPEN_SINGLE_RESULT = "open_single_result"
    private const val KEY_WEB_FALLBACK = "web_fallback"
    private const val KEY_WEB_PREVIEWS = "web_previews"
    private const val KEY_WEB_QUICK_POPUP = "web_quick_popup"
    private const val KEY_WEB_SEARCH_MODE = "web_search_mode"
    private const val KEY_ALIAS_SUGGESTIONS = "alias_suggestions"
    private const val KEY_APP_SHORTCUT_CATEGORY = "app_shortcut_category"
    private const val KEY_QUICK_CONTROLS_CATEGORY = "quick_controls_category"

    val openSingleResult = mutableStateOf(false)
    val webFallback = mutableStateOf(false)
    val webPreviews = mutableStateOf(false)
    val webQuickPopup = mutableStateOf(true)
    val webSearchMode = mutableStateOf(WebSearchMode.QuickPopup)
    val aliasSuggestions = mutableStateOf(true)
    val appShortcutCategory = mutableStateOf(false)
    val quickControlsCategory = mutableStateOf(false)

    fun initialize(context: Context) {
        openSingleResult.value = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_OPEN_SINGLE_RESULT, false)
        webFallback.value = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_WEB_FALLBACK, false)
        webPreviews.value = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_WEB_PREVIEWS, false)
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        webQuickPopup.value = prefs.getBoolean(KEY_WEB_QUICK_POPUP, true)
        webSearchMode.value = prefs.getString(KEY_WEB_SEARCH_MODE, null)
            ?.let { stored -> WebSearchMode.entries.firstOrNull { it.name == stored } }
            ?: if (webQuickPopup.value) WebSearchMode.QuickPopup else WebSearchMode.DefaultBrowser
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

    fun saveWebPreviews(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_WEB_PREVIEWS, enabled)
            .apply()
        webPreviews.value = enabled
    }

    fun saveWebQuickPopup(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_WEB_QUICK_POPUP, enabled)
            .apply()
        webQuickPopup.value = enabled
    }

    fun saveWebSearchMode(context: Context, mode: WebSearchMode) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_WEB_SEARCH_MODE, mode.name)
            .putBoolean(KEY_WEB_QUICK_POPUP, mode != WebSearchMode.DefaultBrowser)
            .apply()
        webSearchMode.value = mode
        webQuickPopup.value = mode != WebSearchMode.DefaultBrowser
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
