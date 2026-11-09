package com.astroboii47.commander

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

internal object WebSearchLauncher {
    private val privateBrowsers = listOf(
        "org.mozilla.focus",
        "org.mozilla.klar",
        "com.duckduckgo.mobile.android",
    )

    fun urlFor(text: String): Uri {
        val target = if (Regex("(?i)^https?://").containsMatchIn(text.trim())) text.trim() else {
            "https://www.google.com/search?q=" +
                URLEncoder.encode(text.trim(), StandardCharsets.UTF_8.toString())
        }
        return Uri.parse(target)
    }

    fun openQuick(activity: Activity, text: String): String? = runCatching {
        CustomTabsIntent.Builder()
            .setShowTitle(true)
            .setUrlBarHidingEnabled(true)
            .build()
            .launchUrl(activity, urlFor(text))
        finishOverlay(activity)
        null
    }.getOrElse { "No browser is installed" }

    fun openNormally(activity: Activity, text: String): String? = startAndFinish(
        activity,
        Intent(Intent.ACTION_VIEW, urlFor(text)),
        "No browser is installed",
    )

    fun openPrivately(activity: Activity, text: String): String? {
        val packageManager = activity.packageManager
        val browser = privateBrowsers.firstOrNull { packageManager.getLaunchIntentForPackage(it) != null }
            ?: return "Install Firefox Focus or DuckDuckGo to use private quick search"
        return startAndFinish(
            activity,
            Intent(Intent.ACTION_VIEW, urlFor(text)).setPackage(browser),
            "Private browser is unavailable",
        )
    }

    private fun startAndFinish(activity: Activity, intent: Intent, unavailable: String): String? {
        if (intent.resolveActivity(activity.packageManager) == null) return unavailable
        return runCatching {
            activity.startActivity(intent, ActivityOptions.makeCustomAnimation(activity, 0, 0).toBundle())
            finishOverlay(activity)
            null
        }.getOrElse { unavailable }
    }

    private fun finishOverlay(activity: Activity) {
        activity.finish()
        @Suppress("DEPRECATION")
        activity.overridePendingTransition(0, 0)
    }
}
