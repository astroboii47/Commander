package com.astroboii47.commander

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.provider.Settings
import android.view.KeyEvent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

enum class ControlKind {
    Flashlight, Rotation, Brightness, Volume, AudioOutput, BluetoothDevices, Play, Pause, Next,
    RingerMode, Theme, BatteryProtection, Caffeine, ScreenDimmer, QrScanner, ScreenRecord, MiniMode, ScrollAssistant,
    SplitScreen,
}

data class ControlResult(
    val kind: ControlKind,
    val label: String,
    val subtitle: String,
    val percent: Int? = null,
    val automatic: Boolean = false,
    val option: String? = null,
)

object ControlActions {
    private const val CONTROL_STATE_PREFS = "quick_control_state"
    private const val KEY_BATTERY_PROTECTION_ENABLED = "battery_protection_enabled"

    /**
     * A mixed-in Quick Control must be executable without first entering the
     * dedicated `!control` screen. Only choose defaults where doing so is
     * predictable: auto brightness is the natural no-number action, while
     * battery protection is a repeatable on/off toggle.
     */
    fun resolveDefault(activity: Activity, result: ControlResult): ControlResult = when (result.kind) {
        ControlKind.Brightness -> if (result.percent == null && !result.automatic) {
            result.copy(automatic = true)
        } else result
        ControlKind.BatteryProtection -> if (result.option == null) {
            val currentlyEnabled = activity.getSharedPreferences(CONTROL_STATE_PREFS, Context.MODE_PRIVATE)
                .getBoolean(KEY_BATTERY_PROTECTION_ENABLED, false)
            result.copy(option = if (currentlyEnabled) "off" else "on")
        } else result
        ControlKind.RingerMode -> if (result.option == null) {
            val audio = activity.getSystemService(AudioManager::class.java)
            result.copy(option = if (audio.ringerMode == AudioManager.RINGER_MODE_NORMAL) "vibrate" else "normal")
        } else result
        else -> result
    }

    fun feedback(activity: Activity, result: ControlResult): String = when (result.kind) {
        ControlKind.Flashlight -> if (FlashlightState.enabled) "on" else "off"
        ControlKind.Rotation -> if (
            Settings.System.getInt(activity.contentResolver, Settings.System.ACCELEROMETER_ROTATION, 0) == 1
        ) "on" else "off"
        ControlKind.Brightness -> if (result.automatic) "auto" else "${result.percent ?: 0}%"
        ControlKind.Volume -> "${result.percent ?: 0}%"
        ControlKind.AudioOutput, ControlKind.BluetoothDevices -> "opened"
        ControlKind.RingerMode -> when (result.option) {
            "sound", "normal" -> "on"
            "silent" -> "off"
            else -> result.option ?: "changed"
        }
        ControlKind.Theme -> when (result.option) {
            "automatic", "system" -> "auto"
            else -> result.option ?: "changed"
        }
        ControlKind.BatteryProtection -> result.option?.let {
            if (it.all(Char::isDigit)) "$it%" else it
        } ?: "changed"
        ControlKind.Play -> "playing"
        ControlKind.Pause -> "paused"
        ControlKind.Next -> "skipped"
        ControlKind.QrScanner -> "opened"
        ControlKind.ScreenRecord -> "opened"
        ControlKind.SplitScreen -> "opened"
        ControlKind.Caffeine, ControlKind.ScreenDimmer,
        ControlKind.MiniMode, ControlKind.ScrollAssistant -> "toggled"
    }

