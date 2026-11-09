package com.astroboii47.commander

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun NavigationHoldGestureSetting(context: Context, accent: Color) {
    @Suppress("UNUSED_VARIABLE") val refreshWhenReturning = (context as? SettingsActivity)?.setupRevision
    val prefs = remember(context) {
        context.getSharedPreferences(NavigationHoldOverlay.PREFS, Context.MODE_PRIVATE)
    }
    var enabled by remember { mutableStateOf(prefs.getBoolean(NavigationHoldOverlay.KEY_ENABLED, false)) }
    var width by remember { mutableIntStateOf(prefs.getInt(NavigationHoldOverlay.KEY_WIDTH, NavigationHoldOverlay.DEFAULT_WIDTH_DP)) }
    var holdMs by remember { mutableIntStateOf(prefs.getInt(NavigationHoldOverlay.KEY_HOLD, NavigationHoldOverlay.DEFAULT_HOLD_MS)) }
    Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Hold navigation handle for Bar", color = Color(0xFFF4F1E9), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("Experimental · needs Commander accessibility access", color = Color(0xFF99958E), fontSize = 11.sp)
            }
            Switch(
                checked = enabled,
                onCheckedChange = {
                    enabled = it
                    prefs.edit().putBoolean(NavigationHoldOverlay.KEY_ENABLED, it).apply()
                },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent),
            )
        }
        if (enabled) {
            Text(
                "This places a small touch zone over the centre of the handle. If Home or app switching is affected, turn it off here.",
                color = Color(0xFF99958E), fontSize = 11.sp,
            )
            if (!HomeTypingAccessibilityService.isConnected()) {
                Text(
                    "Enable Commander accessibility access →",
                    color = accent,
                    fontSize = 12.sp,
                    modifier = Modifier.clickable {
                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    }.padding(vertical = 8.dp),
                )
            }
            Text("Touch width · $width dp", color = Color(0xFFF4F1E9), fontSize = 12.sp)
            Slider(
                value = width.toFloat(),
                onValueChange = { width = it.toInt() },
                onValueChangeFinished = { prefs.edit().putInt(NavigationHoldOverlay.KEY_WIDTH, width).apply() },
                valueRange = 48f..144f,
            )
            Text("Hold time · $holdMs ms", color = Color(0xFFF4F1E9), fontSize = 12.sp)
            Slider(
                value = holdMs.toFloat(),
                onValueChange = { holdMs = it.toInt() },
                onValueChangeFinished = { prefs.edit().putInt(NavigationHoldOverlay.KEY_HOLD, holdMs).apply() },
                valueRange = 350f..850f,
            )
        }
    }
}
