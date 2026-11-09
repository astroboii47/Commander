package com.astroboii47.commander

import android.content.Context
import androidx.compose.runtime.mutableStateOf

object TriggerSettings {
    private const val PREFS = "trigger_settings"
    private const val KEY_SWAP_DOT_BANG = "swap_dot_bang"
    val swapDotBang = mutableStateOf(false)
    private val overrides = mutableMapOf<CommandKind, List<String>>()

    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        swapDotBang.value = prefs.getBoolean(KEY_SWAP_DOT_BANG, false)
        overrides.clear()
        CommandKind.entries.filter { it != CommandKind.Apps }.forEach { kind ->
            prefs.getString("trigger_${kind.name}", null)?.let(::parseList)?.takeIf(List<String>::isNotEmpty)?.let { overrides[kind] = it }
        }
    }

    fun save(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_SWAP_DOT_BANG, enabled).apply()
        swapDotBang.value = enabled
    }

    fun noteSymbol(): Char = if (swapDotBang.value) '.' else '!'
    private fun defaultTrigger(kind: CommandKind): String = when (kind) {
        CommandKind.Apps -> ""
        CommandKind.Note -> noteSymbol().toString()
        CommandKind.Files -> FileSearchSettings.trigger.value.toString()
        else -> kind.symbol?.toString().orEmpty()
    }

    /** A backslash escapes a list separator, so `calc, \,` includes the comma trigger. */
    fun parseInput(value: String): List<String> {
        val items = mutableListOf<String>()
        val current = StringBuilder()
        var escaped = false
        value.forEach { char ->
            when {
                escaped -> {
                    current.append(char)
                    escaped = false
                }
                char == '\\' -> escaped = true
                char == ',' -> {
                    items += current.toString()
                    current.clear()
                }
                else -> current.append(char)
            }
        }
        items += current.toString()
        return items.map(String::trim).filter(String::isNotBlank).map { it.take(24) }
            .distinctBy(String::lowercase).take(8)
    }

    private fun parseList(value: String): List<String> = parseInput(value)

    fun editableTriggers(kind: CommandKind): String = triggers(kind).joinToString(", ") {
        it.replace("\\", "\\\\").replace(",", "\\,")
    }

    fun triggers(kind: CommandKind): List<String> = overrides[kind] ?: listOf(defaultTrigger(kind)).filter(String::isNotEmpty)
    fun trigger(kind: CommandKind): String = triggers(kind).firstOrNull().orEmpty()

    fun matchingTrigger(input: String): Pair<CommandKind, String>? = CommandKind.entries.asSequence()
        .filter { it != CommandKind.Apps }
        .flatMap { kind -> triggers(kind).asSequence().map { kind to it } }
        .sortedByDescending { (_, prefix) -> prefix.length }
        .firstOrNull { (_, prefix) ->
            input.startsWith(prefix, ignoreCase = prefix.first().isLetter()) &&
                (!prefix.first().isLetter() || input.length == prefix.length || input[prefix.length].isWhitespace())
        }

    fun highlightedPrefixLength(input: String): Int {
        val (kind, trigger) = matchingTrigger(input) ?: return 0
        return if (kind == CommandKind.Ask && trigger == "?" && input.startsWith("??")) 2 else trigger.length
    }

    fun matchingSuffixTrigger(input: String): Pair<CommandKind, String>? {
        val end = input.trimEnd()
        return CommandKind.entries.asSequence()
            .filter { it != CommandKind.Apps }
            .flatMap { kind -> triggers(kind).asSequence().map { kind to it } }
            .filter { (_, trigger) -> trigger.isNotBlank() }
            .sortedByDescending { (_, trigger) -> trigger.length }
            .firstOrNull { (_, trigger) ->
                val start = end.length - trigger.length
                start > 0 && end[start - 1].isWhitespace() &&
                    end.regionMatches(start, trigger, 0, trigger.length, ignoreCase = trigger.first().isLetter()) &&
                    end.substring(0, start).isNotBlank()
            }
    }

    /**
     * Finds a custom word trigger used as an inline command marker. This lets
     * users append natural-language modifiers after a suffix-style trigger,
     * for example `Buy milk todo 5pm tomorrow`, without treating punctuation
     * inside ordinary queries (URLs, maths, dates) as a command.
     */
    fun matchingInlineWordTrigger(input: String): Triple<CommandKind, String, Int>? =
        matchInlineWordTrigger(
            input,
            CommandKind.entries.asSequence()
                .filter { it != CommandKind.Apps }
                .flatMap { kind -> triggers(kind).asSequence().map { kind to it } }
                .toList(),
        )

    internal fun matchInlineWordTrigger(
        input: String,
        candidates: List<Pair<CommandKind, String>>,
    ): Triple<CommandKind, String, Int>? = candidates.asSequence()
        .filter { (_, trigger) -> trigger.isNotBlank() && trigger.all(Char::isLetterOrDigit) }
        .sortedByDescending { (_, trigger) -> trigger.length }
        .mapNotNull { (kind, trigger) ->
            var from = 1
            while (from < input.lastIndex) {
                val start = input.indexOf(trigger, startIndex = from, ignoreCase = true)
                if (start < 0) break
                val end = start + trigger.length
                if (start > 0 && end < input.length &&
                    input[start - 1].isWhitespace() && input[end].isWhitespace() &&
                    input.substring(0, start).isNotBlank() && input.substring(end).isNotBlank()
                ) return@mapNotNull Triple(kind, trigger, start)
                from = start + 1
            }
            null
        }
        .firstOrNull()

    fun saveTriggers(context: Context, values: Map<CommandKind, String>) {
        val editor = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
        values.forEach { (kind, raw) ->
            if (kind == CommandKind.Apps) return@forEach
            val values = parseList(raw)
            if (values.any { value -> CommandKind.entries.any { other -> other != kind && triggers(other).any { it.equals(value, true) } } }) return@forEach
            if (values.isEmpty()) editor.remove("trigger_${kind.name}")
            else editor.putString("trigger_${kind.name}", values.joinToString(", ") {
                it.replace("\\", "\\\\").replace(",", "\\,")
            })
        }
        editor.apply()
        initialize(context)
    }

    fun isSingleCharTrigger(char: Char): Boolean = CommandKind.entries.any { kind ->
        triggers(kind).any { it.length == 1 && it[0] == char }
    }
    fun effectiveAlias(saved: String): String =
        if (swapDotBang.value && saved.startsWith('.')) "!${saved.drop(1)}" else saved
}
