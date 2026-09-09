package com.astroboii47.commander

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class HubHistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        HubHistoryStore.initialize(this)
        setContent { AccentSelectionProvider { HubHistoryScreen(::finish) } }
    }
}

private val HistorySerif = FontFamily(Font(R.font.instrument_serif_regular))
private val HistorySans = FontFamily(Font(R.font.inter_variable))
private val HistoryMono = FontFamily(Font(R.font.courier_prime_regular))

@Composable
private fun HubHistoryScreen(onClose: () -> Unit) {
    val entries by HubHistoryStore.entries
    val context = androidx.compose.ui.platform.LocalContext.current
    val accent = AccentSettings.color.value
    Column(
        Modifier.fillMaxSize().background(Color.Black).statusBarsPadding().padding(8.dp)
            .clip(RoundedCornerShape(24.dp)).background(Color(0xFF070707))
            .border(1.dp, Color(0xFF303030), RoundedCornerShape(24.dp)).padding(20.dp),
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.History, null, tint = accent, modifier = Modifier.size(24.dp))
            Text("history", color = Color(0xFFF3F1EA), fontFamily = HistorySerif, fontSize = 38.sp, modifier = Modifier.padding(start = 9.dp))
            Spacer(Modifier.weight(1f))
            if (entries.isNotEmpty()) {
                Row(
                    Modifier.clip(RoundedCornerShape(18.dp)).clickable { HubHistoryStore.clear(context) }.padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.DeleteSweep, null, tint = Color(0xFF8C8A84), modifier = Modifier.size(17.dp))
                    Text("clear", color = Color(0xFF8C8A84), fontFamily = HistoryMono, fontSize = 12.sp)
                }
            }
            Text("close →", color = Color(0xFF8C8A84), fontFamily = HistoryMono, fontSize = 12.sp,
                modifier = Modifier.clickable(onClick = onClose).padding(8.dp))
        }
        Text(
            "kept ${HubSettings.historyRetention.value.label} · stored encrypted on this device",
            color = Color(0xFF8C8A84), fontFamily = HistoryMono, fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 12.dp),
        )
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            if (entries.isEmpty()) {
                Text("no notification history", color = Color(0xFF8C8A84), fontFamily = HistoryMono,
                    modifier = Modifier.padding(top = 28.dp).align(Alignment.CenterHorizontally))
            }
            entries.forEach { item ->
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color(0xFF101010))
                        .border(1.dp, Color(0xFF303030), RoundedCornerShape(18.dp)).padding(15.dp),
                ) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(item.title.ifBlank { item.appName }, color = Color(0xFFF3F1EA), fontFamily = HistorySans,
                            fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f))
                        Text(formatHistoryTime(item.time), color = Color(0xFF8C8A84), fontFamily = HistoryMono, fontSize = 10.sp)
                    }
                    if (item.text.isNotBlank()) Text(item.text, color = Color(0xFFAAA7A0), fontFamily = HistorySans,
                        fontSize = 13.sp, maxLines = 4, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp))
                    Text(item.appName, color = accent, fontFamily = HistoryMono, fontSize = 10.sp, modifier = Modifier.padding(top = 7.dp))
                }
            }
            Spacer(Modifier.padding(bottom = 6.dp))
        }
    }
}

private fun formatHistoryTime(time: Long): String = DateTimeFormatter.ofPattern("d MMM · HH:mm")
    .withZone(ZoneId.systemDefault()).format(Instant.ofEpochMilli(time))
