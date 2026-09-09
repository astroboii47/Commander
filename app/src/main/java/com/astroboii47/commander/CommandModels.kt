package com.astroboii47.commander

import android.graphics.drawable.Drawable

enum class CommandKind(val symbol: Char?, val label: String) {
    Apps(null, "app search"),
    Files(' ', "file search"),
    Message('@', "message"),
    Call('#', "call"),
    Todo('-', "to-do"),
    Note('!', "note"),
    Event('*', "event"),
    Timer('+', "timer"),
    Calculator(',', "calculator"),
    Web('/', "web search"),
    Ask('?', "ask"),
}

data class AppResult(
    val label: String,
    val packageName: String,
    val icon: Drawable?,
    val adaptiveColor: Int? = null,
    val control: ControlResult? = null,
    val shortcut: AppShortcutResult? = null,
)

data class ContactResult(
    val name: String,
    val number: String,
    val label: String,
    val messengerShortcutId: String? = null,
    val avatar: android.graphics.Bitmap? = null,
    val hubNotificationKey: String? = null,
    val commanderMessagingContactId: String? = null,
    val messagingIdentity: String? = null,
    val messagingPlatform: String? = null,
    val messagingPackageName: String? = null,
    val messagingPlatformScope: String? = null,
    val messagingPlatformIcon: android.graphics.Bitmap? = null,
    val messagingIsDefault: Boolean = false,
)

data class CommanderMessageDelivery(
    val error: String? = null,
    val sent: Boolean = false,
    val status: String,
)

data class FileResult(
    val name: String,
    val uri: android.net.Uri,
    val mimeType: String?,
    val location: String,
    val thumbnail: android.graphics.Bitmap? = null,
    val isDirectory: Boolean = false,
    val modifiedAtMillis: Long = 0L,
)

enum class FileSortMode(val label: String) {
    Relevance("relevance"), Newest("newest"), Oldest("oldest");
    fun next() = entries[(ordinal + 1) % entries.size]
}

data class ParsedCommand(
    val kind: CommandKind,
    val text: String,
)

fun parseCommand(input: String): ParsedCommand {
    val first = input.firstOrNull()
    val kind = when {
        first == TriggerSettings.noteSymbol() -> CommandKind.Note
        first == FileSearchSettings.trigger.value -> CommandKind.Files
        else -> CommandKind.entries.firstOrNull { it != CommandKind.Note && it != CommandKind.Files && it.symbol == first } ?: CommandKind.Apps
    }
    val text = when (kind) {
        CommandKind.Apps -> input.trim()
        CommandKind.Files -> input.drop(1).trimStart()
        else -> input.drop(1).trim()
    }
    return ParsedCommand(kind, text)
}

fun commandSymbol(kind: CommandKind): Char? = when (kind) {
    CommandKind.Note -> TriggerSettings.noteSymbol()
    CommandKind.Files -> FileSearchSettings.trigger.value
    else -> kind.symbol
}
