package com.astroboii47.commander

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast

class KeyControlActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!HomeTypingAccessibilityService.toggleKeyControl(delayMs = 140L)) {
            Toast.makeText(this, "Enable Commander accessibility to use Key Control", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        finish()
    }
}
