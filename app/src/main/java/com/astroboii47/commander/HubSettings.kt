package com.astroboii47.commander

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import org.json.JSONArray
import org.json.JSONObject

enum class HubTabVisibility(val storedValue: String, val label: String) {
    Auto("auto", "auto"),
    Always("always", "always"),
    Hidden("hidden", "hidden");

    fun next(): HubTabVisibility = entries[(ordinal + 1) % entries.size]

    companion object {
        fun fromStored(value: String?): HubTabVisibility = entries.firstOrNull { it.storedValue == value } ?: Auto
    }
}

enum class HubHistoryRetention(val storedValue: String, val label: String, val durationMillis: Long?) {
    Off("off", "off", 0L),
    OneDay("1_day", "24 hours", 24L * 60 * 60 * 1000),
    OneWeek("1_week", "7 days", 7L * 24 * 60 * 60 * 1000),
    OneMonth("1_month", "30 days", 30L * 24 * 60 * 60 * 1000),
    ThreeMonths("3_months", "3 months", 90L * 24 * 60 * 60 * 1000),
    Indefinite("indefinite", "indefinitely", null);

    fun next(): HubHistoryRetention = entries[(ordinal + 1) % entries.size]

    companion object {
        fun fromStored(value: String?): HubHistoryRetention = entries.firstOrNull { it.storedValue == value } ?: Off
    }
}

object HubSettings {
    private const val PREFS = "hub_settings"
    private const val KEY_SHOW_SUMMARIES = "show_summaries"
    private const val KEY_QUICK_KEYBOARD_NAVIGATION = "quick_keyboard_navigation"
    private const val KEY_CLOSE_REPLY_AFTER_SENDING = "close_reply_after_sending"
    private const val KEY_SHOW_NOTIFICATION_IMAGES = "show_notification_images"
    private const val KEY_TINT_NOTIFICATION_IMAGES = "tint_notification_images"
    private const val KEY_NOTIFICATION_IMAGES_ON_RIGHT = "notification_images_on_right"
    private const val KEY_APP_NAME_ICON_COLOR = "app_name_icon_color"
    private const val KEY_HISTORY_RETENTION = "history_retention"
    private const val KEY_WALLPAPER_GLASS = "wallpaper_glass"
    private const val TAB_PREFIX = "tab_visibility_"
    // Important is a core destination because Commander Home always exposes
    // its count. Only genuinely optional filters belong in this setting list.
    val optionalTabs = listOf("messages", "calls", "email", "finance", "tasks", "apps", "flagged", "summaries")
    val tabVisibility = mutableStateOf<Map<String, HubTabVisibility>>(emptyMap())
    val quickKeyboardNavigation = mutableStateOf(false)
    val closeReplyAfterSending = mutableStateOf(false)
    val showNotificationImages = mutableStateOf(false)
    val tintNotificationImages = mutableStateOf(false)
    val notificationImagesOnRight = mutableStateOf(false)
    val appNameIconColor = mutableStateOf(false)
    val historyRetention = mutableStateOf(HubHistoryRetention.Off)
    val wallpaperGlass = mutableStateOf(false)

    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val legacySummaries = prefs.getBoolean(KEY_SHOW_SUMMARIES, false)
        tabVisibility.value = optionalTabs.associateWith { tab ->
            val stored = prefs.getString(TAB_PREFIX + tab, null)
            if (stored == null && tab == "summaries") {
                if (legacySummaries) HubTabVisibility.Always else HubTabVisibility.Hidden
            } else HubTabVisibility.fromStored(stored)
        }
        quickKeyboardNavigation.value = prefs.getBoolean(KEY_QUICK_KEYBOARD_NAVIGATION, false)
        closeReplyAfterSending.value = prefs.getBoolean(KEY_CLOSE_REPLY_AFTER_SENDING, false)
        showNotificationImages.value = prefs.getBoolean(KEY_SHOW_NOTIFICATION_IMAGES, false)
        tintNotificationImages.value = prefs.getBoolean(KEY_TINT_NOTIFICATION_IMAGES, false)
        notificationImagesOnRight.value = prefs.getBoolean(KEY_NOTIFICATION_IMAGES_ON_RIGHT, false)
        appNameIconColor.value = prefs.getBoolean(KEY_APP_NAME_ICON_COLOR, false)
        historyRetention.value = HubHistoryRetention.fromStored(prefs.getString(KEY_HISTORY_RETENTION, null))
        wallpaperGlass.value = prefs.getBoolean(KEY_WALLPAPER_GLASS, false)
    }

    fun visibility(tab: String): HubTabVisibility = tabVisibility.value[tab] ?: HubTabVisibility.Auto

    fun saveTabVisibility(context: Context, tab: String, visibility: HubTabVisibility) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(TAB_PREFIX + tab, visibility.storedValue).apply()
        tabVisibility.value = tabVisibility.value + (tab to visibility)
    }

    fun saveQuickKeyboardNavigation(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_QUICK_KEYBOARD_NAVIGATION, enabled).apply()
        quickKeyboardNavigation.value = enabled
    }

    fun saveCloseReplyAfterSending(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_CLOSE_REPLY_AFTER_SENDING, enabled).apply()
        closeReplyAfterSending.value = enabled
    }

    fun saveShowNotificationImages(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_SHOW_NOTIFICATION_IMAGES, enabled).apply()
        showNotificationImages.value = enabled
    }

    fun saveTintNotificationImages(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_TINT_NOTIFICATION_IMAGES, enabled).apply()
        tintNotificationImages.value = enabled
    }

    fun saveNotificationImagesOnRight(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_NOTIFICATION_IMAGES_ON_RIGHT, enabled).apply()
        notificationImagesOnRight.value = enabled
    }

    fun saveAppNameIconColor(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_APP_NAME_ICON_COLOR, enabled).apply()
        appNameIconColor.value = enabled
    }

    fun saveHistoryRetention(context: Context, retention: HubHistoryRetention) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_HISTORY_RETENTION, retention.storedValue).apply()
        historyRetention.value = retention
        HubHistoryStore.prune(context)
    }

    fun saveWallpaperGlass(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_WALLPAPER_GLASS, enabled).apply()
        wallpaperGlass.value = enabled
    }
}

