package com.astroboii47.commander

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TodoistNaturalLanguageTest {
    @Test fun dayFirstDateIsRecognisedAndNormalisedForQuickAdd() {
        val input = "Book dentist 7th Nov"
        assertEquals("7th Nov", TodoistNaturalLanguage.dueDatePattern.find(input)?.value)
        assertEquals("7th November", TodoistNaturalLanguage.detectedDuePreview(input))
        assertEquals("Book dentist Nov 7", TodoistNaturalLanguage.expandCompactTimes(input))
    }

    @Test fun monthFirstAndPlainDayFirstDatesBothRemainSupported() {
        assertEquals("Nov 7", TodoistNaturalLanguage.dueDatePattern.find("Book dentist Nov 7")?.value)
        assertEquals("7 Nov", TodoistNaturalLanguage.dueDatePattern.find("Book dentist 7 Nov")?.value)
        assertEquals("Book dentist Nov 7", TodoistNaturalLanguage.expandCompactTimes("Book dentist 7 Nov"))
        assertTrue(TodoistNaturalLanguage.detectedDuePreview("Book dentist tomorrow")!!.contains("Tomorrow"))
    }
}
