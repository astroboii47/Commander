package com.astroboii47.commander

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object AppearanceSettings {
    private const val PREFS = "command_appearance"
    private const val KEY_INVERT_BLUR_DARKNESS = "invert_blur_darkness"
    private const val KEY_BLUR_STYLE = "blur_style"
    private const val KEY_SINGLE_BLUR_STYLE = "single_blur_style"
    private const val KEY_DARK_BLUR_STYLE = "dark_blur_style"
    private const val KEY_LIGHT_BLUR_STYLE = "light_blur_style"
    private const val KEY_PULSE_CONFIRMATION = "pulse_confirmation"
    private const val KEY_APP_GLOW_MODE = "app_glow_mode"
    private const val KEY_LIST_ANIMATION = "list_animation"
    private const val KEY_TIME_THEME = "time_theme"
    private const val KEY_DAY_START = "day_start"
    private const val KEY_NIGHT_START = "night_start"
    private const val KEY_DAY_STYLE = "day_style"
    private const val KEY_NIGHT_STYLE = "night_style"
    private const val KEY_DARKER_BACKGROUND = "darker_background"
    private const val KEY_BACKGROUND_DARKNESS = "background_darkness"
    private const val KEY_RAYCAST_COMMAND_BAR = "raycast_command_bar"
    private const val KEY_RAYCAST_HINTS = "raycast_hints"
    private const val KEY_NOTHING_BAR = "nothing_bar"

    val invertBlurDarkness = mutableStateOf(false)
    val blurStyle = mutableStateOf(BlurStyle.Standard)
    val singleBlurStyle = mutableStateOf(true)
    val darkBlurStyle = mutableStateOf(BlurStyle.Standard)
    val lightBlurStyle = mutableStateOf(BlurStyle.Standard)
    val pulseConfirmation = mutableStateOf(true)
    val appGlowMode = mutableStateOf(AppGlowMode.Off)
    val listAnimation = mutableStateOf(ListAnimationMode.Quick)
    val timeTheme = mutableStateOf(false)
    val dayStart = mutableStateOf("07:00")
    val nightStart = mutableStateOf("19:00")
    val dayStyle = mutableStateOf(BlurStyle.Standard)
    val nightStyle = mutableStateOf(BlurStyle.Inverted)
    val darkerBackground = mutableStateOf(false)
    val backgroundDarkness = mutableStateOf(.22f)
    val raycastCommandBar = mutableStateOf(false)
    val raycastHints = mutableStateOf(true)
    val nothingBar = mutableStateOf(false)

    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val legacyInverted = prefs.getBoolean(KEY_INVERT_BLUR_DARKNESS, false)
        blurStyle.value = runCatching {
            BlurStyle.valueOf(prefs.getString(KEY_BLUR_STYLE, null).orEmpty())
        }.getOrDefault(if (legacyInverted) BlurStyle.Inverted else BlurStyle.Standard)
        singleBlurStyle.value = prefs.getBoolean(KEY_SINGLE_BLUR_STYLE, true)
        darkBlurStyle.value = readStyle(prefs.getString(KEY_DARK_BLUR_STYLE, null), blurStyle.value)
        lightBlurStyle.value = readStyle(prefs.getString(KEY_LIGHT_BLUR_STYLE, null), blurStyle.value)
        invertBlurDarkness.value = blurStyle.value == BlurStyle.Inverted
        pulseConfirmation.value = prefs.getBoolean(KEY_PULSE_CONFIRMATION, true)
        appGlowMode.value = runCatching {
            AppGlowMode.valueOf(prefs.getString(KEY_APP_GLOW_MODE, AppGlowMode.Off.name).orEmpty())
        }.getOrDefault(AppGlowMode.Off)
        listAnimation.value = runCatching {
            ListAnimationMode.valueOf(prefs.getString(KEY_LIST_ANIMATION, ListAnimationMode.Quick.name).orEmpty())
        }.getOrDefault(ListAnimationMode.Quick)
        timeTheme.value = prefs.getBoolean(KEY_TIME_THEME, false)
        dayStart.value = prefs.getString(KEY_DAY_START, "07:00") ?: "07:00"
        nightStart.value = prefs.getString(KEY_NIGHT_START, "19:00") ?: "19:00"
        dayStyle.value = readStyle(prefs.getString(KEY_DAY_STYLE, null), BlurStyle.Standard)
        nightStyle.value = readStyle(prefs.getString(KEY_NIGHT_STYLE, null), BlurStyle.Inverted)
        darkerBackground.value = prefs.getBoolean(KEY_DARKER_BACKGROUND, false)
        backgroundDarkness.value = prefs.getFloat(KEY_BACKGROUND_DARKNESS, .22f).coerceIn(0f, .65f)
        raycastCommandBar.value = prefs.getBoolean(KEY_RAYCAST_COMMAND_BAR, false)
        raycastHints.value = prefs.getBoolean(KEY_RAYCAST_HINTS, true)
        nothingBar.value = prefs.getBoolean(KEY_NOTHING_BAR, false)
    }

    fun saveInvertBlurDarkness(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_INVERT_BLUR_DARKNESS, enabled)
            .apply()
        invertBlurDarkness.value = enabled
        saveBlurStyle(context, if (enabled) BlurStyle.Inverted else BlurStyle.Standard)
    }

    fun saveBlurStyle(context: Context, style: BlurStyle) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_BLUR_STYLE, style.name)
            .putBoolean(KEY_INVERT_BLUR_DARKNESS, style == BlurStyle.Inverted)
            .apply()
        blurStyle.value = style
        invertBlurDarkness.value = style == BlurStyle.Inverted
    }

    fun saveBlurStyles(context: Context, single: Boolean, shared: BlurStyle, dark: BlurStyle, light: BlurStyle) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_SINGLE_BLUR_STYLE, single)
            .putString(KEY_BLUR_STYLE, shared.name)
            .putString(KEY_DARK_BLUR_STYLE, dark.name)
            .putString(KEY_LIGHT_BLUR_STYLE, light.name)
            .putBoolean(KEY_INVERT_BLUR_DARKNESS, shared == BlurStyle.Inverted)
            .apply()
        singleBlurStyle.value = single
        blurStyle.value = shared
        darkBlurStyle.value = dark
        lightBlurStyle.value = light
        invertBlurDarkness.value = shared == BlurStyle.Inverted
    }

    fun styleFor(darkMode: Boolean): BlurStyle {
        if (timeTheme.value) {
            val now = LocalTime.now()
            val day = parseTime(dayStart.value, LocalTime.of(7, 0))
            val night = parseTime(nightStart.value, LocalTime.of(19, 0))
            val inDay = if (day <= night) now >= day && now < night else now >= day || now < night
            return if (inDay) dayStyle.value else nightStyle.value
        }
        return if (singleBlurStyle.value) blurStyle.value
        else if (darkMode) darkBlurStyle.value else lightBlurStyle.value
    }

    fun saveTimeTheme(context: Context, enabled: Boolean, dayAt: String, nightAt: String, day: BlurStyle, night: BlurStyle) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_TIME_THEME, enabled)
            .putString(KEY_DAY_START, normalizedTime(dayAt, "07:00"))
            .putString(KEY_NIGHT_START, normalizedTime(nightAt, "19:00"))
            .putString(KEY_DAY_STYLE, day.name)
            .putString(KEY_NIGHT_STYLE, night.name)
            .apply()
        timeTheme.value = enabled
        dayStart.value = normalizedTime(dayAt, "07:00")
        nightStart.value = normalizedTime(nightAt, "19:00")
        dayStyle.value = day
        nightStyle.value = night
    }

    fun saveBackgroundDarkness(context: Context, enabled: Boolean, amount: Float) {
        val safe = amount.coerceIn(0f, .65f)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_DARKER_BACKGROUND, enabled)
            .putFloat(KEY_BACKGROUND_DARKNESS, safe)
            .apply()
        darkerBackground.value = enabled
        backgroundDarkness.value = safe
    }

    fun saveRaycastCommandBar(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_RAYCAST_COMMAND_BAR, enabled).apply()
        raycastCommandBar.value = enabled
    }

    fun saveRaycastHints(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_RAYCAST_HINTS, enabled).apply()
        raycastHints.value = enabled
    }

    fun saveNothingBar(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_NOTHING_BAR, enabled).apply()
        nothingBar.value = enabled
    }

    private fun parseTime(value: String, fallback: LocalTime): LocalTime =
        runCatching { LocalTime.parse(value, DateTimeFormatter.ofPattern("H:mm")) }.getOrDefault(fallback)

    private fun normalizedTime(value: String, fallback: String): String = runCatching {
        LocalTime.parse(value, DateTimeFormatter.ofPattern("H:mm")).format(DateTimeFormatter.ofPattern("HH:mm"))
    }.getOrDefault(fallback)

    private fun readStyle(value: String?, fallback: BlurStyle): BlurStyle =
        runCatching { BlurStyle.valueOf(value.orEmpty()) }.getOrDefault(fallback)

    fun savePulseConfirmation(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_PULSE_CONFIRMATION, enabled)
            .apply()
        pulseConfirmation.value = enabled
    }

    fun saveAppGlowMode(context: Context, mode: AppGlowMode) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_APP_GLOW_MODE, mode.name).apply()
        appGlowMode.value = mode
    }

    fun saveListAnimation(context: Context, mode: ListAnimationMode) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_LIST_ANIMATION, mode.name).apply()
        listAnimation.value = mode
    }
}

enum class AppGlowMode { Off, Outline, Reduced, Full }
enum class ListAnimationMode { Off, Quick, Smooth }
enum class BlurStyle { Standard, Glass, Inverted }
