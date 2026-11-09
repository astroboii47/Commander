package com.astroboii47.commander

import java.util.Locale

import android.content.Intent
import android.content.ComponentName
import android.app.role.RoleManager
import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import android.os.Bundle
import android.provider.Settings
import android.os.Environment
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import java.io.ByteArrayOutputStream
import androidx.core.app.ActivityCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.SettingsSuggest
import androidx.compose.material.icons.outlined.AccessibilityNew
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
private fun ImportantSourcesSettings(activity: SettingsActivity, accent: Color) {
    Text("Important sources", color = Color(0xFFF4F1E9), fontSize = 17.sp, fontWeight = FontWeight.Bold)
    Text(
        "People, conversations, alert types and apps that automatically enter Important.",
        color = Color(0xFF99958E),
        fontSize = 12.sp,
    )
    Spacer(Modifier.height(8.dp))
    val importantRules = HubImportantSettings.rules.value
    if (importantRules.isEmpty()) {
        Text("No automatic importance rules", color = Color(0xFF77736D), fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp))
    } else {
        importantRules.forEach { rule ->
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .clickable { HubImportantSettings.remove(activity, rule.id) }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        if (rule.kind == "type") "${rule.appName} alert type" else rule.label,
                        color = Color(0xFFF4F1E9),
                        fontSize = 14.sp,
                    )
                    Text("${rule.kind} · ${rule.appName}", color = Color(0xFF99958E), fontSize = 11.sp)
                }
                Text("remove", color = accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun HubTabsSettings(activity: SettingsActivity, accent: Color) {
    val modes = HubSettings.tabVisibility.value
    Text("Hub tabs", color = Color(0xFFF4F1E9), fontSize = 17.sp, fontWeight = FontWeight.Bold)
    Text(
        "Tap a tab to cycle between auto, always and hidden. All and Important are always shown.",
        color = Color(0xFF99958E),
        fontSize = 12.sp,
    )
    Spacer(Modifier.height(10.dp))
    listOf(
        "messages" to "Messages",
        "calls" to "Calls",
        "email" to "Email",
        "finance" to "Finance",
        "tasks" to "Tasks",
        "apps" to "Apps",
        "flagged" to "Flagged",
        "summaries" to "Summaries",
    ).forEach { (id, label) ->
        val mode = modes[id] ?: HubTabVisibility.Auto
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                .clickable { HubSettings.saveTabVisibility(activity, id, mode.next()) }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, color = Color(0xFFF4F1E9), fontSize = 14.sp, modifier = Modifier.weight(1f))
            Text(
                mode.label,
                color = if (mode == HubTabVisibility.Hidden) Color(0xFF77736D) else accent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
    Text("Hidden summaries remain in Android's notification shade.", color = Color(0xFF99958E), fontSize = 11.sp)
}

@Composable
private fun HubAppNameColourSetting(activity: SettingsActivity, accent: Color) {
    val enabled = HubSettings.appNameIconColor.value
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(Modifier.weight(1f)) {
            Text("Colour Hub app names", color = Color(0xFFF4F1E9), fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text("Match each app name to a readable colour sampled from its icon", color = Color(0xFF99958E), fontSize = 12.sp)
        }
        Switch(
            checked = enabled,
            onCheckedChange = { HubSettings.saveAppNameIconColor(activity, it) },
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent),
        )
    }
}

class SettingsActivity : ComponentActivity() {
    internal var setupRevision by mutableIntStateOf(0)
    private var taskerPickerCallback: ((Intent?) -> Unit)? = null
    private var iconPickerPackage: String? = null
    private var iconPickerCallback: (() -> Unit)? = null
    private val iconPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val packageName = iconPickerPackage
        if (uri != null && packageName != null && AppIconOverrides.save(this, packageName, uri)) iconPickerCallback?.invoke()
        iconPickerPackage = null
        iconPickerCallback = null
    }
    private val contactsPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { recreate() }
    private val taskerPicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        taskerPickerCallback?.invoke(result.data)
        taskerPickerCallback = null
    }
    private val settingsExporter = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) SettingsBackup.export(this, uri)
            .onSuccess { Toast.makeText(this, "Commander settings exported", Toast.LENGTH_SHORT).show() }
            .onFailure { Toast.makeText(this, it.message ?: "Settings export failed", Toast.LENGTH_LONG).show() }
    }
    private val settingsImporter = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) SettingsBackup.import(this, uri)
            .onSuccess {
                Toast.makeText(this, "Settings imported. Add API credentials again if needed.", Toast.LENGTH_LONG).show()
                recreate()
            }
            .onFailure { Toast.makeText(this, it.message ?: "Settings import failed", Toast.LENGTH_LONG).show() }
    }
    private val homeWallpaperPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            HomeSettings.saveCustomWallpaper(this, uri)
            recreate()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { AccentSelectionProvider { TodoistSettingsScreen(this) } }
    }

    override fun onResume() {
        super.onResume()
        setupRevision++
    }

    fun pickTaskerTask(onPicked: (TaskerAlias?) -> Unit) {
        val pickerIntent = Intent(Intent.ACTION_CREATE_SHORTCUT).setClassName(
            TaskerAliases.TASKER_PACKAGE,
            "net.dinglisch.android.taskerm.TaskerAppWidgetConfigureShortcut",
        )
        taskerPickerCallback = { data ->
            @Suppress("DEPRECATION")
            val launchIntent = data?.getParcelableExtra<Intent>(Intent.EXTRA_SHORTCUT_INTENT)
            val name = data?.getStringExtra(Intent.EXTRA_SHORTCUT_NAME)?.trim().orEmpty()
            val icon = data?.let(::extractTaskerIcon)
            onPicked(
                if (launchIntent == null) null else TaskerAlias(
                    alias = "!hue",
                    label = name.ifBlank { "Tasker task" },
                    taskName = name.ifBlank { "Tasker task" },
                    intentUri = launchIntent.toUri(Intent.URI_INTENT_SCHEME),
                    iconBase64 = icon,
                ),
            )
        }
        runCatching { taskerPicker.launch(pickerIntent) }.onFailure {
            taskerPickerCallback = null
            onPicked(null)
        }
    }

    fun pickAppIcon(packageName: String, onChanged: () -> Unit) {
        iconPickerPackage = packageName
        iconPickerCallback = onChanged
        iconPicker.launch("image/*")
    }

    fun exportSettings() {
        settingsExporter.launch("commander-settings.json")
    }

    fun requestContactsAccess() {
        contactsPermission.launch(Manifest.permission.READ_CONTACTS)
    }

    fun importSettings() {
        settingsImporter.launch(arrayOf("application/json", "text/plain", "application/octet-stream"))
    }

    fun pickHomeWallpaper() {
        homeWallpaperPicker.launch(arrayOf("image/*"))
    }

    fun requestAssistantRole() {
        val roles = getSystemService(RoleManager::class.java)
        val intent = if (roles?.isRoleAvailable(RoleManager.ROLE_ASSISTANT) == true) {
            roles.createRequestRoleIntent(RoleManager.ROLE_ASSISTANT)
        } else {
            Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
        }
        runCatching { startActivity(intent) }
            .onFailure { Toast.makeText(this, "Could not open Android assistant settings", Toast.LENGTH_SHORT).show() }
    }

    @Suppress("DEPRECATION")
    private fun extractTaskerIcon(data: Intent): String? {
        val direct = data.getParcelableExtra<Bitmap>(Intent.EXTRA_SHORTCUT_ICON)
        if (direct != null) return encodeIcon(direct)

        val reference = data.getParcelableExtra<Intent.ShortcutIconResource>(Intent.EXTRA_SHORTCUT_ICON_RESOURCE)
            ?: return null
        val target = runCatching { createPackageContext(reference.packageName, 0) }.getOrNull() ?: return null
        val resourceId = target.resources.getIdentifier(reference.resourceName, null, reference.packageName)
        if (resourceId == 0) return null
        val drawable = runCatching { target.resources.getDrawable(resourceId, target.theme) }.getOrNull() ?: return null
        return encodeIcon(drawable.toBitmap(width = 128, height = 128))
    }

    private fun encodeIcon(bitmap: Bitmap): String? = runCatching {
        ByteArrayOutputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
            Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
        }
    }.getOrNull()
}

@Composable
private fun SettingsSectionHeader(title: String, icon: ImageVector, accent: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
        Text(title, color = accent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

private data class SettingsAnchors(
    val setup: BringIntoViewRequester,
    val backup: BringIntoViewRequester,
    val ai: BringIntoViewRequester,
    val messages: BringIntoViewRequester,
    val hub: BringIntoViewRequester,
    val home: BringIntoViewRequester,
    val tasks: BringIntoViewRequester,
    val appearance: BringIntoViewRequester,
    val files: BringIntoViewRequester,
    val shortcuts: BringIntoViewRequester,
    val aliases: BringIntoViewRequester,
    val apps: BringIntoViewRequester,
)

private data class SettingsDestination(val label: String, val terms: String, val icon: ImageVector, val anchor: BringIntoViewRequester)

private fun packageEnabledInSecureSetting(activity: SettingsActivity, setting: String): Boolean =
    Settings.Secure.getString(activity.contentResolver, setting).orEmpty()
        .split(':')
        .mapNotNull(ComponentName::unflattenFromString)
        .any { it.packageName == activity.packageName }

@Composable
private fun CommanderSetup(activity: SettingsActivity, accent: Color) {
    @Suppress("UNUSED_VARIABLE") val refreshWhenReturningFromAndroidSettings = activity.setupRevision
    val accessibilityEnabled = packageEnabledInSecureSetting(activity, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
    val notificationAccess = packageEnabledInSecureSetting(activity, "enabled_notification_listeners")
    val contactsAccess = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
    val fileAccess = android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.R || Environment.isExternalStorageManager()

    fun open(action: String) = activity.startActivity(Intent(action))
    val rows = listOf(
        Triple("App info & restricted settings", "Android permissions and battery settings", Icons.Outlined.SettingsSuggest) to {
            activity.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", activity.packageName, null)
            })
        },
        Triple("Accessibility", if (accessibilityEnabled) "Commander accessibility is enabled" else "Required for home typing, shortcuts and Key Control", Icons.Outlined.AccessibilityNew) to {
            open(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        },
        Triple("Notification access", if (notificationAccess) "Hub notification access is enabled" else "Required for Commander Hub and notification replies", Icons.Outlined.Notifications) to {
            open(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        },
        Triple("Contacts", if (contactsAccess) "Contact search is enabled" else "Required for names in @ messages and # calls", Icons.Outlined.Contacts) to {
            if (!contactsAccess) activity.requestContactsAccess()
        },
        Triple("Files & folders", if (fileAccess) "File search access is enabled" else "Required to search all files and folders", Icons.Outlined.FolderOpen) to {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                activity.startActivity(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:${activity.packageName}")))
            }
        },
    )
    val states = listOf<Boolean?>(null, accessibilityEnabled, notificationAccess, contactsAccess, fileAccess)
    rows.forEachIndexed { index, (details, action) ->
        val (title, subtitle, icon) = details
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).clickable(onClick = action)
                .padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(21.dp))
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text(title, color = Color(0xFFF4F1E9), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Color(0xFF99958E), fontSize = 11.sp)
            }
            states[index]?.let { enabled ->
                Icon(if (enabled) Icons.Outlined.CheckCircle else Icons.Outlined.Cancel, null,
                    tint = if (enabled) Color(0xFF55C98A) else Color(0xFFE06565), modifier = Modifier.size(20.dp))
            } ?: Text("open →", color = accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AssistantGestureSetting(activity: SettingsActivity, accent: Color) {
    @Suppress("UNUSED_VARIABLE") val refreshWhenReturning = activity.setupRevision
    val assistantRole = activity.getSystemService(RoleManager::class.java)
    val commanderIsAssistant = assistantRole?.isRoleHeld(RoleManager.ROLE_ASSISTANT) == true
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp))
            .clickable { activity.requestAssistantRole() }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.Search, null, tint = accent, modifier = Modifier.size(21.dp))
        Column(Modifier.padding(start = 12.dp).weight(1f)) {
            Text("Use Commander as digital assistant", color = Color(0xFFF4F1E9), fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(
                if (commanderIsAssistant) "Selected in Android · assistant gestures open Commander Bar"
                else "Choose Commander in Android's assistant picker",
                color = Color(0xFF99958E), fontSize = 11.sp,
            )
        }
        Text(if (commanderIsAssistant) "selected" else "choose →", color = accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
    Text(
        "If holding the navigation bar still opens Circle to Search, turn that gesture off in Android's Circle to Search settings. Some phones keep it separate from the assistant gesture.",
        color = Color(0xFF99958E), fontSize = 11.sp, modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 14.dp),
    )
    AssistantPowerRouteSettings(activity, accent)
    NavigationHoldGestureSetting(activity, accent)
}

@Composable
private fun CommandBarMessageGlyphSetting(
    enabled: Boolean,
    accent: Color,
    onEnabledChange: (Boolean) -> Unit,
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text("Messaging app glyphs", color = Color(0xFFF4F1E9), fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text("Show messaging app glyphs in Commander Bar message rows", color = Color(0xFF99958E), fontSize = 12.sp)
        }
        Switch(
            checked = enabled,
            onCheckedChange = onEnabledChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent),
        )
    }
}

