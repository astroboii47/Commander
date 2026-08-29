package com.astroboii47.commander

import android.content.Context

object AppLaunchHistory {
    private const val PREFS = "commander_app_recents"
    private const val KEY_PACKAGES = "packages"
    private const val SEPARATOR = "\u001F"

    fun record(context: Context, packageName: String) {
        val updated = (listOf(packageName) + packages(context))
            .distinct()
            .take(10)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PACKAGES, updated.joinToString(SEPARATOR))
            .apply()
    }

    fun packages(context: Context): List<String> = context
        .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .getString(KEY_PACKAGES, "")
        .orEmpty()
        .split(SEPARATOR)
        .filter(String::isNotBlank)
}
