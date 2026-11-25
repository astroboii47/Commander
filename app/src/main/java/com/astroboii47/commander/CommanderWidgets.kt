package com.astroboii47.commander

import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.mutableIntStateOf
import org.json.JSONArray
import org.json.JSONObject
import java.lang.ref.WeakReference

data class CommanderWidget(
    val appWidgetId: Int,
    val name: String,
    val aliases: String,
    val providerPackage: String,
)

object CommanderWidgets {
    const val HOST_ID = 0x434f4d57
    private const val PREFS = "commander_widgets"
    private const val KEY_WIDGETS = "widgets"

    val revision = mutableIntStateOf(0)
    private var cached: List<CommanderWidget>? = null

    fun all(context: Context): List<CommanderWidget> {
        cached?.let { return it }
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_WIDGETS, "[]") ?: "[]"
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    add(
                        CommanderWidget(
                            appWidgetId = item.getInt("id"),
                            name = item.optString("name").ifBlank { "Widget" },
                            aliases = item.optString("aliases"),
                            providerPackage = item.optString("package"),
                        ),
                    )
                }
            }
        }.getOrDefault(emptyList()).also { cached = it }
    }

    fun searchable(context: Context, query: String): List<CommanderWidget> {
        val needle = query.trim().lowercase()
        if (needle.isBlank()) return emptyList()
        return all(context).filter { widget ->
            widget.name.lowercase().contains(needle) ||
                widget.aliases.split(',').any { it.trim().lowercase().let { alias -> alias.isNotBlank() && alias.contains(needle) } }
        }
    }

    fun add(context: Context, appWidgetId: Int) {
        val info = AppWidgetManager.getInstance(context).getAppWidgetInfo(appWidgetId) ?: return
        val label = info.loadLabel(context.packageManager).toString().ifBlank { "Widget" }
        val updated = all(context).filterNot { it.appWidgetId == appWidgetId } + CommanderWidget(
            appWidgetId = appWidgetId,
            name = label,
            aliases = label,
            providerPackage = info.provider.packageName,
        )
        save(context, updated)
    }

    fun update(context: Context, widget: CommanderWidget) {
        save(context, all(context).map { if (it.appWidgetId == widget.appWidgetId) widget else it })
    }

    fun remove(context: Context, appWidgetId: Int) {
        save(context, all(context).filterNot { it.appWidgetId == appWidgetId })
        runCatching { android.appwidget.AppWidgetHost(context, HOST_ID).deleteAppWidgetId(appWidgetId) }
    }

    fun configureIntent(context: Context, appWidgetId: Int): Intent? {
        val info = AppWidgetManager.getInstance(context).getAppWidgetInfo(appWidgetId) ?: return null
        val configure = info.configure ?: return null
        return Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
            component = configure
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
    }

    private fun save(context: Context, widgets: List<CommanderWidget>) {
        val array = JSONArray()
        widgets.forEach { widget ->
            array.put(JSONObject().apply {
                put("id", widget.appWidgetId)
                put("name", widget.name)
                put("aliases", widget.aliases)
                put("package", widget.providerPackage)
            })
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_WIDGETS, array.toString()).apply()
        cached = widgets
        revision.intValue++
    }
}

object CommanderWidgetActions {
    private val views = mutableMapOf<Int, WeakReference<AppWidgetHostView>>()

    fun register(appWidgetId: Int, view: AppWidgetHostView) {
        views[appWidgetId] = WeakReference(view)
    }

    fun unregister(appWidgetId: Int, view: AppWidgetHostView) {
        if (views[appWidgetId]?.get() === view) views.remove(appWidgetId)
    }

    fun perform(appWidgetId: Int): Boolean {
        val host = views[appWidgetId]?.get() ?: return false
        return findClickable(host)?.performClick() == true
    }

    private fun findClickable(view: View): View? {
        if (view.visibility != View.VISIBLE || !view.isEnabled) return null
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                findClickable(view.getChildAt(index))?.let { return it }
            }
        }
        return view.takeIf { it.hasOnClickListeners() || it.isClickable }
    }
}
