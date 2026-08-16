package com.astroboii47.commander

import android.content.Context
import androidx.compose.runtime.mutableStateOf

object HomeTypingSettings {
    private const val PREFS = "home_typing"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_HOLD_FOR_ALT = "hold_first_for_alt"
    val enabled = mutableStateOf(false)
    val holdFirstForAlt = mutableStateOf(true)

    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        enabled.value = prefs.getBoolean(KEY_ENABLED, false)
        holdFirstForAlt.value = prefs.getBoolean(KEY_HOLD_FOR_ALT, true)
    }

    fun save(context: Context, value: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_ENABLED, value).apply()
        enabled.value = value
    }

    fun saveHoldFirstForAlt(context: Context, value: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_HOLD_FOR_ALT, value).apply()
        holdFirstForAlt.value = value
    }
}
