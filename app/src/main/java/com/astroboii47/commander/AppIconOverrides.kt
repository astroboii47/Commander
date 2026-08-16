package com.astroboii47.commander

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.util.concurrent.ConcurrentHashMap

object AppIconOverrides {
    private const val PREFS = "app_icon_overrides"
    private val cache = ConcurrentHashMap<String, Drawable>()

    fun load(context: Context, packageName: String): Drawable? {
        cache[packageName]?.let { return it }
        val encoded = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(packageName, null) ?: return null
        return runCatching {
            val bytes = Base64.decode(encoded, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }.getOrNull()?.let { bitmap ->
            BitmapDrawable(context.resources, bitmap).also { cache[packageName] = it }
        }
    }

    fun has(context: Context, packageName: String): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).contains(packageName)

    fun save(context: Context, packageName: String, source: android.net.Uri): Boolean = runCatching {
        val decoded = context.contentResolver.openInputStream(source)?.use(BitmapFactory::decodeStream)
            ?: return false
        val size = 160
        val scale = minOf(size.toFloat() / decoded.width, size.toFloat() / decoded.height, 1f)
        val width = (decoded.width * scale).toInt().coerceAtLeast(1)
        val height = (decoded.height * scale).toInt().coerceAtLeast(1)
        val bitmap = if (width == decoded.width && height == decoded.height) decoded
            else Bitmap.createScaledBitmap(decoded, width, height, true)
        val encoded = ByteArrayOutputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
            Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(packageName, encoded).apply()
        cache[packageName] = BitmapDrawable(context.resources, bitmap)
        true
    }.getOrDefault(false)

    fun clear(context: Context, packageName: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(packageName).apply()
        cache.remove(packageName)
    }
}
