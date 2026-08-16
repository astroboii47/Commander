package com.astroboii47.commander

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream
import android.app.PendingIntent
import android.util.Log
import android.app.ActivityOptions
import android.os.Build

object MessengerSettings {
    private const val PREFS = "messenger_settings"
    private const val PHOTOS = "profile_photos"
    fun profilePhotos(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(PHOTOS, false)
    fun setProfilePhotos(context: Context, enabled: Boolean) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(PHOTOS, enabled).apply()
}

object MessengerChatStore {
    private const val PACKAGE = "com.facebook.orca"
    private const val PREFS = "messenger_chats"
    private val liveContentIntents = mutableMapOf<String, PendingIntent>()

    fun discover(context: Context, shortcutId: String?, label: String?, avatar: Bitmap? = null) {
        val id = shortcutId?.trim().orEmpty()
        val name = label?.trim().orEmpty()
        if (id.isBlank() || name.isBlank()) return
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(id, name).apply()
        if (MessengerSettings.profilePhotos(context) && avatar != null) runCatching {
            FileOutputStream(avatarFile(context, id)).use { avatar.compress(Bitmap.CompressFormat.PNG, 90, it) }
        }
    }

    fun attachLiveIntent(shortcutId: String?, contentIntent: PendingIntent?) {
        val id = shortcutId?.takeIf(String::isNotBlank) ?: return
        if (contentIntent != null) liveContentIntents[id] = contentIntent
    }

    fun search(context: Context, query: String, limit: Int): List<ContactResult> {
        val needle = query.trim()
        if (needle.isBlank() || !isInstalled(context)) return emptyList()
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).all
            .mapNotNull { (id, value) ->
                val name = value as? String ?: return@mapNotNull null
                if (!name.contains(needle, ignoreCase = true)) return@mapNotNull null
                ContactResult(name, "", "Messenger", id, loadAvatar(context, id))
            }
            .sortedWith(compareBy<ContactResult> { !it.name.startsWith(needle, true) }.thenBy { it.name.lowercase() })
            .take(limit)
    }

    fun openDraft(context: Context, target: ContactResult, body: String): String? {
        val shortcutId = target.messengerShortcutId ?: return "Messenger conversation is unavailable"
        Log.i("MinimalMessenger", "openDraft id=$shortcutId live=${liveContentIntents.containsKey(shortcutId)} bodyLength=${body.length}")
        MessengerDraftBridge.arm(context, body)
        liveContentIntents[shortcutId]?.let { pendingIntent ->
            return runCatching {
                val options = ActivityOptions.makeBasic().apply {
                    if (Build.VERSION.SDK_INT >= 34) {
                        pendingIntentBackgroundActivityStartMode = ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                    }
                }
                pendingIntent.send(context, 0, null, null, null, null, options.toBundle())
                Log.i("MinimalMessenger", "contentIntent.send accepted id=$shortcutId")
                null
            }.getOrElse {
                Log.e("MinimalMessenger", "contentIntent.send failed id=$shortcutId", it)
                MessengerDraftBridge.clear(context)
                "Messenger notification action expired · wait for a new message"
            }
        }
        val uri = shortcutUri(shortcutId) ?: return "This Messenger conversation type is not supported yet"
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setClassName(PACKAGE, "com.facebook.messenger.intents.IntentHandlerActivity")
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        return runCatching { context.startActivity(intent); null }.getOrElse {
            Log.e("MinimalMessenger", "deep link failed id=$shortcutId uri=$uri", it)
            MessengerDraftBridge.clear(context)
            "Messenger could not open this conversation"
        }
    }

    private fun shortcutUri(id: String): Uri? = when {
        id.matches(Regex("\\d+")) -> Uri.parse("fb-messenger://user/$id")
        id.startsWith("thread_shortcut_GROUP:") -> Uri.parse("fb-messenger://groupthreadfbid/${id.substringAfter(':')}")
        id.startsWith("thread_shortcut_ADVANCED_CRYPTO_ONE_TO_ONE:") ->
            Uri.parse("fb-messenger://advanced_crypto_one_to_one/${id.substringAfter(':')}")
        id.startsWith("thread_shortcut_MARKETPLACE:") -> Uri.parse("fb-messenger://marketplace/${id.substringAfter(':')}")
        else -> null
    }

    private fun isInstalled(context: Context) = runCatching {
        context.packageManager.getPackageInfo(PACKAGE, 0)
    }.isSuccess

    private fun avatarFile(context: Context, id: String) = File(context.cacheDir, "messenger_${id.hashCode()}.png")
    private fun loadAvatar(context: Context, id: String): Bitmap? {
        if (!MessengerSettings.profilePhotos(context)) return null
        return runCatching { BitmapFactory.decodeFile(avatarFile(context, id).absolutePath) }.getOrNull()
    }
}

object MessengerDraftBridge {
    private const val PREFS = "messenger_draft"
    private const val TEXT = "text"
    private const val ARMED_AT = "armed_at"

    fun arm(context: Context, text: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(TEXT, text).putLong(ARMED_AT, System.currentTimeMillis()).apply()
        HomeTypingAccessibilityService.monitorAutomation(15_000L)
    }

    fun pending(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (System.currentTimeMillis() - prefs.getLong(ARMED_AT, 0L) > 15_000L) {
            prefs.edit().clear().apply()
            return null
        }
        return prefs.getString(TEXT, null)
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
        HomeTypingAccessibilityService.automationFinished()
    }
}