data class HubIgnoreRule(
    val id: String,
    val packageName: String,
    val appName: String,
    val scopeLabel: String?,
)

object HubIgnoreSettings {
    private const val PREFS = "hub_settings"
    private const val KEY_RULES = "ignored_notification_rules"
    val rules = mutableStateOf<List<HubIgnoreRule>>(emptyList())

    fun initialize(context: Context) {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_RULES, "[]") ?: "[]"
        rules.value = runCatching {
            val array = JSONArray(raw)
            List(array.length()) { index ->
                val item = array.getJSONObject(index)
                HubIgnoreRule(
                    item.getString("id"),
                    item.getString("package"),
                    item.getString("app"),
                    item.optString("scope").takeIf(String::isNotBlank),
                )
            }
        }.getOrDefault(emptyList())
    }

    fun ignoreApp(context: Context, item: HubItem) = add(
        context,
        HubIgnoreRule("app:${item.packageName}", item.packageName, item.appName, null),
    )

    fun ignoreScope(context: Context, item: HubItem): Boolean {
        val scope = item.ignoreScope() ?: return false
        add(context, HubIgnoreRule("scope:${item.packageName}:${scope.first}", item.packageName, item.appName, scope.second))
        return true
    }

    fun matches(item: HubItem): Boolean {
        val scopeId = item.ignoreScope()?.first
        return rules.value.any { rule ->
            rule.id == "app:${item.packageName}" ||
                (scopeId != null && rule.id == "scope:${item.packageName}:$scopeId")
        }
    }

    fun remove(context: Context, id: String) {
        rules.value = rules.value.filterNot { it.id == id }
        save(context)
    }

    private fun add(context: Context, rule: HubIgnoreRule) {
        rules.value = (rules.value.filterNot { it.id == rule.id } + rule).sortedBy { it.appName.lowercase() }
        save(context)
    }

    private fun save(context: Context) {
        val array = JSONArray().apply {
            rules.value.forEach { rule ->
                put(JSONObject().put("id", rule.id).put("package", rule.packageName)
                    .put("app", rule.appName).put("scope", rule.scopeLabel.orEmpty()))
            }
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_RULES, array.toString()).apply()
    }
}

