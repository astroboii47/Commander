package com.astroboii47.commander

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.mutableStateOf

/** A separate shortcut from the legacy "split active app" control. */
object SplitPairSettings {
    private const val PREFS = "split_pair_settings"
    private const val KEY_TRIGGER = "trigger"
    val trigger = mutableStateOf("spl")

    fun initialize(context: Context) {
        trigger.value = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_TRIGGER, "spl")?.trim()?.takeIf(String::isNotEmpty) ?: "spl"
    }

    fun save(context: Context, value: String) {
        val cleaned = value.trim().take(24).ifEmpty { "spl" }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_TRIGGER, cleaned).apply()
        trigger.value = cleaned
    }

    fun queryAfterTrigger(query: String): String? {
        val shortcut = trigger.value
        if (!query.startsWith(shortcut, ignoreCase = true)) return null
        if (query.length == shortcut.length) return null
        if (!query[shortcut.length].isWhitespace()) return null
        return query.substring(shortcut.length).trimStart()
    }
}

object SplitPairLauncher {
    fun launch(activity: Activity, topPackage: String, bottomPackage: String): String? {
        if (topPackage == bottomPackage) return "Choose two different apps"
        val bottom = activity.packageManager.getLaunchIntentForPackage(bottomPackage)
            ?: return "Bottom app cannot be opened"
        val top = activity.packageManager.getLaunchIntentForPackage(topPackage)
            ?: return "Top app cannot be opened"
        val handler = Handler(Looper.getMainLooper())
        top.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        bottom.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT)
        return runCatching {
            activity.startActivity(top)
            handler.postDelayed({
                runCatching { activity.startActivity(bottom) }
                handler.postDelayed({ activity.finish() }, 250L)
            }, 250L)
            null
        }.getOrElse { "Could not start split screen" }
    }
}
