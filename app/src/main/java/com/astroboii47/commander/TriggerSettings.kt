package com.astroboii47.commander

import android.content.Context
import androidx.compose.runtime.mutableStateOf

object TriggerSettings {
    private const val PREFS = "trigger_settings"
    private const val KEY_SWAP_DOT_BANG = "swap_dot_bang"
    val swapDotBang = mutableStateOf(false)

    fun initialize(context: Context) {
        swapDotBang.value = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_SWAP_DOT_BANG, false)
    }

    fun save(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_SWAP_DOT_BANG, enabled).apply()
        swapDotBang.value = enabled
    }

    fun noteSymbol(): Char = if (swapDotBang.value) '.' else '!'
    fun effectiveAlias(saved: String): String =
        if (swapDotBang.value && saved.startsWith('.')) "!${saved.drop(1)}" else saved
}