@Composable
private fun SettingsNavigation(activity: SettingsActivity, accent: Color, anchors: SettingsAnchors) {
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    val destinations = listOf(
        SettingsDestination("Setup", "permissions accessibility app info restricted settings notification access contacts files", Icons.Outlined.SettingsSuggest, anchors.setup),
        SettingsDestination("Commander Contacts", "people messages sms whatsapp messenger telegram contacts", Icons.Outlined.Contacts, anchors.messages),
        SettingsDestination("Hub & command bar", "hub history notifications tabs keyboard navigation", Icons.Outlined.Dashboard, anchors.hub),
        SettingsDestination("Home", "homescreen wallpaper darkness blur timeline cards glow", Icons.Outlined.Home, anchors.home),
        SettingsDestination("Appearance", "theme colour glow blur sound day night background", Icons.Outlined.Palette, anchors.appearance),
        SettingsDestination("Shortcuts", "hardware key control tasker automations home typing", Icons.Outlined.Keyboard, anchors.shortcuts),
        SettingsDestination("Tasks", "todoist task quick add", Icons.Outlined.CheckCircleOutline, anchors.tasks),
        SettingsDestination("Files & folders", "files folders search sort", Icons.Outlined.FolderOpen, anchors.files),
        SettingsDestination("Search aliases", "aliases triggers web maps gmail controls", Icons.Outlined.Search, anchors.aliases),
        SettingsDestination("App search", "apps terms icons shortcuts controls", Icons.Outlined.Apps, anchors.apps),
        SettingsDestination("Ask Gemini", "ai gemini api key chatgpt", Icons.Outlined.AutoAwesome, anchors.ai),
        SettingsDestination("Backup & restore", "export import backup settings", Icons.Outlined.Backup, anchors.backup),
    ).filter { destination ->
        (destination.label != "Home" || ModuleSettings.homeEnabled.value) &&
            (destination.label != "Hub & command bar" || ModuleSettings.hubEnabled.value) &&
            (query.isBlank() || "${destination.label} ${destination.terms}".contains(query.trim(), ignoreCase = true))
    }
    Text("commander settings", color = Color(0xFFF4F1E9), fontSize = 30.sp, fontWeight = FontWeight.Bold)
    Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        if (ModuleSettings.homeEnabled.value) Text("open home →", color = accent, fontSize = 13.sp, modifier = Modifier.clickable {
            activity.startActivity(Intent(activity, MainActivity::class.java))
        })
        if (ModuleSettings.hubEnabled.value) Text("open hub →", color = accent, fontSize = 13.sp, modifier = Modifier.clickable {
            activity.startActivity(Intent(activity, HubActivity::class.java))
        })
        Text("open bar →", color = accent, fontSize = 13.sp, modifier = Modifier.clickable {
            activity.startActivity(Intent(activity, OverlayActivity::class.java))
        })
    }
    Spacer(Modifier.height(16.dp))
    BasicTextField(
        value = query,
        onValueChange = { query = it },
        singleLine = true,
        textStyle = TextStyle(color = Color(0xFFF4F1E9), fontSize = 15.sp),
        decorationBox = { inner ->
            if (query.isEmpty()) Text("Find a setting or category…", color = Color(0xFF77736D), fontSize = 15.sp)
            inner()
        },
        modifier = Modifier.fillMaxWidth().background(Color(0xFF111111), RoundedCornerShape(13.dp)).padding(14.dp),
    )
    Spacer(Modifier.height(10.dp))
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        destinations.forEach { destination ->
            Row(
                Modifier.clip(RoundedCornerShape(15.dp)).background(Color(0xFF111111))
                    .border(1.dp, Color(0xFF292929), RoundedCornerShape(15.dp))
                    .clickable {
                        query = ""
                        scope.launch { destination.anchor.bringIntoView() }
                    }.padding(horizontal = 11.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Icon(destination.icon, null, tint = accent, modifier = Modifier.size(16.dp))
                Text(destination.label, color = Color(0xFFF4F1E9), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
    Spacer(Modifier.height(12.dp))
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFF141414))
            .border(1.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .clickable { activity.startActivity(Intent(activity, MessagingContactsActivity::class.java)) }.padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.Contacts, null, tint = accent, modifier = Modifier.size(22.dp))
        Column(Modifier.weight(1f).padding(start = 11.dp)) {
            Text("Commander Contacts", color = Color(0xFFF4F1E9), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("People, app identities and messaging routes", color = Color(0xFF99958E), fontSize = 11.sp)
        }
        Text("→", color = accent, fontSize = 19.sp)
    }
}

@Composable
private fun HubHistorySettings(
    activity: SettingsActivity,
    accent: Color,
    retention: HubHistoryRetention,
    onRetentionChanged: (HubHistoryRetention) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).background(Color(0xFF111111))
            .clickable { activity.startActivity(Intent(activity, HubHistoryActivity::class.java)) }.padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.History, null, tint = accent, modifier = Modifier.size(21.dp))
        Column(Modifier.weight(1f).padding(start = 10.dp)) {
            Text("Notification history", color = Color(0xFFF4F1E9), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Open encrypted Hub history", color = Color(0xFF99958E), fontSize = 11.sp)
        }
        Text("→", color = accent, fontSize = 19.sp)
    }
    Spacer(Modifier.height(16.dp))
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onRetentionChanged(retention.next()) }.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text("Keep notification history", color = Color(0xFFF4F1E9), fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text("Tap to cycle retention. History stays encrypted on this device.", color = Color(0xFF99958E), fontSize = 12.sp)
        }
        Text(retention.label, color = if (retention == HubHistoryRetention.Off) Color(0xFF77736D) else accent,
            fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 10.dp))
    }
}

