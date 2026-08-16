package com.astroboii47.commander

import android.content.Context

object SmsSettings {
    private const val PREFS = "sms_settings"
    private const val KEY_DIRECT = "direct_send"

    fun directEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_DIRECT, false)

    fun setDirectEnabled(context: Context, value: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_DIRECT, value).apply()
    }
}
