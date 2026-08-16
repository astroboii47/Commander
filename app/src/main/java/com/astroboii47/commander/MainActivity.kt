package com.astroboii47.commander

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        val engine = CommandEngine(this)
        lifecycleScope.launch(Dispatchers.IO) { engine.warmAppIndex() }
        setContent { AccentSelectionProvider { MinimalCommandApp(engine = engine, onDismiss = ::finish) } }
    }
}