@Composable
private fun HomeAppearanceSettings(activity: SettingsActivity, accent: Color, anchor: BringIntoViewRequester) {
    val scope = rememberCoroutineScope()
    var mode by remember { mutableStateOf(HomeSettings.wallpaperMode.value) }
    var darkness by remember { mutableStateOf(HomeSettings.wallpaperDarkness.floatValue) }
    var blur by remember { mutableStateOf(HomeSettings.wallpaperBlur.floatValue) }
    var timelineGlow by remember { mutableStateOf(HomeSettings.timelineGlow.floatValue) }
    var cardGlow by remember { mutableStateOf(HomeSettings.cardGlow.floatValue) }
    var showRemaining by remember { mutableStateOf(HomeSettings.showRemaining.value) }
    var wallpaperAccent by remember { mutableStateOf(HomeSettings.wallpaperAccent.value) }
    var wallpaperPanX by remember { mutableStateOf(HomeSettings.wallpaperPanX.floatValue) }
    var wallpaperPanY by remember { mutableStateOf(HomeSettings.wallpaperPanY.floatValue) }
    var wallpaperZoom by remember { mutableStateOf(HomeSettings.wallpaperZoom.floatValue) }
    var timeline12Hour by remember { mutableStateOf(HomeSettings.timeline12Hour.value) }
    var timelineHourlyLabels by remember { mutableStateOf(HomeSettings.timelineHourlyLabels.value) }
    var glyphStyle by remember { mutableStateOf(HomeSettings.glyphStyle.value) }
    var fontStyle by remember { mutableStateOf(HomeSettings.fontStyle.value) }
    var weatherLocation by remember { mutableStateOf(WeatherPreferences.location(activity).name) }
    var weatherQuery by remember { mutableStateOf(weatherLocation) }
    var weatherStatus by remember { mutableStateOf("") }
    var carouselView by remember { mutableStateOf(HomeSettings.carouselView.value) }
    var carouselPitch by remember { mutableStateOf(HomeSettings.carouselPitch.floatValue) }
    var carouselRearOpacity by remember { mutableStateOf(HomeSettings.carouselRearOpacity.floatValue) }

    fun persist() = HomeSettings.save(activity, mode, darkness, blur, timelineGlow, cardGlow, showRemaining)

    SettingsSectionHeader("home", Icons.Outlined.Home, accent, Modifier.bringIntoViewRequester(anchor))
    Spacer(Modifier.height(12.dp))
    Text("Wallpaper", color = Color(0xFFF4F1E9), fontSize = 17.sp, fontWeight = FontWeight.Bold)
    Text("Wallpaper remains visible beneath lightweight glass surfaces", color = Color(0xFF99958E), fontSize = 12.sp)
    Spacer(Modifier.height(8.dp))
    Text(
        "set Commander as default home →",
        color = accent,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.clickable {
            activity.startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
        }.padding(vertical = 7.dp),
    )
    Spacer(Modifier.height(10.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        HomeWallpaperMode.entries.forEach { choice ->
            val selected = mode == choice
            Text(
                choice.label,
                color = if (selected) Color.White else Color(0xFFAAA7A0),
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(11.dp))
                    .background(if (selected) accent else Color(0xFF111111))
                    .clickable {
                        if (choice == HomeWallpaperMode.Custom && HomeSettings.customWallpaperUri.value == null) activity.pickHomeWallpaper()
                        else { mode = choice; persist() }
                    }.padding(vertical = 11.dp),
            )
        }
    }
    Spacer(Modifier.height(9.dp))
    Text(
        if (HomeSettings.customWallpaperUri.value == null) "choose custom wallpaper" else "change custom wallpaper",
        color = accent,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.clickable { activity.pickHomeWallpaper() }.padding(vertical = 7.dp),
    )
    if (HomeSettings.customWallpaperUri.value != null) {
        Text("Wallpaper zoom  ${String.format(Locale.getDefault(), "%.1f", wallpaperZoom)}×", color = Color(0xFFF4F1E9), fontSize = 13.sp)
        Slider(value = wallpaperZoom, onValueChange = { wallpaperZoom = it; HomeSettings.saveWallpaperPosition(activity, wallpaperPanX, wallpaperPanY, wallpaperZoom) }, valueRange = 1f..3f)
        Text("Horizontal position", color = Color(0xFFF4F1E9), fontSize = 13.sp)
        Slider(value = wallpaperPanX, onValueChange = { wallpaperPanX = it; HomeSettings.saveWallpaperPosition(activity, wallpaperPanX, wallpaperPanY, wallpaperZoom) }, valueRange = -1f..1f)
        Text("Vertical position", color = Color(0xFFF4F1E9), fontSize = 13.sp)
        Slider(value = wallpaperPanY, onValueChange = { wallpaperPanY = it; HomeSettings.saveWallpaperPosition(activity, wallpaperPanX, wallpaperPanY, wallpaperZoom) }, valueRange = -1f..1f)
        Text(
            "set this crop as Android Home wallpaper →",
            color = accent, fontSize = 13.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable {
                Toast.makeText(activity, if (HomeSettings.applyCustomToSystemHome(activity)) "Android Home wallpaper updated" else "Could not set Android wallpaper", Toast.LENGTH_SHORT).show()
            }.padding(vertical = 7.dp),
        )
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text("Use wallpaper accent", color = Color(0xFFF4F1E9), fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("Automatically colour Commander from the selected wallpaper", color = Color(0xFF99958E), fontSize = 11.sp)
        }
        Switch(
            checked = wallpaperAccent,
            onCheckedChange = {
                wallpaperAccent = it
                HomeSettings.setWallpaperAccent(activity, it)
            },
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = AccentSettings.color.value),
        )
    }
    Spacer(Modifier.height(8.dp))
    Text("Wallpaper darkness  ${(darkness * 100).toInt()}%", color = Color(0xFFF4F1E9), fontSize = 13.sp)
    Slider(value = darkness, onValueChange = { darkness = it; persist() }, valueRange = .15f..0.85f)
    Text("Wallpaper blur  ${blur.toInt()}", color = Color(0xFFF4F1E9), fontSize = 13.sp)
    Slider(value = blur, onValueChange = { blur = it; persist() }, valueRange = 0f..12f)
    Spacer(Modifier.height(14.dp))
    Text("Weather location", color = Color(0xFFF4F1E9), fontSize = 17.sp, fontWeight = FontWeight.Bold)
    Text("Open-Meteo · no API key required · currently $weatherLocation", color = Color(0xFF99958E), fontSize = 11.sp)
    Spacer(Modifier.height(7.dp))
    BasicTextField(
        value = weatherQuery,
        onValueChange = { weatherQuery = it },
        singleLine = true,
        textStyle = TextStyle(color = Color(0xFFF4F1E9), fontSize = 14.sp),
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF111111)).padding(horizontal = 12.dp, vertical = 11.dp),
    )
    Text(
        if (weatherStatus.isBlank()) "set weather location →" else weatherStatus,
        color = accent, fontSize = 12.sp, fontWeight = FontWeight.Bold,
        modifier = Modifier.clickable(enabled = weatherQuery.isNotBlank()) {
            weatherStatus = "finding location…"
            scope.launch {
                val result = withContext(Dispatchers.IO) { WeatherPreferences.resolveAndSave(activity, weatherQuery) }
                if (result == null) weatherStatus = "location not found"
                else {
                    weatherLocation = result.name
                    weatherQuery = result.name
                    weatherStatus = "saved · refreshes on Home"
                }
            }
        }.padding(vertical = 10.dp),
    )
    Spacer(Modifier.height(8.dp))
    Text("Home typography", color = Color(0xFFF4F1E9), fontSize = 17.sp, fontWeight = FontWeight.Bold)
    Text("Ryoku uses Space Grotesk for UI, Fraunces for display, and mono faces for labels.", color = Color(0xFF99958E), fontSize = 11.sp)
    Spacer(Modifier.height(7.dp))
    HomeFontStyle.entries.forEach { choice ->
        val selected = fontStyle == choice
        Text(
            choice.label,
            color = if (selected) Color.White else Color(0xFFAAA7A0),
            fontSize = 13.sp,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                .background(if (selected) accent.copy(alpha = .72f) else Color(0xFF111111))
                .clickable {
                    fontStyle = choice
                    HomeSettings.setFontStyle(activity, choice)
                }.padding(horizontal = 12.dp, vertical = 10.dp),
        )
        Spacer(Modifier.height(5.dp))
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text("Bold card titles", color = Color(0xFFF4F1E9), fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("Use the selected font's heavier weight for card names only", color = Color(0xFF99958E), fontSize = 11.sp)
        }
        Switch(
            checked = HomeSettings.boldCardTitles.value,
            onCheckedChange = { HomeSettings.setBoldCardTitles(activity, it) },
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent),
        )
    }
    Text("Timeline glow  ${(timelineGlow * 100).toInt()}%", color = Color(0xFFF4F1E9), fontSize = 13.sp)
    Slider(value = timelineGlow, onValueChange = { timelineGlow = it; persist() }, valueRange = .45f..1.65f)
    Text("Card glow  ${(cardGlow * 100).toInt()}%", color = Color(0xFFF4F1E9), fontSize = 13.sp)
    Slider(value = cardGlow, onValueChange = { cardGlow = it; persist() }, valueRange = .45f..1.65f)
    Text("Carousel view", color = Color(0xFFF4F1E9), fontSize = 15.sp, fontWeight = FontWeight.Bold)
    Text(carouselView.description, color = Color(0xFF99958E), fontSize = 11.sp)
    Spacer(Modifier.height(7.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CarouselViewMode.entries.forEach { choice ->
            val selected = carouselView == choice
            Text(
                choice.label,
                color = if (selected) Color.White else Color(0xFFAAA7A0),
                fontSize = 11.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                    .background(if (selected) accent else Color(0xFF111111))
                    .clickable {
                        carouselView = choice
                        HomeSettings.setCarouselView(activity, choice)
                    }
                    .padding(vertical = 10.dp),
            )
        }
    }
    if (carouselView == CarouselViewMode.Elevated) {
        Spacer(Modifier.height(9.dp))
        Text("Elevated pitch  ${String.format(Locale.getDefault(), "%.1f", carouselPitch)}°", color = Color(0xFFF4F1E9), fontSize = 13.sp)
        Slider(
            value = carouselPitch,
            onValueChange = { carouselPitch = it; HomeSettings.setCarouselPitch(activity, it) },
            valueRange = 5f..11f,
        )
    }
    Spacer(Modifier.height(4.dp))
    Text("Rear card opacity  ${(carouselRearOpacity * 100).toInt()}%", color = Color(0xFFF4F1E9), fontSize = 13.sp)
    Text("Focused cards remain at full opacity", color = Color(0xFF99958E), fontSize = 11.sp)
    Slider(
        value = carouselRearOpacity,
        onValueChange = {
            carouselRearOpacity = it
            HomeSettings.setCarouselRearOpacity(activity, it)
        },
        valueRange = .30f..1f,
    )
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text("Floating animation", color = Color(0xFFF4F1E9), fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("Keep the ring subtly alive while idle", color = Color(0xFF99958E), fontSize = 11.sp)
        }
        Switch(checked = HomeSettings.carouselFloating.value, onCheckedChange = { HomeSettings.setCarouselFloating(activity, it) }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent))
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text("Auto-rotate cards", color = Color(0xFFF4F1E9), fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("Move to the next card every 5 seconds", color = Color(0xFF99958E), fontSize = 11.sp)
        }
        Switch(checked = HomeSettings.carouselAutoRotate.value, onCheckedChange = { HomeSettings.setCarouselAutoRotate(activity, it) }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent))
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text("Upcoming timeline labels", color = Color(0xFFF4F1E9), fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("Show upcoming markers and times beneath Now", color = Color(0xFF99958E), fontSize = 11.sp)
        }
        Switch(checked = HomeSettings.timelineUpcomingLabels.value, onCheckedChange = { HomeSettings.setTimelineUpcomingLabels(activity, it) }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent))
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text("Dot-matrix Tasks count", color = Color(0xFFF4F1E9), fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("Turn off to use the Tasks icon from the selected icon pack", color = Color(0xFF99958E), fontSize = 11.sp)
        }
        Switch(checked = HomeSettings.tasksDotCount.value, onCheckedChange = { HomeSettings.setTasksDotCount(activity, it) }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent))
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text("Weather status island", color = Color(0xFFF4F1E9), fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("Hide it to give the remaining islands more room", color = Color(0xFF99958E), fontSize = 11.sp)
        }
        Switch(checked = HomeSettings.weatherIsland.value, onCheckedChange = { HomeSettings.setWeatherIsland(activity, it) }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent))
    }
    Spacer(Modifier.height(10.dp))
    Text("Carousel icon style", color = Color(0xFFF4F1E9), fontSize = 15.sp, fontWeight = FontWeight.Bold)
    Text("Compare Material, Tabler, Remix, Lucide, Phosphor, Iconoir and Hugeicons live", color = Color(0xFF99958E), fontSize = 11.sp)
    Spacer(Modifier.height(7.dp))
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HomeGlyphStyle.entries.chunked(3).forEach { choices ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                choices.forEach { choice ->
                    val selected = glyphStyle == choice
                    Text(
                        choice.label,
                        color = if (selected) Color.White else Color(0xFFAAA7A0),
                        fontSize = 11.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                            .background(if (selected) accent else Color(0xFF111111))
                            .clickable { glyphStyle = choice; HomeSettings.setGlyphStyle(activity, choice) }
                            .padding(vertical = 10.dp),
                    )
                }
                repeat(3 - choices.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
    Spacer(Modifier.height(10.dp))
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text("Reference carousel Home", color = Color(0xFFF4F1E9), fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("A separate Axial-inspired layout; your current Home remains available", color = Color(0xFF99958E), fontSize = 11.sp)
        }
        Switch(checked = HomeSettings.referenceHome.value, onCheckedChange = { HomeSettings.setReferenceHome(activity, it) }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent))
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text("Status islands", color = Color(0xFFF4F1E9), fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("Use the floating glass header; turn off for the original compact header", color = Color(0xFF99958E), fontSize = 11.sp)
        }
        Switch(checked = HomeSettings.statusIslands.value, onCheckedChange = { HomeSettings.setStatusIslands(activity, it) }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent))
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text("Command Deck preview", color = Color(0xFFF4F1E9), fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("Replace the two Home cards with a cylindrical carousel", color = Color(0xFF99958E), fontSize = 11.sp)
        }
        Switch(checked = HomeSettings.commandDeck.value, onCheckedChange = { HomeSettings.setCommandDeck(activity, it) }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent))
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text("Show time left today", color = Color(0xFFF4F1E9), fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("Display the remaining-time countdown in the header", color = Color(0xFF99958E), fontSize = 11.sp)
        }
        Switch(checked = showRemaining, onCheckedChange = { showRemaining = it; persist() }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent))
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text("12-hour timeline labels", color = Color(0xFFF4F1E9), fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("Show 00, 06, 12, 06, 12 across the day map", color = Color(0xFF99958E), fontSize = 11.sp)
        }
        Switch(checked = timeline12Hour, onCheckedChange = { timeline12Hour = it; HomeSettings.setTimeline12Hour(activity, it) }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent))
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text("Hourly timeline labels", color = Color(0xFFF4F1E9), fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("Add dim hour markers between the main six-hour labels", color = Color(0xFF99958E), fontSize = 11.sp)
        }
        Switch(checked = timelineHourlyLabels, onCheckedChange = { timelineHourlyLabels = it; HomeSettings.setTimelineHourlyLabels(activity, it) }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent))
    }
}

