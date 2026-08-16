package com.astroboii47.commander

import android.content.Context
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import android.os.SystemClock
import android.util.Log

object TodoistApi {
    fun quickAdd(context: Context, text: String): String? {
        val token = TodoistSettings.token(context)
        if (token.isBlank()) return "Add your Todoist API token in Commander Settings"
        val started = SystemClock.elapsedRealtime()
        return runCatching {
            val connection = URL("https://api.todoist.com/api/v1/tasks/quick").openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.connectTimeout = 8_000
            connection.readTimeout = 8_000
            connection.doOutput = true
            connection.setRequestProperty("Authorization", "Bearer $token")
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Connection", "keep-alive")
            val request = JSONObject().put("text", text).put("meta", true).toString().toByteArray()
            connection.setFixedLengthStreamingMode(request.size)
            connection.outputStream.use { output ->
                output.write(request)
            }
            val code = connection.responseCode
            // Fully consume the response so Android can return the underlying
            // socket to its keep-alive pool for the next Quick Add.
            val response = if (code in 200..399) connection.inputStream else connection.errorStream
            response?.use { stream ->
                val buffer = ByteArray(1_024)
                while (stream.read(buffer) != -1) Unit
            }
            connection.disconnect()
            Log.i("MinimalTodoist", "quickAdd code=$code durationMs=${SystemClock.elapsedRealtime() - started}")
            when (code) {
                in 200..299 -> null
                401, 403 -> "Todoist token rejected · check Commander Settings"
                else -> "Todoist could not add the task · error $code"
            }
        }.getOrElse {
            Log.e("MinimalTodoist", "quickAdd failed durationMs=${SystemClock.elapsedRealtime() - started}", it)
            "Could not reach Todoist"
        }
    }
}
