package com.astroboii47.commander

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.compose.runtime.mutableStateOf
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import org.json.JSONArray
import org.json.JSONObject

data class HubHistoryEntry(
    val id: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val time: Long,
    val archivedAt: Long,
    val category: String?,
)

object HubHistoryStore {
    private const val KEY_ALIAS = "commander_hub_history_v1"
    private const val FILE_NAME = "hub_history.enc"
    private const val MAX_ENTRIES = 5_000
    val entries = mutableStateOf<List<HubHistoryEntry>>(emptyList())

    fun initialize(context: Context) {
        entries.value = read(context).let(::applyRetention)
        write(context, entries.value)
    }

    fun archive(context: Context, item: HubItem) {
        if (HubSettings.historyRetention.value == HubHistoryRetention.Off || item.isSummary) return
        val title = (item.conversationTitle ?: item.title).trim()
        val body = item.expandedText.ifBlank { item.textLines.joinToString("\n") }.ifBlank { item.text }.trim()
        if (title.isBlank() && body.isBlank()) return
        val entry = HubHistoryEntry(
            id = "${item.key}:${System.currentTimeMillis()}",
            packageName = item.packageName,
            appName = item.appName,
            title = title,
            text = body,
            time = item.time,
            archivedAt = System.currentTimeMillis(),
            category = item.category,
        )
        entries.value = applyRetention((listOf(entry) + entries.value).distinctBy { it.packageName to (it.title to (it.text to it.time)) }.take(MAX_ENTRIES))
        write(context, entries.value)
    }

    fun prune(context: Context) {
        entries.value = if (HubSettings.historyRetention.value == HubHistoryRetention.Off) emptyList() else applyRetention(read(context))
        write(context, entries.value)
    }

    fun clear(context: Context) {
        entries.value = emptyList()
        runCatching { File(context.filesDir, FILE_NAME).delete() }
    }

    private fun applyRetention(source: List<HubHistoryEntry>): List<HubHistoryEntry> {
        val retention = HubSettings.historyRetention.value
        if (retention == HubHistoryRetention.Off) return emptyList()
        val cutoff = retention.durationMillis?.let { System.currentTimeMillis() - it }
        return source.filter { cutoff == null || it.archivedAt >= cutoff }.sortedByDescending(HubHistoryEntry::archivedAt).take(MAX_ENTRIES)
    }

    private fun read(context: Context): List<HubHistoryEntry> = runCatching {
        val bytes = File(context.filesDir, FILE_NAME).takeIf(File::exists)?.readBytes() ?: return emptyList()
        if (bytes.size < 13) return emptyList()
        val iv = bytes.copyOfRange(0, 12)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, historyKey(), GCMParameterSpec(128, iv))
        val array = JSONArray(String(cipher.doFinal(bytes.copyOfRange(12, bytes.size)), Charsets.UTF_8))
        List(array.length()) { index ->
            array.getJSONObject(index).let { item ->
                HubHistoryEntry(
                    item.getString("id"), item.getString("package"), item.getString("app"),
                    item.optString("title"), item.optString("text"), item.optLong("time"),
                    item.optLong("archivedAt"), item.optString("category").takeIf(String::isNotBlank),
                )
            }
        }
    }.getOrDefault(emptyList())

    private fun write(context: Context, source: List<HubHistoryEntry>) {
        if (source.isEmpty()) {
            File(context.filesDir, FILE_NAME).delete()
            return
        }
        runCatching {
            val array = JSONArray().apply {
                source.forEach { item ->
                    put(JSONObject().put("id", item.id).put("package", item.packageName).put("app", item.appName)
                        .put("title", item.title).put("text", item.text).put("time", item.time)
                        .put("archivedAt", item.archivedAt).put("category", item.category.orEmpty()))
                }
            }
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, historyKey())
            val output = cipher.iv + cipher.doFinal(array.toString().toByteArray(Charsets.UTF_8))
            val target = File(context.filesDir, FILE_NAME)
            val temporary = File(context.filesDir, "$FILE_NAME.tmp")
            temporary.writeBytes(output)
            if (!temporary.renameTo(target)) {
                target.writeBytes(output)
                temporary.delete()
            }
        }
    }

    private fun historyKey(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build(),
        )
        return generator.generateKey()
    }
}
