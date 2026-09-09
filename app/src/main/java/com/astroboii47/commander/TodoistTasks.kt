package com.astroboii47.commander

import android.content.Context
import android.content.Intent
import android.net.Uri
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

data class TodoistTask(
    val id: String,
    val content: String,
    val description: String,
    val project: String,
    val dueDate: String?,
    val dueDateTime: String?,
    val recurring: Boolean,
    val priority: Int,
    val labels: List<String>,
)

object TodoistTasks {
    private const val PROJECT_CACHE_MS = 30 * 60_000L
    @Volatile private var cachedAt = 0L
    @Volatile private var cached = emptyList<TodoistTask>()
    @Volatile private var projectsCachedAt = 0L
    @Volatile private var cachedProjects = emptyMap<String, String>()

    // Stale data is still useful for an instant first frame. The caller
    // refreshes it in the background and replaces it when the network returns.
    fun cached(): List<TodoistTask> = cached

    fun load(context: Context): Result<List<TodoistTask>> = runCatching {
        val token = TodoistSettings.token(context)
        require(token.isNotBlank()) { "Add your Todoist API token in Commander Settings" }
        val now = System.currentTimeMillis()
        val projects = cachedProjects.takeIf { it.isNotEmpty() && now - projectsCachedAt <= PROJECT_CACHE_MS }
            ?: request("https://api.todoist.com/api/v1/projects?limit=200", token)
                .optJSONArray("results")?.let { array ->
                    buildMap {
                        for (i in 0 until array.length()) {
                            val item = array.optJSONObject(i) ?: continue
                            put(item.optString("id"), item.optString("name"))
                        }
                    }
                }.orEmpty().also {
                    cachedProjects = it
                    projectsCachedAt = now
                }
        // One response powers Today, Upcoming and Calendar. This avoids a new
        // request every time the user changes pages.
        val filter = URLEncoder.encode("overdue | 30 days", StandardCharsets.UTF_8.name())
        val response = request("https://api.todoist.com/api/v1/tasks/filter?query=$filter&limit=200", token)
        val results = response.optJSONArray("results")
        val tasks = buildList {
            if (results != null) for (i in 0 until results.length()) {
                val item = results.optJSONObject(i) ?: continue
                val due = item.optJSONObject("due")
                // Unified API v1 places floating and fixed scheduled times in
                // due.date, while some Todoist responses also expose the
                // older due.datetime field. Preserve either representation.
                val dueValue = due?.optString("date")?.takeIf(String::isNotBlank)
                val dueDateTime = due?.optString("datetime")?.takeIf(String::isNotBlank)
                    ?: dueValue?.takeIf { 'T' in it }
                val labelsJson = item.optJSONArray("labels")
                add(TodoistTask(
                    id = item.optString("id"),
                    content = item.optString("content"),
                    description = item.optString("description"),
                    project = projects[item.optString("project_id")].orEmpty(),
                    dueDate = dueValue?.take(10),
                    dueDateTime = dueDateTime,
                    recurring = due?.optBoolean("is_recurring") == true,
                    priority = item.optInt("priority", 1),
                    labels = buildList {
                        if (labelsJson != null) for (j in 0 until labelsJson.length()) add(labelsJson.optString(j))
                    },
                ))
            }
        }
        cached = tasks
        cachedAt = System.currentTimeMillis()
        tasks
    }

    fun complete(context: Context, taskId: String): String? = runCatching {
        val token = TodoistSettings.token(context)
        val connection = URL("https://api.todoist.com/api/v1/tasks/$taskId/close").openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.connectTimeout = 8_000
        connection.readTimeout = 8_000
        connection.setRequestProperty("Authorization", "Bearer $token")
        val code = connection.responseCode
        (if (code in 200..299) connection.inputStream else connection.errorStream)?.close()
        connection.disconnect()
        if (code in 200..299) {
            cached = cached.filterNot { it.id == taskId }
            null
        } else "Todoist could not complete this task"
    }.getOrElse { "Could not reach Todoist" }

    fun open(context: Context, task: TodoistTask) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("todoist://task?id=${Uri.encode(task.id)}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private fun request(url: String, token: String): JSONObject {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 8_000
        connection.readTimeout = 8_000
        connection.setRequestProperty("Authorization", "Bearer $token")
        val code = connection.responseCode
        val body = (if (code in 200..299) connection.inputStream else connection.errorStream)
            ?.bufferedReader()?.use { it.readText() }.orEmpty()
        connection.disconnect()
        if (code !in 200..299) error(if (code in setOf(401, 403)) "Todoist token was rejected" else "Todoist error $code")
        return JSONObject(body)
    }
}
