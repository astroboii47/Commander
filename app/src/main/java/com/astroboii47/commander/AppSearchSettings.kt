package com.astroboii47.commander

import android.content.Context
import androidx.compose.runtime.mutableStateOf

object AppSearchSettings {
    private const val PREFS = "app_search"
    private const val KEY_OPEN_SINGLE_RESULT = "open_single_result"

    val openSingleResult = mutableStateOf(false)

    fun initialize(context: Context) {
        openSingleResult.value = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_OPEN_SINGLE_RESULT, false)
    }

    fun saveOpenSingleResult(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_OPEN_SINGLE_RESULT, enabled)
            .apply()
        openSingleResult.value = enabled
    }
}