@Composable
private fun RaycastCommandBarSetting(activity: SettingsActivity, accent: Color) {
    val enabled = AppearanceSettings.raycastCommandBar.value
    val hints = AppearanceSettings.raycastHints.value
    val nothingBar = AppearanceSettings.nothingBar.value
    Column {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("Nothing-inspired Bar", color = Color(0xFFF4F1E9), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("Monochrome glass, dot-matrix details, and your accent colour", color = Color(0xFF99958E), fontSize = 11.sp)
            }
            Switch(
                checked = nothingBar,
                onCheckedChange = { AppearanceSettings.saveNothingBar(activity, it) },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent),
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("Raycast-style command bar", color = Color(0xFFF4F1E9), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("Show a faded accent Commander mark and simpler search prompt", color = Color(0xFF99958E), fontSize = 11.sp)
            }
            Switch(
                checked = enabled,
                onCheckedChange = { AppearanceSettings.saveRaycastCommandBar(activity, it) },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent),
            )
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("Tappable triggers", color = Color(0xFFF4F1E9), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("Show touch shortcuts floating above the Bar", color = Color(0xFF99958E), fontSize = 11.sp)
            }
            Switch(
                checked = hints,
                onCheckedChange = { AppearanceSettings.saveRaycastHints(activity, it) },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent),
            )
        }
    }
}

@Composable
private fun TodoistConnectionSettings(
    direct: Boolean,
    token: String,
    accent: Color,
    anchor: BringIntoViewRequester,
    onDirectChange: (Boolean) -> Unit,
    onTokenChange: (String) -> Unit,
) {
    SettingsSectionHeader("todoist", Icons.Outlined.CheckCircleOutline, accent, Modifier.bringIntoViewRequester(anchor))
    Spacer(Modifier.height(24.dp))
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text("Add tasks directly", color = Color(0xFFF4F1E9), fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text("Skip Todoist Quick Add confirmation", color = Color(0xFF99958E), fontSize = 13.sp)
        }
        Switch(
            checked = direct,
            onCheckedChange = onDirectChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent, uncheckedBorderColor = Color(0xFF55514C)),
        )
    }
    Spacer(Modifier.height(25.dp))
    Text("Personal API token", color = Color(0xFFF4F1E9), fontSize = 14.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(8.dp))
    BasicTextField(
        value = token,
        onValueChange = onTokenChange,
        visualTransformation = PasswordVisualTransformation(),
        singleLine = true,
        textStyle = TextStyle(color = Color(0xFFF4F1E9), fontSize = 16.sp),
        modifier = Modifier.fillMaxWidth().background(Color(0xFF111111), RoundedCornerShape(13.dp)).padding(15.dp),
    )
    Spacer(Modifier.height(9.dp))
    Text("Todoist → Settings → Integrations → Developer", color = Color(0xFF99958E), fontSize = 12.sp)
}

@Composable
private fun WebSearchSettingsRows(
    webFallback: Boolean,
    webPreviews: Boolean,
    accent: Color,
    onWebFallbackChanged: (Boolean) -> Unit,
    onWebPreviewsChanged: (Boolean) -> Unit,
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text("Fall back to web search", color = Color(0xFFF4F1E9), fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text("When no app matches, show one web-search action.", color = Color(0xFF99958E), fontSize = 12.sp)
        }
        Switch(
            checked = webFallback,
            onCheckedChange = onWebFallbackChanged,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent),
        )
    }
    Spacer(Modifier.height(10.dp))
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text("Web search previews", color = Color(0xFFF4F1E9), fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("Show up to three live Google suggestions after local results are empty", color = Color(0xFF99958E), fontSize = 12.sp)
        }
        Switch(
            checked = webPreviews,
            enabled = webFallback,
            onCheckedChange = onWebPreviewsChanged,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent),
        )
    }
}

