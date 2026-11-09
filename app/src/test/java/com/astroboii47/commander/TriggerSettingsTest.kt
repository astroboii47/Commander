package com.astroboii47.commander

import org.junit.Assert.assertEquals
import org.junit.Test

class TriggerSettingsTest {
    @Test fun chatGptDirectTriggerHighlightsBothQuestionMarks() {
        assertEquals(2, TriggerSettings.highlightedPrefixLength("?? What is this?"))
        assertEquals(1, TriggerSettings.highlightedPrefixLength("? What is this?"))
    }

    @Test fun inlineWordTriggerKeepsNaturalLanguageAfterMarker() {
        val match = TriggerSettings.matchInlineWordTrigger(
            "test query todo 5pm tomorrow",
            listOf(CommandKind.Todo to "todo"),
        )
        assertEquals(Triple(CommandKind.Todo, "todo", 11), match)
    }

    @Test fun inlineMatchingDoesNotTreatSymbolicTriggerAsQueryPunctuation() {
        val match = TriggerSettings.matchInlineWordTrigger(
            "calculate 10 / 2",
            listOf(CommandKind.Web to "/"),
        )
        assertEquals(null, match)
    }

    @Test fun escapedCommaCanBeUsedAsACalculatorTrigger() {
        assertEquals(listOf("calc", ","), TriggerSettings.parseInput("calc, \\,"))
    }

    @Test fun prefixTriggerRemovesOnlyTheCommandMarker() {
        assertEquals(
            ParsedCommand(CommandKind.Todo, "buy milk tomorrow at 5pm"),
            parseCommand("- buy milk tomorrow at 5pm"),
        )
    }

    @Test fun suffixTriggerKeepsTheNaturalLanguageBeforeIt() {
        assertEquals(
            ParsedCommand(CommandKind.Todo, "buy milk tomorrow at 5pm"),
            parseCommand("buy milk tomorrow at 5pm -"),
        )
    }
}
