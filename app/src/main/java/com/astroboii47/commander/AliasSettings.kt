package com.astroboii47.commander

import android.content.Context

data class SearchTarget(
    val id: String,
    val label: String,
    val packageName: String?,
    val urlTemplate: String,
)

data class AliasMatch(
    val target: SearchTarget,
    val alias: String,
    val query: String,
    val previewTitle: String? = null,
    val previewSubtitle: String? = null,
    val routeOrigin: String? = null,
    val routeDestination: String? = null,
    val travelMode: String? = null,
    val isDirections: Boolean = false,
)

object AliasSettings {
    val targets = listOf(
        SearchTarget("play", "Play Store", "com.android.vending", "https://play.google.com/store/search?q=%s&c=apps"),
        SearchTarget("waze", "Waze", "com.waze", "https://www.waze.com/ul?q=%s"),
        SearchTarget("maps", "Google Maps", "com.google.android.apps.maps", "https://www.google.com/maps/search/?api=1&query=%s"),
        SearchTarget("gmail", "Gmail", "com.google.android.gm", "https://mail.google.com/mail/u/0/#search/%s"),
        SearchTarget("onepassword", "1Password", "com.onepassword.android", ""),
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
        val query = input.substring(split + 1).trim()
        return when (target.id) {
            "maps" -> mapsMatch(target, prefix, query)
            "waze" -> wazeMatch(target, prefix, query)
            "onepassword" -> AliasMatch(
                target,
                prefix,
                query,
                previewTitle = "Search 1Password",
                previewSubtitle = "opens the app if search is unavailable",
            )
            else -> AliasMatch(target, prefix, query)
        }
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

    private fun mapsMatch(target: SearchTarget, alias: String, query: String): AliasMatch {
        var remainder = query.trim()
        var travelMode: String? = null
        val modes = listOf(
            Regex("^(drive|driving)\\s+", RegexOption.IGNORE_CASE) to "driving",
            Regex("^(walk|walking)\\s+", RegexOption.IGNORE_CASE) to "walking",
            Regex("^(bike|biking|bicycle|cycle|cycling)\\s+", RegexOption.IGNORE_CASE) to "bicycling",
            Regex("^(transit|public transport|train)\\s+", RegexOption.IGNORE_CASE) to "transit",
        )
        modes.firstOrNull { it.first.containsMatchIn(remainder) }?.let { (pattern, mode) ->
            remainder = remainder.replaceFirst(pattern, "").trim()
            travelMode = mode
        }

        val fromTo = Regex("^from\\s+(.+?)\\s+to\\s+(.+)$", RegexOption.IGNORE_CASE).matchEntire(remainder)
        val destinationFrom = Regex("^(.+?)\\s+from\\s+(.+)$", RegexOption.IGNORE_CASE).matchEntire(remainder)
        val originTo = Regex("^(.+?)\\s+to\\s+(.+)$", RegexOption.IGNORE_CASE).matchEntire(remainder)
        val destinationOnly = Regex("^to\\s+(.+)$", RegexOption.IGNORE_CASE).matchEntire(remainder)
        val originOnly = Regex("^from\\s+(.+)$", RegexOption.IGNORE_CASE).matchEntire(remainder)
        val origin: String?
        val destination: String?
        when {
            fromTo != null -> {
                origin = fromTo.groupValues[1].trim()
                destination = fromTo.groupValues[2].trim()
            }
            destinationFrom != null -> {
                destination = destinationFrom.groupValues[1].trim()
                origin = destinationFrom.groupValues[2].trim()
            }
            originTo != null -> {
                origin = originTo.groupValues[1].trim()
                destination = originTo.groupValues[2].trim()
            }
            destinationOnly != null -> {
                origin = null
                destination = destinationOnly.groupValues[1].trim()
            }
            originOnly != null -> {
                origin = originOnly.groupValues[1].trim()
                destination = null
            }
            else -> {
                origin = null
                destination = null
            }
        }

        if (!origin.isNullOrBlank() || !destination.isNullOrBlank()) {
            val modeLabel = travelMode?.replaceFirstChar { it.uppercase() }
            return AliasMatch(
                target = target,
                alias = alias,
                query = query,
                previewTitle = "Directions",
                previewSubtitle = buildString {
                    append(origin ?: "Current location")
                    append(" → ")
                    append(destination ?: "Choose destination")
                    modeLabel?.let { append(" · ").append(it) }
                },
                routeOrigin = origin,
                routeDestination = destination,
                travelMode = travelMode,
                isDirections = true,
            )
        }
        val searchQuery = remainder.ifBlank { query }
        return AliasMatch(target, alias, searchQuery, previewTitle = "Search Maps", previewSubtitle = searchQuery)
    }

    private fun wazeMatch(target: SearchTarget, alias: String, query: String): AliasMatch {
        var remainder = query.trim()
        val drivePrefix = Regex("^(drive|driving)\\s+", RegexOption.IGNORE_CASE)
        val driveRequested = drivePrefix.containsMatchIn(remainder)
        remainder = remainder.replaceFirst(drivePrefix, "").trim()
        val fromTo = Regex("^from\\s+(.+?)\\s+to\\s+(.+)$", RegexOption.IGNORE_CASE).matchEntire(remainder)
        val destinationOnly = Regex("^to\\s+(.+)$", RegexOption.IGNORE_CASE).matchEntire(remainder)
        val destination = when {
            fromTo != null -> fromTo.groupValues[2].trim()
            destinationOnly != null -> destinationOnly.groupValues[1].trim()
            driveRequested -> remainder
            else -> null
        }
        return if (!destination.isNullOrBlank()) {
            AliasMatch(
                target = target,
                alias = alias,
                query = destination,
                previewTitle = "Navigate with Waze",
                previewSubtitle = buildString {
                    append("Current location → ").append(destination)
                    if (fromTo != null) append(" · Waze uses current location as the start")
                },
                routeDestination = destination,
                travelMode = "driving",
                isDirections = true,
            )
        } else {
            AliasMatch(target, alias, remainder, previewTitle = "Search Waze", previewSubtitle = remainder)
        }
    }
}
