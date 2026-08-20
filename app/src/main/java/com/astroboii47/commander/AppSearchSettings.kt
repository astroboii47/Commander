package com.astroboii47.commander

import android.content.Context
import androidx.compose.runtime.mutableStateOf

object AppSearchSettings {
    private const val PREFS = "app_search"
    private const val KEY_OPEN_SINGLE_RESULT = "open_single_result"
    private const val KEY_WEB_FALLBACK = "web_fallback"
    private const val KEY_ALIAS_SUGGESTIONS = "alias_suggestions"

    val openSingleResult = mutableStateOf(false)
    val webFallback = mutableStateOf(false)
    val aliasSuggestions = mutableStateOf(true)

    fun initialize(context: Context) {
        openSingleResult.value = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_OPEN_SINGLE_RESULT, false)
        webFallback.value = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_WEB_FALLBACK, false)
        aliasSuggestions.value = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_ALIAS_SUGGESTIONS, true)
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
}
