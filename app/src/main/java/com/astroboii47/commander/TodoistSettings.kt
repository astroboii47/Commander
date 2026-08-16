package com.astroboii47.commander

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object TodoistSettings {
    private const val PREFS = "todoist_settings"
    private const val DIRECT = "direct_add"
    private const val TOKEN = "token"
    private const val KEY_ALIAS = "minimal_command_todoist"

    fun directEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(DIRECT, false)

    fun setDirectEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(DIRECT, enabled).apply()
    }

    fun token(context: Context): String = runCatching {
        val encoded = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(TOKEN, null)
            ?: return ""
        val bytes = Base64.decode(encoded, Base64.NO_WRAP)
        val ivLength = bytes.first().toInt()
        val iv = bytes.copyOfRange(1, 1 + ivLength)
        val encrypted = bytes.copyOfRange(1 + ivLength, bytes.size)
        Cipher.getInstance("AES/GCM/NoPadding").run {
            init(Cipher.DECRYPT_MODE, encryptionKey(), GCMParameterSpec(128, iv))
            String(doFinal(encrypted), Charsets.UTF_8)
        }
    }.getOrDefault("")

    fun setToken(context: Context, token: String) {
        val clean = token.trim()
        if (clean.isEmpty()) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(TOKEN).apply()
            return
        }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.ENCRYPT_MODE, encryptionKey())
        }
        val encrypted = cipher.doFinal(clean.toByteArray(Charsets.UTF_8))
        val packed = byteArrayOf(cipher.iv.size.toByte()) + cipher.iv + encrypted
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(TOKEN, Base64.encodeToString(packed, Base64.NO_WRAP))
            .apply()
    }

    private fun encryptionKey(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").run {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build(),
            )
            generateKey()
        }
    }
}
