package com.astroboii47.commander

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlin.concurrent.thread

object RootHomeTyping {
    private const val PREFS = "home_typing"
    private const val KEY_ROOT_KEEP_ENABLED = "root_keep_enabled"
    private const val COMPONENT =
        "com.astroboii47.commander/com.astroboii47.commander.HomeTypingAccessibilityService"

    fun enabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_ROOT_KEEP_ENABLED, false)

    fun setEnabled(context: Context, value: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_ROOT_KEEP_ENABLED, value).apply()
    }

    fun ensureEnabledAsync(context: Context, result: ((Boolean) -> Unit)? = null) {
        if (!enabled(context)) return
        thread(name = "root-home-typing", isDaemon = true) {
            val command = """
                current=${'$'}(settings get secure enabled_accessibility_services)
                component='$COMPONENT'
                case ":${'$'}current:" in
                  *":${'$'}component:"*) ;;
                  *)
                    if [ -z "${'$'}current" ] || [ "${'$'}current" = "null" ]; then
                      current="${'$'}component"
                    else
                      current="${'$'}current:${'$'}component"
                    fi
                    settings put secure enabled_accessibility_services "${'$'}current"
                    ;;
                esac
                settings put secure accessibility_enabled 1
            """.trimIndent()
            val success = runCatching {
                ProcessBuilder("su", "-c", command).redirectErrorStream(true).start().let { process ->
                    process.inputStream.bufferedReader().use { it.readText() }
                    process.waitFor() == 0
                }
            }.getOrDefault(false)
            result?.invoke(success)
        }
    }
}

class RootHomeTypingBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED ||
            intent?.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            RootHomeTyping.ensureEnabledAsync(context.applicationContext)
        }
    }
}