    fun results(query: String): List<ControlResult> {
        val normalized = query.trim().lowercase()
        val percent = Regex("\\b(100|[1-9]?\\d)\\b").find(normalized)?.value?.toIntOrNull()
        val automatic = Regex("\\b(auto|automatic|adaptive)\\b").containsMatchIn(normalized)
        val ringerOption = Regex("\\b(on|sound|normal|vibrate|silent|off|dnd)\\b")
            .find(normalized)?.value
        val themeOption = Regex("\\b(dark|light|auto|automatic|system)\\b")
            .find(normalized)?.value
        val batteryOption = Regex("\\b(on|off|70|80|90|100)\\b")
            .find(normalized)?.value
        val all = listOf(
            ControlResult(ControlKind.Flashlight, "Flashlight", "toggle flashlight"),
            ControlResult(ControlKind.Rotation, "Rotation", "toggle automatic rotation"),
            ControlResult(
                ControlKind.Brightness,
                "Brightness${when { automatic -> " · auto"; percent != null -> " · $percent%"; else -> "" }}",
                if (automatic) "enable automatic brightness" else "set display brightness",
                percent,
                automatic,
            ),
            ControlResult(ControlKind.Volume, "Volume${percent?.let { " · $it%" }.orEmpty()}", "set media volume", percent),
            ControlResult(ControlKind.AudioOutput, "Audio output", "switch speaker or audio device"),
            ControlResult(ControlKind.BluetoothDevices, "Bluetooth devices", "connect or disconnect paired devices"),
            ControlResult(ControlKind.Play, "Play", "resume active media"),
            ControlResult(ControlKind.Pause, "Pause", "pause active media"),
            ControlResult(ControlKind.Next, "Next", "skip to next track"),
            ControlResult(
                ControlKind.RingerMode,
                "Ringer mode${ringerOption?.let { " · $it" }.orEmpty()}",
                "set sound, vibrate, off, or DND",
                option = ringerOption,
            ),
            ControlResult(
                ControlKind.Theme,
                "Theme${themeOption?.let { " · $it" }.orEmpty()}",
                "set dark, light, or auto",
                option = themeOption,
            ),
            ControlResult(
                ControlKind.BatteryProtection,
                "Battery protection${batteryOption?.let { " · ${if (it.all(Char::isDigit)) "$it%" else it}" }.orEmpty()}",
                "set on, off, 70%, 80%, 90%, or 100%",
                option = batteryOption,
            ),
            ControlResult(ControlKind.Caffeine, "Caffeine", "toggle keep-screen-awake"),
            ControlResult(ControlKind.ScreenDimmer, "Screen dimmer", "toggle Screen Filter"),
            ControlResult(ControlKind.QrScanner, "QR scanner", "scan a QR code"),
            ControlResult(ControlKind.ScreenRecord, "Screen recording", "open the system recorder"),
            ControlResult(ControlKind.SplitScreen, "Split screen", "split the active app"),
            ControlResult(ControlKind.MiniMode, "Mini mode", "toggle Titan mini mode"),
            ControlResult(ControlKind.ScrollAssistant, "Scroll Assistant", "toggle Titan touchpad scrolling"),
        )
        if (normalized.isBlank()) return all
        val term = normalized
            .replace(Regex("\\b(100|[1-9]?\\d)\\b"), "")
            .replace(Regex("\\b(auto|automatic|adaptive)\\b"), "")
            .trim()
        return all.filter {
            if (ringerOption != null && normalized.contains("ringer")) return@filter it.kind == ControlKind.RingerMode
            if (themeOption != null && normalized.contains("theme")) return@filter it.kind == ControlKind.Theme
            if (batteryOption != null && (normalized.contains("battery") || normalized.contains("charging"))) {
                return@filter it.kind == ControlKind.BatteryProtection
            }
            if (automatic && term.isBlank()) return@filter it.kind == ControlKind.Brightness
            when (it.kind) {
                ControlKind.Flashlight -> term in listOf("flashlight", "flash", "torch") || "flashlight".startsWith(term)
                ControlKind.Rotation -> term in listOf("rotation", "rotate", "auto rotate") || "rotation".startsWith(term)
                ControlKind.Brightness -> term in listOf("brightness", "bright") || "brightness".startsWith(term)
                ControlKind.Volume -> term in listOf("volume", "vol") || "volume".startsWith(term)
                ControlKind.AudioOutput -> term in listOf("audio", "audio output", "output", "speaker", "media output") ||
                    "audio output".startsWith(term)
                ControlKind.BluetoothDevices -> term in listOf("bluetooth", "bt", "bluetooth devices", "devices") ||
                    "bluetooth devices".startsWith(term)
                ControlKind.RingerMode -> term in listOf("ringer", "ringer mode", "sound mode") || "ringer mode".startsWith(term)
                ControlKind.Theme -> term in listOf("theme", "dark mode", "light mode") || "theme".startsWith(term)
                ControlKind.BatteryProtection -> term in listOf("battery", "battery protection", "charging", "charging limit") ||
                    "battery protection".startsWith(term)
                ControlKind.Caffeine -> term in listOf("caffeine", "cafe", "awake") || "caffeine".startsWith(term)
                ControlKind.ScreenDimmer -> term in listOf("screen dimmer", "dimmer", "screen filter", "filter") || "screen dimmer".startsWith(term)
                ControlKind.QrScanner -> term in listOf("qr", "qr scanner", "scanner") || "qr scanner".startsWith(term)
                ControlKind.ScreenRecord -> term in listOf("record", "screen record", "screen recording") || "screen recording".startsWith(term)
                ControlKind.SplitScreen -> term in listOf("split", "split screen", "multitask") || "split screen".startsWith(term)
                ControlKind.MiniMode -> term in listOf("mini", "mini mode") || "mini mode".startsWith(term)
                ControlKind.ScrollAssistant -> term in listOf("scroll", "scroll assistant", "touchpad scroll") || "scroll assistant".startsWith(term)
                else -> it.label.lowercase().startsWith(term)
            }
        }
    }

