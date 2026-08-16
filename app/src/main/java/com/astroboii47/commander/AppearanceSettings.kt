package com.astroboii47.commander

import android.content.Context
import androidx.compose.runtime.mutableStateOf

object AppearanceSettings {
    private const val PREFS = "command_appearance"
    private const val KEY_INVERT_BLUR_DARKNESS = "invert_blur_darkness"
    private const val KEY_PULSE_CONFIRMATION = "pulse_confirmation"
    private const val KEY_APP_GLOW_MODE = "app_glow_mode"
    private const val KEY_LIST_ANIMATION = "list_animation"

    val invertBlurDarkness = mutableStateOf(false)
    val pulseConfirmation = mutableStateOf(true)
    val appGlowMode = mutableStateOf(AppGlowMode.Off)
    val listAnimation = mutableStateOf(ListAnimationMode.Quick)

    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        invertBlurDarkness.value = prefs.getBoolean(KEY_INVERT_BLUR_DARKNESS, false)
        pulseConfirmation.value = prefs.getBoolean(KEY_PULSE_CONFIRMATION, true)
        appGlowMode.value = runCatching {
            AppGlowMode.valueOf(prefs.getString(KEY_APP_GLOW_MODE, AppGlowMode.Off.name).orEmpty())
        }.getOrDefault(AppGlowMode.Off)
        listAnimation.value = runCatching {
            ListAnimationMode.valueOf(prefs.getString(KEY_LIST_ANIMATION, ListAnimationMode.Quick.name).orEmpty())
        }.getOrDefault(ListAnimationMode.Quick)
    }

    fun saveInvertBlurDarkness(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_INVERT_BLUR_DARKNESS, enabled)
            .apply()
        invertBlurDarkness.value = enabled
    }

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
