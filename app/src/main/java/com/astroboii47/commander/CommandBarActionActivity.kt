package com.astroboii47.commander

import android.app.Activity
import android.content.Intent
import android.os.Bundle

class CommandBarActionActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent()
        finish()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent()
        finish()
    }

    private fun handleIntent() {
        when (intent?.action) {
            Intent.ACTION_CREATE_SHORTCUT -> returnShortcut()
            ACTION_OPEN_COMMAND_BAR -> startActivity(
                Intent(this, OverlayActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }

    private fun returnShortcut() {
        val launchIntent = Intent(OverlayActivity.ACTION_OPEN_OVERLAY).setClass(this, OverlayActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val result = Intent()
            .putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent)
            .putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.shortcut_command_bar_short))
            .putExtra(
                Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(this, R.drawable.ic_commander_bar),
            )
        setResult(RESULT_OK, result)
    }

    companion object {
        const val ACTION_OPEN_COMMAND_BAR = "com.astroboii47.commander.OPEN_COMMAND_BAR"
    }
}
