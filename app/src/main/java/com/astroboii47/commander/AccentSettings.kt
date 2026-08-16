package com.astroboii47.commander

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color

object AccentSettings {
    const val DEFAULT_HEX = "#FF3212"
    private const val PREFS = "appearance_settings"
    private const val KEY_COLOR = "accent_color"
    private const val KEY_RECENT = "recent_custom_colors"
    private val presets = setOf("#FF3212", "#FF5A36", "#FFB000", "#62D26F", "#54A8FF", "#B28CFF")
    val color = mutableStateOf(Color(0xFFFF3212))

    fun initialize(context: Context) {
        color.value = parse(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_COLOR, DEFAULT_HEX))
    }

    fun save(context: Context, value: String): Boolean {
        val normalized = normalize(value) ?: return false
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val editor = prefs.edit().putString(KEY_COLOR, normalized)
        if (normalized !in presets) {
            val recent = (listOf(normalized) + recent(context).filterNot { it == normalized }).take(3)
            editor.putString(KEY_RECENT, recent.joinToString(","))
        }
        editor.apply()
        color.value = parse(normalized)
        return true
    }

    fun currentHex(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_COLOR, DEFAULT_HEX) ?: DEFAULT_HEX

    fun recent(context: Context): List<String> = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .getString(KEY_RECENT, "").orEmpty().split(',').filter { normalize(it) != null }.take(3)

    private fun normalize(value: String): String? {
        val clean = value.trim().removePrefix("#")
        if (!clean.matches(Regex("[0-9a-fA-F]{6}"))) return null
        return "#${clean.uppercase()}"
    }

    private fun parse(value: String?): Color = runCatching {
        Color(android.graphics.Color.parseColor(value ?: DEFAULT_HEX))
    }.getOrDefault(Color(0xFFFF3212))
}