@Composable
private fun TodoistSettingsScreen(activity: SettingsActivity) {
    var direct by remember { mutableStateOf(TodoistSettings.directEnabled(activity)) }
    var token by remember { mutableStateOf(TodoistSettings.token(activity)) }
    var saved by remember { mutableStateOf(false) }
    var accentHex by remember { mutableStateOf(AccentSettings.currentHex(activity)) }
    var recentAccentColors by remember { mutableStateOf(AccentSettings.recent(activity)) }
    var showColorPicker by remember { mutableStateOf(false) }
    val initialHsv = remember {
        FloatArray(3).also { android.graphics.Color.colorToHSV(android.graphics.Color.parseColor(accentHex), it) }
    }
    var pickerHue by remember { mutableStateOf(initialHsv[0]) }
    var pickerSaturation by remember { mutableStateOf(initialHsv[1]) }
    var pickerBrightness by remember { mutableStateOf(initialHsv[2]) }
    var colorValid by remember { mutableStateOf(true) }
    var swapDotBang by remember { mutableStateOf(TriggerSettings.swapDotBang.value) }
    var blurStyle by remember { mutableStateOf(AppearanceSettings.blurStyle.value) }
    var singleBlurStyle by remember { mutableStateOf(AppearanceSettings.singleBlurStyle.value) }
    var darkBlurStyle by remember { mutableStateOf(AppearanceSettings.darkBlurStyle.value) }
    var lightBlurStyle by remember { mutableStateOf(AppearanceSettings.lightBlurStyle.value) }
    var pulseConfirmation by remember { mutableStateOf(AppearanceSettings.pulseConfirmation.value) }
    var appGlowMode by remember { mutableStateOf(AppearanceSettings.appGlowMode.value) }
    var listAnimation by remember { mutableStateOf(AppearanceSettings.listAnimation.value) }
    var timeTheme by remember { mutableStateOf(AppearanceSettings.timeTheme.value) }
    var dayStart by remember { mutableStateOf(AppearanceSettings.dayStart.value) }
    var nightStart by remember { mutableStateOf(AppearanceSettings.nightStart.value) }
    var dayStyle by remember { mutableStateOf(AppearanceSettings.dayStyle.value) }
    var nightStyle by remember { mutableStateOf(AppearanceSettings.nightStyle.value) }
    var darkerBackground by remember { mutableStateOf(AppearanceSettings.darkerBackground.value) }
    var backgroundDarkness by remember { mutableStateOf(AppearanceSettings.backgroundDarkness.value) }
    var soundFeedback by remember { mutableStateOf(SoundSettings.enabled.value) }
    var confirmationSound by remember { mutableStateOf(SoundSettings.confirmationStyle.value) }
    var soundVolume by remember { mutableStateOf(SoundSettings.volume.value) }
    var homeTyping by remember { mutableStateOf(HomeTypingSettings.enabled.value) }
    var hardwareShortcutsEnabled by remember { mutableStateOf(HardwareShortcutSettings.enabled.value) }
    var closeKeyControlAfterActivation by remember { mutableStateOf(KeyControlSettings.closeAfterActivation.value) }
    var holdSwitcherMode by remember { mutableStateOf(HardwareShortcutSettings.holdSwitcher.value) }
    var allowHardwareWhileTyping by remember { mutableStateOf(HardwareShortcutSettings.allowWhileTyping.value) }
    val hardwareShortcutRevision by HardwareShortcutCapture.revision
    val capturingHardwareShortcut by HardwareShortcutCapture.target
    var openSingleAppResult by remember { mutableStateOf(AppSearchSettings.openSingleResult.value) }
    var webFallback by remember { mutableStateOf(AppSearchSettings.webFallback.value) }
    var webPreviews by remember { mutableStateOf(AppSearchSettings.webPreviews.value) }
    var aliasSuggestions by remember { mutableStateOf(AppSearchSettings.aliasSuggestions.value) }
    var appShortcutCategory by remember { mutableStateOf(AppSearchSettings.appShortcutCategory.value) }
    var quickControlsCategory by remember { mutableStateOf(AppSearchSettings.quickControlsCategory.value) }
    var holdFirstForAlt by remember { mutableStateOf(HomeTypingSettings.holdFirstForAlt.value) }
    var quickHubNavigation by remember { mutableStateOf(HubSettings.quickKeyboardNavigation.value) }
    var closeHubReplyAfterSending by remember { mutableStateOf(HubSettings.closeReplyAfterSending.value) }
    var showHubNotificationImages by remember { mutableStateOf(HubSettings.showNotificationImages.value) }
    var hubWallpaperGlass by remember { mutableStateOf(HubSettings.wallpaperGlass.value) }
    var tintHubNotificationImages by remember { mutableStateOf(HubSettings.tintNotificationImages.value) }
    var hubNotificationImagesOnRight by remember { mutableStateOf(HubSettings.notificationImagesOnRight.value) }
    var hubHistoryRetention by remember { mutableStateOf(HubSettings.historyRetention.value) }
    var geminiKey by remember { mutableStateOf(GeminiSettings.apiKey(activity)) }
    var directSms by remember { mutableStateOf(SmsSettings.directEnabled(activity)) }
    val contactsGranted = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
    var messengerPhotos by remember { mutableStateOf(MessengerSettings.profilePhotos(activity)) }
    var commandBarMessageGlyphs by remember { mutableStateOf(MessengerSettings.commandBarMessageGlyphs(activity)) }
    var aliases by remember {
        mutableStateOf(AliasSettings.targets.associate { it.id to AliasSettings.alias(activity, it) })
    }
    var appShortcutAlias by remember { mutableStateOf(AppShortcutSettings.alias(activity)) }
    var taskerAliases by remember { mutableStateOf(TaskerAliases.load(activity)) }
    var fileSearchTrigger by remember { mutableStateOf(FileSearchSettings.displayTrigger()) }
    var showFolders by remember { mutableStateOf(FileSearchSettings.showFolders.value) }
    var filesOnlyPrefix by remember { mutableStateOf(FileSearchSettings.filesOnlyPrefix.value) }
    var foldersOnlyPrefix by remember { mutableStateOf(FileSearchSettings.foldersOnlyPrefix.value) }
    var appTermQuery by remember { mutableStateOf("") }
    var appTermValues by remember { mutableStateOf(AppSearchTerms.aliases(activity).mapValues { it.value.joinToString(", ") }) }
    var iconRevision by remember { mutableStateOf(0) }
    val accent = AccentSettings.color.value
    val settingsScroll = rememberScrollState()
    val setupAnchor = remember { BringIntoViewRequester() }
    val backupAnchor = remember { BringIntoViewRequester() }
    val aiAnchor = remember { BringIntoViewRequester() }
    val messagesAnchor = remember { BringIntoViewRequester() }
    val hubAnchor = remember { BringIntoViewRequester() }
    val homeAnchor = remember { BringIntoViewRequester() }
    val tasksAnchor = remember { BringIntoViewRequester() }
    val appearanceAnchor = remember { BringIntoViewRequester() }
    val filesAnchor = remember { BringIntoViewRequester() }
    val shortcutsAnchor = remember { BringIntoViewRequester() }
    val aliasesAnchor = remember { BringIntoViewRequester() }
    val appsAnchor = remember { BringIntoViewRequester() }
    val anchors = SettingsAnchors(setupAnchor, backupAnchor, aiAnchor, messagesAnchor, hubAnchor, homeAnchor, tasksAnchor, appearanceAnchor, filesAnchor, shortcutsAnchor, aliasesAnchor, appsAnchor)
    fun chooseAccent(hex: String) {
        accentHex = hex
        colorValid = true
        val parsed = runCatching { android.graphics.Color.parseColor(hex) }.getOrNull() ?: return
        val hsv = FloatArray(3).also { android.graphics.Color.colorToHSV(parsed, it) }
        pickerHue = hsv[0]; pickerSaturation = hsv[1]; pickerBrightness = hsv[2]
        saved = false
    }
    fun updatePickerHex() {
        val rgb = android.graphics.Color.HSVToColor(floatArrayOf(pickerHue, pickerSaturation, pickerBrightness))
        chooseAccent(String.format("#%06X", 0xFFFFFF and rgb))
    }
    Column(
        Modifier.fillMaxSize().background(Color(0xFF050505)).verticalScroll(settingsScroll)
            .padding(horizontal = 22.dp, vertical = 52.dp),
    ) {
        SettingsNavigation(activity, accent, anchors)
        Spacer(Modifier.height(10.dp))
        ModuleSettingsPanel(activity, accent)
        Spacer(Modifier.height(22.dp))
        SettingsSectionHeader("setup", Icons.Outlined.SettingsSuggest, accent, Modifier.bringIntoViewRequester(setupAnchor))
        Spacer(Modifier.height(8.dp))
        CommanderSetup(activity, accent)
        Spacer(Modifier.height(24.dp))
        SettingsSectionHeader("backup & restore", Icons.Outlined.Backup, accent, Modifier.bringIntoViewRequester(backupAnchor))
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = activity::exportSettings,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF171717)),
                modifier = Modifier.weight(1f),
            ) { Text("Export settings", color = Color(0xFFF4F1E9)) }
            Button(
                onClick = activity::importSettings,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF171717)),
                modifier = Modifier.weight(1f),
            ) { Text("Import settings", color = Color(0xFFF4F1E9)) }
        }
        Text("API credentials and temporary Hub state are not included", color = Color(0xFF99958E), fontSize = 11.sp)
        Spacer(Modifier.height(24.dp))
        SettingsSectionHeader("ask · gemini", Icons.Outlined.AutoAwesome, accent, Modifier.bringIntoViewRequester(aiAnchor))
        Spacer(Modifier.height(10.dp))
        Text("Gemini API key", color = Color(0xFFF4F1E9), fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Text("Used for ? conversations inside Command. ?? still opens ChatGPT.", color = Color(0xFF99958E), fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        BasicTextField(
            value = geminiKey,
            onValueChange = { geminiKey = it; saved = false },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            textStyle = TextStyle(color = Color(0xFFF4F1E9), fontSize = 16.sp),
            modifier = Modifier.fillMaxWidth().background(Color(0xFF111111), RoundedCornerShape(13.dp)).padding(15.dp),
        )
        Text("Stored encrypted on this device · model: ${GeminiSettings.MODEL_LABEL}", color = Color(0xFF99958E), fontSize = 11.sp)
        Spacer(Modifier.height(26.dp))
        SettingsSectionHeader("messages", Icons.Outlined.ChatBubbleOutline, accent, Modifier.bringIntoViewRequester(messagesAnchor))
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("Contact search", color = Color(0xFFF4F1E9), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text(
                    if (contactsGranted) "Allowed for @ messages and # calls" else "Allow names to appear in message and call search",
                    color = Color(0xFF99958E),
                    fontSize = 12.sp,
                )
            }
            Button(
                onClick = activity::requestContactsAccess,
                enabled = !contactsGranted,
                colors = ButtonDefaults.buttonColors(containerColor = accent, disabledContainerColor = Color(0xFF242424)),
            ) { Text(if (contactsGranted) "Allowed" else "Allow", color = Color.White) }
        }
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("Send SMS directly", color = Color(0xFFF4F1E9), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("Send after contact selection without opening Messages", color = Color(0xFF99958E), fontSize = 12.sp)
            }
            Switch(
                checked = directSms,
                onCheckedChange = { enabled ->
                    directSms = enabled
                    SmsSettings.setDirectEnabled(activity, enabled)
                    if (enabled && ActivityCompat.checkSelfPermission(activity, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.SEND_SMS), 4301)
                    }
                },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent),
            )
        }
        Text("Carrier SMS only · your mobile plan may charge for messages", color = Color(0xFF99958E), fontSize = 11.sp)
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("Messenger profile photos", color = Color(0xFFF4F1E9), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("Show cached conversation photos in @ search", color = Color(0xFF99958E), fontSize = 12.sp)
            }
            Switch(
                checked = messengerPhotos,
                onCheckedChange = { messengerPhotos = it; MessengerSettings.setProfilePhotos(activity, it) },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent),
            )
        }
        Text("Photos are cached only when Messenger notifications arrive", color = Color(0xFF99958E), fontSize = 11.sp)
        Spacer(Modifier.height(18.dp))
        CommandBarMessageGlyphSetting(commandBarMessageGlyphs, accent) {
            commandBarMessageGlyphs = it
            MessengerSettings.setCommandBarMessageGlyphs(activity, it)
        }
        Spacer(Modifier.height(16.dp))
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).background(Color(0xFF111111))
                .clickable { activity.startActivity(Intent(activity, MessagingContactsActivity::class.java)) }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Messaging people", color = Color(0xFFF4F1E9), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Assign app conversations to people and manage each destination", color = Color(0xFF99958E), fontSize = 11.sp)
            }
            Text("→", color = accent, fontSize = 19.sp)
        }
        Spacer(Modifier.height(26.dp))
        SettingsSectionHeader("hub & command bar", Icons.Outlined.Dashboard, accent, Modifier.bringIntoViewRequester(hubAnchor))
        Spacer(Modifier.height(12.dp))
        HubHistorySettings(activity, accent, hubHistoryRetention) { next ->
            hubHistoryRetention = next
            HubSettings.saveHistoryRetention(activity, next)
        }
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("Wallpaper glass in Hub", color = Color(0xFFF4F1E9), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("Use the Home wallpaper beneath a dark full-screen blur", color = Color(0xFF99958E), fontSize = 12.sp)
            }
            Switch(
                checked = hubWallpaperGlass,
                onCheckedChange = { hubWallpaperGlass = it; HubSettings.saveWallpaperGlass(activity, it) },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent),
            )
        }
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("Pulse confirmation glow", color = Color(0xFFF4F1E9), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("Light the whole command bar at once; turn off for the left-to-right sweep", color = Color(0xFF99958E), fontSize = 12.sp)
            }
            Switch(
                checked = pulseConfirmation,
                onCheckedChange = { pulseConfirmation = it; saved = false },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent),
            )
        }
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("Notification images in Hub", color = Color(0xFFF4F1E9), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("Show conversation photos or app icons beside notification titles", color = Color(0xFF99958E), fontSize = 12.sp)
            }
            Switch(
                checked = showHubNotificationImages,
                onCheckedChange = {
                    showHubNotificationImages = it
                    HubSettings.saveShowNotificationImages(activity, it)
                },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent),
            )
        }
        Spacer(Modifier.height(18.dp))
        HubAppNameColourSetting(activity, accent)
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("Place Hub images on right", color = Color(0xFFF4F1E9), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("Show notification images beside the timestamp instead of the title", color = Color(0xFF99958E), fontSize = 12.sp)
            }
            Switch(
                checked = hubNotificationImagesOnRight,
                enabled = showHubNotificationImages,
                onCheckedChange = {
                    hubNotificationImagesOnRight = it
                    HubSettings.saveNotificationImagesOnRight(activity, it)
                },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent),
            )
        }
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("Tint profile and app images", color = Color(0xFFF4F1E9), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("Colour full-colour profile and app images with the Hub accent", color = Color(0xFF99958E), fontSize = 12.sp)
            }
            Switch(
                checked = tintHubNotificationImages,
                enabled = showHubNotificationImages,
                onCheckedChange = {
                    tintHubNotificationImages = it
                    HubSettings.saveTintNotificationImages(activity, it)
                },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent),
            )
        }
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("Close reply after sending", color = Color(0xFFF4F1E9), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("Return to the Hub list after a successful reply", color = Color(0xFF99958E), fontSize = 12.sp)
            }
            Switch(
                checked = closeHubReplyAfterSending,
                onCheckedChange = {
                    closeHubReplyAfterSending = it
                    HubSettings.saveCloseReplyAfterSending(activity, it)
                },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent),
            )
        }
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("Quick Hub keyboard navigation", color = Color(0xFFF4F1E9), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("I/K select · J/L categories · O opens · U dismisses", color = Color(0xFF99958E), fontSize = 12.sp)
            }
            Switch(
                checked = quickHubNavigation,
                onCheckedChange = { quickHubNavigation = it; HubSettings.saveQuickKeyboardNavigation(activity, it) },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent),
            )
        }
        Spacer(Modifier.height(24.dp))
        HubTabsSettings(activity, accent)
        Spacer(Modifier.height(24.dp))
        ImportantSourcesSettings(activity, accent)
        Spacer(Modifier.height(24.dp))
        Text("Hardware shortcuts", color = Color(0xFFF4F1E9), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(
            "Launch Commander directly from physical keys without Key Mapper. Disable any matching Key Mapper action after assigning it here.",
            color = Color(0xFF99958E),
            fontSize = 12.sp,
        )
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("Enable hardware shortcuts", color = Color(0xFFF4F1E9), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("Uses Commander's existing accessibility service", color = Color(0xFF99958E), fontSize = 12.sp)
            }
            Switch(
                checked = hardwareShortcutsEnabled,
                onCheckedChange = { enabled ->
                    hardwareShortcutsEnabled = enabled
                    HardwareShortcutSettings.saveEnabled(activity, enabled)
                    if (enabled) activity.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent),
            )
        }
        Spacer(Modifier.height(8.dp))
        HardwareShortcutAction.entries.filterNot { it.isSwitcherNested() }.forEach { action ->
            @Suppress("UNUSED_VARIABLE") val revision = hardwareShortcutRevision
            val binding = HardwareShortcutSettings.binding(activity, action)
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .clickable {
                        HardwareShortcutCapture.begin(action)
                    }
                    .padding(horizontal = 12.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(action.label, color = Color(0xFFF4F1E9), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(
                        if (capturingHardwareShortcut == action) "Press the shortcut key now…"
                        else binding?.displayName() ?: "Not assigned · tap to assign",
                        color = if (capturingHardwareShortcut == action) accent else Color(0xFF99958E),
                        fontSize = 12.sp,
                    )
                }
                if (binding != null) {
                    Text(
                        "clear",
                        color = accent,
                        fontSize = 12.sp,
                        modifier = Modifier.clickable {
                            HardwareShortcutSettings.saveBinding(activity, action, null)
                            HardwareShortcutCapture.bindingChanged()
                            if (capturingHardwareShortcut == action) HardwareShortcutCapture.cancel()
                        }.padding(8.dp),
                    )
                }
            }
            if (action == HardwareShortcutAction.Switcher) {
                val supportsHold = binding?.secondKeyCode != android.view.KeyEvent.KEYCODE_UNKNOWN
                Row(
                    Modifier.fillMaxWidth().padding(start = 28.dp, end = 4.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Hold-to-switch mode", color = if (supportsHold) Color(0xFFF4F1E9) else Color(0xFF77736D), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Hold the first key, tap the second to cycle, then release the first to open", color = Color(0xFF99958E), fontSize = 11.sp)
                    }
                    Switch(
                        checked = holdSwitcherMode && supportsHold,
                        enabled = supportsHold,
                        onCheckedChange = {
                            holdSwitcherMode = it
                            HardwareShortcutSettings.saveHoldSwitcher(activity, it)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent),
                    )
                }
                if (holdSwitcherMode && supportsHold) {
                    listOf(HardwareShortcutAction.SwitcherReverse, HardwareShortcutAction.SwitcherDismiss).forEach { auxiliary ->
                        val auxiliaryBinding = HardwareShortcutSettings.binding(activity, auxiliary)
                        Row(
                            Modifier.fillMaxWidth().padding(start = 28.dp, end = 4.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).clickable { HardwareShortcutCapture.begin(auxiliary) }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                            ) {
                                Text(auxiliary.label, color = Color(0xFFF4F1E9), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    if (capturingHardwareShortcut == auxiliary) "Hold the same first key, then press the new key…"
                                    else auxiliaryBinding?.displayName() ?: "Not assigned · tap to assign",
                                    color = if (capturingHardwareShortcut == auxiliary) accent else Color(0xFF99958E),
                                    fontSize = 11.sp,
                                )
                            }
                            if (auxiliaryBinding != null) Text(
                                "clear", color = accent, fontSize = 12.sp,
                                modifier = Modifier.clickable {
                                    HardwareShortcutSettings.saveBinding(activity, auxiliary, null)
                                    HardwareShortcutCapture.bindingChanged()
                                }.padding(8.dp),
                            )
                        }
                    }
                }
                val browseAction = HardwareShortcutAction.SwitcherBrowse
                val browseBinding = HardwareShortcutSettings.binding(activity, browseAction)
                Row(
                    Modifier.fillMaxWidth().padding(start = 28.dp, end = 4.dp, top = 4.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).clickable { HardwareShortcutCapture.begin(browseAction) }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                    ) {
                        Text(browseAction.label, color = Color(0xFFF4F1E9), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(
                            if (capturingHardwareShortcut == browseAction) "Press the shortcut key now…"
                            else browseBinding?.displayName() ?: "Opens Switcher for navigation and filtering · tap to assign",
                            color = if (capturingHardwareShortcut == browseAction) accent else Color(0xFF99958E),
                            fontSize = 11.sp,
                        )
                    }
                    if (browseBinding != null) Text(
                        "clear", color = accent, fontSize = 12.sp,
                        modifier = Modifier.clickable {
                            HardwareShortcutSettings.saveBinding(activity, browseAction, null)
                            HardwareShortcutCapture.bindingChanged()
                        }.padding(8.dp),
                    )
                }
            }
            if (action == HardwareShortcutAction.KeyControl) {
                Row(
                    Modifier.fillMaxWidth().padding(start = 28.dp, end = 4.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Close after activation", color = Color(0xFFF4F1E9), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Turn on for one action at a time", color = Color(0xFF99958E), fontSize = 11.sp)
                    }
                    Switch(
                        checked = closeKeyControlAfterActivation,
                        onCheckedChange = {
                            closeKeyControlAfterActivation = it
                            KeyControlSettings.saveCloseAfterActivation(activity, it)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent),
                    )
                }
            }
        }
        Text("Only the first key-down is handled. Holding a key cannot repeat the shortcut.", color = Color(0xFF99958E), fontSize = 11.sp)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("Allow shortcuts while typing", color = Color(0xFFF4F1E9), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("Enable Commander shortcuts in text fields and while the on-screen keyboard is visible", color = Color(0xFF99958E), fontSize = 11.sp)
            }
            Switch(
                checked = allowHardwareWhileTyping,
                onCheckedChange = {
                    allowHardwareWhileTyping = it
                    HardwareShortcutSettings.saveAllowWhileTyping(activity, it)
                },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent),
            )
        }
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("Type from home screen", color = Color(0xFFF4F1E9), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("Begin typing on your launcher to open Commander Bar", color = Color(0xFF99958E), fontSize = 12.sp)
            }
            Switch(
                checked = homeTyping,
                onCheckedChange = { enabled ->
                    homeTyping = enabled
                    HomeTypingSettings.save(activity, enabled)
                    saved = false
                    if (enabled) activity.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent),
            )
        }
        Text("When enabled, also turn on ‘Commander home typing’ in Accessibility.", color = Color(0xFF99958E), fontSize = 11.sp)
        Spacer(Modifier.height(18.dp))
        WebSearchSettingsRows(
            webFallback = webFallback,
            webPreviews = webPreviews,
            accent = accent,
            onWebFallbackChanged = {
                webFallback = it
                AppSearchSettings.saveWebFallback(activity, it)
            },
            onWebPreviewsChanged = {
                webPreviews = it
                AppSearchSettings.saveWebPreviews(activity, it)
            },
        )
        Spacer(Modifier.height(14.dp))
        Text("App search categories", color = Color(0xFFF4F1E9), fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Text("Optionally mix their matching actions into normal app results", color = Color(0xFF99958E), fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        listOf(
            Triple("App Shortcuts", appShortcutCategory) { value: Boolean ->
                appShortcutCategory = value
                AppSearchSettings.saveAppShortcutCategory(activity, value)
            },
            Triple("Quick Controls", quickControlsCategory) { value: Boolean ->
                quickControlsCategory = value
                AppSearchSettings.saveQuickControlsCategory(activity, value)
            },
        ).forEach { (label, checked, save) ->
            Row(
                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(label, color = Color(0xFFF4F1E9), fontSize = 14.sp, modifier = Modifier.weight(1f))
                Switch(
                    checked = checked,
                    onCheckedChange = save,
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent),
                )
            }
        }
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("Open the last app result", color = Color(0xFFF4F1E9), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("Automatically open an app when search narrows to one result", color = Color(0xFF99958E), fontSize = 12.sp)
            }
            Switch(
                checked = openSingleAppResult,
                onCheckedChange = {
                    openSingleAppResult = it
                    AppSearchSettings.saveOpenSingleResult(activity, it)
                },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent),
            )
        }
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("Hold first key for Alt", color = Color(0xFFF4F1E9), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("On the home screen, hold the first key to use its Android Alt symbol as a command trigger", color = Color(0xFF99958E), fontSize = 12.sp)
            }
            Switch(
                checked = holdFirstForAlt,
                onCheckedChange = { enabled ->
                    holdFirstForAlt = enabled
                    HomeTypingSettings.saveHoldFirstForAlt(activity, enabled)
                },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent),
            )
        }
        Spacer(Modifier.height(26.dp))
        TodoistConnectionSettings(
            direct = direct,
            token = token,
            accent = accent,
            anchor = tasksAnchor,
            onDirectChange = { direct = it; saved = false },
            onTokenChange = { token = it; saved = false },
        )
        Spacer(Modifier.height(28.dp))
        if (ModuleSettings.homeEnabled.value) {
            HomeAppearanceSettings(activity, accent, homeAnchor)
            Spacer(Modifier.height(28.dp))
        }
        SettingsSectionHeader("appearance", Icons.Outlined.Palette, accent, Modifier.bringIntoViewRequester(appearanceAnchor))
        Spacer(Modifier.height(12.dp))
        Text("Accent colour", color = Color(0xFFF4F1E9), fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("#FF3212", "#FF5A36", "#FFB000", "#62D26F", "#54A8FF", "#B28CFF").forEach { hex ->
                val swatch = Color(android.graphics.Color.parseColor(hex))
                Spacer(
                    Modifier.size(31.dp).background(swatch, RoundedCornerShape(16.dp)).clickable {
                        chooseAccent(hex)
                    },
                )
            }
        }
        if (recentAccentColors.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("recent", color = Color(0xFF99958E), fontSize = 12.sp)
                recentAccentColors.forEach { hex ->
                    Spacer(
                        Modifier.size(31.dp).background(Color(android.graphics.Color.parseColor(hex)), RoundedCornerShape(16.dp))
                            .clickable { chooseAccent(hex) },
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        BasicTextField(
            value = accentHex,
            onValueChange = { accentHex = it; colorValid = true; saved = false },
            singleLine = true,
            textStyle = TextStyle(color = if (colorValid) Color(0xFFF4F1E9) else Color(0xFFFF6B5A), fontSize = 16.sp),
            modifier = Modifier.fillMaxWidth().background(Color(0xFF111111), RoundedCornerShape(13.dp)).padding(15.dp),
        )
        Text("Enter a six-digit hex colour", color = Color(0xFF99958E), fontSize = 12.sp)
        Spacer(Modifier.height(9.dp))
        Text(
            if (showColorPicker) "hide colour picker" else "pick colour visually",
            color = accent,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { showColorPicker = !showColorPicker }.padding(vertical = 7.dp),
        )
        if (showColorPicker) {
            val preview = Color(android.graphics.Color.HSVToColor(floatArrayOf(pickerHue, pickerSaturation, pickerBrightness)))
            Row(Modifier.fillMaxWidth().padding(top = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.size(44.dp).background(preview, RoundedCornerShape(22.dp)))
                Column(Modifier.padding(start = 13.dp).weight(1f)) {
                    Text("Hue", color = Color(0xFFF4F1E9), fontSize = 12.sp)
                    Slider(value = pickerHue, onValueChange = { pickerHue = it; updatePickerHex() }, valueRange = 0f..360f)
                }
            }
            Text("Saturation", color = Color(0xFFF4F1E9), fontSize = 12.sp)
            Slider(value = pickerSaturation, onValueChange = { pickerSaturation = it; updatePickerHex() }, valueRange = 0f..1f)
            Text("Brightness", color = Color(0xFFF4F1E9), fontSize = 12.sp)
            Slider(value = pickerBrightness, onValueChange = { pickerBrightness = it; updatePickerHex() }, valueRange = .12f..1f)
        }
        Spacer(Modifier.height(24.dp))
        Text("Blur appearance", color = Color(0xFFF4F1E9), fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Text("Choose the screen darkness and glass treatment", color = Color(0xFF99958E), fontSize = 12.sp)
        Spacer(Modifier.height(9.dp))
        RaycastCommandBarSetting(activity, accent)
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Use one style for light and dark mode", color = Color(0xFFF4F1E9), fontSize = 14.sp, modifier = Modifier.weight(1f))
            Switch(
                checked = singleBlurStyle,
                onCheckedChange = { singleBlurStyle = it; saved = false },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent),
            )
        }
        @Composable fun blurChoices(label: String, selectedStyle: BlurStyle, onSelect: (BlurStyle) -> Unit) {
            if (label.isNotBlank()) Text(label, color = Color(0xFF99958E), fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp, bottom = 5.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                BlurStyle.entries.forEach { style ->
                    val selected = selectedStyle == style
                    Text(
                        style.name.lowercase(), color = if (selected) Color.White else Color(0xFFAAA7A0), fontSize = 12.sp,
                        modifier = Modifier.weight(1f).background(
                            if (selected) accent else Color(0xFF111111), RoundedCornerShape(10.dp),
                        ).clickable { onSelect(style); saved = false }.padding(vertical = 11.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
        }
        if (singleBlurStyle) blurChoices("", blurStyle) { blurStyle = it }
        else {
            blurChoices("Dark mode", darkBlurStyle) { darkBlurStyle = it }
            blurChoices("Light mode", lightBlurStyle) { lightBlurStyle = it }
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Time-based appearance", color = Color(0xFFF4F1E9), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("Use Commander styles on your own day and night schedule", color = Color(0xFF99958E), fontSize = 11.sp)
            }
            Switch(
                checked = timeTheme,
                onCheckedChange = { timeTheme = it; saved = false },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent),
            )
        }
        if (timeTheme) {
            @Composable fun timeEntry(label: String, value: String, onValue: (String) -> Unit) {
                Row(Modifier.fillMaxWidth().padding(top = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(label, color = Color(0xFFAAA7A0), fontSize = 12.sp, modifier = Modifier.weight(1f))
                    BasicTextField(
                        value = value,
                        onValueChange = { input -> onValue(input.filter { it.isDigit() || it == ':' }.take(5)); saved = false },
                        singleLine = true,
                        textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                        modifier = Modifier.background(Color(0xFF111111), RoundedCornerShape(9.dp)).padding(horizontal = 12.dp, vertical = 9.dp),
                    )
                }
            }
            timeEntry("Day begins", dayStart) { dayStart = it }
            blurChoices("Day style", dayStyle) { dayStyle = it }
            timeEntry("Night begins", nightStart) { nightStart = it }
            blurChoices("Night style", nightStyle) { nightStyle = it }
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Darker background", color = Color(0xFFF4F1E9), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("Adds contrast on devices where background blur is unavailable", color = Color(0xFF99958E), fontSize = 11.sp)
            }
            Switch(
                checked = darkerBackground,
                onCheckedChange = { darkerBackground = it; saved = false },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent),
            )
        }
        if (darkerBackground) {
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Background darkness", color = Color(0xFFAAA7A0), fontSize = 12.sp)
                Text("${(backgroundDarkness * 100).toInt()}%", color = accent, fontSize = 12.sp)
            }
            Slider(value = backgroundDarkness, onValueChange = { backgroundDarkness = it; saved = false }, valueRange = 0f..0.65f)
        }
        Spacer(Modifier.height(28.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("Command sounds", color = Color(0xFFF4F1E9), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("Subtle cues for opening, selection and confirmation", color = Color(0xFF99958E), fontSize = 12.sp)
            }
            Switch(
                checked = soundFeedback,
                onCheckedChange = { soundFeedback = it; SoundSettings.save(activity, it) },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent),
            )
        }
        Text("Automatically silent in Silent or Vibrate mode", color = Color(0xFF99958E), fontSize = 11.sp)
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("UI sound volume", color = Color(0xFFF4F1E9), fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("${(soundVolume * 100).toInt()}%", color = accent, fontSize = 12.sp)
        }
        Slider(
            value = soundVolume,
            onValueChange = { soundVolume = it; SoundSettings.saveVolume(activity, it) },
            valueRange = 0f..2f,
        )
        Spacer(Modifier.height(8.dp))
        Text("Confirmation sound", color = Color(0xFFF4F1E9), fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Text("Tap a sound to select and preview it", color = Color(0xFF99958E), fontSize = 11.sp)
        Spacer(Modifier.height(8.dp))
        ConfirmationSoundStyle.entries.chunked(2).forEach { styles ->
            Row(Modifier.fillMaxWidth().padding(bottom = 7.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                styles.forEach { style ->
                    val selected = confirmationSound == style
                    Text(
                        style.label,
                        color = if (selected) Color.White else Color(0xFFAAA7A0),
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1f).background(
                            if (selected) accent else Color(0xFF111111), RoundedCornerShape(10.dp),
                        ).clickable {
                            confirmationSound = style
                            SoundSettings.saveConfirmationStyle(activity, style)
                            SoundFeedback.play(activity, CommandSound.Confirm)
                        }.padding(horizontal = 9.dp, vertical = 11.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
        }
        Spacer(Modifier.height(28.dp))
        Text("Adaptive app-result glow", color = Color(0xFFF4F1E9), fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Text("Use the selected app icon’s colour. Processing is cached.", color = Color(0xFF99958E), fontSize = 12.sp)
        Spacer(Modifier.height(9.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            AppGlowMode.entries.forEach { mode ->
                val selected = appGlowMode == mode
                Text(
                    mode.name.lowercase(),
                    color = if (selected) Color.White else Color(0xFFAAA7A0),
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f).background(
                        if (selected) accent else Color(0xFF111111), RoundedCornerShape(10.dp),
                    ).clickable { appGlowMode = mode; saved = false }.padding(vertical = 11.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("App-list updates", color = Color(0xFFF4F1E9), fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Text("Search stays immediate; this only changes how new rows settle.", color = Color(0xFF99958E), fontSize = 12.sp)
        Spacer(Modifier.height(9.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            ListAnimationMode.entries.forEach { mode ->
                val selected = listAnimation == mode
                Text(
                    mode.name.lowercase(),
                    color = if (selected) Color.White else Color(0xFFAAA7A0),
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f).background(
                        if (selected) accent else Color(0xFF111111), RoundedCornerShape(10.dp),
                    ).clickable {
                        listAnimation = mode
                        AppearanceSettings.saveListAnimation(activity, mode)
                    }.padding(vertical = 11.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
        Spacer(Modifier.height(28.dp))
        SettingsSectionHeader("files & folders", Icons.Outlined.FolderOpen, accent, Modifier.bringIntoViewRequester(filesAnchor))
        Spacer(Modifier.height(10.dp))
        Text("File-search trigger", color = Color(0xFFF4F1E9), fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Text("Type one character, or use ‘space’. Default: space", color = Color(0xFF99958E), fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        BasicTextField(
            value = fileSearchTrigger,
            onValueChange = { value ->
                val replacement = when {
                    value.isEmpty() -> ""
                    fileSearchTrigger.equals("space", true) && value.length < fileSearchTrigger.length -> ""
                    fileSearchTrigger.equals("space", true) && value.startsWith(fileSearchTrigger) && value.length > fileSearchTrigger.length -> value.last().toString()
                    fileSearchTrigger.length == 1 && value.startsWith(fileSearchTrigger) && value.length > 1 -> value.last().toString()
                    else -> value.take(8)
                }
                fileSearchTrigger = replacement
                if (replacement.isNotBlank()) {
                    FileSearchSettings.save(activity, replacement, showFolders, filesOnlyPrefix, foldersOnlyPrefix)
                }
                saved = false
            },
            singleLine = true,
            textStyle = TextStyle(color = Color(0xFFF4F1E9), fontSize = 16.sp),
            modifier = Modifier.fillMaxWidth().background(Color(0xFF111111), RoundedCornerShape(13.dp)).padding(15.dp),
        )
        Spacer(Modifier.height(15.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("Show folders", color = Color(0xFFF4F1E9), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("Include matching device folders with file results", color = Color(0xFF99958E), fontSize = 12.sp)
            }
            Switch(
                checked = showFolders,
                onCheckedChange = {
                    showFolders = it
                    FileSearchSettings.save(activity, fileSearchTrigger, it, filesOnlyPrefix, foldersOnlyPrefix)
                    saved = false
                },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent),
            )
        }
        Spacer(Modifier.height(15.dp))
        Text("Result filters", color = Color(0xFFF4F1E9), fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Text("Inside file search, type the prefix followed by a space", color = Color(0xFF99958E), fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SettingsMiniField("files only", filesOnlyPrefix, Modifier.weight(1f)) {
                filesOnlyPrefix = it.take(8)
                FileSearchSettings.save(activity, fileSearchTrigger, showFolders, filesOnlyPrefix, foldersOnlyPrefix)
                saved = false
            }
            SettingsMiniField("folders only", foldersOnlyPrefix, Modifier.weight(1f)) {
                foldersOnlyPrefix = it.take(8)
                FileSearchSettings.save(activity, fileSearchTrigger, showFolders, filesOnlyPrefix, foldersOnlyPrefix)
                saved = false
            }
        }
        Spacer(Modifier.height(28.dp))
        SettingsSectionHeader("shortcuts & automations", Icons.Outlined.Keyboard, accent, Modifier.bringIntoViewRequester(shortcutsAnchor))
        Spacer(Modifier.height(10.dp))
        AssistantGestureSetting(activity, accent)
        Text("App shortcut search alias", color = Color(0xFFF4F1E9), fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Text("Search shortcuts declared by installed apps. Default: !as", color = Color(0xFF99958E), fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        BasicTextField(
            value = appShortcutAlias,
            onValueChange = { appShortcutAlias = it.take(16); saved = false },
            singleLine = true,
            textStyle = TextStyle(color = Color(0xFFF4F1E9), fontSize = 16.sp),
            modifier = Modifier.fillMaxWidth().background(Color(0xFF111111), RoundedCornerShape(13.dp)).padding(15.dp),
        )
        Spacer(Modifier.height(22.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("Tasker aliases", color = Color(0xFFF4F1E9), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("Map an alias to an exact Tasker task name", color = Color(0xFF99958E), fontSize = 12.sp)
            }
            Text("+ pick task", color = accent, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    activity.pickTaskerTask { picked ->
                        if (picked != null) {
                            taskerAliases = taskerAliases + picked
                            saved = false
                        }
                    }
                }.padding(8.dp))
        }
        taskerAliases.forEachIndexed { index, item ->
            Column(Modifier.fillMaxWidth().padding(top = 11.dp).background(Color(0xFF0D0D0D), RoundedCornerShape(13.dp)).padding(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsMiniField("alias", item.alias, Modifier.weight(.7f)) { value ->
                        taskerAliases = taskerAliases.toMutableList().also { it[index] = item.copy(alias = value.take(20)) }
                        saved = false
                    }
                    SettingsMiniField("display name", item.label, Modifier.weight(1.3f)) { value ->
                        taskerAliases = taskerAliases.toMutableList().also { it[index] = item.copy(label = value.take(40)) }
                        saved = false
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("selected Tasker task", color = Color(0xFF77736D), fontSize = 9.sp)
                        Text(item.taskName, color = Color(0xFFF4F1E9), fontSize = 13.sp,
                            modifier = Modifier.fillMaxWidth().background(Color(0xFF171717), RoundedCornerShape(9.dp)).padding(10.dp))
                    }
                    Text("remove", color = Color(0xFFFF6B5A), fontSize = 11.sp,
                        modifier = Modifier.clickable {
                            taskerAliases = taskerAliases.filterIndexed { removeIndex, _ -> removeIndex != index }
                            saved = false
                        }.padding(start = 10.dp, top = 12.dp, bottom = 12.dp))
                }
            }
        }
        Text("Tasker supplies the task shortcut directly. No external-access permission is required.", color = Color(0xFF99958E), fontSize = 11.sp,
            modifier = Modifier.padding(top = 8.dp))
        Spacer(Modifier.height(28.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("Swap . with !", color = Color(0xFFF4F1E9), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("Note becomes . and dot aliases become ! aliases", color = Color(0xFF99958E), fontSize = 12.sp)
            }
            Switch(
                checked = swapDotBang,
                onCheckedChange = { swapDotBang = it; saved = false },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent),
            )
        }
        Spacer(Modifier.height(20.dp))
        CommandTriggerSettingsPanel(activity, accent)
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("Show alias suggestions", color = Color(0xFFF4F1E9), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("Show configured aliases when you type their shared trigger", color = Color(0xFF99958E), fontSize = 12.sp)
            }
            Switch(
                checked = aliasSuggestions,
                onCheckedChange = {
                    aliasSuggestions = it
                    AppSearchSettings.saveAliasSuggestions(activity, it)
                },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent),
            )
        }
        Spacer(Modifier.height(20.dp))
        SettingsSectionHeader("search aliases", Icons.Outlined.Search, accent, Modifier.bringIntoViewRequester(aliasesAnchor))
        Spacer(Modifier.height(7.dp))
        Text("Use commas for multiple aliases. Example: .ps, .play. Leave blank to disable.", color = Color(0xFF99958E), fontSize = 12.sp)
        Spacer(Modifier.height(13.dp))
        AliasSettings.targets.forEach { target ->
            Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(target.label, Modifier.weight(1f), color = Color(0xFFF4F1E9), fontSize = 14.sp)
                BasicTextField(
                    value = aliases[target.id].orEmpty(),
                    onValueChange = { value -> aliases = aliases + (target.id to value.take(48)); saved = false },
                    singleLine = true,
                    textStyle = TextStyle(color = Color(0xFFF4F1E9), fontSize = 14.sp),
                    modifier = Modifier.size(width = 148.dp, height = 42.dp)
                        .background(Color(0xFF111111), RoundedCornerShape(11.dp)).padding(horizontal = 11.dp, vertical = 10.dp),
                )
            }
        }
        Spacer(Modifier.height(28.dp))
        SettingsSectionHeader("app search terms", Icons.Outlined.Apps, accent, Modifier.bringIntoViewRequester(appsAnchor))
        Spacer(Modifier.height(7.dp))
        Text("Find an app, then add optional comma-separated terms such as ps, store.", color = Color(0xFF99958E), fontSize = 12.sp)
        Spacer(Modifier.height(12.dp))
        BasicTextField(
            value = appTermQuery,
            onValueChange = { appTermQuery = it },
            singleLine = true,
            textStyle = TextStyle(color = Color(0xFFF4F1E9), fontSize = 15.sp),
            decorationBox = { inner ->
                if (appTermQuery.isEmpty()) Text("Find an app…", color = Color(0xFF77736D), fontSize = 15.sp)
                inner()
            },
            modifier = Modifier.fillMaxWidth().background(Color(0xFF111111), RoundedCornerShape(13.dp)).padding(14.dp),
        )
        Spacer(Modifier.height(9.dp))
        val termApps = AppCatalog.settingsEntries(activity, appTermQuery)
        @Suppress("UNUSED_VARIABLE") val currentIconRevision = iconRevision
        if (termApps.isEmpty()) {
            Text(
                if (appTermQuery.isBlank()) "Type above to add an app." else "No matching apps",
                color = Color(0xFF77736D), fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp),
            )
        }
        termApps.forEach { app ->
            Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f).padding(end = 10.dp)) {
                    Text(app.label, color = Color(0xFFF4F1E9), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(app.packageName, color = Color(0xFF77736D), fontSize = 9.sp, maxLines = 1)
                }
                BasicTextField(
                    value = appTermValues[app.packageName].orEmpty(),
                    onValueChange = { value ->
                        appTermValues = appTermValues + (app.packageName to value.take(64))
                        AppSearchTerms.save(activity, app.packageName, value)
                        saved = false
                    },
                    singleLine = true,
                    textStyle = TextStyle(color = Color(0xFFF4F1E9), fontSize = 13.sp),
                    decorationBox = { inner ->
                        if (appTermValues[app.packageName].isNullOrEmpty()) Text("terms", color = Color(0xFF77736D), fontSize = 13.sp)
                        inner()
                    },
                    modifier = Modifier.size(width = 148.dp, height = 42.dp)
                        .background(Color(0xFF111111), RoundedCornerShape(11.dp)).padding(horizontal = 11.dp, vertical = 10.dp),
                )
                val hasCustomIcon = AppIconOverrides.has(activity, app.packageName)
                Text(
                    if (hasCustomIcon) "reset\nicon" else "choose\nicon",
                    color = if (hasCustomIcon) accent else Color(0xFFAAA7A0),
                    fontSize = 9.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(start = 7.dp).clickable {
                        if (hasCustomIcon) {
                            AppIconOverrides.clear(activity, app.packageName)
                            iconRevision++
                        } else activity.pickAppIcon(app.packageName) { iconRevision++ }
                    }.padding(5.dp),
                )
            }
        }
        Spacer(Modifier.height(22.dp))
        Button(colors = ButtonDefaults.buttonColors(containerColor = accent), onClick = {
            TodoistSettings.setToken(activity, token)
            GeminiSettings.setApiKey(activity, geminiKey)
            TodoistSettings.setDirectEnabled(activity, direct)
            colorValid = AccentSettings.save(activity, accentHex)
            if (colorValid) recentAccentColors = AccentSettings.recent(activity)
            TriggerSettings.save(activity, swapDotBang)
            FileSearchSettings.save(activity, fileSearchTrigger, showFolders, filesOnlyPrefix, foldersOnlyPrefix)
            AppearanceSettings.saveBlurStyles(activity, singleBlurStyle, blurStyle, darkBlurStyle, lightBlurStyle)
            AppearanceSettings.saveTimeTheme(activity, timeTheme, dayStart, nightStart, dayStyle, nightStyle)
            AppearanceSettings.saveBackgroundDarkness(activity, darkerBackground, backgroundDarkness)
            AppearanceSettings.savePulseConfirmation(activity, pulseConfirmation)
            AppearanceSettings.saveAppGlowMode(activity, appGlowMode)
            AppearanceSettings.saveListAnimation(activity, listAnimation)
            HomeTypingSettings.save(activity, homeTyping)
            AppShortcutSettings.save(activity, appShortcutAlias)
            TaskerAliases.save(activity, taskerAliases)
            AliasSettings.targets.forEach { AliasSettings.save(activity, it, aliases[it.id].orEmpty()) }
            appTermValues.forEach { (packageName, terms) -> AppSearchTerms.save(activity, packageName, terms) }
            saved = colorValid
        }) { Text(if (saved) "Saved" else "Save") }
    }
}

@Composable
private fun ModuleSettingsPanel(activity: SettingsActivity, accent: Color) {
    var homeModule by remember { mutableStateOf(ModuleSettings.homeEnabled.value) }
    var hubModule by remember { mutableStateOf(ModuleSettings.hubEnabled.value) }
    var message by remember { mutableStateOf("") }
    SettingsSectionHeader("modules", Icons.Outlined.Dashboard, accent)
    Text("Commander Bar works independently of optional modules. Disabled modules do not appear elsewhere in Commander.", color = Color(0xFF99958E), fontSize = 12.sp)
    if (ModuleSettings.homeAvailable(activity)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Commander Home · experimental", Modifier.weight(1f), color = Color(0xFFF4F1E9), fontSize = 16.sp)
            Switch(checked = homeModule, onCheckedChange = { enabled ->
                if (ModuleSettings.setHome(activity, enabled)) { homeModule = enabled; message = "" }
                else {
                    message = "Choose another default Home app first, then turn Commander Home off."
                    activity.startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
                }
            }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent))
        }
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("Commander Hub", Modifier.weight(1f), color = Color(0xFFF4F1E9), fontSize = 16.sp)
        Switch(checked = hubModule, onCheckedChange = { enabled ->
            ModuleSettings.setHub(activity, enabled)
            hubModule = enabled
        }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent))
    }
    if (message.isNotBlank()) Text(message, color = accent, fontSize = 12.sp)
}

@Composable
private fun CommandTriggerSettingsPanel(activity: SettingsActivity, accent: Color) {
    var triggers by remember { mutableStateOf(CommandKind.entries.filter { it != CommandKind.Apps }.associateWith(TriggerSettings::editableTriggers)) }
    var splitPairTrigger by remember { mutableStateOf(SplitPairSettings.trigger.value) }
    var conflict by remember { mutableStateOf("") }
    SettingsSectionHeader("command triggers", Icons.Outlined.Keyboard, accent)
    Spacer(Modifier.height(7.dp))
    Text("Separate alternatives with commas. Use \\, for a literal comma (e.g. calc, \\,). A trigger can go before or after the query; put a space before it at the end. Clear a field to restore its default.", color = Color(0xFF99958E), fontSize = 12.sp)
    Spacer(Modifier.height(10.dp))
    CommandKind.entries.filter { it != CommandKind.Apps }.forEach { kind ->
        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(kind.label, Modifier.weight(1f), color = Color(0xFFF4F1E9), fontSize = 14.sp)
            BasicTextField(
                value = triggers[kind].orEmpty(),
                onValueChange = { value ->
                    triggers = triggers + (kind to value.take(160))
                    val candidates = TriggerSettings.parseInput(value)
                    val other = triggers.entries.firstOrNull { entry -> entry.key != kind &&
                        TriggerSettings.parseInput(entry.value).any { saved -> candidates.any { it.equals(saved, true) } } }
                    conflict = if (other == null) "" else "${kind.label} conflicts with ${other.key.label}"
                    if (other == null) TriggerSettings.saveTriggers(activity, mapOf(kind to value))
                },
                singleLine = true,
                textStyle = TextStyle(color = Color(0xFFF4F1E9), fontSize = 14.sp),
                modifier = Modifier.size(width = 148.dp, height = 42.dp)
                    .background(Color(0xFF111111), RoundedCornerShape(11.dp)).padding(horizontal = 11.dp, vertical = 10.dp),
            )
        }
    }
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("split two apps", Modifier.weight(1f), color = Color(0xFFF4F1E9), fontSize = 14.sp)
        BasicTextField(
            value = splitPairTrigger,
            onValueChange = { value ->
                splitPairTrigger = value.take(24)
                if (value.isNotBlank()) SplitPairSettings.save(activity, value)
            },
            singleLine = true,
            textStyle = TextStyle(color = Color(0xFFF4F1E9), fontSize = 14.sp),
            modifier = Modifier.size(width = 148.dp, height = 42.dp)
                .background(Color(0xFF111111), RoundedCornerShape(11.dp)).padding(horizontal = 11.dp, vertical = 10.dp),
        )
    }
    if (conflict.isNotBlank()) Text(conflict, color = accent, fontSize = 12.sp)
}

@Composable
private fun SettingsMiniField(
    hint: String,
    value: String,
    modifier: Modifier,
    onValueChange: (String) -> Unit,
) {
    Column(modifier) {
        Text(hint, color = Color(0xFF77736D), fontSize = 9.sp)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(color = Color(0xFFF4F1E9), fontSize = 13.sp),
            modifier = Modifier.fillMaxWidth().background(Color(0xFF171717), RoundedCornerShape(9.dp)).padding(10.dp),
        )
    }
}
