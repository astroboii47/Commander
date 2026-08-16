package com.astroboii47.commander

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject

object SettingsBackup {
    private const val FORMAT = "commander-settings"
    private const val VERSION = 1

    private val preferenceFiles = listOf(
        "appearance_settings",
        "command_appearance",
        "trigger_settings",
        "file_search_settings",
        "app_search",
        "app_shortcut_search",
        "app_search_terms",
        "app_icon_overrides",
        "command_aliases",
        "tasker_aliases",
        "home_typing",
        "hub_settings",
        "sms_settings",
        "messenger_settings",
        "messenger_chats",
        "sound_feedback",
        "todoist_settings",
    )

    private val excludedKeys = mapOf(
        "todoist_settings" to setOf("token"),
    )

    fun export(context: Context, uri: Uri): Result<Unit> = runCatching {
        val files = JSONObject()
        preferenceFiles.forEach { name ->
            val values = JSONObject()
            context.getSharedPreferences(name, Context.MODE_PRIVATE).all.forEach { (key, value) ->
                if (key !in excludedKeys[name].orEmpty()) values.put(key, encode(value))
            }
            files.put(name, values)
        }
        val root = JSONObject()
            .put("format", FORMAT)
            .put("version", VERSION)
            .put("credentialsIncluded", false)
            .put("preferences", files)
        context.contentResolver.openOutputStream(uri, "wt")?.bufferedWriter()?.use { writer ->
            writer.write(root.toString(2))
        } ?: error("Could not open the selected file")
    }

    fun import(context: Context, uri: Uri): Result<Unit> = runCatching {
        val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            ?: error("Could not open the selected file")
        val root = JSONObject(text)
        require(root.optString("format") == FORMAT) { "This is not a Commander settings backup" }
        require(root.optInt("version") in 1..VERSION) { "This backup was made by a newer Commander version" }
        val files = root.getJSONObject("preferences")
        preferenceFiles.forEach { name ->
            val imported = files.optJSONObject(name) ?: return@forEach
            val prefs = context.getSharedPreferences(name, Context.MODE_PRIVATE)
            val preserved = excludedKeys[name].orEmpty().associateWith { prefs.all[it] }
            val editor = prefs.edit().clear()
            val keys = imported.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                decodeInto(editor, key, imported.getJSONObject(key))
            }
            preserved.forEach { (key, value) -> put(editor, key, value) }
            editor.commit()
        }
        refreshRuntimeSettings(context)
    }

    private fun encode(value: Any?): JSONObject = when (value) {
        is Boolean -> JSONObject().put("type", "boolean").put("value", value)
        is Float -> JSONObject().put("type", "float").put("value", value.toDouble())
        is Int -> JSONObject().put("type", "int").put("value", value)
        is Long -> JSONObject().put("type", "long").put("value", value)
        is String -> JSONObject().put("type", "string").put("value", value)
        is Set<*> -> JSONObject().put("type", "stringSet").put("value", JSONArray(value.filterIsInstance<String>()))
        else -> error("Unsupported settings value")
    }

    private fun decodeInto(editor: android.content.SharedPreferences.Editor, key: String, encoded: JSONObject) {
        when (encoded.getString("type")) {
            "boolean" -> editor.putBoolean(key, encoded.getBoolean("value"))
            "float" -> editor.putFloat(key, encoded.getDouble("value").toFloat())
            "int" -> editor.putInt(key, encoded.getInt("value"))
            "long" -> editor.putLong(key, encoded.getLong("value"))
            "string" -> editor.putString(key, encoded.getString("value"))
            "stringSet" -> {
                val array = encoded.getJSONArray("value")
                editor.putStringSet(key, buildSet { for (index in 0 until array.length()) add(array.getString(index)) })
            }
            else -> error("Unsupported settings type")
        }
    }

    private fun put(editor: android.content.SharedPreferences.Editor, key: String, value: Any?) {
        when (value) {
            is Boolean -> editor.putBoolean(key, value)
            is Float -> editor.putFloat(key, value)
            is Int -> editor.putInt(key, value)
            is Long -> editor.putLong(key, value)
            is String -> editor.putString(key, value)
            is Set<*> -> editor.putStringSet(key, value.filterIsInstance<String>().toSet())
        }
    }

    private fun refreshRuntimeSettings(context: Context) {
        AccentSettings.initialize(context)
        TriggerSettings.initialize(context)
        FileSearchSettings.initialize(context)
        AppearanceSettings.initialize(context)
        AppSearchSettings.initialize(context)
        SoundSettings.initialize(context)
        SoundFeedback.initialize(context)
        HomeTypingSettings.initialize(context)
        HubSettings.initialize(context)
        AppCatalog.initialize(context)
        AppShortcutCatalog.clear()
    }
}
