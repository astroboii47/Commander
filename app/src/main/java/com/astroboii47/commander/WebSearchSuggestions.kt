package com.astroboii47.commander

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.LinkedHashMap

/** Lightweight, opt-in query previews for Commander's existing Google search. */
object WebSearchSuggestions {
    private const val MaxCacheEntries = 24
    private val cache = object : LinkedHashMap<String, List<String>>(MaxCacheEntries, .75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, List<String>>?) =
            size > MaxCacheEntries
    }

    suspend fun load(query: String): List<String> = withContext(Dispatchers.IO) {
        val normalized = query.trim()
        if (normalized.length < 2) return@withContext emptyList()
        synchronized(cache) { cache[normalized.lowercase()] }?.let { return@withContext it }
        val encoded = URLEncoder.encode(normalized, StandardCharsets.UTF_8.toString())
        val connection = (URL("https://suggestqueries.google.com/complete/search?client=firefox&q=$encoded")
            .openConnection() as HttpURLConnection).apply {
            connectTimeout = 1_500
            readTimeout = 1_500
            requestMethod = "GET"
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "Commander/1 Android")
        }
        val suggestions = runCatching {
            connection.inputStream.bufferedReader().use { reader ->
                val values = JSONArray(reader.readText()).optJSONArray(1) ?: JSONArray()
                buildList<String> {
                    for (index in 0 until values.length()) {
                        val value = values.optString(index).trim()
                        if (value.isNotEmpty() && !value.equals(normalized, true) && none { it.equals(value, true) }) add(value)
                        if (size == 3) break
                    }
                }
            }
        }.getOrDefault(emptyList())
        connection.disconnect()
        synchronized(cache) { cache[normalized.lowercase()] = suggestions }
        suggestions
    }
}
