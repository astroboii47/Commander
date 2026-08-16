package com.astroboii47.commander

object TodoistNaturalLanguage {
    /** Standalone Hmm/HHmm values, such as 145, 0745, or 2140. */
    val compactTimePattern = Regex("(?<!\\d)(?:(?:[01]\\d|2[0-3])[0-5]\\d|[0-9][0-5]\\d)(?!\\d)")
    val compactTimeRecognitionPattern = Regex(
        "(?i)(?<!\\d)(?:(?:[01]\\d|2[0-3])[0-5]\\d|[0-9][0-5]\\d)(?: ?(?:am|pm))?(?![\\p{L}\\d])",
    )

    fun expandCompactTimes(text: String): String = compactTimePattern.replace(text) { match ->
        val splitAt = match.value.length - 2
        match.value.substring(0, splitAt) + ":" + match.value.substring(splitAt)
    }
}
