package com.astroboii47.commander

import android.app.Activity
import android.content.Intent
import android.os.Bundle

abstract class CommanderShortcutActivity : Activity() {
    protected abstract val shortcutName: String
    protected abstract val shortcutIcon: Int
    protected abstract fun launchIntent(): Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent?.action == Intent.ACTION_CREATE_SHORTCUT) {
            setResult(
                RESULT_OK,
                Intent()
                    .putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent())
                    .putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutName)
                    .putExtra(
                        Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                        Intent.ShortcutIconResource.fromContext(this, shortcutIcon),
                    ),
            )
        } else {
            startActivity(launchIntent())
        }
        finish()
    }
}

/**
 * Stable activity shortcut for third-party launchers.  Unlike an Android
 * pinned/dynamic shortcut this does not require the calling launcher to be
 * the current shortcut host, so it continues to work when Commander itself
 * is the default HOME app.
 */
class CommanderHomeShortcutActivity : CommanderShortcutActivity() {
    override val shortcutName = "Commander Home"
    override val shortcutIcon = R.drawable.ic_commander_home
    override fun launchIntent() = Intent(MainActivity.ACTION_SHOW_SURFACE)
        .setClass(this, MainActivity::class.java)
        .putExtra(MainActivity.EXTRA_SURFACE, MainActivity.SURFACE_HOME)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
}

class CommanderHubShortcutActivity : CommanderShortcutActivity() {
    override val shortcutName = "Commander Hub"
    override val shortcutIcon = R.drawable.ic_commander_hub
    override fun launchIntent() = Intent(this, HubActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}

class CommanderSwitcherShortcutActivity : CommanderShortcutActivity() {
    override val shortcutName = "Commander Switcher"
    override val shortcutIcon = R.drawable.ic_commander_bar
    override fun launchIntent() = Intent("com.astroboii47.commander.OPEN_SWITCHER")
        .setClass(this, SwitcherActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}

class CommanderKeyControlShortcutActivity : CommanderShortcutActivity() {
    override val shortcutName = "Commander Key Control"
    override val shortcutIcon = R.drawable.ic_commander_bar
    override fun launchIntent() = Intent("com.astroboii47.commander.OPEN_KEY_CONTROL")
        .setClass(this, KeyControlActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}
