package com.astroboii47.commander

object TodoistNaturalLanguage {
    private const val WEEKDAY = "(?:mon(?:day)?|tue(?:s|sday)?|wed(?:s|nesday)?|thu(?:r|rs|rsday)?|fri(?:day)?|sat(?:urday)?|sun(?:day)?)"
    private const val MONTH = "(?:jan(?:uary)?|feb(?:ruary)?|mar(?:ch)?|apr(?:il)?|may|jun(?:e)?|jul(?:y)?|aug(?:ust)?|sep(?:tember)?|oct(?:ober)?|nov(?:ember)?|dec(?:ember)?)"
    private val dayFirstDatePattern = Regex("(?i)\\b(\\d{1,2})(?:st|nd|rd|th)?\\s+($MONTH)\\b")
    val dueDatePattern = Regex(
        "(?i)\\b(?:tod|today|tom|tomorrow|tonight|this (?:morning|afternoon|evening)|next (?:week|month|year)|every (?:day|weekday|week|month|year|$WEEKDAY)|(?:next|this) $WEEKDAY|$WEEKDAY|in \\d+ (?:days?|weeks?|months?|years?)|$MONTH \\d{1,2}(?:st|nd|rd|th)?|\\d{1,2}(?:st|nd|rd|th)? $MONTH)\\b",
    )
    private val dueTimePattern = Regex(
        "(?i)\\b(?:at )?(?:[01]?\\d|2[0-3])(?::[0-5]\\d)? ?(?:am|pm)\\b",
    )
    private val colonTimePattern = Regex("(?i)\\b(?:at )?([01]?\\d|2[0-3]):([0-5]\\d)\\b")
    private val hashtagPattern = Regex("(?<![\\p{L}\\p{N}_])#[\\p{L}\\p{N}_-]+")
    /** Standalone Hmm/HHmm values, such as 145, 0745, or 2140. */
    val compactTimePattern = Regex("(?<!\\d)(?:(?:[01]\\d|2[0-3])[0-5]\\d|[0-9][0-5]\\d)(?!\\d)")
    val compactTimeRecognitionPattern = Regex(
        "(?i)(?<!\\d)(?:(?:[01]\\d|2[0-3])[0-5]\\d|[0-9][0-5]\\d)(?: ?(?:am|pm))?(?![\\p{L}\\d])",
    )

    fun expandCompactTimes(text: String): String = compactTimePattern.replace(normalizeDayFirstDates(text)) { match ->
        val splitAt = match.value.length - 2
        match.value.substring(0, splitAt) + ":" + match.value.substring(splitAt)
    }

    /** Todoist's Quick Add documentation uses month-first English dates; keep day-first typing natural in Commander. */
    fun normalizeDayFirstDates(text: String): String = dayFirstDatePattern.replace(text) { match ->
        "${match.groupValues[2]} ${match.groupValues[1]}"
    }

    fun detectedDuePreview(text: String): String? {
        val date = dueDatePattern.find(text)?.value?.trim()?.let(::formatDetectedDate)
        val regularTime = dueTimePattern.find(text)?.value?.trim()?.removePrefix("at ")?.removePrefix("At ")
        val compactTime = compactTimeRecognitionPattern.find(text)?.value?.trim()?.let(::formatCompactTime)
        val colonTime = colonTimePattern.find(text)?.value?.trim()?.removePrefix("at ")?.removePrefix("At ")
        val time = regularTime?.let(::formatMeridiem) ?: compactTime ?: colonTime?.let(::formatInferredTime)
        val hashtags = hashtagPattern.findAll(text).map { it.value }.distinct().toList()
            .takeIf(List<String>::isNotEmpty)?.joinToString(" ")
        return listOfNotNull(date, time, hashtags).distinct().takeIf { it.isNotEmpty() }?.joinToString(" · ")
    }

    private fun formatDetectedDate(value: String): String {
        val expansions = mapOf(
            "tod" to "today",
            "tom" to "tomorrow",
            "mon" to "monday",
            "tue" to "tuesday",
            "tues" to "tuesday",
            "wed" to "wednesday",
            "weds" to "wednesday",
            "thu" to "thursday",
            "thur" to "thursday",
            "thurs" to "thursday",
            "fri" to "friday",
            "sat" to "saturday",
            "sun" to "sunday",
            "jan" to "january",
            "feb" to "february",
            "mar" to "march",
            "apr" to "april",
            "jun" to "june",
            "jul" to "july",
            "aug" to "august",
            "sep" to "september",
            "oct" to "october",
            "nov" to "november",
            "dec" to "december",
        )
        val expanded = Regex("[A-Za-z]+").replace(value) { match ->
            expansions[match.value.lowercase()] ?: match.value.lowercase()
        }
        if (dayFirstDatePattern.matches(value)) {
            val parts = expanded.split(' ', limit = 2)
            return "${parts[0]} ${parts[1].replaceFirstChar { it.uppercase() }}"
        }
        return expanded.replaceFirstChar { it.uppercase() }
    }

    private fun formatCompactTime(value: String): String {
        val match = Regex("(?i)^(\\d{3,4}) ?(am|pm)?$").matchEntire(value) ?: return value
        val digits = match.groupValues[1]
        val withColon = digits.dropLast(2).trimStart('0').ifEmpty { "0" } + ":" + digits.takeLast(2)
        val suffix = match.groupValues[2]
        return if (suffix.isBlank()) formatInferredTime(withColon) else "$withColon ${suffix.uppercase()}"
    }

    private fun formatInferredTime(value: String): String {
        val parts = value.split(':')
        val hour = parts.firstOrNull()?.toIntOrNull() ?: return value
        val minute = parts.getOrNull(1) ?: "00"
        val suffix = when {
            hour >= 12 -> "PM"
            hour in 1..6 -> "PM"
            else -> "AM"
        }
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        return "$displayHour:$minute $suffix"
    }

    private fun formatMeridiem(value: String): String = value.replace(
        Regex("(?i) ?(am|pm)$"),
    ) { " ${it.value.trim().uppercase()}" }
}
