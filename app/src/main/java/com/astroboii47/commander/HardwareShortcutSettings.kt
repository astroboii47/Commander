package com.astroboii47.commander

import android.content.Context
import android.view.KeyEvent
import androidx.compose.runtime.mutableStateOf

enum class HardwareShortcutAction(val id: String, val label: String) {
    Bar("bar", "Commander Bar"),
    Hub("hub", "Commander Hub"),
    Switcher("switcher", "App Switcher"),
    SwitcherBrowse("switcher_browse", "Sticky App Switcher shortcut"),
    SwitcherReverse("switcher_reverse", "Cycle backwards"),
    SwitcherDismiss("switcher_dismiss", "Remove selected app"),
    KeyControl("key_control", "Key Control"),
}

fun HardwareShortcutAction.isSwitcherAuxiliary() =
    this == HardwareShortcutAction.SwitcherReverse || this == HardwareShortcutAction.SwitcherDismiss

fun HardwareShortcutAction.isSwitcherNested() = isSwitcherAuxiliary() || this == HardwareShortcutAction.SwitcherBrowse

data class HardwareKeyBinding(
    val keyCode: Int,
    val modifiers: Int,
    val secondKeyCode: Int = KeyEvent.KEYCODE_UNKNOWN,
) {
    fun matches(event: KeyEvent): Boolean =
        event.keyCode == keyCode && normalizedModifiers(event.metaState) == modifiers

    fun displayName(): String {
        if (secondKeyCode != KeyEvent.KEYCODE_UNKNOWN) {
            return "${keyName(keyCode)} + ${keyName(secondKeyCode)}"
        }
        val parts = mutableListOf<String>()
        if (modifiers and KeyEvent.META_CTRL_ON != 0) parts += "Ctrl"
        if (modifiers and KeyEvent.META_ALT_ON != 0) parts += "Alt"
        if (modifiers and KeyEvent.META_SHIFT_ON != 0) parts += "Shift"
        if (modifiers and KeyEvent.META_META_ON != 0) parts += "Meta"
        if (modifiers and KeyEvent.META_SYM_ON != 0) parts += "Sym"
        if (modifiers and KeyEvent.META_FUNCTION_ON != 0) parts += "Fn"
        parts += keyName(keyCode)
        return parts.joinToString(" + ")
    }

    companion object {
        fun from(event: KeyEvent) = HardwareKeyBinding(event.keyCode, normalizedModifiers(event.metaState))

        private fun keyName(keyCode: Int): String = KeyEvent.keyCodeToString(keyCode)
            .removePrefix("KEYCODE_").replace('_', ' ').lowercase().replaceFirstChar { it.titlecase() }

        private fun normalizedModifiers(metaState: Int): Int = KeyEvent.normalizeMetaState(metaState) and
            (KeyEvent.META_CTRL_ON or KeyEvent.META_ALT_ON or KeyEvent.META_SHIFT_ON or KeyEvent.META_META_ON or
                KeyEvent.META_SYM_ON or KeyEvent.META_FUNCTION_ON)
    }
}

object HardwareShortcutSettings {
    private const val PREFS = "hardware_shortcuts"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_HOLD_SWITCHER = "hold_switcher"
    private const val KEY_ALLOW_WHILE_TYPING = "allow_while_typing"
    val enabled = mutableStateOf(false)
    val holdSwitcher = mutableStateOf(false)
    val allowWhileTyping = mutableStateOf(false)

    fun initialize(context: Context) {
        enabled.value = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_ENABLED, false)
        holdSwitcher.value = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_HOLD_SWITCHER, false)
        allowWhileTyping.value = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_ALLOW_WHILE_TYPING, false)
    }

    fun saveEnabled(context: Context, value: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_ENABLED, value).apply()
        enabled.value = value
    }

    fun saveHoldSwitcher(context: Context, value: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_HOLD_SWITCHER, value).apply()
        holdSwitcher.value = value
    }

    fun saveAllowWhileTyping(context: Context, value: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_ALLOW_WHILE_TYPING, value).apply()
        allowWhileTyping.value = value
    }

    fun binding(context: Context, action: HardwareShortcutAction): HardwareKeyBinding? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val keyCode = prefs.getInt("${action.id}_key", KeyEvent.KEYCODE_UNKNOWN)
        if (keyCode == KeyEvent.KEYCODE_UNKNOWN) return null
        return HardwareKeyBinding(
            keyCode,
            prefs.getInt("${action.id}_modifiers", 0),
            prefs.getInt("${action.id}_second_key", KeyEvent.KEYCODE_UNKNOWN),
        )
    }

    fun saveBinding(context: Context, action: HardwareShortcutAction, binding: HardwareKeyBinding?) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().apply {
            if (binding == null) {
                remove("${action.id}_key")
                remove("${action.id}_modifiers")
                remove("${action.id}_second_key")
            } else {
                putInt("${action.id}_key", binding.keyCode)
                putInt("${action.id}_modifiers", binding.modifiers)
                putInt("${action.id}_second_key", binding.secondKeyCode)
            }
        }.apply()
    }
}

object KeyControlSettings {
    private const val PREFS = "key_control_settings"
    private const val KEY_CLOSE_AFTER_ACTIVATION = "close_after_activation"

    val closeAfterActivation = mutableStateOf(false)

    fun initialize(context: Context) {
        closeAfterActivation.value = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_CLOSE_AFTER_ACTIVATION, false)
    }

    fun saveCloseAfterActivation(context: Context, value: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_CLOSE_AFTER_ACTIVATION, value)
            .apply()
        closeAfterActivation.value = value
    }
}
