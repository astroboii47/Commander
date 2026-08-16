package com.astroboii47.commander

import android.content.Context

object AppSearchTerms {
    private const val PREFS = "app_search_terms"

    fun terms(context: Context, packageName: String): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(packageName, "").orEmpty()

    fun save(context: Context, packageName: String, value: String) {
        val normalized = value.split(',').map(String::trim).filter(String::isNotBlank).distinct().joinToString(", ")
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().apply {
            if (normalized.isBlank()) remove(packageName) else putString(packageName, normalized)
        }.apply()
    }

    fun aliases(context: Context): Map<String, List<String>> =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).all.mapNotNull { (pkg, value) ->
            (value as? String)?.split(',')?.map(String::trim)?.filter(String::isNotBlank)?.takeIf(List<String>::isNotEmpty)?.let { pkg to it }
        }.toMap()
}
