package com.astroboii47.commander

import android.Manifest
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val contactsPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        contactsPermission.launch(Manifest.permission.READ_CONTACTS)
        val engine = CommandEngine(this)
        lifecycleScope.launch(Dispatchers.IO) { engine.warmAppIndex() }
        setContent { AccentSelectionProvider { MinimalCommandApp(engine = engine, onDismiss = ::finish) } }
    }
}
