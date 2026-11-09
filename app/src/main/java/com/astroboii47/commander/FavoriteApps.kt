package com.astroboii47.commander

import android.content.Context

object FavoriteApps {
    private const val Key = "dock_favourites"
    private const val Separator = "|"

    fun pinned(context: Context): List<String> = context
        .getSharedPreferences("commander_apps", Context.MODE_PRIVATE)
        .getString(Key, "").orEmpty().split(Separator).filter(String::isNotBlank).distinct().take(9)

    fun contains(context: Context, packageName: String) = packageName in pinned(context)

    fun toggle(context: Context, packageName: String) {
        val current = pinned(context).toMutableList()
        if (!current.remove(packageName)) {
            if (current.size == 9) current.removeAt(0)
            current += packageName
        }
        save(context, current)
    }

    fun move(context: Context, packageName: String, targetIndex: Int) {
        val current = pinned(context).toMutableList()
        val sourceIndex = current.indexOf(packageName)
        if (sourceIndex < 0) return
        val item = current.removeAt(sourceIndex)
        current.add(targetIndex.coerceIn(0, current.size), item)
        save(context, current)
    }

    private fun save(context: Context, packages: List<String>) =
        context.getSharedPreferences("commander_apps", Context.MODE_PRIVATE).edit()
            .putString(Key, packages.distinct().take(9).joinToString(Separator)).apply()

    fun resolved(context: Context): List<AppCatalogEntry> {
        val all = AppCatalog.all(context)
        val byPackage = all.associateBy(AppCatalogEntry::packageName)
        val fixed = pinned(context).mapNotNull(byPackage::get)
        val prefs = context.getSharedPreferences("commander_apps", Context.MODE_PRIVATE)
        val recent = prefs.all.asSequence()
            .filter { (key, value) -> key.startsWith("recent_") && value is Long }
            .sortedByDescending { (_, value) -> value as Long }
            .mapNotNull { (key, _) -> byPackage[key.removePrefix("recent_")] }
            .filter { candidate -> fixed.none { it.packageName == candidate.packageName } }
            .toList()
        return if (prefs.contains(Key)) fixed.take(9) else emptyList()
    }

    fun glyph(context: Context, packageName: String): String = context
        .getSharedPreferences("commander_apps", Context.MODE_PRIVATE)
        .getString("dock_glyph_$packageName", "auto") ?: "auto"

    fun setGlyph(context: Context, packageName: String, glyph: String) {
        context.getSharedPreferences("commander_apps", Context.MODE_PRIVATE).edit()
            .putString("dock_glyph_$packageName", glyph).apply()
    }
}
