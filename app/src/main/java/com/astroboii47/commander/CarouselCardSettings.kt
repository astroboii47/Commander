package com.astroboii47.commander

import android.content.Context

object CarouselCardSettings {
    private const val Prefs = "commander_carousel_cards"
    private const val Order = "order"
    private const val Hidden = "hidden"
    private const val Home = "home"
    private const val Custom = "custom_packages"
    private const val CustomCards = "custom_cards"
    private const val Separator = "|"

    fun id(label: String) = label.trim().lowercase().replace("|", "")
    private fun prefs(context: Context) = context.getSharedPreferences(Prefs, Context.MODE_PRIVATE)
    private fun list(context: Context, key: String) = prefs(context).getString(key, "").orEmpty()
        .split(Separator).filter(String::isNotBlank).distinct()
    private fun putList(context: Context, key: String, values: List<String>) {
        prefs(context).edit().putString(key, values.distinct().joinToString(Separator)).apply()
    }

    fun order(context: Context) = list(context, Order)
    fun stackOrder(context: Context, rootId: String) = list(context, "stack_order_$rootId")
    fun hidden(context: Context) = list(context, Hidden).toSet()
    fun home(context: Context) = prefs(context).getString(Home, null)
    fun customPackages(context: Context) = list(context, Custom)
    fun customCards(context: Context) = list(context, CustomCards)
    fun stackParent(context: Context, cardId: String) = prefs(context).getString("stack_$cardId", null)
    fun appOverride(context: Context, cardId: String) = prefs(context).getString("app_$cardId", null)
    fun tapAction(context: Context, cardId: String) = prefs(context).getString("tap_$cardId", null)
    fun glyph(context: Context, cardId: String) = prefs(context).getString("glyph_$cardId", null)
    fun swipeUpAction(context: Context, cardId: String) = prefs(context).getString("swipe_up_$cardId", null)
    fun swipeDownAction(context: Context, cardId: String) = prefs(context).getString("swipe_down_$cardId", null)
    fun title(context: Context, cardId: String) = prefs(context).getString("title_$cardId", null)
    fun detail(context: Context, cardId: String) = prefs(context).getString("detail_$cardId", null)
    fun topText(context: Context, cardId: String) = prefs(context).getString("top_$cardId", null)
    fun showAppNotifications(context: Context, cardId: String, defaultValue: Boolean = false): Boolean =
        prefs(context).getBoolean("notifications_$cardId", defaultValue)

    fun setHome(context: Context, cardId: String) {
        prefs(context).edit().putString(Home, cardId).apply()
    }

    fun setStackParent(context: Context, cardId: String, parentId: String?) {
        setOptionalString(context, "stack_$cardId", parentId?.takeIf { it != cardId })
    }

    fun setAppOverride(context: Context, cardId: String, packageName: String?) {
        prefs(context).edit().apply {
            if (packageName == null) remove("app_$cardId") else putString("app_$cardId", packageName)
        }.apply()
    }

    fun setTapAction(context: Context, cardId: String, action: String?) {
        setOptionalString(context, "tap_$cardId", action)
    }

    fun setShowAppNotifications(context: Context, cardId: String, show: Boolean) {
        prefs(context).edit().putBoolean("notifications_$cardId", show).apply()
    }

    fun setGlyph(context: Context, cardId: String, glyph: String?) {
        prefs(context).edit().apply {
            if (glyph == null) remove("glyph_$cardId") else putString("glyph_$cardId", glyph)
        }.apply()
    }

    fun setSwipeUpAction(context: Context, cardId: String, action: String?) {
        setOptionalString(context, "swipe_up_$cardId", action)
    }

    fun setSwipeDownAction(context: Context, cardId: String, action: String?) {
        setOptionalString(context, "swipe_down_$cardId", action)
    }

    fun setText(context: Context, cardId: String, title: String?, detail: String?, topText: String?) {
        setOptionalString(context, "title_$cardId", title?.trim()?.takeIf(String::isNotBlank))
        setOptionalString(context, "detail_$cardId", detail?.trim()?.takeIf(String::isNotBlank))
        setOptionalString(context, "top_$cardId", topText?.trim()?.takeIf(String::isNotBlank))
    }

    private fun setOptionalString(context: Context, key: String, value: String?) {
        prefs(context).edit().apply {
            if (value == null) remove(key) else putString(key, value)
        }.apply()
    }

    fun setVisible(context: Context, cardId: String, visible: Boolean) {
        val values = hidden(context).toMutableSet()
        if (visible) values.remove(cardId) else values.add(cardId)
        putList(context, Hidden, values.toList())
    }

    fun move(context: Context, cardId: String, delta: Int, availableIds: List<String>) {
        val values = (order(context) + availableIds).distinct().filter(availableIds::contains).toMutableList()
        val from = values.indexOf(cardId)
        if (from < 0) return
        val to = (from + delta).coerceIn(0, values.lastIndex)
        if (from != to) {
            values.removeAt(from)
            values.add(to, cardId)
            putList(context, Order, values)
        }
    }

    fun moveInStack(context: Context, rootId: String, cardId: String, delta: Int, memberIds: List<String>) {
        val members = memberIds.distinct()
        val values = (stackOrder(context, rootId) + members).distinct()
            .filter(members::contains).toMutableList()
        val from = values.indexOf(cardId)
        if (from < 0) return
        val to = (from + delta).coerceIn(0, values.lastIndex)
        if (from != to) {
            values.add(to, values.removeAt(from))
            putList(context, "stack_order_$rootId", values)
        }
    }

    fun addCustom(context: Context, packageName: String) {
        putList(context, Custom, customPackages(context) + packageName)
    }

    fun removeCustom(context: Context, packageName: String) {
        putList(context, Custom, customPackages(context).filterNot(packageName::equals))
        val label = runCatching {
            context.packageManager.getApplicationLabel(
                context.packageManager.getApplicationInfo(packageName, 0),
            ).toString()
        }.getOrNull()
        if (label != null) setStackParent(context, id(label), null)
    }

    fun addCustomCard(context: Context, label: String, reservedIds: Set<String> = emptySet()): String {
        val base = label.trim().ifBlank { "New card" }
        val existing = customCards(context).map(::id).toSet() + reservedIds
        var unique = base
        var suffix = 2
        while (id(unique) in existing) unique = "$base $suffix".also { suffix++ }
        putList(context, CustomCards, customCards(context) + unique)
        return id(unique)
    }

    fun removeCustomCard(context: Context, cardId: String) {
        putList(context, CustomCards, customCards(context).filterNot { id(it) == cardId })
        setStackParent(context, cardId, null)
    }
}
