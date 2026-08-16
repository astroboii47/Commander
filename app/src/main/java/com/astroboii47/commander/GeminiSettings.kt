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

object GeminiSettings {
    private const val PREFS = "gemini_settings"
    private const val API_KEY = "api_key"
    private const val KEY_ALIAS = "minimal_command_gemini"
    const val MODEL_LABEL = "automatic Flash model"

    fun apiKey(context: Context): String = runCatching {
        val encoded = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(API_KEY, null) ?: return ""
        val bytes = Base64.decode(encoded, Base64.NO_WRAP)
        val ivLength = bytes.first().toInt()
        val iv = bytes.copyOfRange(1, 1 + ivLength)
        val encrypted = bytes.copyOfRange(1 + ivLength, bytes.size)
        Cipher.getInstance("AES/GCM/NoPadding").run {
            init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, iv))
            String(doFinal(encrypted), Charsets.UTF_8)
        }
    }.getOrDefault("")

    fun setApiKey(context: Context, value: String) {
        val clean = value.trim()
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (clean.isEmpty()) {
            prefs.edit().remove(API_KEY).apply()
            return
        }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.ENCRYPT_MODE, key())
        }
        val packed = byteArrayOf(cipher.iv.size.toByte()) + cipher.iv +
            cipher.doFinal(clean.toByteArray(Charsets.UTF_8))
        prefs.edit().putString(API_KEY, Base64.encodeToString(packed, Base64.NO_WRAP)).apply()
    }

    private fun key(): SecretKey {
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
