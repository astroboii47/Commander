package com.astroboii47.commander

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MessagingContactsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { AccentSelectionProvider { MessagingContactsScreen(this) } }
    }
}

@Composable
private fun MessagingContactsScreen(activity: MessagingContactsActivity) {
    val accent = AccentSettings.color.value
    var revision by remember { mutableStateOf(0) }
    var query by remember { mutableStateOf("") }
    var savedTab by remember { mutableStateOf(true) }
    var editingId by remember { mutableStateOf<String?>(null) }
    var editName by remember { mutableStateOf("") }
    var addingIdentity by remember { mutableStateOf<String?>(null) }
    var assigningContactId by remember { mutableStateOf<String?>(null) }
    var smsPickerContactId by remember { mutableStateOf<String?>(null) }
    var smsSearch by remember { mutableStateOf("") }
    var smsCandidates by remember { mutableStateOf<List<SmsContactCandidate>>(emptyList()) }
    var deleteId by remember { mutableStateOf<String?>(null) }
    val contacts = remember(revision) { CommanderMessagingContacts.contacts(activity) }
    val recent = remember(revision) { CommanderMessagingContacts.recent(activity) }
    val discoveries = remember(revision) { CommanderMessagingContacts.discoveries(activity).associateBy(MessagingDiscovery::identity) }
    val needle = query.trim()
    LaunchedEffect(smsPickerContactId, smsSearch, revision) {
        smsCandidates = if (smsPickerContactId == null) emptyList() else withContext(Dispatchers.IO) {
            CommanderMessagingContacts.smsContacts(activity, smsSearch)
        }
    }

    Column(Modifier.fillMaxSize().background(Color(0xFF050505)).padding(top = 48.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("‹", color = accent, fontSize = 30.sp, modifier = Modifier.clickable { activity.finish() }.padding(end = 14.dp))
            Column(Modifier.weight(1f)) {
                Text("messaging people", color = Color(0xFFF4F1E9), fontSize = 27.sp, fontWeight = FontWeight.Bold)
                Text("${contacts.size} people · ${recent.size} unassigned", color = Color(0xFF99958E), fontSize = 11.sp)
            }
        }
        BasicTextField(
            value = query, onValueChange = { query = it }, singleLine = true,
            textStyle = TextStyle(color = Color(0xFFF4F1E9), fontSize = 15.sp),
            decorationBox = { inner ->
                if (query.isBlank()) Text("Search people or app identities…", color = Color(0xFF77736D), fontSize = 15.sp)
                inner()
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp)
                .background(Color(0xFF111111), RoundedCornerShape(13.dp)).padding(14.dp),
        )
        Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Text("people", color = if (savedTab) accent else Color(0xFF77736D), fontSize = 13.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { savedTab = true; addingIdentity = null; assigningContactId = null; smsPickerContactId = null; deleteId = null })
            Text("unassigned", color = if (!savedTab) accent else Color(0xFF77736D), fontSize = 13.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { savedTab = false; addingIdentity = null; assigningContactId = null; smsPickerContactId = null; deleteId = null })
            Spacer(Modifier.weight(1f))
            if (!savedTab && recent.isNotEmpty()) Text("clear unassigned", color = Color(0xFF99958E), fontSize = 12.sp, modifier = Modifier.clickable {
                CommanderMessagingContacts.clearRecent(activity); revision++
            })
        }
        Text(
            if (savedTab) "Each person can contain separate WhatsApp, Messenger, Signal and other app identities."
            else "Conversations discovered from notifications stay here until you create a person or assign them to one.",
            color = Color(0xFF77736D), fontSize = 11.sp, modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp),
        )
        LazyColumn(
            Modifier.fillMaxWidth().weight(1f), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (savedTab) {
                items(contacts.filter { contact ->
                    needle.isBlank() || contact.name.contains(needle, true) || contact.identities.any { identity ->
                        discoveries[identity]?.let { it.title.contains(needle, true) || it.platform.contains(needle, true) } == true
                    }
                }, key = { it.id }) { contact ->
                    val routes = contact.identities.sortedByDescending { it == contact.defaultIdentity }.mapNotNull(discoveries::get)
                    ManagerCard {
                        Text(contact.name, color = Color(0xFFF4F1E9), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("${routes.size} assigned ${if (routes.size == 1) "destination" else "destinations"}", color = Color(0xFF77736D), fontSize = 11.sp)
                        if (routes.isEmpty()) Text("No app identities assigned", color = Color(0xFFFFA36E), fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
                        routes.forEach { route ->
                            Row(
                                Modifier.fillMaxWidth().padding(top = 8.dp)
                                    .background(Color(0xFF1A1A1A), RoundedCornerShape(10.dp)).padding(horizontal = 10.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        if (route.identity == contact.defaultIdentity) "${route.platform} · default" else route.platform,
                                        color = accent, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                    )
                                    Text(route.title, color = Color(0xFFD2CEC7), fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                if (route.identity != contact.defaultIdentity) Action("make default", accent) {
                                    CommanderMessagingContacts.setDefault(activity, contact.id, route.identity); revision++
                                }
                                Spacer(Modifier.width(12.dp))
                                Action("detach", Color(0xFFFFA36E)) {
                                    CommanderMessagingContacts.removeFrom(activity, contact.id, route.identity); revision++
                                }
                            }
                        }
                        when {
                            editingId == contact.id -> RenameField(editName, { editName = it.take(60) }, accent) {
                                CommanderMessagingContacts.rename(activity, contact.id, editName); editingId = null; revision++
                            }
                            deleteId == contact.id -> ConfirmDelete(
                                onDelete = { CommanderMessagingContacts.deleteContact(activity, contact.id); deleteId = null; revision++ },
                                onCancel = { deleteId = null },
                            )
                            assigningContactId == contact.id -> {
                                Text("choose an unassigned conversation", color = Color(0xFF99958E), fontSize = 11.sp, modifier = Modifier.padding(top = 11.dp, bottom = 3.dp))
                                if (recent.isEmpty()) Text("No unassigned conversations", color = Color(0xFF77736D), fontSize = 12.sp, modifier = Modifier.padding(vertical = 7.dp))
                                recent.forEach { discovery ->
                                    Row(
                                        Modifier.fillMaxWidth().clickable {
                                            CommanderMessagingContacts.addTo(activity, contact.id, discovery.identity)
                                            assigningContactId = null
                                            revision++
                                        }.padding(vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(discovery.platform, color = accent, fontSize = 11.sp, modifier = Modifier.width(82.dp))
                                        Text(discovery.title, color = Color(0xFFD2CEC7), fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                                Action("cancel", Color(0xFF99958E)) { assigningContactId = null }
                            }
                            smsPickerContactId == contact.id -> {
                                Text("assign an SMS contact", color = Color(0xFF99958E), fontSize = 11.sp, modifier = Modifier.padding(top = 11.dp, bottom = 5.dp))
                                BasicTextField(
                                    value = smsSearch,
                                    onValueChange = { smsSearch = it.take(60) },
                                    singleLine = true,
                                    textStyle = TextStyle(color = Color(0xFFF4F1E9), fontSize = 14.sp),
                                    decorationBox = { inner ->
                                        if (smsSearch.isBlank()) Text("Search phone contacts…", color = Color(0xFF77736D), fontSize = 13.sp)
                                        inner()
                                    },
                                    modifier = Modifier.fillMaxWidth().background(Color(0xFF1B1B1B), RoundedCornerShape(9.dp)).padding(10.dp),
                                )
                                smsCandidates.take(12).forEach { candidate ->
                                    Row(
                                        Modifier.fillMaxWidth().clickable {
                                            CommanderMessagingContacts.addSmsTo(activity, contact.id, candidate)
                                            smsPickerContactId = null
                                            smsSearch = ""
                                            revision++
                                        }.padding(vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Column(Modifier.weight(1f)) {
                                            Text(candidate.name, color = Color(0xFFD2CEC7), fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text("${candidate.label} · ${candidate.number}", color = Color(0xFF77736D), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                        Text("SMS", color = accent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Action("cancel", Color(0xFF99958E)) { smsPickerContactId = null; smsSearch = "" }
                            }
                            else -> Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    if (recent.isNotEmpty()) Action("assign conversation", accent) { assigningContactId = contact.id }
                                    Action("assign SMS contact", accent) {
                                        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                                            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.READ_CONTACTS), 4302)
                                        } else {
                                            smsPickerContactId = contact.id
                                            smsSearch = ""
                                        }
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Action("rename person", accent) { editingId = contact.id; editName = contact.name }
                                    Action("delete person", Color(0xFFFF6B62)) { deleteId = contact.id }
                                }
                            }
                        }
                    }
                }
            } else {
                items(recent.filter { needle.isBlank() || it.title.contains(needle, true) || it.platform.contains(needle, true) }, key = { it.identity }) { discovery ->
                    ManagerCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(discovery.title, color = Color(0xFFF4F1E9), fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(discovery.platform, color = Color(0xFF77736D), fontSize = 11.sp)
                            }
                            val replyAvailable = CommanderMessagingContacts.isReplyAvailable(discovery)
                            Text(if (replyAvailable) "reply available" else "waiting for notification",
                                color = if (replyAvailable) Color(0xFF54C98A) else Color(0xFF77736D), fontSize = 10.sp)
                        }
                        when {
                            editingId == discovery.identity -> RenameField(editName, { editName = it.take(60) }, accent) {
                                CommanderMessagingContacts.create(activity, discovery, editName); editingId = null; revision++
                            }
                            addingIdentity == discovery.identity -> {
                                Text("assign to person", color = Color(0xFF99958E), fontSize = 11.sp, modifier = Modifier.padding(top = 9.dp, bottom = 4.dp))
                                contacts.forEach { contact ->
                                    Text(contact.name, color = accent, fontSize = 13.sp, modifier = Modifier.fillMaxWidth().clickable {
                                        CommanderMessagingContacts.addTo(activity, contact.id, discovery.identity); addingIdentity = null; revision++
                                    }.padding(vertical = 7.dp))
                                }
                                Action("cancel", Color(0xFF99958E)) { addingIdentity = null }
                            }
                            deleteId == discovery.identity -> ConfirmDelete(
                                onDelete = { CommanderMessagingContacts.deleteDiscovery(activity, discovery.identity); deleteId = null; revision++ },
                                onCancel = { deleteId = null },
                            )
                            else -> Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                Action("create person", accent) { editingId = discovery.identity; editName = discovery.title }
                                if (contacts.isNotEmpty()) Action("assign to person", accent) { addingIdentity = discovery.identity }
                                Action("delete", Color(0xFFFF6B62)) { deleteId = discovery.identity }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun ManagerCard(content: @Composable ColumnScope.() -> Unit) = Column(
    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFF111111)).padding(13.dp), content = content,
)

@Composable private fun Action(label: String, color: Color, onClick: () -> Unit) =
    Text(label, color = color, fontSize = 12.sp, modifier = Modifier.clickable(onClick = onClick))

@Composable private fun RenameField(value: String, onValue: (String) -> Unit, accent: Color, onSave: () -> Unit) {
    BasicTextField(value, onValue, singleLine = true, textStyle = TextStyle(color = Color(0xFFF4F1E9), fontSize = 14.sp),
        modifier = Modifier.fillMaxWidth().padding(top = 9.dp).background(Color(0xFF1B1B1B), RoundedCornerShape(9.dp)).padding(10.dp))
    Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) { Action("save", accent, onSave) }
}

@Composable private fun ConfirmDelete(onDelete: () -> Unit, onCancel: () -> Unit) =
    Row(Modifier.padding(top = 9.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Action("delete permanently", Color(0xFFFF6B62), onDelete)
        Action("cancel", Color(0xFF99958E), onCancel)
    }
