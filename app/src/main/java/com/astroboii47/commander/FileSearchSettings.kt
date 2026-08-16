package com.astroboii47.commander

import android.content.Context
import androidx.compose.runtime.mutableStateOf

object FileSearchSettings {
    private const val PREFS = "file_search_settings"
    private const val KEY_TRIGGER = "trigger"
    private const val KEY_FOLDERS = "show_folders"
    private const val KEY_FILES_ONLY = "files_only_prefix"
    private const val KEY_FOLDERS_ONLY = "folders_only_prefix"

    val trigger = mutableStateOf(' ')
    val showFolders = mutableStateOf(false)
    val filesOnlyPrefix = mutableStateOf("f")
    val foldersOnlyPrefix = mutableStateOf("fo")

    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        trigger.value = prefs.getString(KEY_TRIGGER, " ")?.firstOrNull() ?: ' '
        showFolders.value = prefs.getBoolean(KEY_FOLDERS, false)
        filesOnlyPrefix.value = prefs.getString(KEY_FILES_ONLY, "f").orEmpty().ifBlank { "f" }
        foldersOnlyPrefix.value = prefs.getString(KEY_FOLDERS_ONLY, "fo").orEmpty().ifBlank { "fo" }
    }

    fun save(context: Context, triggerText: String, folders: Boolean, filesPrefix: String, foldersPrefix: String) {
        val normalized = when (triggerText.trim().lowercase()) {
            "", "space", "spacebar", "␠" -> ' '
            else -> triggerText.trim().first()
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_TRIGGER, normalized.toString())
            .putBoolean(KEY_FOLDERS, folders)
            .putString(KEY_FILES_ONLY, normalizePrefix(filesPrefix, "f"))
            .putString(KEY_FOLDERS_ONLY, normalizePrefix(foldersPrefix, "fo"))
            .apply()
        trigger.value = normalized
        showFolders.value = folders
        filesOnlyPrefix.value = normalizePrefix(filesPrefix, "f")
        foldersOnlyPrefix.value = normalizePrefix(foldersPrefix, "fo")
    }

    fun displayTrigger(): String = if (trigger.value == ' ') "space" else trigger.value.toString()

    fun parseFilter(text: String): FileSearchQuery {
        val split = text.indexOf(' ')
        if (split <= 0) return FileSearchQuery(text, FileSearchMode.Default)
        val prefix = text.substring(0, split).trim().lowercase()
        val query = text.substring(split + 1).trim()
        return when (prefix) {
            filesOnlyPrefix.value -> FileSearchQuery(query, FileSearchMode.FilesOnly)
            foldersOnlyPrefix.value -> FileSearchQuery(query, FileSearchMode.FoldersOnly)
            else -> FileSearchQuery(text, FileSearchMode.Default)
        }
    }

    private fun normalizePrefix(value: String, fallback: String): String = value.trim().lowercase()
        .filter { it.isLetterOrDigit() || it == '_' || it == '-' }
        .take(8).ifBlank { fallback }
}

enum class FileSearchMode { Default, FilesOnly, FoldersOnly }
data class FileSearchQuery(val query: String, val mode: FileSearchMode)
