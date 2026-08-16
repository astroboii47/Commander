package com.astroboii47.commander

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Process
import android.util.Log
import org.xmlpull.v1.XmlPullParser
import java.util.Locale

data class AppShortcutResult(
    val packageName: String,
    val id: String,
    val label: String,
    val appLabel: String,
    val intent: Intent?,
    val icon: Drawable?,
)

object AppShortcutSettings {
    private const val PREFS = "app_shortcut_search"
    private const val KEY_ALIAS = "alias"
    fun alias(context: Context): String = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .getString(KEY_ALIAS, "!as").orEmpty().trim().lowercase()
    fun save(context: Context, value: String) {
        val normalized = value.trim().lowercase().filter { it.isLetterOrDigit() || it in ".!_-" }.ifBlank { "!as" }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_ALIAS, normalized).apply()
        AppShortcutCatalog.clear()
    }
    fun query(context: Context, input: String): String? {
        val alias = alias(context)
        val normalized = input.trimStart()
        return when {
            normalized.equals(alias, true) -> ""
            normalized.startsWith("$alias ", true) -> normalized.substring(alias.length).trim()
            else -> null
        }
    }
}

object AppShortcutCatalog {
    private const val ANDROID_NS = "http://schemas.android.com/apk/res/android"
    @Volatile private var cached: List<AppShortcutResult>? = null

    fun clear() { cached = null }

    fun search(context: Context, query: String, limit: Int = 6): List<AppShortcutResult> {
        val all = cached ?: load(context.applicationContext).also { cached = it }
        val needle = query.trim().lowercase(Locale.ROOT)
        return all.asSequence()
            .filter { needle.isBlank() || needle in it.label.lowercase(Locale.ROOT) || needle in it.appLabel.lowercase(Locale.ROOT) }
            .sortedWith(compareBy<AppShortcutResult> { needle.isNotBlank() && !it.label.startsWith(needle, true) }.thenBy { it.label })
            .take(limit).toList()
    }

    fun launch(context: Context, result: AppShortcutResult): String? = runCatching {
        if (result.intent != null) {
            context.startActivity(Intent(result.intent).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra(Intent.EXTRA_SHORTCUT_ID, result.id))
        } else {
            val launcherApps = context.getSystemService(LauncherApps::class.java)
            launcherApps.startShortcut(result.packageName, result.id, null, null, Process.myUserHandle())
        }
        null
    }.getOrElse { "${result.label} could not be opened" }

    private fun load(context: Context): List<AppShortcutResult> {
        loadViaLauncherApps(context).takeIf(List<AppShortcutResult>::isNotEmpty)?.let { return it }
        return loadManifestShortcuts(context)
    }

    private fun loadViaLauncherApps(context: Context): List<AppShortcutResult> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return emptyList()
        val launcherApps = context.getSystemService(LauncherApps::class.java) ?: return emptyList()
        if (!launcherApps.hasShortcutHostPermission()) return emptyList()
        val flags = LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED
        val infos = runCatching { launcherApps.getShortcuts(LauncherApps.ShortcutQuery().setQueryFlags(flags), Process.myUserHandle()) }.getOrNull().orEmpty()
        return infos.filter { it.isEnabled }.map { info ->
            val appLabel = runCatching {
                context.packageManager.getApplicationLabel(context.packageManager.getApplicationInfo(info.`package`, 0)).toString()
            }.getOrDefault(info.`package`)
            AppShortcutResult(info.`package`, info.id, info.shortLabel?.toString() ?: info.id, appLabel, null,
                runCatching { launcherApps.getShortcutIconDrawable(info, context.resources.displayMetrics.densityDpi) }.getOrNull())
        }.distinctBy { "${it.packageName}:${it.id}" }
    }

    private fun loadManifestShortcuts(context: Context): List<AppShortcutResult> {
        val pm = context.packageManager
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        @Suppress("DEPRECATION")
        val activities = pm.queryIntentActivities(launcherIntent, PackageManager.GET_META_DATA)
        return activities.asSequence().mapNotNull { resolve ->
            val packageName = resolve.activityInfo.packageName
            val xmlId = resolve.activityInfo.metaData?.getInt("android.app.shortcuts", 0) ?: 0
            if (xmlId == 0) null else Triple(packageName, resolve.loadLabel(pm).toString(), xmlId)
        }.distinct().flatMap { (packageName, appLabel, xmlId) ->
            parsePackage(context, packageName, appLabel, xmlId).asSequence()
        }.distinctBy { "${it.packageName}:${it.id}" }.toList()
    }

    private fun parsePackage(context: Context, packageName: String, appLabel: String, xmlId: Int): List<AppShortcutResult> {
        val target = runCatching { context.createPackageContext(packageName, 0) }.getOrNull() ?: return emptyList()
        val parser = runCatching { target.resources.getXml(xmlId) }.getOrNull() ?: return emptyList()
        val results = mutableListOf<AppShortcutResult>()
        var id: String? = null; var label: String? = null; var iconId = 0; var intent: Intent? = null; var enabled = true
        runCatching {
            while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                if (parser.eventType == XmlPullParser.START_TAG) when (parser.name) {
                    "shortcut" -> {
                        id = parser.getAttributeValue(ANDROID_NS, "shortcutId")
                        enabled = parser.getAttributeBooleanValue(ANDROID_NS, "enabled", true)
                        iconId = parser.getAttributeResourceValue(ANDROID_NS, "icon", 0)
                        val labelId = parser.getAttributeResourceValue(ANDROID_NS, "shortcutShortLabel", 0)
                        label = if (labelId != 0) runCatching { target.getString(labelId) }.getOrNull() else parser.getAttributeValue(ANDROID_NS, "shortcutShortLabel")
                        intent = null
                    }
                    "intent" -> {
                        val action = parser.getAttributeValue(ANDROID_NS, "action") ?: Intent.ACTION_VIEW
                        intent = Intent(action).apply {
                            parser.getAttributeValue(ANDROID_NS, "data")?.let { data = android.net.Uri.parse(it) }
                            val targetPackage = parser.getAttributeValue(ANDROID_NS, "targetPackage") ?: packageName
                            val targetClass = parser.getAttributeValue(ANDROID_NS, "targetClass")
                            if (targetClass != null) setClassName(targetPackage, if (targetClass.startsWith('.')) targetPackage + targetClass else targetClass)
                            else setPackage(targetPackage)
                        }
                    }
                } else if (parser.eventType == XmlPullParser.END_TAG && parser.name == "shortcut") {
                    val shortcutId = id
                    if (enabled && !shortcutId.isNullOrBlank() && intent != null) {
                        val icon = if (iconId != 0) runCatching { target.getDrawable(iconId) }.getOrNull()
                            else runCatching { context.packageManager.getApplicationIcon(packageName) }.getOrNull()
                        results += AppShortcutResult(packageName, shortcutId, label ?: shortcutId, appLabel, intent, icon)
                    }
                }
                parser.next()
            }
        }.onFailure { Log.w("MinimalShortcuts", "parse failed package=$packageName", it) }
        parser.close()
        return results
    }
}
