package com.astroboii47.commander

import android.content.Context

data class SearchTarget(
    val id: String,
    val label: String,
    val packageName: String?,
    val urlTemplate: String,
)

data class AliasMatch(val target: SearchTarget, val alias: String, val query: String)

object AliasSettings {
    val targets = listOf(
        SearchTarget("play", "Play Store", "com.android.vending", "https://play.google.com/store/search?q=%s&c=apps"),
        SearchTarget("waze", "Waze", "com.waze", "https://www.waze.com/ul?q=%s"),
        SearchTarget("maps", "Google Maps", "com.google.android.apps.maps", "https://maps.google.com/?q=%s"),
        SearchTarget("youtube", "YouTube", "com.google.android.youtube", "https://www.youtube.com/results?search_query=%s"),
        SearchTarget("reddit", "Reddit", "com.reddit.frontpage", "https://www.reddit.com/search/?q=%s"),
        SearchTarget("spotify", "Spotify", "com.spotify.music", "https://open.spotify.com/search/%s"),
        SearchTarget("marketplace", "Facebook Marketplace", "com.facebook.katana", "https://www.facebook.com/marketplace/search/?query=%s"),
        SearchTarget("drive", "Google Drive", "com.google.android.apps.docs", "https://drive.google.com/drive/u/0/search?q=%s"),
        SearchTarget("photos", "Google Photos", "com.google.android.apps.photos", "https://photos.google.com/search/%s"),
        SearchTarget("ebay", "eBay Australia", "com.ebay.mobile", "https://www.ebay.com.au/sch/i.html?_nkw=%s"),
        SearchTarget("focus", "Firefox Focus", "org.mozilla.focus", "https://www.google.com/search?q=%s"),
        SearchTarget("arc", "Arc Search", "company.thebrowser.arc", "https://www.google.com/search?q=%s"),
        SearchTarget("amazon", "Amazon Australia", "com.amazon.mShop.android.shopping", "https://www.amazon.com.au/s?k=%s"),
    )

    private fun prefs(context: Context) = context.getSharedPreferences("command_aliases", Context.MODE_PRIVATE)
    fun alias(context: Context, target: SearchTarget): String = prefs(context).getString(target.id, "").orEmpty()
    fun save(context: Context, target: SearchTarget, alias: String) {
        prefs(context).edit().putString(target.id, aliases(alias).joinToString(", ")).apply()
    }
    fun match(context: Context, input: String): AliasMatch? {
        val split = input.indexOf(' ')
        if (split <= 0) return null
        val prefix = normalize(input.substring(0, split))
        if (prefix.isBlank()) return null
        val target = targets.firstOrNull { target ->
            aliases(alias(context, target)).any { TriggerSettings.effectiveAlias(it) == prefix }
        } ?: return null
        return AliasMatch(target, prefix, input.substring(split + 1).trim())
    }
    fun usesPrefix(context: Context, prefix: Char): Boolean = targets.any { target ->
        aliases(alias(context, target)).any { TriggerSettings.effectiveAlias(it).startsWith(prefix) }
    }
    private fun aliases(value: String): List<String> = value.split(',')
        .map(::normalize)
        .filter(String::isNotBlank)
        .distinct()

    private fun normalize(value: String) = value.trim().lowercase()
        .filter { it.isLetterOrDigit() || it == '_' || it == '-' || it == '.' || it == '!' }
}
