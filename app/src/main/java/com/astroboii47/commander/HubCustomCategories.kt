package com.astroboii47.commander

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class HubCustomCategory(val id: String, val label: String)

object HubCustomCategories {
    private const val Prefs = "hub_custom_categories"
    private const val Key = "categories"

    fun load(context: Context): List<HubCustomCategory> = runCatching {
        val raw = context.getSharedPreferences(Prefs, Context.MODE_PRIVATE)
            .getString(Key, "[]").orEmpty()
        val array = JSONArray(raw)
        buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                add(HubCustomCategory(item.getString("id"), item.getString("label")))
            }
        }
    }.getOrDefault(emptyList())

    fun save(context: Context, categories: List<HubCustomCategory>) {
        val array = JSONArray()
        categories.forEach { category ->
            array.put(JSONObject().put("id", category.id).put("label", category.label))
        }
        context.getSharedPreferences(Prefs, Context.MODE_PRIVATE).edit()
            .putString(Key, array.toString()).apply()
    }

    fun create(label: String): HubCustomCategory = HubCustomCategory(
        id = "custom_${System.currentTimeMillis().toString(36)}",
        label = label.trim(),
    )
}
