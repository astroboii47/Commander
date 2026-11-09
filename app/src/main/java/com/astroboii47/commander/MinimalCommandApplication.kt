package com.astroboii47.commander

import android.app.Application
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.content.ComponentCallbacks2
import android.os.SystemClock
import android.os.Process
import android.util.Log

class MinimalCommandApplication : Application() {
    override fun onCreate() {
        val startupAt = SystemClock.uptimeMillis()
        super.onCreate()
        AccentSettings.initialize(this)
        ModuleSettings.initialize(this)
        if (ModuleSettings.homeEnabled.value) HomeSettings.initialize(this)
        TriggerSettings.initialize(this)
        SplitPairSettings.initialize(this)
        FileSearchSettings.initialize(this)
        AppearanceSettings.initialize(this)
        AppSearchSettings.initialize(this)
        SoundSettings.initialize(this)
        // Audio setup is deferred until the opening sound runs off the UI
        // thread. It does not need to block every cold process launch.
        HomeTypingSettings.initialize(this)
        HardwareShortcutSettings.initialize(this)
        KeyControlSettings.initialize(this)
        if (ModuleSettings.hubEnabled.value) {
            HubSettings.initialize(this)
            HubIgnoreSettings.initialize(this)
            HubImportantSettings.initialize(this)
        }
        Log.i("CommanderLaunch", "process settings ready in ${SystemClock.uptimeMillis() - startupAt}ms")
        // None of these are required to draw the launcher. History may involve
        // encrypted disk I/O and the app catalogue/shortcut manager may query
        // package services, so keep them off Home's first-frame path.
        // These disk/package-manager tasks are unrelated to Bar's first frame.
        // Keep them available promptly for Hub and launcher shortcuts, but do
        // not let their default-priority thread contend with UI/IME startup.
        Thread({
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
            // Reconcile component overrides after an upgrade. Android retains
            // setComponentEnabledSetting state across installs, so a public
            // build that hides experimental Home must actively remove any
            // launcher/shortcut entry enabled by an earlier development build.
            ModuleSettings.syncComponents(this)
            if (ModuleSettings.hubEnabled.value) HubHistoryStore.initialize(this)
            refreshShortcutsIfChanged(this)
        }, "commander-warmup").start()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        when {
            level >= ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> HubImageCache.clear()
            level >= ComponentCallbacks2.TRIM_MEMORY_BACKGROUND -> HubImageCache.trim()
        }
    }

    companion object {
    private const val SHORTCUT_PREFS = "commander_shortcut_sync"
    private const val SHORTCUT_SIGNATURE = "signature"
    fun refreshShortcutsIfChanged(context: android.content.Context) {
        val signature = "${BuildConfig.VERSION_CODE}:${ModuleSettings.homeEnabled.value}:${ModuleSettings.hubEnabled.value}"
        val prefs = context.getSharedPreferences(SHORTCUT_PREFS, android.content.Context.MODE_PRIVATE)
        if (prefs.getString(SHORTCUT_SIGNATURE, null) == signature) return
        if (refreshShortcuts(context)) prefs.edit().putString(SHORTCUT_SIGNATURE, signature).apply()
    }
    fun refreshShortcuts(context: android.content.Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return false
        val manager = context.getSystemService(ShortcutManager::class.java) ?: return false
        val commandBar = ShortcutInfo.Builder(context, COMMAND_BAR_SHORTCUT_ID)
            .setShortLabel(context.getString(R.string.shortcut_command_bar_short))
            .setLongLabel(context.getString(R.string.shortcut_command_bar_long))
            .setIcon(Icon.createWithResource(context, R.drawable.ic_commander_bar))
            .setIntent(
                Intent(OverlayActivity.ACTION_OPEN_OVERLAY)
                    .setClass(context, OverlayActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
            .build()
        val hub = ShortcutInfo.Builder(context, HUB_SHORTCUT_ID)
            .setShortLabel(context.getString(R.string.shortcut_hub_short))
            .setLongLabel(context.getString(R.string.shortcut_hub_long))
            .setIcon(Icon.createWithResource(context, R.drawable.ic_commander_hub))
            .setIntent(Intent(Intent.ACTION_VIEW).setClass(context, HubActivity::class.java))
            .build()
        val settings = ShortcutInfo.Builder(context, SETTINGS_SHORTCUT_ID)
            .setShortLabel(context.getString(R.string.shortcut_settings_short))
            .setLongLabel(context.getString(R.string.shortcut_settings_long))
            .setIcon(Icon.createWithResource(context, R.drawable.ic_commander_bar))
            .setIntent(Intent(Intent.ACTION_VIEW).setClass(context, SettingsActivity::class.java))
            .build()
        val switcher = ShortcutInfo.Builder(context, SWITCHER_SHORTCUT_ID)
            .setShortLabel("App Switcher")
            .setLongLabel("Open Commander App Switcher")
            .setIcon(Icon.createWithResource(context, R.drawable.ic_commander_bar))
            .setIntent(
                Intent("com.astroboii47.commander.OPEN_SWITCHER")
                    .setClass(context, SwitcherActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
            .build()
        val keyControl = ShortcutInfo.Builder(context, KEY_CONTROL_SHORTCUT_ID)
            .setShortLabel("Key Control")
            .setLongLabel("Control the screen with keys")
            .setIcon(Icon.createWithResource(context, R.drawable.ic_commander_bar))
            .setIntent(
                Intent("com.astroboii47.commander.OPEN_KEY_CONTROL")
                    .setClass(context, KeyControlActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
            .build()
        val home = ShortcutInfo.Builder(context, HOME_SHORTCUT_ID)
            .setShortLabel("Commander Home")
            .setLongLabel("Open Commander Home")
            .setIcon(Icon.createWithResource(context, R.drawable.ic_commander_home))
            .setIntent(
                Intent(MainActivity.ACTION_SHOW_SURFACE)
                    .setClass(context, CommanderHomeShortcutActivity::class.java)
                    .putExtra(MainActivity.EXTRA_SURFACE, MainActivity.SURFACE_HOME),
            )
            .build()
        return runCatching {
            val shortcuts = mutableListOf(commandBar)
            if (ModuleSettings.homeEnabled.value || ModuleSettings.hubEnabled.value)
                shortcuts.addAll(listOf(switcher, keyControl, settings))
            if (ModuleSettings.hubEnabled.value) shortcuts.add(hub)
            if (ModuleSettings.homeEnabled.value) shortcuts.add(home)
            // Preserve the stable IDs so launchers can update their cached
            // records instead of seeing them removed and recreated at boot.
            manager.setDynamicShortcuts(shortcuts)
            true
        }.getOrDefault(false)
    }


        const val COMMAND_BAR_SHORTCUT_ID = "command_bar_dynamic"
        const val HUB_SHORTCUT_ID = "minimal_hub_dynamic"
        const val SETTINGS_SHORTCUT_ID = "command_settings_dynamic"
        const val SWITCHER_SHORTCUT_ID = "app_switcher_dynamic"
        const val KEY_CONTROL_SHORTCUT_ID = "key_control_dynamic"
        const val HOME_SHORTCUT_ID = "command_home_dynamic"
    }
}
