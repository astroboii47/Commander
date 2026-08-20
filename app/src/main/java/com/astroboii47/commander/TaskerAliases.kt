package com.astroboii47.commander

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject

data class TaskerAlias(
    val alias: String,
    val label: String,
    val taskName: String,
    val intentUri: String? = null,
    val iconBase64: String? = null,
)

object TaskerAliases {
    private const val PREFS = "tasker_aliases"
    private const val KEY_ITEMS = "items"
    const val TASKER_PACKAGE = "net.dinglisch.android.taskerm"

    fun load(context: Context): List<TaskerAlias> = runCatching {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_ITEMS, "[]") ?: "[]"
        val array = JSONArray(raw)
        buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                add(TaskerAlias(item.getString("alias"), item.getString("label"), item.optString("task"), item.optString("intent").takeIf(String::isNotBlank), item.optString("icon").takeIf(String::isNotBlank)))
            }
        }
    }.getOrDefault(emptyList())

    fun save(context: Context, items: List<TaskerAlias>) {
        val array = JSONArray()
        items.mapNotNull(::normalize).distinct().forEach { item ->
            array.put(JSONObject().put("alias", item.alias).put("label", item.label).put("task", item.taskName).put("intent", item.intentUri.orEmpty()).put("icon", item.iconBase64.orEmpty()))
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_ITEMS, array.toString()).apply()
    }

    fun matches(context: Context, input: String): List<TaskerAlias> {
        val normalized = input.trimStart().lowercase()
        return load(context).filter { item ->
            val prefix = TriggerSettings.effectiveAlias(item.alias.lowercase())
            normalized == prefix || normalized.startsWith("$prefix ")
        }.let { candidates ->
            val term = candidates.firstOrNull()?.let { normalized.removePrefix(TriggerSettings.effectiveAlias(it.alias.lowercase())).trim() }.orEmpty()
            if (term.isBlank()) candidates else candidates.filter {
                it.label.contains(term, true) || it.taskName.contains(term, true)
            }
        }
    }

    fun run(context: Context, item: TaskerAlias): String? {
        val installed = runCatching { context.packageManager.getPackageInfo(TASKER_PACKAGE, 0) }.isSuccess
        if (!installed) return "Tasker is not installed"
        val intentUri = item.intentUri ?: return "Pick this Tasker task again in Command settings"
        return runCatching {
            val launchIntent = Intent.parseUri(intentUri, Intent.URI_INTENT_SCHEME).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(launchIntent)
            null
        }.getOrElse { "Tasker task could not be started" }
    }

    fun icon(context: Context, item: TaskerAlias): Drawable? {
        val custom = item.iconBase64?.let { encoded ->
            runCatching {
                val bytes = Base64.decode(encoded, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.let { BitmapDrawable(context.resources, it) }
            }.getOrNull()
        }
        return custom ?: runCatching { context.packageManager.getApplicationIcon(TASKER_PACKAGE) }.getOrNull()
    }

    private fun normalize(item: TaskerAlias): TaskerAlias? {
        val alias = item.alias.trim().lowercase().filter { it.isLetterOrDigit() || it in ".!_-" }
        val task = item.taskName.trim()
        if (alias.isBlank() || task.isBlank() || item.intentUri.isNullOrBlank()) return null
        return TaskerAlias(alias, item.label.trim().ifBlank { task }, task, item.intentUri, item.iconBase64)
    }
}