    fun execute(activity: Activity, result: ControlResult): String? {
        val error = when (result.kind) {
        ControlKind.Flashlight -> toggleFlashlight(activity)
        ControlKind.Rotation -> if (ensureWriteSettings(activity)) "Allow system settings access, then try rotation again" else {
            val enabled = Settings.System.getInt(activity.contentResolver, Settings.System.ACCELEROMETER_ROTATION, 0) == 1
            Settings.System.putInt(activity.contentResolver, Settings.System.ACCELEROMETER_ROTATION, if (enabled) 0 else 1)
            null
        }
        ControlKind.Brightness -> {
            if (ensureWriteSettings(activity)) "Allow system settings access, then try brightness again" else {
                if (result.automatic) {
                    Settings.System.putInt(
                        activity.contentResolver,
                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC,
                    )
                } else {
                    val percent = result.percent ?: return "Type a brightness percentage or auto"
                    Settings.System.putInt(
                        activity.contentResolver,
                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL,
                    )
                    Settings.System.putInt(activity.contentResolver, Settings.System.SCREEN_BRIGHTNESS, (percent * 255 / 100).coerceIn(1, 255))
                }
                null
            }
        }
        ControlKind.Volume -> {
            val percent = result.percent ?: return "Type a volume percentage"
            val audio = activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, (percent * max / 100).coerceIn(0, max), AudioManager.FLAG_SHOW_UI)
            null
        }
        ControlKind.AudioOutput -> launchAudioOutput(activity)
        ControlKind.BluetoothDevices -> launchBluetoothDevices(activity)
        ControlKind.Play -> media(activity, KeyEvent.KEYCODE_MEDIA_PLAY)
        ControlKind.Pause -> media(activity, KeyEvent.KEYCODE_MEDIA_PAUSE)
        ControlKind.Next -> media(activity, KeyEvent.KEYCODE_MEDIA_NEXT)
        ControlKind.RingerMode -> setRingerMode(activity, result.option)
        ControlKind.Theme -> setTheme(result.option)
        ControlKind.BatteryProtection -> setBatteryProtection(result.option)
        ControlKind.Caffeine -> launchShortcut(activity, "moe.zhs.caffeine", "toggle", "Caffeine is not installed")
        ControlKind.ScreenDimmer -> launchShortcut(
            activity,
            "com.github.ericytsang.screenfilter.app.android",
            "shortcut__toggle_service__id",
            "Screen Filter is not installed",
        )
        ControlKind.QrScanner -> launchQrScanner(activity)
        ControlKind.ScreenRecord -> launchScreenRecorder(activity)
        ControlKind.SplitScreen -> {
            val target = TaskSwitcherShell.underlyingSplitTarget(activity).getOrElse {
                return it.message ?: "Split screen unavailable"
            }
            if (!HomeTypingAccessibilityService.splitWhenForeground(target.taskId, target.packageName)) {
                "Enable Commander accessibility to use split screen"
            } else {
                activity.finish()
                null
            }
        }
        ControlKind.MiniMode -> ShizukuBridge.run(arrayOf("service", "call", "AService", "5"))
        ControlKind.ScrollAssistant -> ShizukuBridge.run(
            arrayOf(
                "sh", "-c",
                "v=\$(getprop persist.sys.scroll_assistant); if [ \"\$v\" = 1 ]; then n=0; else n=1; fi; service call AService 2 s16 persist.sys.scroll_assistant s16 \"\$n\"",
            ),
        )
        }
        if (error == null && result.kind == ControlKind.BatteryProtection) {
            when (result.option) {
                "on" -> activity.getSharedPreferences(CONTROL_STATE_PREFS, Context.MODE_PRIVATE)
                    .edit().putBoolean(KEY_BATTERY_PROTECTION_ENABLED, true).apply()
                "off" -> activity.getSharedPreferences(CONTROL_STATE_PREFS, Context.MODE_PRIVATE)
                    .edit().putBoolean(KEY_BATTERY_PROTECTION_ENABLED, false).apply()
                "70", "80", "90", "100" -> activity.getSharedPreferences(CONTROL_STATE_PREFS, Context.MODE_PRIVATE)
                    .edit().putBoolean(KEY_BATTERY_PROTECTION_ENABLED, true).apply()
            }
        }
        return error
    }

    private fun setRingerMode(activity: Activity, requested: String?): String? {
        val option = requested ?: return "Choose on, vibrate, off, or DND"
        val audio = activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val notifications = activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (option == "dnd" && !notifications.isNotificationPolicyAccessGranted) {
            activity.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
            return "Allow Do Not Disturb access, then try again"
        }
        return runCatching {
            when (option) {
                "dnd" -> notifications.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
                "vibrate" -> {
                    if (notifications.isNotificationPolicyAccessGranted) {
                        notifications.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                    }
                    audio.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                }
                "off", "silent" -> {
                    if (notifications.isNotificationPolicyAccessGranted) {
                        notifications.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                    }
                    audio.ringerMode = AudioManager.RINGER_MODE_SILENT
                }
                else -> {
                    if (notifications.isNotificationPolicyAccessGranted) {
                        notifications.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                    }
                    audio.ringerMode = AudioManager.RINGER_MODE_NORMAL
                }
            }
            null
        }.getOrElse { "Ringer mode unavailable" }
    }

    private fun setTheme(requested: String?): String? {
        val option = requested ?: return "Choose dark, light, or auto"
        val mode = when (option) {
            "dark" -> "yes"
            "light" -> "no"
            else -> "auto"
        }
        return ShizukuBridge.run(arrayOf("cmd", "uimode", "night", mode))
    }

    private fun setBatteryProtection(requested: String?): String? {
        val option = requested ?: return "Choose on, off, 70, 80, 90, or 100"
        val commands = when (option) {
            "on" -> listOf(
                arrayOf("service", "call", "agui_common", "4", "s16", "key_charging_custom_mode", "s16", "80,false"),
                arrayOf("service", "call", "agui_common", "4", "s16", "key_charging_Intelligent_mode", "s16", "true"),
                arrayOf("service", "call", "agui_common", "7", "i32", "1"),
            )
            "off" -> listOf(
                arrayOf("service", "call", "agui_common", "4", "s16", "key_charging_Intelligent_mode", "s16", "false"),
                arrayOf("service", "call", "agui_common", "4", "s16", "key_charging_custom_mode", "s16", "80,false"),
                arrayOf("service", "call", "agui_common", "7", "i32", "0"),
            )
            "70", "80", "90", "100" -> listOf(
                arrayOf("service", "call", "agui_common", "4", "s16", "key_charging_Intelligent_mode", "s16", "false"),
                arrayOf("service", "call", "agui_common", "4", "s16", "key_charging_custom_mode", "s16", "$option,true"),
                arrayOf("service", "call", "agui_common", "7", "i32", "1"),
            )
            else -> return "Use on, off, 70, 80, 90, or 100"
        }
        commands.forEach { command ->
            ShizukuBridge.run(command)?.let { return it }
        }
        return null
    }

    private fun ensureWriteSettings(activity: Activity): Boolean {
        if (Settings.System.canWrite(activity)) return false
        activity.startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:${activity.packageName}")))
        return true
    }

    private fun toggleFlashlight(activity: Activity): String? {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA), 4310)
            return "Allow camera access, then try flashlight again"
        }
        return runCatching {
            val manager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val camera = manager.cameraIdList.first { id ->
                manager.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
            FlashlightState.enabled = !FlashlightState.enabled
            manager.setTorchMode(camera, FlashlightState.enabled)
            null
        }.getOrElse { "Flashlight unavailable" }
    }

    private fun media(activity: Activity, keyCode: Int): String? = runCatching {
        val audio = activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audio.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
        audio.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
        null
    }.getOrElse { "Media control unavailable" }

    private fun launchAudioOutput(activity: Activity): String? = runCatching {
        val intent = Intent("com.android.systemui.action.LAUNCH_SYSTEM_MEDIA_OUTPUT_DIALOG").setComponent(
            ComponentName("com.android.systemui", "com.android.systemui.media.dialog.MediaOutputDialogReceiver"),
        )
        activity.sendBroadcast(intent)
        null
    }.getOrElse {
        ShizukuBridge.run(
            arrayOf(
                "am", "broadcast",
                "-a", "com.android.systemui.action.LAUNCH_SYSTEM_MEDIA_OUTPUT_DIALOG",
                "-n", "com.android.systemui/.media.dialog.MediaOutputDialogReceiver",
            ),
        ) ?: return null
        "Audio output switcher unavailable"
    }

    private fun launchBluetoothDevices(activity: Activity): String? = runCatching {
        activity.startActivity(
            Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
        )
        null
    }.getOrElse { "Bluetooth settings unavailable" }

    private fun launchShortcut(activity: Activity, packageName: String, id: String, unavailable: String): String? {
        val shortcut = AppShortcutCatalog.search(activity, "").firstOrNull { it.packageName == packageName && it.id == id }
            ?: return unavailable
        return AppShortcutCatalog.launch(activity, shortcut)
    }

    private fun launchQrScanner(activity: Activity): String? = runCatching {
        val intent = Intent("com.google.android.gms.mlkit_barcode_ui.SCAN_QR_CODE")
            .setPackage("com.google.android.gms")
        activity.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
        null
    }.getOrElse { "QR scanner unavailable" }

    private fun launchScreenRecorder(activity: Activity): String? = runCatching {
        val intent = Intent("com.android.systemui.action.TAKE_SCREEN_RECORD").setComponent(
            ComponentName("com.android.systemui", "com.agui.systemui.qs.screenrecord.TakeScreenRecordService"),
        )
        activity.startService(intent)
        null
    }.getOrElse { "Screen recorder unavailable" }
}

private object FlashlightState { var enabled = false }
