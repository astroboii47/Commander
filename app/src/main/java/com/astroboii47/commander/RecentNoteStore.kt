package com.astroboii47.commander

import android.content.Context

object RecentNoteStore {
    private const val Preferences = "commander_recent_note"
    private const val Latest = "latest"

    fun save(context: Context, text: String) {
        val note = text.trim()
        if (note.isNotEmpty()) {
            context.getSharedPreferences(Preferences, Context.MODE_PRIVATE)
                .edit()
                .putString(Latest, note)
                .apply()
        }
    }

    fun latest(context: Context): String? =
        context.getSharedPreferences(Preferences, Context.MODE_PRIVATE)
            .getString(Latest, null)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
}