private fun HubItem.ignoreScope(): Pair<String, String>? = when {
    !shortcutId.isNullOrBlank() -> "shortcut:$shortcutId" to (conversationTitle ?: title)
    !conversationTitle.isNullOrBlank() -> "conversation:${conversationTitle.lowercase()}" to conversationTitle
    !channelId.isNullOrBlank() -> "channel:$channelId" to "${channelId} notifications"
    else -> null
}

data class HubImportantRule(
    val id: String,
    val kind: String,
    val packageName: String,
    val appName: String,
    val label: String,
)

object HubImportantSettings {
    private const val PREFS = "hub_settings"
    private const val KEY_RULES = "important_source_rules"
    val rules = mutableStateOf<List<HubImportantRule>>(emptyList())

    fun initialize(context: Context) {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_RULES, "[]") ?: "[]"
        rules.value = runCatching {
            val array = JSONArray(raw)
            List(array.length()) { index ->
                val item = array.getJSONObject(index)
                HubImportantRule(
                    id = item.getString("id"),
                    kind = item.getString("kind"),
                    packageName = item.getString("package"),
                    appName = item.getString("app"),
                    label = item.getString("label"),
                )
            }
        }.getOrDefault(emptyList())
    }

    fun candidates(item: HubItem): List<HubImportantRule> = buildList {
        item.importantConversationScope()?.let { (id, label) ->
            add(HubImportantRule("conversation:${item.packageName}:$id", "conversation", item.packageName, item.appName, label))
        }
        item.importantTypeScope()?.let { (id, label) ->
            add(HubImportantRule("type:${item.packageName}:$id", "type", item.packageName, item.appName, label))
        }
        add(HubImportantRule("app:${item.packageName}", "app", item.packageName, item.appName, item.appName))
    }.distinctBy(HubImportantRule::id)

    fun matches(item: HubItem): Boolean {
        val ids = candidates(item).mapTo(mutableSetOf(), HubImportantRule::id)
        return rules.value.any { it.id in ids }
    }

    fun contains(rule: HubImportantRule): Boolean = rules.value.any { it.id == rule.id }

    fun toggle(context: Context, rule: HubImportantRule) {
        rules.value = if (contains(rule)) rules.value.filterNot { it.id == rule.id }
        else (rules.value + rule).sortedWith(compareBy(HubImportantRule::kind, HubImportantRule::label))
        save(context)
    }

    fun remove(context: Context, id: String) {
        rules.value = rules.value.filterNot { it.id == id }
        save(context)
    }

    private fun save(context: Context) {
        val array = JSONArray().apply {
            rules.value.forEach { rule ->
                put(JSONObject().put("id", rule.id).put("kind", rule.kind).put("package", rule.packageName)
                    .put("app", rule.appName).put("label", rule.label))
            }
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_RULES, array.toString()).apply()
    }
}

private fun HubItem.importantConversationScope(): Pair<String, String>? = when {
    !shortcutId.isNullOrBlank() -> "shortcut:$shortcutId" to (conversationTitle ?: title)
    !conversationTitle.isNullOrBlank() -> "conversation:${conversationTitle.trim().lowercase()}" to conversationTitle
    messages.any { !it.outgoing && !it.sender.isNullOrBlank() } -> {
        val sender = messages.lastOrNull { !it.outgoing && !it.sender.isNullOrBlank() }?.sender ?: return null
        "sender:${sender.trim().lowercase()}" to sender
    }
    else -> null
}

private fun HubItem.importantTypeScope(): Pair<String, String>? = when {
    !channelId.isNullOrBlank() -> "channel:$channelId" to "$appName alert type"
    !category.isNullOrBlank() -> "category:$category" to "${category.replace('_', ' ')} alerts"
    else -> null
}
