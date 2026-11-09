package com.astroboii47.commander

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.drawable.toBitmap

@Composable
fun AssistantPowerRouteSettings(activity: SettingsActivity, accent: Color) {
    var selected by remember { mutableStateOf(AssistantPowerRouting.selectedPackage(activity)) }
    var showPicker by remember { mutableStateOf(false) }
    val selectedLabel = remember(selected, activity.setupRevision) {
        selected?.let { packageName ->
            runCatching {
                val pm = activity.packageManager
                pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
            }.getOrNull() ?: packageName
        } ?: "Commander Bar"
    }

    Text("Assistant gesture routing", color = Color(0xFFF4F1E9), fontSize = 16.sp, fontWeight = FontWeight.Bold)
    Text("Corner swipe → Commander Bar", color = Color(0xFFAAA7A0), fontSize = 12.sp)
    Text(
        "Power-button hold can open any app. A second app opens normally, not as Android's system assistant overlay.",
        color = Color(0xFF99958E), fontSize = 11.sp,
    )
    Spacer(Modifier.height(9.dp))
    Row(
        Modifier.fillMaxWidth()
            .background(Color(0xFF151515), RoundedCornerShape(11.dp))
            .clickable { showPicker = true }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Power hold", color = Color(0xFFAAA7A0), fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(selectedLabel, color = accent, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text("  →", color = accent, fontSize = 13.sp)
    }

    if (showPicker) {
        AssistantPowerAppPicker(activity, accent, selected) { packageName ->
            if (packageName != selected) {
                selected = packageName
                AssistantPowerRouting.select(activity, packageName)
            }
            showPicker = false
        }
    }
}

@Composable
private fun AssistantPowerAppPicker(
    activity: SettingsActivity,
    accent: Color,
    selected: String?,
    onPick: (String?) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val choices = remember(activity.setupRevision) { AssistantPowerRouting.choices(activity) }
    val filtered = remember(choices, query) {
        choices.filter { query.isBlank() || it.label.contains(query.trim(), ignoreCase = true) }
    }
    Dialog(onDismissRequest = { onPick(selected) }) {
        Column(
            Modifier.fillMaxWidth()
                .heightIn(max = 580.dp)
                .background(Color(0xF51B1B1F), RoundedCornerShape(20.dp))
                .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                .padding(16.dp),
        ) {
            Text("Power-button app", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Choose what opens on power hold", color = Color(0xFFAAA7A0), fontSize = 11.sp)
            Spacer(Modifier.height(12.dp))
            BasicTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                cursorBrush = SolidColor(accent),
                modifier = Modifier.fillMaxWidth().background(Color(0xFF101014), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 11.dp),
                decorationBox = { inner ->
                    if (query.isEmpty()) Text("Search apps", color = Color(0xFF77777D), fontSize = 14.sp)
                    inner()
                },
            )
            Spacer(Modifier.height(10.dp))
            LazyColumn {
                item(key = "commander") {
                    PickerRow("Commander Bar", null, selected == null, accent, activity, onPick)
                }
                items(filtered, key = { it.packageName }) { choice ->
                    PickerRow(choice.label, choice.packageName, selected == choice.packageName, accent, activity, onPick)
                }
            }
        }
    }
}

@Composable
private fun PickerRow(
    label: String,
    packageName: String?,
    selected: Boolean,
    accent: Color,
    activity: SettingsActivity,
    onPick: (String?) -> Unit,
) {
    val icon = remember(packageName) {
        packageName?.let {
            runCatching { activity.packageManager.getApplicationIcon(it).toBitmap(64, 64).asImageBitmap() }.getOrNull()
        }
    }
    Row(
        Modifier.fillMaxWidth()
            .background(if (selected) accent.copy(alpha = 0.18f) else Color.Transparent, RoundedCornerShape(10.dp))
            .clickable { onPick(packageName) }
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) Image(icon, null, modifier = Modifier.size(30.dp))
        else Spacer(Modifier.size(30.dp))
        Text(label, color = Color.White, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(start = 12.dp))
        if (selected) Text("✓", color = accent, fontSize = 16.sp)
    }
}
