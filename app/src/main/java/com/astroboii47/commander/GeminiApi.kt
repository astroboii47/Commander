package com.astroboii47.commander

import android.content.Context
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONArray
import org.json.JSONObject

data class AskMessage(val role: String, val text: String)

object GeminiApi {
    @Volatile private var cachedModel: String? = null

    fun generate(context: Context, history: List<AskMessage>): Result<String> = runCatching {
        val key = GeminiSettings.apiKey(context)
        require(key.isNotBlank()) { "Add your Gemini API key in Command settings" }
        var model = cachedModel ?: discoverModel(key).also { cachedModel = it }
        val first = request(key, model, history)
        if (first.isSuccess) return@runCatching first.getOrThrow()
        val error = first.exceptionOrNull() ?: error("Gemini request failed")
        if (error.message?.contains("no longer available", true) == true ||
            error.message?.contains("not found", true) == true
        ) {
            cachedModel = null
            model = discoverModel(key)
            cachedModel = model
            return@runCatching request(key, model, history).getOrThrow()
        }
        throw error
    }

    private fun request(key: String, model: String, history: List<AskMessage>): Result<String> = runCatching {
        val contents = JSONArray().apply {
            history.forEach { message ->
                put(JSONObject().apply {
                    put("role", if (message.role == "user") "user" else "model")
                    put("parts", JSONArray().put(JSONObject().put("text", message.text)))
                })
            }
        }
        val body = JSONObject().apply {
            put("contents", contents)
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.7)
                put("maxOutputTokens", 1024)
            })
        }.toString()
        val connection = URL(
            "https://generativelanguage.googleapis.com/v1beta/$model:generateContent",
        ).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 15_000
            connection.readTimeout = 45_000
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("x-goog-api-key", key)
            connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            val code = connection.responseCode
            val raw = (if (code in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) {
                val apiMessage = runCatching {
                    JSONObject(raw).getJSONObject("error").getString("message")
                }.getOrNull()
                error(apiMessage ?: "Gemini request failed · $code")
            }
            val candidates = JSONObject(raw).optJSONArray("candidates")
                ?: error("Gemini returned no answer")
            val parts = candidates.optJSONObject(0)?.optJSONObject("content")?.optJSONArray("parts")
                ?: error("Gemini returned no text")
            buildString {
                for (index in 0 until parts.length()) {
                    parts.optJSONObject(index)?.optString("text")?.let(::append)
                }
            }.trim().ifBlank { error("Gemini returned an empty answer") }
        } finally {
            connection.disconnect()
        }
    }

    private fun discoverModel(key: String): String {
        val connection = URL("https://generativelanguage.googleapis.com/v1beta/models")
            .openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 15_000
            connection.readTimeout = 20_000
            connection.setRequestProperty("x-goog-api-key", key)
            val code = connection.responseCode
            val raw = (if (code in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) {
                val message = runCatching { JSONObject(raw).getJSONObject("error").getString("message") }.getOrNull()
                error(message ?: "Could not list Gemini models · $code")
            }
            val models = JSONObject(raw).optJSONArray("models") ?: error("No Gemini models available")
            val candidates = buildList {
                for (index in 0 until models.length()) {
                    val model = models.optJSONObject(index) ?: continue
                    val methods = model.optJSONArray("supportedGenerationMethods") ?: continue
                    val supportsGenerate = (0 until methods.length()).any {
                        methods.optString(it) == "generateContent"
                    }
                    val name = model.optString("name")
                    val lower = name.lowercase()
                    if (supportsGenerate && "gemini" in lower && "flash" in lower &&
                        listOf("image", "tts", "live", "embedding", "vision").none(lower::contains)
                    ) add(name)
                }
            }
            return candidates.maxByOrNull { name ->
                var score = 0
                val lower = name.lowercase()
                if ("lite" in lower) score += 100
                if ("preview" !in lower && "experimental" !in lower && "exp" !in lower) score += 50
                Regex("gemini-(\\d+)(?:\\.(\\d+))?").find(lower)?.let { match ->
                    score += match.groupValues[1].toIntOrNull()?.times(10) ?: 0
                    score += match.groupValues.getOrNull(2)?.toIntOrNull() ?: 0
                }
                score
            } ?: error("No compatible Gemini Flash text model is available for this key")
        } finally {
            connection.disconnect()
        }
    }
}
