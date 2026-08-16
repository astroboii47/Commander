package com.astroboii47.commander

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.mutableStateOf
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

data class AppCatalogEntry(
    val label: String,
    val packageName: String,
    val searchable: String,
)

object AppCatalog {
    private const val PREFS = "app_catalog"
    private const val KEY_ENTRIES = "entries_v1"
    private val rebuilding = AtomicBoolean(false)

    private val entriesState = mutableStateOf<List<AppCatalogEntry>>(emptyList())

    fun initialize(context: Context) {
        if (entriesState.value.isNotEmpty()) return
        entriesState.value = readSaved(context.applicationContext)
        if (entriesState.value.isEmpty()) refreshAsync(context.applicationContext)
    }

    fun search(context: Context, query: String, limit: Int): List<AppCatalogEntry> {
        initialize(context)
        val needle = query.trim().lowercase(Locale.ROOT)
        if (needle.isEmpty()) return emptyList()
        val customTerms = AppSearchTerms.aliases(context)
        return entriesState.value.asSequence()
            .filter { entry -> needle in entry.searchable || customTerms[entry.packageName].orEmpty().any { term -> term.lowercase(Locale.ROOT).startsWith(needle) } }
            .sortedWith(
                compareBy<AppCatalogEntry> { entry -> !customTerms[entry.packageName].orEmpty().any { it.equals(needle, true) } }
                    .thenBy { entry -> !customTerms[entry.packageName].orEmpty().any { it.startsWith(needle, true) } }
                    .thenBy { !it.label.lowercase(Locale.ROOT).startsWith(needle) }
                    .thenBy { it.label.length },
            )
            .take(limit)
            .toList()
    }

    fun settingsEntries(context: Context, query: String, limit: Int = 8): List<AppCatalogEntry> {
        initialize(context)
        val saved = AppSearchTerms.aliases(context).keys
        val needle = query.trim().lowercase(Locale.ROOT)
        return entriesState.value.asSequence()
            .filter { if (needle.isBlank()) it.packageName in saved else needle in it.searchable }
            .sortedWith(compareBy<AppCatalogEntry> { it.packageName !in saved }.thenBy { it.label.lowercase(Locale.ROOT) })
            .take(limit)
            .toList()
    }

    fun refreshAsync(context: Context) {
        val appContext = context.applicationContext
        if (!rebuilding.compareAndSet(false, true)) return
        Thread({
            try {
                val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
                val rebuilt = appContext.packageManager.queryIntentActivities(launcherIntent, 0)
                    .asSequence()
                    .filter { it.activityInfo.packageName != appContext.packageName }
                    .map {
                        val label = it.loadLabel(appContext.packageManager).toString()
                        val packageName = it.activityInfo.packageName
                        AppCatalogEntry(
                            label = label,
                            packageName = packageName,
                            searchable = "$label $packageName".lowercase(Locale.ROOT),
                        )
                    }
                    .distinctBy { it.packageName }
                    .sortedBy { it.label.lowercase(Locale.ROOT) }
                    .toList()
                entriesState.value = rebuilt
                save(appContext, rebuilt)
            } finally {
                rebuilding.set(false)
            }
        }, "minimal-app-catalog").start()
    }

    private fun readSaved(context: Context): List<AppCatalogEntry> = runCatching {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_ENTRIES, null)
            ?: return emptyList()
        val array = JSONArray(raw)
        buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                val label = item.getString("label")
                val packageName = item.getString("package")
                add(AppCatalogEntry(label, packageName, "$label $packageName".lowercase(Locale.ROOT)))
            }
        }
    }.getOrDefault(emptyList())

    private fun save(context: Context, value: List<AppCatalogEntry>) {
        val array = JSONArray()
        value.forEach { entry ->
            array.put(JSONObject().put("label", entry.label).put("package", entry.packageName))
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_ENTRIES, array.toString())
            .apply()
    }
}
