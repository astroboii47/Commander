package com.astroboii47.commander

import android.app.Application
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build

class MinimalCommandApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AccentSettings.initialize(this)
        TriggerSettings.initialize(this)
        FileSearchSettings.initialize(this)
        AppearanceSettings.initialize(this)
        AppSearchSettings.initialize(this)
        SoundSettings.initialize(this)
        SoundFeedback.initialize(this)
        HomeTypingSettings.initialize(this)
        HubSettings.initialize(this)
        AppCatalog.initialize(this)
        publishDynamicShortcuts()
    }

    private fun publishDynamicShortcuts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return
        val manager = getSystemService(ShortcutManager::class.java) ?: return
        val commandBar = ShortcutInfo.Builder(this, COMMAND_BAR_SHORTCUT_ID)
            .setShortLabel(getString(R.string.shortcut_command_bar_short))
            .setLongLabel(getString(R.string.shortcut_command_bar_long))
            .setIcon(Icon.createWithResource(this, R.drawable.ic_commander_bar))
            .setIntent(
                Intent(OverlayActivity.ACTION_OPEN_OVERLAY)
                    .setClass(this, OverlayActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
            .build()
        val hub = ShortcutInfo.Builder(this, HUB_SHORTCUT_ID)
            .setShortLabel(getString(R.string.shortcut_hub_short))
            .setLongLabel(getString(R.string.shortcut_hub_long))
            .setIcon(Icon.createWithResource(this, R.drawable.ic_commander_hub))
            .setIntent(Intent(Intent.ACTION_VIEW).setClass(this, HubActivity::class.java))
            .build()
        val settings = ShortcutInfo.Builder(this, SETTINGS_SHORTCUT_ID)
            .setShortLabel(getString(R.string.shortcut_settings_short))
            .setLongLabel(getString(R.string.shortcut_settings_long))
            .setIcon(Icon.createWithResource(this, R.drawable.ic_commander_bar))
            .setIntent(Intent(Intent.ACTION_VIEW).setClass(this, SettingsActivity::class.java))
            .build()
        val home = ShortcutInfo.Builder(this, HOME_SHORTCUT_ID)
            .setShortLabel("Commander Home")
            .setLongLabel("Open Commander Home")
            .setIcon(Icon.createWithResource(this, R.drawable.ic_commander_home))
            .setIntent(Intent(Intent.ACTION_VIEW).setClass(this, MainActivity::class.java))
            .build()
        runCatching {
            manager.removeAllDynamicShortcuts()
            val shortcuts = mutableListOf(commandBar, hub, settings)
            if (resources.getBoolean(R.bool.commander_home_launcher_enabled)) shortcuts.add(home)
            manager.addDynamicShortcuts(shortcuts)
        }
    }

    private companion object {
        const val COMMAND_BAR_SHORTCUT_ID = "command_bar_dynamic"
        const val HUB_SHORTCUT_ID = "minimal_hub_dynamic"
        const val SETTINGS_SHORTCUT_ID = "command_settings_dynamic"
        const val HOME_SHORTCUT_ID = "command_home_dynamic"
    }
}
