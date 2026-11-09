package com.astroboii47.commander

import android.content.Context
import androidx.compose.ui.graphics.vector.ImageVector
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.*
import dalvik.system.DexFile
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class PhosphorCatalogEntry(
    val key: String,
    val label: String,
    internal val className: String,
)

/**
 * Discovers the Phosphor Duotone vectors already packaged with Commander.
 * Names are cheap to enumerate; ImageVectors are reflected and cached only
 * when their cells become visible in the lazy picker grid.
 */
object PhosphorIconCatalog {
    private const val Prefix = "com.adamglin.phosphoricons.duotone."
    private const val KeyPrefix = "phosphor:duotone:"
    @Volatile private var cachedEntries: List<PhosphorCatalogEntry>? = null
    private val vectors = ConcurrentHashMap<String, ImageVector>()

    suspend fun entries(context: Context): List<PhosphorCatalogEntry> = withContext(Dispatchers.IO) {
        cachedEntries?.let { return@withContext it }
        kotlin.synchronized(this@PhosphorIconCatalog) {
            cachedEntries ?: run {
                val dex = DexFile(context.applicationInfo.sourceDir)
                try {
                    dex.entries().asSequence()
                    .filter { it.startsWith(Prefix) && it.endsWith("Kt") && '$' !in it }
                    .map { className ->
                        val raw = className.substringAfterLast('.').removeSuffix("Kt")
                        PhosphorCatalogEntry(
                            key = "$KeyPrefix$className",
                            label = raw.toReadableIconName(),
                            className = className,
                        )
                    }
                    .sortedBy { it.label.lowercase(Locale.ROOT) }
                    .toList()
                    .also { cachedEntries = it }
                } finally {
                    dex.close()
                }
            }
        }
    }

    fun resolve(key: String): ImageVector? {
        if (!key.startsWith(KeyPrefix)) return null
        return vectors[key] ?: runCatching {
            val className = key.removePrefix(KeyPrefix)
            val holder = Class.forName(className)
            val getter = holder.methods.first { method ->
                method.parameterCount == 1 &&
                    method.returnType.name == ImageVector::class.java.name
            }
            (getter.invoke(null, PhosphorIcons.Duotone) as ImageVector)
                .also { vectors[key] = it }
        }.getOrNull()
    }

    fun isPhosphorKey(key: String): Boolean = key.startsWith(KeyPrefix)
}

private fun String.toReadableIconName(): String =
    replace(Regex("([a-z0-9])([A-Z])"), "$1 $2")
        .replace('_', ' ')
        .replace('-', ' ')
        .trim()
