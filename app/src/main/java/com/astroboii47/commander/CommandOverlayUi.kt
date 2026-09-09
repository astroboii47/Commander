package com.astroboii47.commander

import android.content.res.Configuration
import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardReturn
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.outlined.Brightness6
import androidx.compose.material.icons.outlined.FlashlightOn
import androidx.compose.material.icons.outlined.ScreenRotation
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Speaker
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.BrightnessLow
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.ScreenShare
import androidx.compose.material.icons.outlined.Splitscreen
import androidx.compose.material.icons.outlined.StayCurrentPortrait
import androidx.compose.material.icons.outlined.Swipe
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.ViewCarousel
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material.icons.outlined.Slideshow
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.PhoneInTalk
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.distinctUntilChanged

private val OverlayAccent: Color get() = AccentSettings.color.value
private val OverlayMono = FontFamily(
    Font(R.font.courier_prime_regular, FontWeight.Normal),
    Font(R.font.courier_prime_bold, FontWeight.Bold),
)
private val OverlaySans = FontFamily(Font(R.font.inter_variable))
private val OverlaySerif = FontFamily(Font(R.font.instrument_serif_regular))
private const val InputSentinel = '\u2060'
private val ResultViewportBottomPadding = 10.dp

private enum class FileAction(val title: String, val subtitle: String) {
    Open("Open", "open with the default app"),
    OpenWith("Open with…", "choose another app"),
    Reveal("Reveal in folder", "show the containing folder"),
    Preview("Preview", "view without leaving Commander"),
    Info("File info", "type, size, location and date"),
    Delete("Delete", "permanently delete this file"),
}
private enum class AppListMode { All, Recents }
private const val RecentMessagesDestination = "internal:recent-messages"
private enum class TodoistPage(val title: String) {
    Today("tasks"),
    Upcoming("upcoming"),
    Calendar("calendar"),
}

private data class OverlayPalette(
    val panel: Color,
    val tile: Color,
    val text: Color,
    val secondary: Color,
    val border: Color,
)

@Composable
fun CommandOverlayApp(
    engine: CommandEngine,
    onDismiss: () -> Unit,
    initialQuery: String = "",
    initialAskPreview: String? = null,
    initialAskPrompt: String? = null,
    initialAskWaiting: Boolean = false,
    initialSwitcherMode: Boolean = false,
    switcherRequestToken: Int = 0,
) {
    val configuration = LocalConfiguration.current
    val dark = (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    // Keep only complete result rows. The action label and 70 dp command bar
    // are reserved first. Do not derive this from the animated IME inset: on
    // physical-keyboard phones Pastiera can publish several intermediate inset
    // values while its toolbar attaches, which made the result list jump and
    // briefly reorder itself. imePadding still keeps the complete panel clear.
    val maxVisibleResults = (((configuration.screenHeightDp - 180f) / 74f).toInt())
        .coerceIn(1, 5)
    val blurStyle = AppearanceSettings.styleFor(dark)
    val glassBar = blurStyle != BlurStyle.Standard
    val palette = if (glassBar) {
        OverlayPalette(
            // Both layers must remain translucent: the glass tile is drawn over
            // this panel, so an opaque panel would make it look flat grey.
            panel = Color.Transparent,
            tile = Color(0x20F2EEE6),
            text = Color(0xFFF6F3EA),
            secondary = Color(0xFFB8B4AC),
            border = Color(0x26F2EEE6),
        )
    } else {
        OverlayPalette(Color(0x70070707), Color(0x70111111), Color(0xFFF6F3EA), Color(0xFFAAA7A0), Color(0x604F4F4F))
    }
    var query by remember { mutableStateOf(initialQuery) }
    var editorValue by remember { mutableStateOf(TextFieldValue(InputSentinel + initialQuery, TextRange(initialQuery.length + 1))) }
    var notice by remember { mutableStateOf<String?>(null) }
    var revealed by remember { mutableStateOf(false) }
    var settledQuery by remember { mutableStateOf("") }
    var selectedContact by remember { mutableStateOf<ContactResult?>(null) }
    var selectedContactKind by remember { mutableStateOf<CommandKind?>(null) }
    var previousStageQuery by remember { mutableStateOf<String?>(null) }
    var switcherMode by remember { mutableStateOf(initialSwitcherMode) }
    val holdSwitcherState by HoldSwitcherBridge.state
    var handledHoldDismissRequests by remember { mutableStateOf(0) }
    var todoistMode by remember { mutableStateOf(false) }
    var todoistPage by remember { mutableStateOf(TodoistPage.Today) }
    var todoistCalendarDate by remember { mutableStateOf(java.time.LocalDate.now()) }
    var todoistTasks by remember { mutableStateOf(TodoistTasks.cached()) }
    var todoistLoading by remember { mutableStateOf(false) }
    var todoistError by remember { mutableStateOf<String?>(null) }
    var completingTodoistId by remember { mutableStateOf<String?>(null) }
    var fileActionTarget by remember { mutableStateOf<FileResult?>(null) }
    var fileActionPreviousQuery by remember { mutableStateOf("") }
    var fileActionPreviousIndex by remember { mutableStateOf(0) }
    var fileRestoreIndex by remember { mutableStateOf<Int?>(null) }
    var skipNextFileReload by remember { mutableStateOf(false) }
    var fileActionDetail by remember { mutableStateOf<String?>(null) }
    var fileActionPreview by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var fileActionDirectDetail by remember { mutableStateOf(false) }
    var fileDeleteConfirmation by remember { mutableStateOf(false) }
    var switcherAllTasks by remember { mutableStateOf<List<SwitcherTask>>(emptyList()) }
    var switcherExecutingTaskId by remember { mutableStateOf<Int?>(null) }
    val switcherClient = remember { TaskSwitcherClient(engine.context) {} }
    val switcherTasks = remember(switcherAllTasks, query) {
        switcherAllTasks.filter { query.isBlank() || it.label.contains(query, ignoreCase = true) }
    }
    val visibleTodoistTasks = remember(todoistTasks, query, todoistPage, todoistCalendarDate) {
        val needle = query.trim()
        val today = java.time.LocalDate.now().toString()
        todoistTasks.asSequence()
            .filter { task ->
                when (todoistPage) {
                    TodoistPage.Today -> task.dueDate?.take(10)?.let { it <= today } == true
                    TodoistPage.Upcoming -> task.dueDate?.take(10)?.let { it > today } == true
                    TodoistPage.Calendar -> task.dueDate?.take(10) == todoistCalendarDate.toString()
                }
            }
            .filter { task -> needle.isBlank() || listOf(task.content, task.description, task.project, task.labels.joinToString(" ")).any { it.contains(needle, true) } }
            .sortedWith(
                compareBy<TodoistTask> { todoistPage == TodoistPage.Today && it.isOverdue() }
                    .thenBy { it.dueDateTime ?: it.dueDate.orEmpty() }
                    .thenBy { it.content.lowercase() },
            )
            .toList()
    }
    var selectedIndex by remember { mutableStateOf(0) }
    var appListMode by remember { mutableStateOf<AppListMode?>(null) }
    var visibleAppIndices by remember { mutableStateOf<List<Int>>(emptyList()) }
    var visibleFileIndices by remember { mutableStateOf<List<Int>>(emptyList()) }
    val fileListState = rememberLazyListState()
    var executing by remember { mutableStateOf(false) }
    var touchExecutingAppKey by remember { mutableStateOf<String?>(null) }
    var controlFeedback by remember { mutableStateOf<Pair<ControlKind, String>?>(null) }
    var askPreview by remember { mutableStateOf(initialAskPreview) }
    var askWaiting by remember { mutableStateOf(initialAskWaiting) }
    var askTimedOut by remember { mutableStateOf(false) }
    var askMessages by remember { mutableStateOf<List<AskMessage>>(emptyList()) }
    var askLoading by remember { mutableStateOf(false) }
    var editorFocused by remember { mutableStateOf(false) }
    var automaticallyOpenedAppKey by remember { mutableStateOf<String?>(null) }
    var heldOverlayFirstKey by remember { mutableStateOf(android.view.KeyEvent.KEYCODE_UNKNOWN) }
    var executeBlockedUntil by remember { mutableStateOf(0L) }
    val isHomeTypingLaunch = remember { initialQuery.isNotEmpty() && HomeTypingHandoff.isCollecting() }
    val askPrompt = remember(initialAskPrompt) { initialAskPrompt.orEmpty() }
    val parsed = remember(query) { parseCommand(query) }
    val fileSearchQuery = remember(
        parsed.kind,
        parsed.text,
        FileSearchSettings.filesOnlyPrefix.value,
        FileSearchSettings.foldersOnlyPrefix.value,
    ) {
        if (parsed.kind == CommandKind.Files) FileSearchSettings.parseFilter(parsed.text)
        else FileSearchQuery(parsed.text, FileSearchMode.Default)
    }
    val aliasMatch = remember(query) { engine.matchAlias(query) }
    val settingsResults = remember(aliasMatch) {
        if (aliasMatch?.target?.id == "settings") engine.searchSystemSettings(aliasMatch.query) else emptyList()
    }
    val controlResults = remember(aliasMatch) {
        if (aliasMatch?.target?.id == "control") ControlActions.results(aliasMatch.query) else emptyList()
    }
    val discoveredAliasSuggestions = remember(query, appListMode, AppSearchSettings.aliasSuggestions.value) {
        if (appListMode == null) AliasSettings.suggestions(engine.context, query) else emptyList()
    }
    val aliasSuggestions = remember(query, discoveredAliasSuggestions) {
        val plainText = query.isNotBlank() && query.first().isLetterOrDigit() && !query.contains(' ')
        if (plainText) discoveredAliasSuggestions.filter { it.alias.equals(query, ignoreCase = true) }
        else discoveredAliasSuggestions
    }
    val shortcutQuery = remember(query, aliasSuggestions) {
        if (aliasSuggestions.isEmpty()) AppShortcutSettings.query(engine.context, query) else null
    }
    val exactTaskerResults = remember(query) {
        TaskerAliases.exactMatches(engine.context, query)
    }
    val taskerResults = remember(query, aliasSuggestions, exactTaskerResults) {
        exactTaskerResults.ifEmpty {
            if (aliasSuggestions.isEmpty()) TaskerAliases.matches(engine.context, query) else emptyList()
        }
    }
    val calculatorResult = remember(parsed.kind, parsed.text) {
        if (parsed.kind == CommandKind.Calculator) engine.calculatorPreview(parsed.text) else null
    }
    val inGeminiConversation = askMessages.isNotEmpty()
    val activeKind = if (fileActionTarget != null) CommandKind.Files else if (inGeminiConversation) CommandKind.Ask else selectedContactKind ?: parsed.kind
    val fileActions = remember(fileActionTarget, query) {
        val target = fileActionTarget
        if (target == null) emptyList() else FileAction.entries.filter { action ->
            !(target.isDirectory && action in setOf(FileAction.Reveal, FileAction.Preview, FileAction.Delete)) &&
                (query.isBlank() || action.title.contains(query.trim(), ignoreCase = true) || action.subtitle.contains(query.trim(), ignoreCase = true))
        }
    }
    val plainAliasDiscovery = activeKind == CommandKind.Apps && query.isNotBlank() &&
        query.first().isLetterOrDigit() && !query.contains(' ') && aliasSuggestions.isNotEmpty()
    var searchableShortcutResults by remember { mutableStateOf<List<AppShortcutResult>>(emptyList()) }
    val baseApps = remember(query, activeKind, aliasMatch, plainAliasDiscovery, appListMode, switcherMode, todoistMode, searchableShortcutResults,
        AppSearchSettings.quickControlsCategory.value, AppSearchSettings.appShortcutCategory.value) {
        if (switcherMode || todoistMode) emptyList() else
        when (appListMode) {
            AppListMode.All -> engine.allApps(query)
            AppListMode.Recents -> engine.commanderRecents(query)
            null -> if (activeKind == CommandKind.Apps && aliasMatch == null && (aliasSuggestions.isEmpty() || plainAliasDiscovery)) {
                // Web fallback must be decided after every searchable category
                // has contributed. Asking the app catalogue for it here put a
                // web row above valid Quick Controls and app shortcuts.
                val appMatches = engine.searchApps(parsed.text, Int.MAX_VALUE, includeWebFallback = false)
                val controlMatches = if (AppSearchSettings.quickControlsCategory.value && parsed.text.isNotBlank()) {
                    ControlActions.results(parsed.text).map { result ->
                        AppResult(
                            result.label,
                            "internal:quick-control:${result.kind.name}",
                            androidx.core.content.ContextCompat.getDrawable(engine.context, R.drawable.ic_quick_controls),
                            control = result,
                        )
                    }
                } else emptyList()
                val shortcutMatches = if (AppSearchSettings.appShortcutCategory.value) {
                    searchableShortcutResults.map { result ->
                        AppResult(
                            result.label,
                            "internal:app-shortcut:${result.packageName}:${result.id}",
                            result.icon ?: androidx.core.content.ContextCompat.getDrawable(engine.context, R.drawable.ic_app_shortcuts),
                            shortcut = result,
                        )
                    }
                } else emptyList()
                // AppCatalog already ranks exact custom search terms first.
                // Preserve that ordering when optional searchable categories
                // are mixed in, otherwise aliases such as "ps" for Play Store
                // are incorrectly demoted by a second label-only sort.
                val combined = (appMatches + controlMatches + shortcutMatches).distinctBy { it.packageName }
                if (combined.isEmpty()) engine.webFallbackResult(parsed.text)?.let(::listOf).orEmpty()
                else combined
            } else emptyList()
        }
    }
    var apps by remember { mutableStateOf<List<AppResult>>(emptyList()) }
    var rawFiles by remember { mutableStateOf<List<FileResult>>(emptyList()) }
    var fileSortMode by remember { mutableStateOf(FileSortMode.Relevance) }
    val files = remember(rawFiles, fileSortMode) {
        when (fileSortMode) {
            FileSortMode.Relevance -> rawFiles
            FileSortMode.Newest -> rawFiles.sortedByDescending { it.modifiedAtMillis }
            FileSortMode.Oldest -> rawFiles.sortedBy { it.modifiedAtMillis }
        }
    }
    var aliasIcon by remember { mutableStateOf<Drawable?>(null) }
    var shortcutResults by remember { mutableStateOf<List<AppShortcutResult>>(emptyList()) }
    val hasFileAccess = engine.hasFileSearchAccess()
    val hasContactsPermission = engine.hasContactsPermission()
    val contactQuery = if (selectedContact == null) parsed.text else ""
    val hubItems = HubRepository.items
    val messagingContactsRevision by CommanderMessagingContacts.revision
    val recentMessagesMode = activeKind == CommandKind.Message &&
        selectedContact == null && contactQuery.trim().equals("recents", ignoreCase = true)
    val messagingPlatform = if (activeKind == CommandKind.Message && !recentMessagesMode) {
        CommanderMessagingContacts.platformForInput(engine.context, contactQuery)
    } else null
    val scopedContactQuery = if (messagingPlatform != null) {
        contactQuery.trim().substringAfter(' ', "").trim()
    } else contactQuery
    val selectedRecentItem = selectedContact?.hubNotificationKey?.let { key ->
        hubItems.firstOrNull { it.key == key || key in it.sourceKeys }
    }
    val contacts = remember(activeKind, contactQuery, recentMessagesMode, hubItems, messagingContactsRevision) {
        if ((activeKind == CommandKind.Message || activeKind == CommandKind.Call) && selectedContact == null) {
            if (recentMessagesMode) engine.recentReplyContacts()
            else {
                val normal = engine.searchContacts(
                    scopedContactQuery,
                    Int.MAX_VALUE,
                    includeMessenger = activeKind == CommandKind.Message,
                    platform = messagingPlatform,
                )
                val pendingMessages = if (activeKind == CommandKind.Message) {
                    engine.recentReplyContacts().filter { contact ->
                        (messagingPlatform == null || contact.messagingPackageName in messagingPlatform.packageNames) &&
                            (scopedContactQuery.isBlank() || contact.name.contains(scopedContactQuery, true))
                    }
                } else emptyList()
                val needle = contactQuery.trim().lowercase()
                val showRecents = activeKind == CommandKind.Message && needle.isNotEmpty() &&
                    ("recents".startsWith(needle) || "recent messages".startsWith(needle))
                val recents = if (activeKind == CommandKind.Message && (needle.isBlank() || showRecents)) listOf(
                    ContactResult(
                        name = "Recent messages",
                        number = "",
                        label = "pending conversations you can reply to",
                        hubNotificationKey = RecentMessagesDestination,
                    ),
                ) else emptyList()
                val platforms = if (activeKind == CommandKind.Message && messagingPlatform == null) {
                    CommanderMessagingContacts.platformSuggestions(engine.context, contactQuery)
                } else emptyList()
                (recents + platforms + normal + pendingMessages).distinctBy { contact ->
                    contact.messagingIdentity ?: contact.hubNotificationKey ?: contact.messagingPlatformScope
                        ?: contact.commanderMessagingContactId ?: "${contact.name}:${contact.number}"
                }
            }
        } else emptyList()
    }
    val needsContactPermission = !hasContactsPermission && selectedContact == null && when (activeKind) {
        CommandKind.Call -> true
        // Commander contacts and notification-backed recents do not depend on
        // Android's contacts provider. Only offer the permission prompt when
        // neither of those sources produced a message result.
        CommandKind.Message -> !recentMessagesMode && contacts.isEmpty()
        else -> false
    }
    val actionReady = when {
        todoistMode -> visibleTodoistTasks.isNotEmpty()
        fileActionTarget != null -> fileActionDetail != null || fileActions.isNotEmpty()
        switcherMode -> switcherTasks.isNotEmpty()
        plainAliasDiscovery -> aliasSuggestions.isNotEmpty() || apps.isNotEmpty()
        taskerResults.isNotEmpty() -> true
        aliasSuggestions.isNotEmpty() -> true
        aliasMatch?.target?.id == "settings" -> settingsResults.isNotEmpty()
        aliasMatch?.target?.id == "control" -> controlResults.isNotEmpty()
        shortcutQuery != null -> shortcutResults.isNotEmpty()
        taskerResults.isNotEmpty() -> true
        else -> when (activeKind) {
        CommandKind.Apps -> if (aliasMatch != null) aliasMatch.query.isNotBlank() else apps.isNotEmpty()
        CommandKind.Files -> !hasFileAccess || files.isNotEmpty()
        CommandKind.Message -> if (needsContactPermission) true else if (selectedContact != null) query.isNotBlank() else contacts.isNotEmpty()
        CommandKind.Call -> if (needsContactPermission) true else selectedContact != null || contacts.isNotEmpty()
        CommandKind.Ask -> if (inGeminiConversation) query.isNotBlank() && !askLoading
            else parsed.text.isNotBlank() && settledQuery == query
        else -> parsed.text.isNotBlank()
        }
    }
    val selectedFileIsDirectory = activeKind == CommandKind.Files &&
        files.getOrNull(selectedIndex.coerceIn(0, files.lastIndex.coerceAtLeast(0)))?.isDirectory == true
    val showActionAccent = actionReady && (query.isNotBlank() || selectedContact != null)
    val barExecuting = executing && touchExecutingAppKey == null
    val enterColor by animateColorAsState(
        targetValue = if (barExecuting) Color.White else if (showActionAccent) OverlayAccent else palette.secondary,
        animationSpec = tween(150),
        label = "commandEnterColor",
    )
    val glowAlpha by animateFloatAsState(
        targetValue = if (showActionAccent) .34f else if (query.isNotBlank()) .10f else 0f,
        animationSpec = tween(190),
        label = "commandGlow",
    )
    val executionFill by animateFloatAsState(
        targetValue = if (barExecuting) 1f else 0f,
        animationSpec = tween(125),
        label = "commandExecutionFill",
    )
    val executionPulse = remember { Animatable(0f) }
    val usePulseConfirmation = AppearanceSettings.pulseConfirmation.value
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    val homeHandoffText = HomeTypingHandoff.text.value
    val completionVisible = isCompletionNotice(notice)
    val todoistDuePreview = remember(activeKind, parsed.text) {
        if (activeKind == CommandKind.Todo) TodoistNaturalLanguage.detectedDuePreview(parsed.text) else null
    }
    val timerInputPreview = remember(activeKind, parsed.text) {
        if (activeKind == CommandKind.Timer) engine.timerInputPreview(parsed.text) else null
    }
    // Keep the Command surface dark, but allow the blurred scene to remain
    // visible. Success states reuse this exact colour to avoid an opacity jump.
    val commandSurface = palette.tile.copy(alpha = palette.tile.alpha * .24f)
    val commandBarShape = RoundedCornerShape(17.dp)
    // The bar is inset by 6 dp, so the outer radius must also be 6 dp larger
    // for both curves to remain optically concentric.
    val resultsPanelShape = RoundedCornerShape(23.dp)

    LaunchedEffect(switcherMode) {
        if (switcherMode) {
            switcherClient.bind()
            val loadedTasks = withContext(Dispatchers.IO) {
                TaskSwitcherShell.load(engine.context, switcherClient, forceRefresh = holdSwitcherState.active)
            }
            selectedIndex = if (holdSwitcherState.active) {
                loadedTasks.indexOfFirst { it.packageName != holdSwitcherState.sourcePackage }
                    .takeIf { it >= 0 } ?: 0
            } else 0
            switcherAllTasks = loadedTasks
            launch(Dispatchers.IO) { switcherClient.prefetch(loadedTasks.take(3)) }
        }
    }
    DisposableEffect(Unit) { onDispose { switcherClient.unbind() } }

    fun setEditorQuery(value: String) {
        query = value
        editorValue = TextFieldValue(
            InputSentinel + value,
            TextRange(value.length + 1),
        )
    }

    LaunchedEffect(switcherRequestToken) {
        if (switcherRequestToken > 0 && !switcherMode) {
            setEditorQuery("")
            switcherMode = true
            selectedIndex = 0
        }
    }

    LaunchedEffect(query, todoistMode) {
        // Match the rest of Commander's word aliases: show the discovery
        // result while "tasks" is being typed, then enter it on the delimiter.
        if (!todoistMode && query.equals("tasks ", ignoreCase = true)) {
            HomeTypingHandoff.finish()
            previousStageQuery = "tasks"
            todoistMode = true
            todoistPage = TodoistPage.Today
            todoistTasks = TodoistTasks.cached()
            todoistError = null
            setEditorQuery("")
            selectedIndex = 0
        }
    }
    LaunchedEffect(todoistMode, todoistPage) {
        if (todoistMode) {
            // Clearing the alias can rebuild the editor, but requesting focus
            // while it already owns focus restarts some hardware-keyboard IMEs
            // and makes adjustResize visibly move the overlay. Reclaim it only
            // when focus was genuinely lost.
            delay(1)
            if (!editorFocused) focusRequester.requestFocus()
            // Today, Upcoming, and Calendar must reflect changes made in
            // Todoist itself or on another device. Keep the in-memory list
            // for the immediate frame, then fetch once whenever the user
            // enters Tasks or moves between its pages. This is deliberately
            // navigation-driven rather than background polling.
            todoistLoading = true
            withContext(Dispatchers.IO) { TodoistTasks.load(engine.context) }.fold(
                onSuccess = { todoistTasks = it; todoistError = null },
                onFailure = { todoistError = it.message ?: "Could not load Todoist tasks" },
            )
            todoistLoading = false
            selectedIndex = 0
            if (!editorFocused) focusRequester.requestFocus()
        }
    }

    fun isCommandTrigger(symbol: Char): Boolean =
        symbol == TriggerSettings.noteSymbol() ||
            FileSearchSettings.trigger.value == symbol ||
            CommandKind.entries.any { it != CommandKind.Note && it != CommandKind.Files && it.symbol == symbol } ||
            AliasSettings.usesPrefix(engine.context, symbol)

    LaunchedEffect(askWaiting, askPreview) {
        if (askWaiting && askPreview == null) {
            delay(12_000)
            if (askPreview == null) askTimedOut = true
        }
    }

    LaunchedEffect(barExecuting, usePulseConfirmation) {
        if (!usePulseConfirmation) {
            executionPulse.snapTo(0f)
        } else if (barExecuting) {
            executionPulse.snapTo(0f)
            executionPulse.animateTo(1f, tween(115))
            executionPulse.animateTo(.48f, tween(165))
        } else {
            executionPulse.animateTo(0f, tween(140))
        }
    }

    LaunchedEffect(executing, touchExecutingAppKey) {
        if (!executing && touchExecutingAppKey != null) {
            delay(150)
            touchExecutingAppKey = null
        }
    }

    LaunchedEffect(baseApps, maxVisibleResults) {
        val retained = apps.associateBy { it.packageName }
        val stable = baseApps.map { fresh ->
            retained[fresh.packageName]?.takeIf { it.icon != null }?.copy(
                label = fresh.label,
                control = fresh.control,
                shortcut = fresh.shortcut,
            ) ?: fresh
        }
        // Never blank and reload an icon that is already visible. Apart from
        // avoiding needless package-manager work, this keeps an unchanged app
        // in an unchanged slot visually stable while the query evolves.
        apps = stable
        // Results are complete, but icon decoding follows the viewport so a
        // broad one-letter query does not decode every installed app at once.
        val missing = stable.take(maxVisibleResults).filter { it.icon == null }
        if (missing.isNotEmpty()) {
            val loaded = withContext(Dispatchers.IO) { engine.loadVisibleAppIcons(missing) }
                .associateBy { it.packageName }
            apps = stable.map { loaded[it.packageName] ?: it }
        }
    }
    LaunchedEffect(selectedIndex, baseApps) {
        val selected = apps.getOrNull(selectedIndex) ?: return@LaunchedEffect
        if (selected.icon == null) {
            val loaded = withContext(Dispatchers.IO) { engine.loadVisibleAppIcons(listOf(selected)).firstOrNull() }
                ?: return@LaunchedEffect
            apps = apps.mapIndexed { index, app -> if (index == selectedIndex) loaded else app }
        }
    }
    LaunchedEffect(baseApps, visibleAppIndices) {
        val snapshot = apps
        val missingIndices = visibleAppIndices.filter { index -> snapshot.getOrNull(index)?.icon == null }
        if (missingIndices.isEmpty()) return@LaunchedEffect
        val missing = missingIndices.mapNotNull(snapshot::getOrNull)
        val loaded = withContext(Dispatchers.IO) { engine.loadVisibleAppIcons(missing) }
        if (loaded.isEmpty()) return@LaunchedEffect
        val loadedByPosition = missingIndices.zip(loaded).toMap()
        apps = apps.mapIndexed { index, app ->
            loadedByPosition[index]?.takeIf { it.packageName == app.packageName } ?: app
        }
    }
    LaunchedEffect(query, baseApps, activeKind, aliasMatch, shortcutQuery, taskerResults, AppSearchSettings.openSingleResult.value) {
        val app = baseApps.singleOrNull()
        val eligible = AppSearchSettings.openSingleResult.value &&
            query.isNotBlank() && activeKind == CommandKind.Apps && aliasMatch == null &&
            shortcutQuery == null && taskerResults.isEmpty() && app != null &&
            app.control == null && app.shortcut == null &&
            !app.packageName.startsWith("internal:web-search:")
            && app.packageName !in setOf("internal:all-apps", "internal:recent-apps", "internal:switcher")
        if (!eligible) {
            automaticallyOpenedAppKey = null
            return@LaunchedEffect
        }
        val launchKey = "${app.packageName}:$query"
        if (automaticallyOpenedAppKey == launchKey) return@LaunchedEffect
        delay(280)
        automaticallyOpenedAppKey = launchKey
        executing = true
        val error = engine.execute(ParsedCommand(CommandKind.Apps, app.label), app)
        executing = false
        SoundFeedback.play(engine.context, if (error == null) CommandSound.Confirm else CommandSound.Error)
        if (error == null) {
            onDismiss()
        } else notice = error
    }
    LaunchedEffect(aliasMatch?.target?.id, aliasMatch?.target?.packageName) {
        if (aliasMatch?.target?.id != "control") {
            controlFeedback = null
        }
        aliasIcon = if (aliasMatch?.target?.id == "settings") {
            withContext(Dispatchers.IO) { engine.loadSystemSettingsIcon() }
        } else aliasMatch?.target?.packageName?.let { packageName ->
            withContext(Dispatchers.IO) { engine.loadPackageIcon(packageName) }
        }
    }
    LaunchedEffect(fileSearchQuery, activeKind, fileSortMode, maxVisibleResults, fileActionTarget) {
        if (fileActionTarget != null) return@LaunchedEffect
        if (skipNextFileReload) {
            skipNextFileReload = false
            return@LaunchedEffect
        }
        rawFiles = if (activeKind == CommandKind.Files && fileSearchQuery.query.isNotBlank()) {
            delay(140)
            withContext(Dispatchers.IO) {
                val all = engine.searchFiles(fileSearchQuery.query, Int.MAX_VALUE, fileSearchQuery.mode, fileSortMode)
                val previewed = engine.loadFilePreviews(all.take(maxVisibleResults)).associateBy { it.uri }
                all.map { previewed[it.uri] ?: it }
            }
        } else emptyList()
    }
    LaunchedEffect(files, visibleFileIndices) {
        if (activeKind != CommandKind.Files) return@LaunchedEffect
        val missing = visibleFileIndices.mapNotNull(files::getOrNull).filter { !it.isDirectory && it.thumbnail == null }
        if (missing.isEmpty()) return@LaunchedEffect
        val loaded = withContext(Dispatchers.IO) { engine.loadFilePreviews(missing) }
            .filter { it.thumbnail != null }
            .associateBy { it.uri }
        if (loaded.isNotEmpty()) {
            rawFiles = rawFiles.map { loaded[it.uri] ?: it }
        }
    }
    LaunchedEffect(shortcutQuery) {
        shortcutResults = if (shortcutQuery != null) {
            withContext(Dispatchers.IO) { AppShortcutCatalog.search(engine.context, shortcutQuery) }
        } else emptyList()
    }
    LaunchedEffect(query, activeKind, aliasMatch, appListMode, AppSearchSettings.appShortcutCategory.value) {
        searchableShortcutResults = if (
            AppSearchSettings.appShortcutCategory.value && activeKind == CommandKind.Apps &&
            aliasMatch == null && appListMode == null && query.isNotBlank()
        ) withContext(Dispatchers.IO) { AppShortcutCatalog.search(engine.context, query) }
        else emptyList()
    }

    fun playOutcome(error: String?) {
        SoundFeedback.play(engine.context, if (error == null) CommandSound.Confirm else CommandSound.Error)
    }

    fun runTaskerTask(task: TaskerAlias) {
        if (executing) return
        executing = true
        scope.launch {
            delay(135)
            val error = TaskerAliases.run(engine.context, task)
            executing = false
            playOutcome(error)
            notice = error ?: "task started · ${task.label}"
        }
    }

    lateinit var runControl: (ControlResult) -> Unit

    fun launchApp(app: AppResult, fromTouch: Boolean = false) {
        if (fromTouch) touchExecutingAppKey = app.packageName
        app.control?.let {
            runControl(it)
            return
        }
        app.shortcut?.let { shortcut ->
            if (executing) return
            executing = true
            scope.launch {
                delay(90)
                val error = AppShortcutCatalog.launch(engine.context, shortcut)
                executing = false
                playOutcome(error)
                if (error != null) notice = error
            }
            return
        }
        if (app.packageName == "internal:quick-controls") {
            val target = AliasSettings.targets.first { it.id == "control" }
            val alias = AliasSettings.alias(engine.context, target).split(',').firstOrNull()?.trim().orEmpty()
            HomeTypingHandoff.finish()
            SoundFeedback.play(engine.context, CommandSound.Step)
            setEditorQuery("${TriggerSettings.effectiveAlias(alias)} ")
            selectedIndex = 0
            notice = null
            focusRequester.requestFocus()
            return
        }
        if (app.packageName == "internal:app-shortcuts") {
            val alias = TriggerSettings.effectiveAlias(AppShortcutSettings.alias(engine.context))
            HomeTypingHandoff.finish()
            SoundFeedback.play(engine.context, CommandSound.Step)
            setEditorQuery("$alias ")
            selectedIndex = 0
            notice = null
            focusRequester.requestFocus()
            return
        }
        if (app.packageName == "internal:todoist-tasks") {
            HomeTypingHandoff.finish()
            previousStageQuery = query
            todoistMode = true
            todoistPage = TodoistPage.Today
            todoistTasks = TodoistTasks.cached()
            todoistError = null
            SoundFeedback.play(engine.context, CommandSound.Step)
            setEditorQuery("")
            selectedIndex = 0
            notice = null
            focusRequester.requestFocus()
            return
        }
        if (app.packageName == "internal:switcher") {
            HomeTypingHandoff.finish()
            previousStageQuery = query
            switcherMode = true
            appListMode = null
            SoundFeedback.play(engine.context, CommandSound.Step)
            setEditorQuery("")
            selectedIndex = 0
            notice = null
            return
        }
        val mode = when (app.packageName) {
            "internal:all-apps" -> AppListMode.All
            "internal:recent-apps" -> AppListMode.Recents
            else -> null
        }
        if (mode != null) {
            HomeTypingHandoff.finish()
            previousStageQuery = query
            appListMode = mode
            SoundFeedback.play(engine.context, CommandSound.Step)
            setEditorQuery("")
            selectedIndex = 0
            notice = null
            return
        }
        if (executing) return
        executing = true
        scope.launch {
            delay(135)
            val error = engine.execute(ParsedCommand(CommandKind.Apps, app.label), app)
            executing = false
            playOutcome(error)
            if (error != null) notice = error
        }
    }

    fun acceptAliasSuggestion(suggestion: AliasSuggestion, blockDuplicateEnter: Boolean) {
        suggestion.taskerAlias?.let {
            runTaskerTask(it)
            return
        }
        if (blockDuplicateEnter) executeBlockedUntil = android.os.SystemClock.elapsedRealtime() + 300L
        HomeTypingHandoff.finish()
        SoundFeedback.play(engine.context, CommandSound.Step)
        setEditorQuery("${suggestion.alias} ")
        selectedIndex = 0
        notice = null
    }

    runControl = fun(result: ControlResult) {
        // Mixed-in controls arrive from app search without the `!control`
        // alias. Resolve safe no-value actions before deciding whether we need
        // to turn the bar into a value prompt.
        val readyResult = ControlActions.resolveDefault(
            engine.context as android.app.Activity,
            result,
        )
        val needsValue = when (readyResult.kind) {
            ControlKind.Brightness, ControlKind.Volume -> readyResult.percent == null && !readyResult.automatic
            ControlKind.RingerMode, ControlKind.Theme, ControlKind.BatteryProtection -> readyResult.option == null
            else -> false
        }
        if (needsValue) {
            val command = when (readyResult.kind) {
                ControlKind.Brightness -> "brightness"
                ControlKind.Volume -> "volume"
                ControlKind.RingerMode -> "ringer"
                ControlKind.Theme -> "theme"
                ControlKind.BatteryProtection -> "battery"
                else -> ""
            }
            SoundFeedback.play(engine.context, CommandSound.Step)
            setEditorQuery("${aliasMatch?.alias ?: "!control"} $command ")
            selectedIndex = 0
            notice = null
            focusRequester.requestFocus()
            return
        }
        val queryBeforeExecution = query
        if (readyResult.kind == ControlKind.MiniMode) {
            (engine.context as? OverlayActivity)?.preserveQueryForMiniMode(queryBeforeExecution)
        }
        executing = true
        scope.launch {
            delay(90)
            val error = ControlActions.execute(engine.context as android.app.Activity, readyResult)
            executing = false
            playOutcome(error)
            if (error == null) {
                // Controls are deliberately repeatable. Keep the selected result
                // and its query on screen so Enter can toggle it again.
                notice = null
                controlFeedback = readyResult.kind to ControlActions.feedback(
                    engine.context as android.app.Activity,
                    readyResult,
                )
                if (query.length < queryBeforeExecution.length) {
                    setEditorQuery(queryBeforeExecution)
                }
            } else notice = error
        }
    }

    fun openFileActions(file: FileResult) {
        fileActionPreviousQuery = query
        fileActionPreviousIndex = files.indexOfFirst { it.uri == file.uri }.coerceAtLeast(0)
        fileActionTarget = file
        fileActionDetail = null
        fileActionPreview = null
        fileActionDirectDetail = false
        fileDeleteConfirmation = false
        notice = null
        setEditorQuery("")
        selectedIndex = 0
        SoundFeedback.play(engine.context, CommandSound.Step)
    }

    fun openFileQuickPreview(file: FileResult) {
        fileActionPreviousQuery = query
        fileActionPreviousIndex = files.indexOfFirst { it.uri == file.uri }.coerceAtLeast(0)
        fileActionTarget = file
        fileActionPreview = null
        fileActionDetail = "loading preview…"
        fileActionDirectDetail = true
        notice = null
        setEditorQuery("")
        SoundFeedback.play(engine.context, CommandSound.Step)
        scope.launch {
            if (file.isDirectory) {
                fileActionDetail = withContext(Dispatchers.IO) { engine.fileInfo(file) }
            } else {
                fileActionPreview = withContext(Dispatchers.IO) { engine.filePreview(file) }
                fileActionDetail = if (fileActionPreview == null) "Preview is unavailable for this file type\n\n${withContext(Dispatchers.IO) { engine.fileInfo(file) }}" else "preview"
            }
        }
    }

    fun completeTodoistTask(task: TodoistTask) {
        if (completingTodoistId != null) return
        scope.launch {
            completingTodoistId = task.id
            val startedAt = System.currentTimeMillis()
            val error = withContext(Dispatchers.IO) { TodoistTasks.complete(engine.context, task.id) }
            delay((360L - (System.currentTimeMillis() - startedAt)).coerceAtLeast(0L))
            playOutcome(error)
            if (error == null) {
                todoistTasks = todoistTasks.filterNot { it.id == task.id }
                selectedIndex = selectedIndex.coerceAtMost((visibleTodoistTasks.size - 2).coerceAtLeast(0))
            } else todoistError = error
            completingTodoistId = null
        }
    }

    fun moveTodoistPage(direction: Int) {
        val pages = TodoistPage.entries
        val next = (todoistPage.ordinal + direction).coerceIn(0, pages.lastIndex)
        if (next != todoistPage.ordinal) {
            todoistPage = pages[next]
            selectedIndex = 0
            SoundFeedback.play(engine.context, CommandSound.Step)
        }
    }

    fun execute() {
        if (executing) return
        if (todoistMode) {
            visibleTodoistTasks.getOrNull(selectedIndex)?.let { TodoistTasks.open(engine.context, it) }
            return
        }
        fileActionTarget?.let { target ->
            if (fileActionDetail != null) {
                if (fileDeleteConfirmation) {
                    executing = true
                    scope.launch {
                        delay(90)
                        val error = withContext(Dispatchers.IO) { engine.deleteFile(target) }
                        executing = false
                        playOutcome(error)
                        if (error == null) {
                            rawFiles = rawFiles.filterNot { it.uri == target.uri }
                            val restored = fileActionPreviousIndex.coerceAtMost(rawFiles.lastIndex.coerceAtLeast(0))
                            selectedIndex = restored
                            fileRestoreIndex = restored
                            skipNextFileReload = true
                            fileDeleteConfirmation = false
                            fileActionDetail = null
                            fileActionTarget = null
                            setEditorQuery(fileActionPreviousQuery)
                        } else {
                            notice = error
                            fileDeleteConfirmation = false
                            fileActionDetail = null
                        }
                    }
                }
                return
            }
            fileActions.getOrNull(selectedIndex)?.let { action ->
                executing = true
                scope.launch {
                    delay(90)
                    when (action) {
                        FileAction.Open -> engine.openFile(target)
                        FileAction.OpenWith -> engine.openFileWith(target)
                        FileAction.Reveal -> engine.revealFile(target)
                        FileAction.Preview -> {
                            fileActionDirectDetail = false
                            fileActionPreview = withContext(Dispatchers.IO) { engine.filePreview(target) }
                            fileActionDetail = if (fileActionPreview == null) "Preview is unavailable for this file type" else "preview"
                            null
                        }
                        FileAction.Info -> {
                            fileActionDirectDetail = false
                            fileActionDetail = withContext(Dispatchers.IO) { engine.fileInfo(target) }
                            null
                        }
                        FileAction.Delete -> {
                            fileDeleteConfirmation = true
                            fileActionDetail = "Permanently delete ${target.name}?\n\nPress Enter or Right Arrow to confirm.\nLeft Arrow or Back to cancel."
                            null
                        }
                    }.also { error ->
                        executing = false
                        if (action !in setOf(FileAction.Preview, FileAction.Info)) {
                            playOutcome(error)
                            if (error != null) notice = error
                        } else SoundFeedback.play(engine.context, CommandSound.Step)
                    }
                }
            }
            return
        }
        if (switcherMode) {
            switcherTasks.getOrNull(selectedIndex)?.let { task ->
                switcherExecutingTaskId = task.taskId
                SoundFeedback.play(engine.context, CommandSound.Confirm)
                scope.launch {
                    delay(125)
                    val opened = withContext(Dispatchers.IO) { switcherClient.switch(task) }
                    if (opened) onDismiss() else {
                        switcherExecutingTaskId = null
                        notice = "Could not switch to ${task.label}"
                        playOutcome(notice)
                    }
                }
            }
            return
        }
        val now = android.os.SystemClock.elapsedRealtime()
        if (now < executeBlockedUntil) return
        if (plainAliasDiscovery) {
            if (selectedIndex < aliasSuggestions.size) {
                aliasSuggestions.getOrNull(selectedIndex)?.let { acceptAliasSuggestion(it, true) }
            } else {
                apps.getOrNull(selectedIndex - aliasSuggestions.size)?.let(::launchApp)
            }
            return
        }
        if (taskerResults.isNotEmpty()) {
            taskerResults.getOrNull(selectedIndex.coerceIn(0, taskerResults.lastIndex.coerceAtLeast(0)))?.let { task ->
                runTaskerTask(task)
            }
            return
        }
        if (aliasSuggestions.isNotEmpty()) {
            aliasSuggestions.getOrNull(selectedIndex.coerceIn(0, aliasSuggestions.lastIndex))?.let { suggestion ->
                acceptAliasSuggestion(suggestion, true)
            }
            return
        }
        if (aliasMatch?.target?.id == "settings") {
            settingsResults.getOrNull(selectedIndex.coerceIn(0, settingsResults.lastIndex.coerceAtLeast(0)))?.let { result ->
                executing = true
                scope.launch {
                    delay(90)
                    val error = engine.openSystemSetting(result)
                    executing = false
                    playOutcome(error)
                    if (error != null) notice = error
                }
            }
            return
        }
        if (aliasMatch?.target?.id == "control") {
            controlResults.getOrNull(selectedIndex.coerceIn(0, controlResults.lastIndex.coerceAtLeast(0)))?.let { result ->
                runControl(result)
            }
            return
        }
        if (shortcutQuery != null) {
            shortcutResults.getOrNull(selectedIndex.coerceIn(0, shortcutResults.lastIndex.coerceAtLeast(0)))?.let { shortcut ->
                executing = true
                scope.launch {
                    delay(135)
                    val error = AppShortcutCatalog.launch(engine.context, shortcut)
                    executing = false
                    playOutcome(error)
                    if (error == null) {
                        onDismiss()
                    } else notice = error
                }
            }
            return
        }
        if (activeKind == CommandKind.Ask && !query.startsWith("??")) {
            val prompt = if (inGeminiConversation) query.trim() else parsed.text.trim()
            if (prompt.isBlank()) return
            if (!engine.hasGeminiKey()) {
                notice = "Add your Gemini API key in Command settings"
                playOutcome(notice)
                return
            }
            val nextHistory = askMessages + AskMessage("user", prompt)
            askMessages = nextHistory
            setEditorQuery("")
            askLoading = true
            notice = null
            scope.launch {
                engine.askGemini(nextHistory).fold(
                    onSuccess = { answer -> askMessages = nextHistory + AskMessage("model", answer) },
                    onFailure = { error ->
                        notice = error.message ?: "Gemini request failed"
                        playOutcome(notice)
                    },
                )
                askLoading = false
                focusRequester.requestFocus()
            }
            return
        }
        aliasMatch?.let {
            executing = true
            scope.launch {
                delay(135)
                engine.openAlias(it)
                executing = false
                playOutcome(null)
            }
            return
        }
        if (needsContactPermission) {
            engine.requestContactsPermission()
            return
        }
        if ((activeKind == CommandKind.Message || activeKind == CommandKind.Call) && selectedContact == null) {
            contacts.getOrNull(selectedIndex.coerceIn(0, contacts.lastIndex.coerceAtLeast(0)))?.let { contact ->
                if (contact.hubNotificationKey == RecentMessagesDestination) {
                    HomeTypingHandoff.finish()
                    SoundFeedback.play(engine.context, CommandSound.Step)
                    setEditorQuery("@recents")
                    selectedIndex = 0
                    notice = null
                    return
                }
                contact.messagingPlatformScope?.let { scope ->
                    HomeTypingHandoff.finish()
                    SoundFeedback.play(engine.context, CommandSound.Step)
                    setEditorQuery("@$scope ")
                    selectedIndex = 0
                    notice = null
                    focusRequester.requestFocus()
                    return
                }
                previousStageQuery = query
                selectedContact = contact
                selectedContactKind = activeKind
                // Contact selection ends the launcher-to-overlay key handoff.
                // Otherwise its delayed final commit can restore "@name"
                // after this second-stage message field has been cleared.
                HomeTypingHandoff.finish()
                SoundFeedback.play(engine.context, CommandSound.Step)
                setEditorQuery("")
                notice = null
                return
            }
        }
        if (activeKind == CommandKind.Apps) {
            apps.getOrNull(selectedIndex.coerceIn(0, apps.lastIndex.coerceAtLeast(0)))?.let(::launchApp)
            return
        }
        if (activeKind == CommandKind.Files) {
            if (!hasFileAccess) {
                engine.openFileSearchAccess()
                return
            }
            files.getOrNull(selectedIndex.coerceIn(0, files.lastIndex.coerceAtLeast(0)))?.let {
                executing = true
                scope.launch {
                    delay(135)
                    val error = engine.openFile(it)
                    executing = false
                    playOutcome(error)
                    if (error != null) notice = error
                }
            }
            return
        }
        if (activeKind == CommandKind.Message && selectedContact != null) {
            val contact = selectedContact!!
            val body = query.trim()
            executing = true
            scope.launch {
                delay(135)
                if (contact.messagingPlatform.equals("SMS", true) && contact.number.isNotBlank()) {
                    val error = engine.sendMessageContact(contact, body)
                    executing = false
                    playOutcome(error)
                    notice = error ?: if (engine.directSmsEnabled()) "message sent · ${contact.name} · SMS"
                        else "message prepared · ${contact.name} · SMS"
                    return@launch
                }
                if (contact.commanderMessagingContactId != null) {
                    val delivery = engine.sendCommanderMessage(contact, body)
                    executing = false
                    if (delivery.sent) playOutcome(null)
                    else if (delivery.error != null) SoundFeedback.play(engine.context, CommandSound.Error)
                    notice = delivery.status
                    return@launch
                }
                // A successful notification reply returns null. Do not use an
                // Elvis fallback here: null means success, not that this route
                // was unavailable.
                val error = if (contact.hubNotificationKey != null) {
                    engine.replyToRecentMessage(contact.hubNotificationKey, body)
                } else {
                    engine.sendMessageContact(contact, body)
                }
                executing = false
                playOutcome(error)
                if (contact.hubNotificationKey != null) {
                    notice = error ?: "message sent"
                    return@launch
                }
                if (contact.messengerShortcutId != null && error == null) {
                    // Messenger routes the public deep link through a small
                    // trampoline activity. Let that hand-off complete before
                    // finishing our translucent overlay or some vendor task
                    // managers put the launcher in front of the new chat.
                    delay(450)
                    onDismiss()
                    return@launch
                }
                notice = error ?: if (contact.messengerShortcutId != null) "message prepared · Messenger"
                    else if (engine.directSmsEnabled()) "message sent" else "message prepared · ${contact.name}"
            }
            return
        }
        if (activeKind == CommandKind.Call && selectedContact != null) {
            val contact = selectedContact!!
            executing = true
            scope.launch {
                delay(135)
                val error = engine.callContact(contact)
                executing = false
                playOutcome(error)
                notice = error ?: "opening call · ${contact.name}"
            }
            return
        }
        executing = true
        scope.launch {
            // Todoist already has a visible send/glow transition while the
            // network request runs. Do not add latency before starting it.
            if (parsed.kind != CommandKind.Todo) delay(135)
            val error = engine.execute(parsed, apps.getOrNull(selectedIndex.coerceIn(0, apps.lastIndex.coerceAtLeast(0))))
            executing = false
            playOutcome(error)
            notice = error ?: when {
                parsed.kind == CommandKind.Todo && engine.directTodoistEnabled() -> "task added · todoist"
                parsed.kind == CommandKind.Timer -> engine.alarmTimeLabel(parsed.text)?.let { "alarm set · $it" }
                    ?: "timer added · ${timerDurationLabel(parsed.text)}"
                else -> overlayPendingLabel(parsed.kind)
            }
        }
    }

    LaunchedEffect(
        holdSwitcherState.sessionId,
        holdSwitcherState.steps,
        holdSwitcherState.active,
        switcherMode,
        switcherTasks.size,
    ) {
        if (holdSwitcherState.active && switcherMode && switcherTasks.isNotEmpty()) {
            // Recent-task ordering differs between ROMs. Select the first task
            // that is not the app visible when the chord began rather than
            // assuming the current app always occupies a particular index.
            val initialPreviousApp = switcherTasks.indexOfFirst {
                it.packageName != holdSwitcherState.sourcePackage
            }.takeIf { it >= 0 } ?: 0
            selectedIndex = (initialPreviousApp + holdSwitcherState.steps).mod(switcherTasks.size)
        }
    }

    LaunchedEffect(
        holdSwitcherState.sessionId,
        holdSwitcherState.dismissRequests,
        holdSwitcherState.active,
        switcherMode,
        switcherTasks,
    ) {
        if (!holdSwitcherState.active || !switcherMode || switcherTasks.isEmpty()) return@LaunchedEffect
        if (holdSwitcherState.dismissRequests <= handledHoldDismissRequests) return@LaunchedEffect
        handledHoldDismissRequests = holdSwitcherState.dismissRequests
        val task = switcherTasks.getOrNull(selectedIndex) ?: return@LaunchedEffect
        val removed = withContext(Dispatchers.IO) { switcherClient.remove(task) }
        if (removed) {
            switcherAllTasks = switcherAllTasks.filterNot { it.taskId == task.taskId }
            selectedIndex = selectedIndex.coerceAtMost((switcherTasks.size - 2).coerceAtLeast(0))
            SoundFeedback.play(engine.context, CommandSound.Confirm)
        }
    }

    LaunchedEffect(
        holdSwitcherState.sessionId,
        holdSwitcherState.committed,
        switcherMode,
        switcherTasks.size,
    ) {
        if (holdSwitcherState.active && holdSwitcherState.committed && switcherMode && switcherTasks.isNotEmpty()) {
            HoldSwitcherBridge.clearCommit()
            execute()
        }
    }
    fun submitFromEditor(shiftPressed: Boolean = false) {
        if (todoistMode) {
            visibleTodoistTasks.getOrNull(selectedIndex)?.let { task ->
                if (shiftPressed) TodoistTasks.open(engine.context, task) else completeTodoistTask(task)
            }
        } else execute()
    }

    fun emptyBackspace(): Boolean {
        if (todoistMode) {
            todoistMode = false
            todoistError = null
            val previous = previousStageQuery.orEmpty()
            previousStageQuery = null
            setEditorQuery(previous)
            selectedIndex = 0
            return true
        }
        if (fileActionTarget != null) {
            if (fileActionDetail != null) {
                if (fileActionDirectDetail) {
                    selectedIndex = fileActionPreviousIndex
                    fileRestoreIndex = fileActionPreviousIndex
                    skipNextFileReload = true
                    fileActionTarget = null
                    fileActionDetail = null
                    fileActionPreview = null
                    fileActionDirectDetail = false
                    fileDeleteConfirmation = false
                    setEditorQuery(fileActionPreviousQuery)
                } else {
                    fileDeleteConfirmation = false
                    fileActionDetail = null
                    fileActionPreview = null
                    setEditorQuery("")
                    selectedIndex = 0
                }
            } else {
                selectedIndex = fileActionPreviousIndex
                fileRestoreIndex = fileActionPreviousIndex
                skipNextFileReload = true
                fileActionTarget = null
                setEditorQuery(fileActionPreviousQuery)
            }
            return true
        }
        if (switcherMode) {
            switcherMode = false
            switcherAllTasks = emptyList()
            val previousQuery = previousStageQuery.orEmpty()
            previousStageQuery = null
            notice = null
            setEditorQuery(previousQuery)
            selectedIndex = 0
            return true
        }
        if (previousStageQuery != null) {
            val previousQuery = previousStageQuery!!
            previousStageQuery = null
            appListMode = null
            selectedContact = null
            selectedContactKind = null
            notice = null
            setEditorQuery(previousQuery)
        } else {
            onDismiss()
        }
        return true
    }
    BackHandler(enabled = fileActionTarget != null) { emptyBackspace() }
    DisposableEffect(
        switcherMode, switcherTasks.size, todoistMode, visibleTodoistTasks.size,
        fileActionTarget, fileActions.size, fileActionDetail,
    ) {
        val handler: (android.view.KeyEvent) -> Boolean = { event ->
            if (event.action != android.view.KeyEvent.ACTION_DOWN) false
            else if (fileActionTarget != null) when (event.keyCode) {
                android.view.KeyEvent.KEYCODE_DPAD_DOWN -> {
                    if (fileActionDetail == null) {
                        val next = (selectedIndex + 1).coerceAtMost(fileActions.lastIndex.coerceAtLeast(0))
                        if (next != selectedIndex) SoundFeedback.play(engine.context, CommandSound.Step)
                        selectedIndex = next
                    }
                    true
                }
                android.view.KeyEvent.KEYCODE_DPAD_UP -> {
                    if (fileActionDetail == null) {
                        val next = (selectedIndex - 1).coerceAtLeast(0)
                        if (next != selectedIndex) SoundFeedback.play(engine.context, CommandSound.Step)
                        selectedIndex = next
                    }
                    true
                }
                android.view.KeyEvent.KEYCODE_DPAD_LEFT -> emptyBackspace()
                android.view.KeyEvent.KEYCODE_DPAD_RIGHT,
                android.view.KeyEvent.KEYCODE_ENTER,
                android.view.KeyEvent.KEYCODE_DPAD_CENTER -> {
                    if ((fileActionDetail == null && fileActions.isNotEmpty()) || fileDeleteConfirmation) execute()
                    true
                }
                else -> false
            } else if (todoistMode) when (event.keyCode) {
                android.view.KeyEvent.KEYCODE_DPAD_RIGHT -> { moveTodoistPage(1); true }
                android.view.KeyEvent.KEYCODE_DPAD_LEFT -> { moveTodoistPage(-1); true }
                android.view.KeyEvent.KEYCODE_DPAD_DOWN -> {
                    val next = (selectedIndex + 1).coerceAtMost(visibleTodoistTasks.lastIndex.coerceAtLeast(0))
                    if (next != selectedIndex) SoundFeedback.play(engine.context, CommandSound.Step)
                    selectedIndex = next
                    true
                }
                android.view.KeyEvent.KEYCODE_DPAD_UP -> {
                    val next = (selectedIndex - 1).coerceAtLeast(0)
                    if (next != selectedIndex) SoundFeedback.play(engine.context, CommandSound.Step)
                    selectedIndex = next
                    true
                }
                android.view.KeyEvent.KEYCODE_ENTER,
                android.view.KeyEvent.KEYCODE_DPAD_CENTER -> {
                    visibleTodoistTasks.getOrNull(selectedIndex)?.let { task ->
                        if (event.isShiftPressed) TodoistTasks.open(engine.context, task)
                        else completeTodoistTask(task)
                    }
                    true
                }
                else -> false
            } else if (switcherMode) when (event.keyCode) {
                android.view.KeyEvent.KEYCODE_DPAD_RIGHT,
                android.view.KeyEvent.KEYCODE_DPAD_DOWN -> {
                    val next = (selectedIndex + 1).coerceAtMost(switcherTasks.lastIndex.coerceAtLeast(0))
                    if (next != selectedIndex) SoundFeedback.play(engine.context, CommandSound.Step)
                    selectedIndex = next
                    true
                }
                android.view.KeyEvent.KEYCODE_DPAD_LEFT,
                android.view.KeyEvent.KEYCODE_DPAD_UP -> {
                    val next = (selectedIndex - 1).coerceAtLeast(0)
                    if (next != selectedIndex) SoundFeedback.play(engine.context, CommandSound.Step)
                    selectedIndex = next
                    true
                }
                android.view.KeyEvent.KEYCODE_ENTER,
                android.view.KeyEvent.KEYCODE_DPAD_CENTER -> {
                    if (switcherTasks.isNotEmpty()) execute()
                    true
                }
                else -> false
            } else false
        }
        // App, contact, alias, and ordinary file-result navigation stays with
        // the focused BasicTextField below. Only the modes that must own keys
        // outside the editor use the activity bridge.
        if (switcherMode || todoistMode || fileActionTarget != null) {
            SwitcherKeyBridge.handler = handler
            onDispose { if (SwitcherKeyBridge.handler === handler) SwitcherKeyBridge.handler = null }
        } else {
            onDispose { }
        }
    }
    LaunchedEffect(Unit) {
        SoundFeedback.play(engine.context, CommandSound.Open)
        revealed = true
        delay(80)
        focusRequester.requestFocus()
    }
    LaunchedEffect(homeHandoffText) {
        // Empty is meaningful here: it means Backspace removed the first
        // home-launch character while the handoff still owns the keyboard.
        if (isHomeTypingLaunch && HomeTypingHandoff.isCollecting() && query != homeHandoffText) {
            setEditorQuery(homeHandoffText)
        }
        if (isHomeTypingLaunch && HomeTypingHandoff.isCollecting()) {
            // The IME is already visible. Keep only the accessibility
            // handoff alive until the first fast burst or long hold finishes.
            // Each buffered character restarts this effect.
            delay(750)
            if (HomeTypingHandoff.isCollecting() && HomeTypingHandoff.text.value == homeHandoffText) {
                val finalText = HomeTypingHandoff.text.value
                if (query != finalText) setEditorQuery(finalText)
                HomeTypingHandoff.finish()
            }
        }
    }
    LaunchedEffect(query) {
        val restoreIndex = fileRestoreIndex
        if (restoreIndex != null && query == fileActionPreviousQuery) {
            selectedIndex = restoreIndex
            fileRestoreIndex = null
        } else {
            selectedIndex = 0
        }
        if (query.isBlank()) {
            settledQuery = ""
        } else {
            delay(320)
            settledQuery = query
        }
    }
    LaunchedEffect(notice) {
        if (isCompletionNotice(notice)) {
            delay(if (notice?.startsWith("timer added") == true || notice?.startsWith("alarm set") == true) 2_500 else 950)
            onDismiss()
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(
                if (AppearanceSettings.darkerBackground.value)
                    Color.Black.copy(alpha = AppearanceSettings.backgroundDarkness.value)
                else Color.Transparent,
            )
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter,
    ) {
        AnimatedVisibility(
            visible = revealed,
            enter = fadeIn(tween(150)) + slideInVertically(tween(220)) { it / 2 },
            exit = fadeOut(tween(100)) + slideOutVertically(tween(150)) { it / 3 },
        ) {
        Column(
            Modifier
                .fillMaxWidth()
                .widthIn(max = 720.dp)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 14.dp)
                // A clipped transparent panel cuts the command bar's
                // unbounded confirmation bloom at the outer panel edge. The
                // opaque Standard style still uses the clip as before, while
                // Glass and Inverted retain the same shaped background but
                // allow the glow to render through its full falloff.
                .then(if (glassBar) Modifier else Modifier.clip(resultsPanelShape))
                .background(palette.panel, resultsPanelShape)
                .border(
                    1.dp,
                    if (glassBar) Color.Transparent else palette.border,
                    resultsPanelShape,
                )
                .clickable(enabled = false) {}
                .padding(6.dp),
        ) {
            if (askWaiting && askPreview == null) {
                AskWaitingPreview(
                    prompt = askPrompt,
                    palette = palette,
                    timedOut = askTimedOut,
                    onOpenChatGpt = {
                        engine.openChatGpt()
                        onDismiss()
                    },
                )
                Spacer(Modifier.height(10.dp))
            }
            askPreview?.let { response ->
                AskConversationPreview(
                    prompt = askPrompt,
                    response = response,
                    palette = palette,
                    onContinue = {
                        engine.openChatGpt()
                        onDismiss()
                    },
                    onClose = { askPreview = null },
                )
                Spacer(Modifier.height(10.dp))
            }
            if (askMessages.isNotEmpty()) {
                GeminiConversation(
                    messages = askMessages,
                    loading = askLoading,
                    palette = palette,
                )
                Spacer(Modifier.height(10.dp))
            }
            if (isCompletionNotice(notice)) {
                OverlayCompletion(notice!!, palette, commandSurface, focusRequester)
            } else {
            when {
                notice != null -> OverlayNotice(notice!!, palette)
                todoistMode -> OverlayTodoistTasks(
                    tasks = visibleTodoistTasks,
                    calendarTasks = todoistTasks,
                    page = todoistPage,
                    calendarDate = todoistCalendarDate,
                    selectedIndex = selectedIndex,
                    visibleRowCount = if (todoistPage == TodoistPage.Calendar) {
                        (((configuration.screenHeightDp - 400f) / 74f).toInt()).coerceIn(1, 4)
                    } else maxVisibleResults,
                    palette = palette,
                    loading = todoistLoading,
                    error = todoistError,
                    completingTaskId = completingTodoistId,
                    onPreviousPage = { moveTodoistPage(-1) },
                    onNextPage = { moveTodoistPage(1) },
                    onCalendarDate = {
                        todoistCalendarDate = it
                        selectedIndex = 0
                    },
                    onOpen = { TodoistTasks.open(engine.context, it) },
                    onComplete = ::completeTodoistTask,
                )
                switcherMode -> AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(120)) + expandVertically(tween(190), expandFrom = Alignment.Bottom),
                ) {
                    OverlaySwitcherResults(
                        tasks = switcherTasks,
                        selectedIndex = selectedIndex,
                        palette = palette,
                        client = switcherClient,
                        executingTaskId = switcherExecutingTaskId,
                        onClick = { task ->
                            selectedIndex = switcherTasks.indexOf(task).coerceAtLeast(0)
                            execute()
                        },
                        onDismiss = { task ->
                            val before = switcherTasks
                            val removedIndex = before.indexOfFirst { it.taskId == task.taskId }
                            val selectedTaskId = before.getOrNull(selectedIndex)?.taskId
                            switcherAllTasks = switcherAllTasks.filterNot { it.taskId == task.taskId }
                            val after = before.filterNot { it.taskId == task.taskId }
                            selectedIndex = if (selectedTaskId == task.taskId) {
                                removedIndex.coerceIn(0, after.lastIndex.coerceAtLeast(0))
                            } else {
                                after.indexOfFirst { it.taskId == selectedTaskId }
                                    .takeIf { it >= 0 }
                                    ?: selectedIndex.coerceAtMost(after.lastIndex.coerceAtLeast(0))
                            }
                            SoundFeedback.play(engine.context, CommandSound.Confirm)
                        },
                    )
                }
                plainAliasDiscovery -> OverlayDiscoveryResults(
                    suggestions = aliasSuggestions,
                    apps = apps,
                    selectedIndex = selectedIndex,
                    visibleRowCount = maxVisibleResults,
                    palette = palette,
                    controlFeedback = controlFeedback,
                    onVisibleAppIndicesChanged = { visibleAppIndices = it },
                    onSuggestionClick = { acceptAliasSuggestion(it, false) },
                    onAppClick = { launchApp(it, fromTouch = true) },
                    touchExecutingAppKey = touchExecutingAppKey,
                    onAppLongClick = { app ->
                        if (engine.openAppInfo(app)) {
                            SoundFeedback.play(engine.context, CommandSound.Step)
                            onDismiss()
                        }
                    },
                )
                taskerResults.isNotEmpty() -> OverlayTaskerResults(taskerResults, selectedIndex, maxVisibleResults, palette) { task ->
                    runTaskerTask(task)
                }
                aliasSuggestions.isNotEmpty() -> OverlayAliasSuggestions(aliasSuggestions, selectedIndex, maxVisibleResults, palette) { suggestion ->
                    acceptAliasSuggestion(suggestion, false)
                }
                aliasMatch?.target?.id == "settings" -> OverlaySettingsResults(
                    settingsResults,
                    selectedIndex,
                    maxVisibleResults,
                    aliasIcon,
                    palette,
                ) { result ->
                    executing = true
                    scope.launch {
                        delay(90)
                        val error = engine.openSystemSetting(result)
                        executing = false
                        playOutcome(error)
                        if (error != null) notice = error
                    }
                }
                aliasMatch?.target?.id == "control" -> OverlayControlResults(
                    controlResults, selectedIndex, maxVisibleResults, palette, controlFeedback,
                ) { result ->
                    selectedIndex = controlResults.indexOf(result).coerceAtLeast(0)
                    runControl(result)
                }
                aliasMatch != null -> OverlayAliasPreview(aliasMatch, aliasIcon, palette, ::execute)
                activeKind == CommandKind.Calculator && calculatorResult != null -> OverlayCalculatorResult(calculatorResult, palette, ::execute)
                shortcutQuery != null -> OverlayShortcutResults(shortcutResults, selectedIndex, maxVisibleResults, palette) { shortcut ->
                    executing = true
                    scope.launch {
                        delay(135)
                        val error = AppShortcutCatalog.launch(engine.context, shortcut)
                        executing = false
                        playOutcome(error)
                        if (error == null) {
                            onDismiss()
                        } else notice = error
                    }
                }
                activeKind == CommandKind.Apps && (query.isNotBlank() || appListMode != null) -> OverlayAppResults(
                    apps = apps,
                    selectedIndex = selectedIndex,
                    visibleRowCount = maxVisibleResults,
                    palette = palette,
                    controlFeedback = controlFeedback,
                    touchExecutingAppKey = touchExecutingAppKey,
                    onVisibleIndicesChanged = { visibleAppIndices = it },
                    onClick = { launchApp(it, fromTouch = true) },
                    onLongClick = { app ->
                        if (engine.openAppInfo(app)) {
                            SoundFeedback.play(engine.context, CommandSound.Step)
                            onDismiss()
                        }
                    },
                )
                fileActionTarget != null -> if (fileActionDetail != null) {
                    OverlayFileActionDetail(
                        fileActionTarget!!,
                        fileActionDetail!!,
                        fileActionPreview,
                        palette,
                        onConfirmDelete = if (fileDeleteConfirmation) ::execute else null,
                    )
                } else {
                    OverlayFileActions(fileActionTarget!!, fileActions, selectedIndex, maxVisibleResults, palette) { action ->
                        selectedIndex = fileActions.indexOf(action).coerceAtLeast(0)
                        execute()
                    }
                }
                activeKind == CommandKind.Files && !hasFileAccess -> OverlayFileAccess(palette) { engine.openFileSearchAccess() }
                activeKind == CommandKind.Files && parsed.text.isNotBlank() -> OverlayFileResults(
                    files,
                    selectedIndex,
                    maxVisibleResults,
                    palette,
                    fileSortMode,
                    listState = fileListState,
                    onVisibleIndicesChanged = { visibleFileIndices = it },
                    onLongClick = ::openFileActions,
                ) { file ->
                    executing = true
                    scope.launch {
                        delay(135)
                        val error = engine.openFile(file)
                        executing = false
                        playOutcome(error)
                        if (error != null) notice = error
                    }
                }
                needsContactPermission -> {
                    OverlayContactAccess(palette) { engine.requestContactsPermission() }
                }
                activeKind == CommandKind.Message && selectedRecentItem != null -> {
                    OverlayRecentConversation(selectedRecentItem, palette)
                }
                (activeKind == CommandKind.Message || activeKind == CommandKind.Call) && query.isNotEmpty() && selectedContact == null -> {
                    OverlayContactResults(contacts, parsed, selectedIndex, maxVisibleResults, palette) { contact ->
                        if (contact.hubNotificationKey == RecentMessagesDestination) {
                            HomeTypingHandoff.finish()
                            SoundFeedback.play(engine.context, CommandSound.Step)
                            setEditorQuery("@recents")
                            selectedIndex = 0
                            notice = null
                        } else if (contact.messagingPlatformScope != null) {
                            HomeTypingHandoff.finish()
                            SoundFeedback.play(engine.context, CommandSound.Step)
                            setEditorQuery("@${contact.messagingPlatformScope} ")
                            selectedIndex = 0
                            notice = null
                            focusRequester.requestFocus()
                        } else {
                            previousStageQuery = query
                            selectedContact = contact
                            selectedContactKind = activeKind
                            HomeTypingHandoff.finish()
                            SoundFeedback.play(engine.context, CommandSound.Step)
                            setEditorQuery("")
                            notice = null
                        }
                    }
                }
            }

            if (query.isNotBlank() || selectedContact != null || inGeminiConversation || switcherMode || todoistMode || fileActionTarget != null) {
                Spacer(Modifier.height(10.dp))
                if (selectedContact != null) {
                    Row(Modifier.padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (activeKind == CommandKind.Message) "▸ text → " else "▸ call → ",
                            modifier = Modifier.alignByBaseline(),
                            color = palette.secondary,
                            fontFamily = OverlayMono,
                            fontSize = 12.sp,
                        )
                        Text(
                            listOfNotNull(selectedContact!!.name, selectedContact!!.messagingPlatform)
                                .distinct().joinToString(" · "),
                            modifier = Modifier.alignByBaseline(),
                            color = OverlayAccent,
                            fontFamily = OverlayMono,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                        )
                    }
                } else if (!inGeminiConversation) {
                    if (activeKind == CommandKind.Files) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (fileActionTarget != null) "▸ ${fileActionTarget!!.name} → ${if (fileActionDirectDetail) if (fileActionTarget!!.isDirectory) "info" else "preview" else "actions"}" else "▸ ${when (fileSearchQuery.mode) {
                                    FileSearchMode.FilesOnly -> "file search"
                                    FileSearchMode.FoldersOnly -> "folder search"
                                    FileSearchMode.Default -> if (selectedFileIsDirectory) "open folder" else "open file"
                                }}",
                                color = if (fileActionTarget != null || fileSearchQuery.mode != FileSearchMode.Default) OverlayAccent else palette.secondary,
                                fontFamily = OverlayMono,
                                fontWeight = if (fileActionTarget == null && fileSearchQuery.mode == FileSearchMode.Default) FontWeight.Normal else FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                modifier = Modifier.weight(1f).then(
                                    if (fileActionTarget != null) Modifier.clickable { emptyBackspace() }
                                    else Modifier,
                                ),
                            )
                            if (fileActionTarget == null) {
                                Text(
                                    text = fileSortMode.label,
                                    color = OverlayAccent,
                                    fontFamily = OverlayMono,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    modifier = Modifier.clickable { fileSortMode = fileSortMode.next() }
                                        .padding(horizontal = 7.dp, vertical = 5.dp),
                                )
                            }
                        }
                    } else Text(
                        text = "▸ ${when {
                            shortcutQuery != null -> "search app shortcuts"
                            plainAliasDiscovery && selectedIndex < aliasSuggestions.size -> {
                                if (aliasSuggestions.getOrNull(selectedIndex)?.taskerAlias != null) "run Tasker task" else "use search alias"
                            }
                            plainAliasDiscovery -> "open app"
                            taskerResults.isNotEmpty() -> "run Tasker task"
                            aliasMatch?.target?.id == "control" -> "trigger control"
                            appListMode == AppListMode.All -> "all apps · ${apps.size}"
                            appListMode == AppListMode.Recents -> "recent apps · ${apps.size}"
                            todoistMode -> "todoist ${todoistPage.title.lowercase()} · ${visibleTodoistTasks.size}"
                            switcherMode -> "switch recent app · ${switcherTasks.size}"
                            activeKind == CommandKind.Apps && apps.getOrNull(selectedIndex)?.control != null -> "trigger control"
                            recentMessagesMode || selectedRecentItem != null -> "reply to recent messages"
                            activeKind == CommandKind.Apps && apps.getOrNull(selectedIndex)?.packageName?.startsWith("internal:web-search:") == true -> "search web"
                            activeKind == CommandKind.Ask && query.startsWith("??") -> "ask ChatGPT"
                            activeKind == CommandKind.Todo && todoistDuePreview != null -> "add to todoist → $todoistDuePreview"
                            activeKind == CommandKind.Timer && timerInputPreview != null -> "set timer or alarm → $timerInputPreview"
                            else -> overlayActionLabel(activeKind)
                        }}",
                        color = palette.secondary,
                        fontFamily = OverlayMono,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp),
                    )
                }
                Spacer(Modifier.height(10.dp))
            }

            Box(Modifier.fillMaxWidth()) {
            if (query.isNotBlank()) {
                Box(
                    Modifier
                        .matchParentSize()
                        .blur(
                            radius = if (actionReady) 12.dp else 7.dp,
                            edgeTreatment = BlurredEdgeTreatment.Unbounded,
                        )
                        .border(
                            width = if (actionReady) 3.dp else 2.dp,
                            color = OverlayAccent.copy(alpha = if (actionReady) .88f else .48f),
                            shape = commandBarShape,
                        ),
                )
            }
            Box(
                Modifier
                    .matchParentSize()
                    .clip(commandBarShape)
                    .background(commandSurface),
            )
            if (usePulseConfirmation && executionPulse.value > 0f) {
                // Confirmation is a simultaneous glass bloom, not progress:
                // the whole bar illuminates, peaks, then settles and recedes.
                Box(
                    Modifier
                        .matchParentSize()
                        .graphicsLayer {
                            val expansion = 1f + executionPulse.value * .018f
                            scaleX = expansion
                            scaleY = expansion
                        }
                        .blur(
                            radius = (10 + executionPulse.value * 10).dp,
                            edgeTreatment = BlurredEdgeTreatment.Unbounded,
                        )
                        .border(
                            width = (3 + executionPulse.value * 3).dp,
                            color = OverlayAccent.copy(alpha = .28f + executionPulse.value * .58f),
                            shape = commandBarShape,
                        ),
                )
                Box(
                    Modifier
                        .matchParentSize()
                        .clip(commandBarShape)
                        .background(OverlayAccent.copy(alpha = .16f + executionPulse.value * .30f)),
                )
            } else if (!usePulseConfirmation && executionFill > 0f) {
                // A soft bloom travels with the fill so execution reads as light
                // passing through the glass, rather than an opaque colour block.
                Box(
                    Modifier
                        .matchParentSize()
                        .graphicsLayer {
                            scaleX = executionFill
                            transformOrigin = TransformOrigin(0f, .5f)
                        }
                        .blur(
                            radius = 12.dp,
                            edgeTreatment = BlurredEdgeTreatment.Unbounded,
                        )
                        .border(
                            width = 5.dp,
                            color = OverlayAccent.copy(alpha = .78f),
                            shape = commandBarShape,
                        ),
                )
                Box(
                    Modifier
                        .matchParentSize()
                        .clip(commandBarShape)
                        .graphicsLayer {
                            scaleX = executionFill
                            transformOrigin = TransformOrigin(0f, .5f)
                        }
                        .background(OverlayAccent.copy(alpha = .58f)),
                )
            }
            Row(
                Modifier.fillMaxWidth()
                    .requiredHeight(70.dp)
                    .clip(commandBarShape)
                    .border(1.dp, if (barExecuting) Color.White.copy(alpha = .5f) else if (showActionAccent) OverlayAccent.copy(alpha = .72f) else palette.border, commandBarShape)
                    .padding(horizontal = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (query.isEmpty()) {
                            Text(
                                if (fileActionDetail != null) "left or back to actions"
                                else if (fileActionTarget != null) "type to filter actions…"
                                else if (inGeminiConversation) "reply…"
                                else if (todoistMode) "filter tasks…"
                                else if (switcherMode) "type to filter apps…"
                                else if (selectedContact != null && activeKind == CommandKind.Message) "type message…"
                                else if (selectedContact != null && activeKind == CommandKind.Call) "press enter to call"
                                else "${FileSearchSettings.displayTrigger()} files  @ message  - todo  ${TriggerSettings.noteSymbol()} note  + timer  , calc  / web  ? ask",
                                color = palette.secondary.copy(alpha = .58f),
                                fontFamily = OverlayMono,
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        // BasicTextField treats a replacement VisualTransformation as an
                        // editor change and can restart the IME. Keep its outer transform
                        // stable for the whole overlay session, while swapping only the
                        // internal colour rule as command state changes.
                        val editorTransformDelegate = remember { DelegatingVisualTransformation() }
                        val editorTransformation = remember { SentinelTransformation(editorTransformDelegate) }
                        editorTransformDelegate.delegate = when {
                            plainAliasDiscovery && selectedIndex < aliasSuggestions.size -> AliasPrefixTransformation(
                                aliasSuggestions.getOrNull(selectedIndex)?.alias?.length ?: 0,
                            )
                            taskerResults.isNotEmpty() -> AliasPrefixTransformation(
                                TriggerSettings.effectiveAlias(taskerResults.first().alias).length,
                            )
                            aliasSuggestions.isNotEmpty() -> AliasPrefixTransformation(
                                aliasSuggestions.getOrNull(selectedIndex)?.alias?.length ?: 0,
                            )
                            shortcutQuery != null -> AliasPrefixTransformation(
                                TriggerSettings.effectiveAlias(AppShortcutSettings.alias(engine.context)).length,
                            )
                            aliasMatch != null -> AliasPrefixTransformation(aliasMatch.alias.length)
                            activeKind == CommandKind.Todo -> TodoistRecognitionTransformation
                            activeKind == CommandKind.Timer -> TimerRecognitionTransformation
                            activeKind == CommandKind.Files && fileSearchQuery.mode != FileSearchMode.Default -> FileFilterPrefixTransformation(
                                when (fileSearchQuery.mode) {
                                    FileSearchMode.FilesOnly -> FileSearchSettings.filesOnlyPrefix.value.length
                                    FileSearchMode.FoldersOnly -> FileSearchSettings.foldersOnlyPrefix.value.length
                                    FileSearchMode.Default -> 0
                                },
                            )
                            activeKind == CommandKind.Message && messagingPlatform != null -> AliasPrefixTransformation(
                                1 + contactQuery.trim().substringBefore(' ').length,
                            )
                            recentMessagesMode -> AliasPrefixTransformation(query.length)
                            else -> CommandPrefixTransformation
                        }
                        BasicTextField(
                            value = editorValue,
                            onValueChange = { next ->
                                val wasDeletingSelection = !editorValue.selection.collapsed
                                if (executing) {
                                    // Keep the editor connection alive so Android never
                                    // dismisses the IME during the execution animation.
                                } else if (!next.text.startsWith(InputSentinel)) {
                                    // Ctrl+A can include the invisible sentinel. If the
                                    // user types immediately, Android replaces the whole
                                    // selection with that first character. Preserve it
                                    // while restoring the sentinel instead of eating it.
                                    val replacement = if (wasDeletingSelection) next.text else ""
                                    HomeTypingHandoff.finish()
                                    setEditorQuery(replacement)
                                    // Removing a Ctrl+A/range selection can also
                                    // remove the invisible sentinel. That is a
                                    // normal edit, not Backspace on an empty bar.
                                    if (!wasDeletingSelection) emptyBackspace()
                                } else {
                                    HomeTypingHandoff.finish()
                                    editorValue = next
                                    query = next.text.drop(1)
                                    notice = null
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp)
                                .focusRequester(focusRequester)
                                .onFocusChanged { editorFocused = it.isFocused }
                                .onPreviewKeyEvent { event ->
                                    val native = event.nativeKeyEvent
                                    val isBackspace = event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DEL
                                    val resultCount = when {
                                        fileActionTarget != null -> if (fileActionDetail != null) 0 else fileActions.size
                                        todoistMode -> visibleTodoistTasks.size
                                        switcherMode -> switcherTasks.size
                                        plainAliasDiscovery -> aliasSuggestions.size + apps.size
                                        taskerResults.isNotEmpty() -> taskerResults.size
                                        aliasSuggestions.isNotEmpty() -> aliasSuggestions.size
                                        aliasMatch?.target?.id == "settings" -> settingsResults.size
                                        aliasMatch?.target?.id == "control" -> controlResults.size
                                        shortcutQuery != null -> shortcutResults.size
                                        taskerResults.isNotEmpty() -> taskerResults.size
                                        aliasMatch != null -> 1
                                        activeKind == CommandKind.Files -> files.size
                                        activeKind == CommandKind.Apps -> apps.size
                                        activeKind == CommandKind.Message || activeKind == CommandKind.Call -> contacts.size
                                        else -> 0
                                    }
                                    if (event.type == KeyEventType.KeyUp && native.keyCode == heldOverlayFirstKey) {
                                        heldOverlayFirstKey = android.view.KeyEvent.KEYCODE_UNKNOWN
                                        false
                                    } else if (event.type == KeyEventType.KeyDown && native.repeatCount > 0 &&
                                        HomeTypingSettings.holdFirstForAlt.value && query.length == 1 &&
                                        heldOverlayFirstKey == native.keyCode
                                    ) {
                                        val alternateCodePoint = native.getUnicodeChar(
                                            native.metaState or android.view.KeyEvent.META_ALT_ON or android.view.KeyEvent.META_ALT_LEFT_ON,
                                        )
                                        val alternate = if (alternateCodePoint != 0 && !Character.isISOControl(alternateCodePoint)) {
                                            String(Character.toChars(alternateCodePoint))
                                        } else ""
                                        val symbol = alternate.singleOrNull()
                                        if (symbol != null && isCommandTrigger(symbol)) setEditorQuery(alternate)
                                        true
                                    } else if (event.type == KeyEventType.KeyDown && native.repeatCount == 0 && query.isEmpty() &&
                                        !native.isCtrlPressed && !native.isMetaPressed
                                    ) {
                                        heldOverlayFirstKey = native.keyCode
                                        false
                                    } else if (event.type == KeyEventType.KeyDown && isBackspace && !editorValue.selection.collapsed) {
                                        // Clear a Ctrl+A selection before Android removes
                                        // the sentinel and asynchronously rebuilds the
                                        // input connection. Fast follow-up typing can
                                        // otherwise lose its first character.
                                        HomeTypingHandoff.finish()
                                        setEditorQuery("")
                                        notice = null
                                        true
                                    } else if (event.type == KeyEventType.KeyDown && fileActionTarget == null && activeKind == CommandKind.Files && files.isNotEmpty() &&
                                        native.keyCode == android.view.KeyEvent.KEYCODE_DPAD_RIGHT
                                    ) {
                                        files.getOrNull(selectedIndex.coerceIn(0, files.lastIndex))?.let(::openFileActions)
                                        true
                                    } else if (event.type == KeyEventType.KeyDown && fileActionTarget == null && activeKind == CommandKind.Files && files.isNotEmpty() &&
                                        native.keyCode == android.view.KeyEvent.KEYCODE_DPAD_LEFT
                                    ) {
                                        files.getOrNull(selectedIndex.coerceIn(0, files.lastIndex))?.let(::openFileQuickPreview)
                                        true
                                    } else if (event.type == KeyEventType.KeyDown && fileActionTarget != null && native.keyCode == android.view.KeyEvent.KEYCODE_DPAD_LEFT) {
                                        emptyBackspace()
                                    } else if (event.type == KeyEventType.KeyDown && switcherMode && native.keyCode == android.view.KeyEvent.KEYCODE_DPAD_RIGHT && resultCount > 0) {
                                        val next = (selectedIndex + 1).coerceAtMost(resultCount - 1)
                                        if (next != selectedIndex) SoundFeedback.play(engine.context, CommandSound.Step)
                                        selectedIndex = next
                                        true
                                    } else if (event.type == KeyEventType.KeyDown && switcherMode && native.keyCode == android.view.KeyEvent.KEYCODE_DPAD_LEFT && resultCount > 0) {
                                        val next = (selectedIndex - 1).coerceAtLeast(0)
                                        if (next != selectedIndex) SoundFeedback.play(engine.context, CommandSound.Step)
                                        selectedIndex = next
                                        true
                                    } else if (event.type == KeyEventType.KeyDown && event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_DOWN && resultCount > 0) {
                                        val next = (selectedIndex + 1).coerceAtMost(resultCount - 1)
                                        if (next != selectedIndex) SoundFeedback.play(engine.context, CommandSound.Step)
                                        selectedIndex = next
                                        true
                                    } else if (event.type == KeyEventType.KeyDown && event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_UP && resultCount > 0) {
                                        val next = (selectedIndex - 1).coerceAtLeast(0)
                                        if (next != selectedIndex) SoundFeedback.play(engine.context, CommandSound.Step)
                                        selectedIndex = next
                                        true
                                    } else if (event.type == KeyEventType.KeyDown && event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_ENTER && actionReady) {
                                        submitFromEditor(native.isShiftPressed)
                                        true
                                    } else if (event.type == KeyEventType.KeyDown && isBackspace && query.isEmpty()) {
                                        emptyBackspace()
                                    } else {
                                        false
                                    }
                                },
                            textStyle = TextStyle(color = palette.text, fontFamily = OverlayMono, fontWeight = FontWeight.Bold, fontSize = 24.sp),
                            cursorBrush = SolidColor(OverlayAccent),
                            visualTransformation = editorTransformation,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(onGo = { submitFromEditor() }),
                        )
                    }
                AnimatedVisibility(
                        visible = query.isNotBlank() || selectedContact != null || inGeminiConversation,
                        enter = fadeIn(tween(120)),
                        exit = fadeOut(tween(90)),
                ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardReturn,
                            contentDescription = "Enter",
                            tint = enterColor,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(42.dp)
                                .clickable(enabled = actionReady) { submitFromEditor() },
                        )
                }
            }
            }
            }
        }
        }
    }
}

@Composable
private fun OverlaySwitcherResults(
    tasks: List<SwitcherTask>,
    selectedIndex: Int,
    palette: OverlayPalette,
    client: TaskSwitcherClient,
    executingTaskId: Int?,
    onClick: (SwitcherTask) -> Unit,
    onDismiss: (SwitcherTask) -> Unit,
) {
    val state = rememberLazyListState()
    var initiallyPositioned by remember { mutableStateOf(false) }
    val selectedTaskId = tasks.getOrNull(selectedIndex)?.taskId
    LaunchedEffect(selectedIndex, selectedTaskId) {
        if (selectedIndex in tasks.indices) {
            if (!initiallyPositioned) {
                state.scrollToItem(selectedIndex)
                initiallyPositioned = true
            } else if (state.firstVisibleItemIndex == 0 && selectedIndex == 0) state.scrollToItem(0)
            else state.animateScrollToItem(selectedIndex)
        }
    }
    if (tasks.isEmpty()) {
        Text(
            "No matching recent apps",
            color = palette.secondary,
            fontFamily = OverlayMono,
            fontSize = 14.sp,
            modifier = Modifier.fillMaxWidth().padding(18.dp),
        )
        return
    }
    LazyRow(
        state = state,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth().height(230.dp),
    ) {
        itemsIndexed(tasks, key = { _, task -> task.taskId }) { index, task ->
            SwitcherCard(
                task = task,
                selected = index == selectedIndex,
                accent = OverlayAccent,
                tile = palette.tile,
                text = palette.text,
                border = palette.border,
                client = client,
                executing = task.taskId == executingTaskId,
                onDismiss = { onDismiss(task) },
                open = { onClick(task) },
            )
        }
    }
}

@Composable
private fun AskWaitingPreview(
    prompt: String,
    palette: OverlayPalette,
    timedOut: Boolean,
    onOpenChatGpt: () -> Unit,
) {
    val context = LocalContext.current
    Column(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 5.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text(
                text = prompt,
                color = Color.White,
                fontFamily = OverlayMono,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.widthIn(max = 300.dp).clip(RoundedCornerShape(15.dp))
                    .background(OverlayAccent).padding(horizontal = 13.dp, vertical = 9.dp),
            )
        }
        if (timedOut) {
            Text(
                "no response notification received",
                color = palette.secondary,
                fontFamily = OverlayMono,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 7.dp, top = 10.dp),
            )
            Text(
                "open ChatGPT to view reply →",
                color = OverlayAccent,
                fontFamily = OverlayMono,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier.clickable(onClick = onOpenChatGpt).padding(start = 7.dp, top = 7.dp, bottom = 3.dp),
            )
        } else {
            Text(
                "waiting for ChatGPT notification…",
                color = palette.secondary,
                fontFamily = OverlayMono,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 7.dp, top = 10.dp, bottom = 3.dp),
            )
        }
    }
}

@Composable
private fun GeminiConversation(
    messages: List<AskMessage>,
    loading: Boolean,
    palette: OverlayPalette,
) {
    val scrollState = rememberScrollState()
    val maxHeight = (LocalConfiguration.current.screenHeightDp * .58f).dp
    LaunchedEffect(messages.size, loading) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }
    Column(
        Modifier.fillMaxWidth().heightIn(max = maxHeight).verticalScroll(scrollState)
            .padding(horizontal = 4.dp, vertical = 5.dp),
    ) {
        messages.forEach { message ->
            val user = message.role == "user"
            Row(
                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = if (user) Arrangement.End else Arrangement.Start,
            ) {
                Text(
                    text = if (user) AnnotatedString(message.text) else renderGeminiMarkdown(message.text),
                    color = if (user) Color.White else palette.text,
                    fontFamily = if (user) OverlayMono else OverlaySans,
                    fontWeight = if (user) FontWeight.Bold else FontWeight.Normal,
                    fontSize = if (user) 13.sp else 14.sp,
                    lineHeight = 19.sp,
                    modifier = Modifier.widthIn(max = if (user) 300.dp else 560.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(if (user) OverlayAccent else palette.tile)
                        .border(
                            1.dp,
                            if (user) OverlayAccent else palette.border,
                            RoundedCornerShape(15.dp),
                        )
                        .padding(horizontal = 13.dp, vertical = 10.dp),
                )
            }
        }
        if (loading) {
            Text(
                "Gemini is thinking…",
                color = palette.secondary,
                fontFamily = OverlayMono,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 7.dp, top = 5.dp, bottom = 3.dp),
            )
        }
    }
}

private fun renderGeminiMarkdown(source: String): AnnotatedString = buildAnnotatedString {
    source.lines().forEachIndexed { lineIndex, rawLine ->
        val heading = rawLine.takeWhile { it == '#' }.length.takeIf { it in 1..3 }
        val bulletMatch = Regex("^(\\s*)[-*]\\s+(.+)$").matchEntire(rawLine)
        val content = when {
            heading != null -> rawLine.drop(heading).trimStart()
            bulletMatch != null -> "• ${bulletMatch.groupValues[2]}"
            else -> rawLine
        }
        if (heading != null) {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = (19 - heading * 2).sp)) {
                appendInlineMarkdown(content)
            }
        } else {
            appendInlineMarkdown(content)
        }
        if (lineIndex < source.lines().lastIndex) append('\n')
    }
}

private fun AnnotatedString.Builder.appendInlineMarkdown(text: String) {
    val pattern = Regex("(`[^`]+`|\\*\\*[^*]+\\*\\*|__[^_]+__|(?<!\\*)\\*[^*]+\\*(?!\\*)|_[^_]+_)")
    var cursor = 0
    pattern.findAll(text).forEach { match ->
        append(text.substring(cursor, match.range.first))
        val token = match.value
        when {
            token.startsWith('`') -> withStyle(
                SpanStyle(fontFamily = OverlayMono, background = Color.Black.copy(alpha = .22f)),
            ) { append(token.drop(1).dropLast(1)) }
            token.startsWith("**") || token.startsWith("__") -> withStyle(
                SpanStyle(fontWeight = FontWeight.Bold),
            ) { append(token.drop(2).dropLast(2)) }
            else -> withStyle(SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)) {
                append(token.drop(1).dropLast(1))
            }
        }
        cursor = match.range.last + 1
    }
    append(text.substring(cursor))
}

@Composable
private fun AskConversationPreview(
    prompt: String,
    response: String,
    palette: OverlayPalette,
    onContinue: () -> Unit,
    onClose: () -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 5.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text(
                text = prompt,
                color = Color.White,
                fontFamily = OverlayMono,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.widthIn(max = 300.dp).clip(RoundedCornerShape(15.dp))
                    .background(OverlayAccent).padding(horizontal = 13.dp, vertical = 9.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp))
                .background(palette.tile).border(1.dp, palette.border, RoundedCornerShape(15.dp))
                .padding(horizontal = 13.dp, vertical = 11.dp),
        ) {
            Text(
                text = response,
                color = palette.text,
                fontFamily = OverlaySans,
                fontSize = 14.sp,
                lineHeight = 19.sp,
            )
            Text(
                "notification preview · may be shortened",
                color = palette.secondary,
                fontFamily = OverlayMono,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        Row(
            Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("close", color = palette.secondary, fontFamily = OverlayMono, fontSize = 11.sp, modifier = Modifier.clickable { onClose() }.padding(5.dp))
            Text("continue in ChatGPT →", color = OverlayAccent, fontFamily = OverlayMono, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.clickable { onContinue() }.padding(5.dp))
        }
    }
}

private fun isCompletionNotice(notice: String?): Boolean = notice == "task added · todoist" || notice == "message sent" ||
    notice?.startsWith("timer added") == true ||
    notice?.startsWith("alarm set") == true ||
    notice?.startsWith("task started ·") == true

private fun timerDurationLabel(text: String): String {
    val match = Regex("(?i)\\b(\\d+(?:\\.\\d+)?)\\s*(seconds?|secs?|s|minutes?|mins?|m|hours?|hrs?|h)\\b").find(text)
        ?: return text.trim()
    val amount = match.groupValues[1].toDoubleOrNull() ?: return text.trim()
    val totalSeconds = when {
        match.groupValues[2].startsWith("h", true) -> (amount * 3600).toLong()
        match.groupValues[2].startsWith("m", true) -> (amount * 60).toLong()
        else -> amount.toLong()
    }.coerceAtLeast(1)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%02d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}

@Composable
private fun OverlayCompletion(
    text: String,
    palette: OverlayPalette,
    commandSurface: Color,
    focusRequester: FocusRequester,
) {
    val visible = remember(text) { MutableTransitionState(false).apply { targetState = true } }
    val settleGlow = remember(text) { Animatable(1f) }
    LaunchedEffect(text) {
        focusRequester.requestFocus()
        settleGlow.snapTo(1f)
        settleGlow.animateTo(.35f, tween(220))
    }
    Box(Modifier.fillMaxWidth()) {
        Box(
            Modifier
                .matchParentSize()
                .blur(14.dp, BlurredEdgeTreatment.Unbounded)
                .border(
                    width = (3f + settleGlow.value * 2f).dp,
                    color = OverlayAccent.copy(alpha = .34f + settleGlow.value * .34f),
                    shape = RoundedCornerShape(17.dp),
                ),
        )
        Row(
            Modifier
                .fillMaxWidth()
                .height(70.dp)
                .clip(RoundedCornerShape(17.dp))
                .background(commandSurface)
                .background(OverlayAccent.copy(alpha = .06f + settleGlow.value * .08f))
                .border(1.dp, OverlayAccent.copy(alpha = .72f), RoundedCornerShape(17.dp))
                .padding(horizontal = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedVisibility(
                visibleState = visible,
                enter = fadeIn(tween(140)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(31.dp).clip(CircleShape).background(OverlayAccent),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("✓", color = Color.White, fontFamily = OverlaySans, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
                Text(
                    text.substringBefore(" ·"),
                    modifier = Modifier.padding(start = 12.dp),
                    color = palette.text,
                    fontFamily = OverlaySerif,
                    fontWeight = FontWeight.Normal,
                    fontSize = 25.sp,
                    letterSpacing = .25.sp,
                )
                if (text.startsWith("timer added ·") || text.startsWith("alarm set ·")) {
                    Spacer(Modifier.weight(1f))
                    Text(
                        text.substringAfter(" ·"),
                        color = OverlayAccent,
                        fontFamily = OverlayMono,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                    )
                }
                }
            }
        }
        // Preserve the input connection while the confirmation is displayed.
        // Removing the focused editor makes Android hide the IME and shifts the overlay.
        BasicTextField(
            value = InputSentinel.toString(),
            onValueChange = {},
            modifier = Modifier.size(1.dp).alpha(0f).focusRequester(focusRequester),
            textStyle = TextStyle(fontSize = 1.sp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
        )
    }
}

@Composable
private fun OverlayDiscoveryResults(
    suggestions: List<AliasSuggestion>,
    apps: List<AppResult>,
    selectedIndex: Int,
    visibleRowCount: Int,
    palette: OverlayPalette,
    controlFeedback: Pair<ControlKind, String>?,
    touchExecutingAppKey: String?,
    onVisibleAppIndicesChanged: (List<Int>) -> Unit,
    onSuggestionClick: (AliasSuggestion) -> Unit,
    onAppClick: (AppResult) -> Unit,
    onAppLongClick: (AppResult) -> Unit,
) {
    val listState = rememberLazyListState()
    val total = suggestions.size + apps.size
    LaunchedEffect(selectedIndex, suggestions.firstOrNull()?.alias, apps.firstOrNull()?.packageName, total) {
        if (selectedIndex in 0 until total) listState.keepSelectionVisible(selectedIndex, visibleRowCount)
    }
    LaunchedEffect(listState, suggestions.size, apps.size) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.mapNotNull { visible ->
                (visible.index - suggestions.size).takeIf { it in apps.indices }
            }
        }.distinctUntilChanged().collect(onVisibleAppIndicesChanged)
    }
    LazyColumn(
        Modifier.fillMaxWidth().height(resultListHeight(total, visibleRowCount)),
        state = listState,
        contentPadding = PaddingValues(bottom = ResultViewportBottomPadding),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(suggestions, key = { index, suggestion -> "alias:${suggestion.alias}:${suggestion.label}:$index" }) { index, suggestion ->
            OverlayAliasSuggestionRow(suggestion, index == selectedIndex, palette, onSuggestionClick)
        }
        itemsIndexed(apps, key = { index, app -> "app:${app.packageName}:$index" }) { index, app ->
            val combinedIndex = suggestions.size + index
            OverlayAppResultRow(app, combinedIndex == selectedIndex, palette, controlFeedback, touchExecutingAppKey == app.packageName, onAppClick, onAppLongClick)
        }
    }
}

@Composable
private fun OverlayAppResults(
    apps: List<AppResult>,
    selectedIndex: Int,
    visibleRowCount: Int,
    palette: OverlayPalette,
    controlFeedback: Pair<ControlKind, String>?,
    touchExecutingAppKey: String?,
    onVisibleIndicesChanged: (List<Int>) -> Unit,
    onClick: (AppResult) -> Unit,
    onLongClick: (AppResult) -> Unit,
) {
    if (apps.isEmpty()) {
        OverlayNotice("no apps found", palette)
        return
    }
    val animationMode = AppearanceSettings.listAnimation.value
    val listState = rememberLazyListState()
    LaunchedEffect(selectedIndex, apps.firstOrNull()?.packageName, apps.size) {
        if (selectedIndex in apps.indices) listState.keepSelectionVisible(selectedIndex, visibleRowCount)
    }
    LaunchedEffect(listState, apps.size) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.map { it.index } }
            .distinctUntilChanged()
            .collect(onVisibleIndicesChanged)
    }
    LazyColumn(
        Modifier.fillMaxWidth().height(resultListHeight(apps.size, visibleRowCount)),
        state = listState,
        contentPadding = PaddingValues(bottom = ResultViewportBottomPadding),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(apps, key = { index, app -> "${app.packageName}:$index" }) { index, app ->
            if (animationMode == ListAnimationMode.Off) {
                OverlayAppResultRow(app, index == selectedIndex, palette, controlFeedback, touchExecutingAppKey == app.packageName, onClick, onLongClick)
            } else {
                AnimatedContent(
                    targetState = app,
                    contentKey = { it.packageName },
                    transitionSpec = {
                        val enterMs = if (animationMode == ListAnimationMode.Quick) 80 else 170
                        val exitMs = if (animationMode == ListAnimationMode.Quick) 65 else 120
                        (fadeIn(tween(enterMs, delayMillis = if (animationMode == ListAnimationMode.Smooth) 45 else 0)) togetherWith
                            fadeOut(tween(exitMs))).using(SizeTransform(clip = false))
                    },
                    label = "appResultSlot$index",
                ) { shownApp ->
                    OverlayAppResultRow(shownApp, index == selectedIndex, palette, controlFeedback, touchExecutingAppKey == shownApp.packageName, onClick, onLongClick)
                }
            }
        }
    }
}

@Composable
private fun OverlayAppResultRow(
    app: AppResult,
    selected: Boolean,
    palette: OverlayPalette,
    controlFeedback: Pair<ControlKind, String>?,
    touchExecuting: Boolean,
    onClick: (AppResult) -> Unit,
    onLongClick: (AppResult) -> Unit,
) {
    val glowMode = AppearanceSettings.appGlowMode.value
    val isAppListMode = app.packageName in setOf(
        "internal:all-apps", "internal:recent-apps", "internal:switcher", "internal:todoist-tasks",
        "internal:key-control", "internal:quick-controls", "internal:app-shortcuts",
    )
    val appColor = if (isAppListMode) OverlayAccent else app.adaptiveColor?.let { Color(it) } ?: OverlayAccent
    val visualMode = if (glowMode == AppGlowMode.Off) AppGlowMode.Reduced else glowMode
    val shape = RoundedCornerShape(15.dp)
    val selectedBrush = when (visualMode) {
        AppGlowMode.Off -> SolidColor(palette.tile)
        AppGlowMode.Outline -> SolidColor(palette.tile)
        AppGlowMode.Reduced -> SolidColor((if (glowMode == AppGlowMode.Off) OverlayAccent else appColor).copy(alpha = .20f))
        AppGlowMode.Full -> SolidColor(appColor.copy(alpha = .62f))
    }
    val selectionColor = if (glowMode == AppGlowMode.Off) OverlayAccent else appColor
    val hasControlFeedback = app.control != null &&
        controlFeedback != null &&
        app.control.kind == controlFeedback.first
    val activeControlFeedback = hasControlFeedback && controlFeedback?.second != "off"
    val touchPulse by animateFloatAsState(
        targetValue = if (touchExecuting) 1f else 0f,
        animationSpec = tween(if (touchExecuting) 90 else 150),
        label = "touchResultConfirmation",
    )
    Box(Modifier.fillMaxWidth()) {
        if (selected || touchPulse > 0f) {
            Box(
                Modifier.matchParentSize()
                    .blur(
                        radius = when (visualMode) {
                            AppGlowMode.Outline -> 7.dp
                            AppGlowMode.Reduced -> 10.dp
                            else -> 13.dp
                        },
                        edgeTreatment = BlurredEdgeTreatment.Unbounded,
                    )
                    .border(
                        width = when (visualMode) {
                            AppGlowMode.Outline -> 2.dp
                            AppGlowMode.Reduced -> 3.dp
                            else -> 5.dp
                        },
                        color = selectionColor.copy(alpha = (when (visualMode) {
                            AppGlowMode.Outline -> .72f
                            AppGlowMode.Reduced -> .82f
                            else -> .94f
                        }) * if (touchPulse > 0f) touchPulse else 1f),
                        shape = shape,
                    ),
            )
        }
        Row(
            Modifier.fillMaxWidth().clip(shape).background(if (activeControlFeedback) OverlayAccent.copy(alpha = .34f) else palette.tile)
                .then(if (touchPulse > 0f) Modifier.background(selectionColor.copy(alpha = .18f + touchPulse * .36f)) else Modifier)
                .then(if (selected && visualMode != AppGlowMode.Outline) Modifier.background(selectedBrush) else Modifier)
                .border(
                    if (activeControlFeedback || (selected && visualMode == AppGlowMode.Outline)) 2.dp else 1.dp,
                    if (activeControlFeedback) OverlayAccent else if (selected) selectionColor else palette.border,
                    shape,
                )
                .combinedClickable(
                    onClick = { onClick(app) },
                    onLongClick = { if (!app.packageName.startsWith("internal:")) onLongClick(app) },
                ).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (app.control != null) {
                Box(
                    Modifier.size(42.dp).clip(RoundedCornerShape(12.dp))
                        .background(OverlayAccent.copy(alpha = .16f)),
                    contentAlignment = Alignment.Center,
                ) {
                    ControlGlyph(app.control.kind, app.label)
                }
            } else if (isAppListMode) {
                Box(
                    Modifier.size(42.dp).clip(RoundedCornerShape(12.dp))
                        .background(OverlayAccent.copy(alpha = .16f)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (app.packageName == "internal:quick-controls" || app.packageName == "internal:app-shortcuts") {
                        Icon(
                            painter = painterResource(
                                if (app.packageName == "internal:quick-controls") R.drawable.ic_quick_controls
                                else R.drawable.ic_app_shortcuts,
                            ),
                            contentDescription = app.label,
                            tint = OverlayAccent,
                            modifier = Modifier.size(23.dp),
                        )
                    } else Icon(
                        imageVector = when (app.packageName) {
                            "internal:all-apps" -> Icons.Outlined.Apps
                            "internal:switcher" -> Icons.Outlined.ViewCarousel
                            "internal:todoist-tasks" -> Icons.Outlined.CheckCircle
                            "internal:key-control" -> Icons.Outlined.Keyboard
                            else -> Icons.Outlined.History
                        },
                        contentDescription = app.label,
                        tint = OverlayAccent,
                        modifier = Modifier.size(22.dp),
                    )
                }
            } else {
                RealAppIcon(app.icon, app.label, palette, Modifier.size(42.dp))
            }
            if (app.control != null) {
                Column(Modifier.padding(start = 13.dp).weight(1f)) {
                    Text(app.label, color = palette.text, fontFamily = OverlaySans, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text(
                        if (hasControlFeedback) controlFeedback?.second.orEmpty() else app.control.subtitle,
                        color = if (activeControlFeedback) OverlayAccent else palette.secondary,
                        fontFamily = OverlayMono,
                        fontWeight = if (activeControlFeedback) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 10.sp,
                    )
                }
            } else Text(
                app.label,
                Modifier.padding(start = 13.dp).weight(1f),
                color = palette.text,
                fontFamily = OverlaySans,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (selected) Text("↵", color = if (visualMode == AppGlowMode.Full) Color.White else selectionColor, fontFamily = OverlayMono, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        }
    }
}

@Composable
private fun OverlayShortcutResults(
    shortcuts: List<AppShortcutResult>,
    selectedIndex: Int,
    visibleRowCount: Int,
    palette: OverlayPalette,
    onClick: (AppShortcutResult) -> Unit,
) {
    if (shortcuts.isEmpty()) {
        OverlayNotice("no app shortcuts found", palette)
        return
    }
    val listState = rememberLazyListState()
    LaunchedEffect(selectedIndex, shortcuts.firstOrNull()?.id, shortcuts.size) {
        if (selectedIndex in shortcuts.indices) listState.keepSelectionVisible(selectedIndex, visibleRowCount)
    }
    LazyColumn(
        Modifier.fillMaxWidth()
            .heightIn(max = resultListHeight(shortcuts.size, visibleRowCount)),
        state = listState,
        contentPadding = PaddingValues(bottom = ResultViewportBottomPadding),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
    itemsIndexed(shortcuts, key = { _, shortcut -> "${shortcut.packageName}:${shortcut.id}" }) { index, shortcut ->
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp))
                .background(if (index == selectedIndex) OverlayAccent.copy(alpha = .22f) else palette.tile)
                .border(1.dp, if (index == selectedIndex) OverlayAccent else palette.border, RoundedCornerShape(15.dp))
                .clickable { onClick(shortcut) }.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RealAppIcon(shortcut.icon, shortcut.appLabel, palette, Modifier.size(42.dp))
            Column(Modifier.padding(start = 13.dp).weight(1f)) {
                Text(shortcut.label, color = palette.text, fontFamily = OverlaySans, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(shortcut.appLabel, color = palette.secondary, fontFamily = OverlayMono, fontSize = 10.sp)
            }
            if (index == selectedIndex) Text("↵", color = OverlayAccent, fontFamily = OverlayMono, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        }
    }
    }
}

@Composable
private fun OverlaySettingsResults(
    results: List<SystemSettingResult>,
    selectedIndex: Int,
    visibleRowCount: Int,
    settingsIcon: Drawable?,
    palette: OverlayPalette,
    onClick: (SystemSettingResult) -> Unit,
) {
    if (results.isEmpty()) {
        OverlayNotice("no matching settings", palette)
        return
    }
    val listState = rememberLazyListState()
    LaunchedEffect(selectedIndex, results.firstOrNull()?.action, results.size) {
        if (selectedIndex in results.indices) listState.keepSelectionVisible(selectedIndex, visibleRowCount)
    }
    LazyColumn(
        Modifier.fillMaxWidth().height(resultListHeight(results.size, visibleRowCount)),
        state = listState,
        contentPadding = PaddingValues(bottom = ResultViewportBottomPadding),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(results, key = { _, result -> result.action }) { index, result ->
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp))
                    .background(if (index == selectedIndex) OverlayAccent.copy(alpha = .22f) else palette.tile)
                    .border(1.dp, if (index == selectedIndex) OverlayAccent else palette.border, RoundedCornerShape(15.dp))
                    .clickable { onClick(result) }.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RealAppIcon(settingsIcon, "Settings", palette, Modifier.size(42.dp))
                Column(Modifier.padding(start = 13.dp).weight(1f)) {
                    Text(result.label, color = palette.text, fontFamily = OverlaySans, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text(result.subtitle, color = palette.secondary, fontFamily = OverlayMono, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (index == selectedIndex) Text("↵", color = OverlayAccent, fontFamily = OverlayMono, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            }
        }
    }
}

@Composable
private fun OverlayControlResults(
    results: List<ControlResult>,
    selectedIndex: Int,
    visibleRowCount: Int,
    palette: OverlayPalette,
    feedback: Pair<ControlKind, String>?,
    onClick: (ControlResult) -> Unit,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(selectedIndex, results.firstOrNull()?.kind, results.size) {
        if (selectedIndex in results.indices) listState.keepSelectionVisible(selectedIndex, visibleRowCount)
    }
    LazyColumn(
        Modifier.fillMaxWidth().heightIn(max = resultListHeight(results.size, visibleRowCount)),
        state = listState,
        contentPadding = PaddingValues(bottom = ResultViewportBottomPadding),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(results, key = { _, result -> result.kind }) { index, result ->
            val hasFeedback = feedback?.first == result.kind
            val activeFeedback = hasFeedback && feedback?.second != "off"
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp))
                    .background(
                        when {
                            activeFeedback -> OverlayAccent.copy(alpha = .34f)
                            index == selectedIndex -> OverlayAccent.copy(alpha = .22f)
                            else -> palette.tile
                        },
                    )
                    .border(
                        if (activeFeedback) 2.dp else 1.dp,
                        if (index == selectedIndex || activeFeedback) OverlayAccent else palette.border,
                        RoundedCornerShape(15.dp),
                    )
                    .clickable { onClick(result) }.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(OverlayAccent.copy(alpha = .16f)), contentAlignment = Alignment.Center) {
                    ControlGlyph(result.kind, result.label)
                }
                Column(Modifier.padding(start = 13.dp).weight(1f)) {
                    Text(result.label, color = palette.text, fontFamily = OverlaySans, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text(
                        if (hasFeedback) feedback.second else result.subtitle,
                        color = if (activeFeedback) OverlayAccent else palette.secondary,
                        fontFamily = OverlayMono,
                        fontWeight = if (activeFeedback) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 10.sp,
                    )
                }
                if (index == selectedIndex) Text("↵", color = OverlayAccent, fontFamily = OverlayMono, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            }
        }
    }
}

@Composable
private fun ControlGlyph(kind: ControlKind, label: String) {
    val context = LocalContext.current
    val suppliedIcon = remember(kind) { aguiControlIcon(context, kind) }
    if (suppliedIcon != null) {
        val painter = remember(suppliedIcon) { BitmapPainter(suppliedIcon.toBitmap(72, 72).asImageBitmap()) }
        Image(
            painter,
            label,
            Modifier.size(22.dp),
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(OverlayAccent),
        )
        return
    }
    val icon = when (kind) {
        ControlKind.Flashlight -> Icons.Outlined.FlashlightOn
        ControlKind.Rotation -> Icons.Outlined.ScreenRotation
        ControlKind.Brightness -> Icons.Outlined.Brightness6
        ControlKind.Volume -> Icons.Outlined.VolumeUp
        ControlKind.AudioOutput -> Icons.Outlined.Speaker
        ControlKind.BluetoothDevices -> Icons.Outlined.Bluetooth
        ControlKind.Play -> Icons.Rounded.PlayArrow
        ControlKind.Pause -> Icons.Rounded.Pause
        ControlKind.Next -> Icons.Rounded.SkipNext
        ControlKind.RingerMode -> Icons.Outlined.NotificationsActive
        ControlKind.Theme -> Icons.Outlined.DarkMode
        ControlKind.BatteryProtection -> Icons.Outlined.BatteryChargingFull
        ControlKind.Caffeine -> Icons.Outlined.Coffee
        ControlKind.ScreenDimmer -> Icons.Outlined.BrightnessLow
        ControlKind.QrScanner -> Icons.Outlined.QrCodeScanner
        ControlKind.ScreenRecord -> Icons.Outlined.ScreenShare
        ControlKind.SplitScreen -> Icons.Outlined.Splitscreen
        ControlKind.MiniMode -> Icons.Outlined.StayCurrentPortrait
        ControlKind.ScrollAssistant -> Icons.Outlined.Swipe
    }
    Icon(icon, label, tint = OverlayAccent, modifier = Modifier.size(22.dp))
}

private fun aguiControlIcon(context: android.content.Context, kind: ControlKind): Drawable? {
    val name = when (kind) {
        ControlKind.MiniMode -> "ic_mini_mode"
        ControlKind.ScrollAssistant -> "ic_touchpad_qs"
        else -> return null
    }
    return runCatching {
        val systemUi = context.createPackageContext("com.android.systemui", 0)
        val id = systemUi.resources.getIdentifier(name, "drawable", "com.android.systemui")
        id.takeIf { it != 0 }?.let(systemUi::getDrawable)
    }.getOrNull()
}

@Composable
private fun OverlayAliasSuggestions(
    suggestions: List<AliasSuggestion>,
    selectedIndex: Int,
    visibleRowCount: Int,
    palette: OverlayPalette,
    onClick: (AliasSuggestion) -> Unit,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(selectedIndex, suggestions.firstOrNull()?.alias, suggestions.size) {
        if (selectedIndex in suggestions.indices) listState.keepSelectionVisible(selectedIndex, visibleRowCount)
    }
    LazyColumn(
        Modifier.fillMaxWidth().height(resultListHeight(suggestions.size, visibleRowCount)),
        state = listState,
        contentPadding = PaddingValues(bottom = ResultViewportBottomPadding),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
    itemsIndexed(suggestions, key = { _, suggestion -> "${suggestion.alias}:${suggestion.subtitle}" }) { index, suggestion ->
        OverlayAliasSuggestionRow(suggestion, index == selectedIndex, palette, onClick)
    }
    }
}

@Composable
private fun OverlayAliasSuggestionRow(
    suggestion: AliasSuggestion,
    selected: Boolean,
    palette: OverlayPalette,
    onClick: (AliasSuggestion) -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val icon = remember(suggestion.packageName, suggestion.taskerAlias?.iconBase64) {
        suggestion.taskerAlias?.let { TaskerAliases.icon(context, it) }
            ?: suggestion.packageName?.let { packageName ->
                runCatching { context.packageManager.getApplicationIcon(packageName) }.getOrNull()
            }
    }
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp))
            .background(if (selected) OverlayAccent.copy(alpha = .22f) else palette.tile)
            .border(1.dp, if (selected) OverlayAccent else palette.border, RoundedCornerShape(15.dp))
            .clickable { onClick(suggestion) }.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (suggestion.label == "Quick Controls") {
            Box(Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(OverlayAccent.copy(alpha = .16f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Tune, "Quick Controls", tint = OverlayAccent, modifier = Modifier.size(24.dp))
            }
        } else RealAppIcon(icon, suggestion.label, palette, Modifier.size(42.dp))
        Column(Modifier.padding(start = 13.dp).weight(1f)) {
            Text(suggestion.label, color = palette.text, fontFamily = OverlaySans, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Text(
                "${suggestion.alias}  ·  ${suggestion.subtitle}",
                color = OverlayAccent,
                fontFamily = OverlayMono,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (selected) Text("↵", color = OverlayAccent, fontFamily = OverlayMono, fontWeight = FontWeight.Bold, fontSize = 24.sp)
    }
}

@Composable
private fun OverlayTaskerResults(
    tasks: List<TaskerAlias>,
    selectedIndex: Int,
    visibleRowCount: Int,
    palette: OverlayPalette,
    onClick: (TaskerAlias) -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val listState = rememberLazyListState()
    LaunchedEffect(selectedIndex, tasks.firstOrNull()?.taskName, tasks.size) {
        if (selectedIndex in tasks.indices) listState.keepSelectionVisible(selectedIndex, visibleRowCount)
    }
    LazyColumn(
        Modifier.fillMaxWidth()
            .heightIn(max = resultListHeight(tasks.size, visibleRowCount)),
        state = listState,
        contentPadding = PaddingValues(bottom = ResultViewportBottomPadding),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
    itemsIndexed(tasks, key = { _, task -> "${task.alias}:${task.taskName}" }) { index, task ->
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp))
                .background(if (index == selectedIndex) OverlayAccent.copy(alpha = .22f) else palette.tile)
                .border(1.dp, if (index == selectedIndex) OverlayAccent else palette.border, RoundedCornerShape(15.dp))
                .clickable { onClick(task) }.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val taskIcon = remember(task.iconBase64) { TaskerAliases.icon(context, task) }
            RealAppIcon(taskIcon, task.label, palette, Modifier.size(42.dp))
            Column(Modifier.padding(start = 13.dp).weight(1f)) {
                Text(task.label, color = palette.text, fontFamily = OverlaySans, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(task.taskName, color = palette.secondary, fontFamily = OverlayMono, fontSize = 10.sp)
            }
            if (index == selectedIndex) Text("↵", color = OverlayAccent, fontFamily = OverlayMono, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        }
    }
    }
}

private fun TodoistTask.isOverdue(): Boolean = dueDate?.take(10)?.let { it < java.time.LocalDate.now().toString() } == true

private fun TodoistTask.isDueToday(): Boolean =
    dueDate?.take(10) == java.time.LocalDate.now().toString()

private fun TodoistTask.dueDateLabel(): String {
    val date = runCatching { java.time.LocalDate.parse(dueDate?.take(10)) }.getOrNull()
    return when {
        date == null -> "Today"
        date == java.time.LocalDate.now() -> "Today"
        else -> date.format(java.time.format.DateTimeFormatter.ofPattern("d MMM"))
    }
}

private fun TodoistTask.dueTimeLabel(context: android.content.Context): String? {
    val raw = dueDateTime ?: return null
    val instant = runCatching { java.time.OffsetDateTime.parse(raw).toInstant() }
        .recoverCatching { java.time.LocalDateTime.parse(raw).atZone(java.time.ZoneId.systemDefault()).toInstant() }
        .getOrNull() ?: return raw.substringAfter('T', "").take(5).takeIf(String::isNotBlank)
    return android.text.format.DateFormat.getTimeFormat(context).format(java.util.Date.from(instant))
}

private fun TodoistTask.todoistSection(page: TodoistPage): String {
    if (isOverdue()) return "overdue"
    val date = runCatching { java.time.LocalDate.parse(dueDate?.take(10)) }.getOrNull() ?: return page.title.lowercase()
    val today = java.time.LocalDate.now()
    return when (date) {
        today -> "today"
        today.plusDays(1) -> "tomorrow"
        else -> date.format(java.time.format.DateTimeFormatter.ofPattern("EEE, d MMM")).lowercase()
    }
}

@Composable
private fun OverlayTodoistCalendar(
    tasks: List<TodoistTask>,
    selectedDate: java.time.LocalDate,
    palette: OverlayPalette,
    onDate: (java.time.LocalDate) -> Unit,
) {
    val month = java.time.YearMonth.from(selectedDate)
    val counts = remember(tasks) {
        tasks.mapNotNull { it.dueDate?.take(10) }
            .groupingBy { it }
            .eachCount()
    }
    val first = month.atDay(1)
    val leading = first.dayOfWeek.value - 1
    val cells = List(leading) { null } + (1..month.lengthOfMonth()).map(month::atDay)
    val padded = cells + List((7 - cells.size % 7) % 7) { null }

    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp))
            .background(palette.tile)
            .border(1.dp, palette.border, RoundedCornerShape(15.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "‹",
                color = OverlayAccent,
                fontFamily = OverlayMono,
                fontSize = 20.sp,
                modifier = Modifier.clickable {
                    onDate(month.minusMonths(1).atDay(selectedDate.dayOfMonth.coerceAtMost(month.minusMonths(1).lengthOfMonth())))
                }.padding(horizontal = 7.dp),
            )
            Text(
                month.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy")),
                color = palette.text,
                fontFamily = OverlaySans,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Text(
                "›",
                color = OverlayAccent,
                fontFamily = OverlayMono,
                fontSize = 20.sp,
                modifier = Modifier.clickable {
                    onDate(month.plusMonths(1).atDay(selectedDate.dayOfMonth.coerceAtMost(month.plusMonths(1).lengthOfMonth())))
                }.padding(horizontal = 7.dp),
            )
        }
        Row(Modifier.fillMaxWidth()) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                Text(
                    day,
                    color = palette.secondary,
                    fontFamily = OverlayMono,
                    fontSize = 9.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.weight(1f).padding(vertical = 3.dp),
                )
            }
        }
        padded.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    val selected = date == selectedDate
                    val today = date == java.time.LocalDate.now()
                    val count = date?.let { counts[it.toString()] }.orZero()
                    Box(
                        Modifier.weight(1f).height(34.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (selected) OverlayAccent.copy(alpha = .30f) else Color.Transparent)
                            .border(
                                if (selected || today) 1.dp else 0.dp,
                                if (selected) OverlayAccent else palette.border,
                                RoundedCornerShape(9.dp),
                            )
                            .then(if (date != null) Modifier.clickable { onDate(date) } else Modifier),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (date != null) {
                            Text(
                                date.dayOfMonth.toString(),
                                color = if (selected) palette.text else palette.secondary,
                                fontFamily = OverlayMono,
                                fontWeight = if (selected || today) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 10.sp,
                            )
                            if (count > 0) {
                                Box(
                                    Modifier.align(Alignment.BottomCenter).padding(bottom = 2.dp)
                                        .size(if (count > 1) 4.dp else 3.dp).clip(CircleShape).background(OverlayAccent),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun Int?.orZero() = this ?: 0

@Composable
private fun OverlayTodoistTasks(
    tasks: List<TodoistTask>,
    calendarTasks: List<TodoistTask>,
    page: TodoistPage,
    calendarDate: java.time.LocalDate,
    selectedIndex: Int,
    visibleRowCount: Int,
    palette: OverlayPalette,
    loading: Boolean,
    error: String?,
    completingTaskId: String?,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onCalendarDate: (java.time.LocalDate) -> Unit,
    onOpen: (TodoistTask) -> Unit,
    onComplete: (TodoistTask) -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Row(
        Modifier.fillMaxWidth().padding(start = 6.dp, end = 6.dp, bottom = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("‹", color = if (page == TodoistPage.Today) palette.border else OverlayAccent, fontFamily = OverlayMono, fontSize = 22.sp, modifier = Modifier.clickable(enabled = page != TodoistPage.Today, onClick = onPreviousPage))
        Text(page.title, color = OverlayAccent, fontFamily = OverlayMono, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Text("›", color = if (page == TodoistPage.Calendar) palette.border else OverlayAccent, fontFamily = OverlayMono, fontSize = 22.sp, modifier = Modifier.clickable(enabled = page != TodoistPage.Calendar, onClick = onNextPage))
    }
    if (page == TodoistPage.Calendar) {
        OverlayTodoistCalendar(
            tasks = calendarTasks,
            selectedDate = calendarDate,
            palette = palette,
            onDate = onCalendarDate,
        )
        Spacer(Modifier.height(8.dp))
    }
    if (tasks.isEmpty()) {
        OverlayNotice(
            error ?: if (loading) "loading ${page.title.lowercase()} tasks…"
            else if (page == TodoistPage.Calendar) "nothing due ${calendarDate.format(java.time.format.DateTimeFormatter.ofPattern("d MMM"))}"
            else "nothing in ${page.title.lowercase()}",
            palette,
        )
        return
    }
    val state = rememberLazyListState()
    LaunchedEffect(selectedIndex, tasks.size) {
        if (selectedIndex in tasks.indices) state.keepSelectionVisible(selectedIndex, visibleRowCount)
    }
    LazyColumn(
        // Task rows can contain a section label plus separate date and time
        // metadata, so they are taller than standard app results. Let a short
        // filtered list measure to its content while keeping a bounded viewport.
        Modifier.fillMaxWidth().heightIn(
            max = resultListHeight(tasks.size, visibleRowCount) + 38.dp,
        ),
        state = state,
        contentPadding = PaddingValues(top = 8.dp, bottom = ResultViewportBottomPadding),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(tasks, key = { _, task -> task.id }) { index, task ->
            Column {
                val completing = task.id == completingTaskId
                val section = task.todoistSection(page)
                val previousSection = tasks.getOrNull(index - 1)?.todoistSection(page)
                if (index == 0 || section != previousSection) {
                    Row(Modifier.fillMaxWidth().padding(start = 5.dp, end = 7.dp, bottom = 5.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        val sectionColor = when (section) {
                            "overdue" -> Color(0xFFFF5B65)
                            "today" -> Color(0xFF54C98A)
                            else -> OverlayAccent
                        }
                        Text(section, color = sectionColor, fontFamily = OverlayMono, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text("${tasks.count { it.todoistSection(page) == section }} tasks", color = palette.secondary, fontFamily = OverlayMono, fontSize = 10.sp)
                    }
                }
                val completionGlow by animateFloatAsState(
                    targetValue = if (completing) 1f else 0f,
                    animationSpec = tween(125),
                    label = "todoist-task-completion",
                )
                val taskShape = RoundedCornerShape(15.dp)
                Box(Modifier.fillMaxWidth()) {
                    if (completionGlow > 0f) {
                        Box(
                            Modifier.matchParentSize()
                                .blur(12.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                                .border(4.dp, OverlayAccent.copy(alpha = .78f * completionGlow), taskShape),
                        )
                    }
                    Row(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(15.dp))
                        .background(palette.tile)
                        .background(OverlayAccent.copy(alpha = .30f * completionGlow))
                        .border(1.dp, if (completing || index == selectedIndex) OverlayAccent.copy(alpha = .88f) else palette.border, taskShape)
                        .clickable { onOpen(task) }.padding(horizontal = 12.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        if (completing) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                        contentDescription = "Complete task",
                        tint = when (task.priority) {
                            4 -> Color(0xFFFF5B65)
                            3 -> Color(0xFFFF9A42)
                            2 -> Color(0xFF54A8FF)
                            else -> if (index == selectedIndex) OverlayAccent else palette.secondary
                        },
                        modifier = Modifier.size(27.dp).clickable { onComplete(task) },
                    )
                    Column(Modifier.padding(start = 12.dp).weight(1f)) {
                        Text(task.content, color = palette.text, fontFamily = OverlaySans, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        val secondary = listOfNotNull(task.project.takeIf(String::isNotBlank), task.labels.takeIf(List<String>::isNotEmpty)?.joinToString(" · ")).joinToString("  ·  ")
                        if (secondary.isNotBlank()) Text(secondary, color = palette.secondary, fontFamily = OverlayMono, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        if (task.isOverdue() || task.isDueToday()) {
                            val dueColor = if (task.isDueToday()) Color(0xFF54C98A) else Color(0xFFFF5B65)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Event, null, tint = dueColor, modifier = Modifier.size(15.dp))
                                Text(task.dueDateLabel(), color = dueColor, fontFamily = OverlayMono, fontSize = 10.sp, modifier = Modifier.padding(start = 4.dp))
                            }
                        }
                        task.dueTimeLabel(context)?.let { dueTime ->
                            Row(Modifier.padding(top = if (task.isOverdue() || task.isDueToday()) 4.dp else 0.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Schedule, null, tint = palette.secondary, modifier = Modifier.size(15.dp))
                                Text(dueTime, color = palette.secondary, fontFamily = OverlayMono, fontSize = 10.sp, modifier = Modifier.padding(start = 4.dp))
                            }
                        }
                        if (task.recurring) Icon(Icons.Outlined.Repeat, "Recurring", tint = palette.secondary, modifier = Modifier.padding(top = 4.dp).size(14.dp))
                    }
                    // Keep this slot present on every row so moving selection
                    // never shifts the date and time column sideways.
                    Box(Modifier.width(29.dp), contentAlignment = Alignment.CenterEnd) {
                        if (index == selectedIndex) Text("↵", color = OverlayAccent, fontFamily = OverlayMono, fontWeight = FontWeight.Bold, fontSize = 21.sp)
                    }
                    }
                }
            }
        }
    }
}

@Composable
private fun OverlayRecentConversation(item: HubItem, palette: OverlayPalette) {
    val history = (if (item.messages.isNotEmpty()) item.messages.takeLast(6)
    else listOf(HubMessage(item.text, item.title, false))).filter { it.text.isNotBlank() }
    Column(Modifier.fillMaxWidth().padding(horizontal = 5.dp, vertical = 4.dp)) {
        Text(
            item.conversationTitle?.takeIf(String::isNotBlank) ?: item.title,
            color = OverlayAccent,
            fontFamily = OverlaySerif,
            fontSize = 21.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp).padding(bottom = 7.dp),
        )
        val state = rememberLazyListState()
        LaunchedEffect(history.size, history.lastOrNull()?.text) {
            if (history.isNotEmpty()) state.scrollToItem(history.lastIndex)
        }
        LazyColumn(
            modifier = Modifier.fillMaxWidth().heightIn(max = 245.dp),
            state = state,
            verticalArrangement = Arrangement.spacedBy(7.dp),
            contentPadding = PaddingValues(horizontal = 7.dp, vertical = 3.dp),
        ) {
            itemsIndexed(history) { _, message ->
                val incoming = !message.outgoing
                Box(Modifier.fillMaxWidth()) {
                    Box(
                        Modifier.align(if (incoming) Alignment.CenterStart else Alignment.CenterEnd)
                            .fillMaxWidth(if (incoming) .88f else .78f)
                            .clip(RoundedCornerShape(15.dp))
                            .background(if (incoming) palette.tile else OverlayAccent.copy(alpha = .18f))
                            .border(1.dp, if (incoming) palette.border else OverlayAccent.copy(alpha = .44f), RoundedCornerShape(15.dp))
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                    ) {
                        Column {
                            if (item.conversationTitle != null && incoming && !message.sender.isNullOrBlank()) {
                                Text(message.sender, color = OverlayAccent, fontFamily = OverlaySans, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                Spacer(Modifier.height(2.dp))
                            }
                            Text(message.text, color = palette.text, fontFamily = OverlaySans, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

private fun resultViewportHeight(visibleRowCount: Int) =
    (visibleRowCount * 66 + (visibleRowCount - 1).coerceAtLeast(0) * 8).dp

@Composable
private fun OverlayFileResults(
    files: List<FileResult>,
    selectedIndex: Int,
    visibleRowCount: Int,
    palette: OverlayPalette,
    sortMode: FileSortMode,
    listState: LazyListState,
    onVisibleIndicesChanged: (List<Int>) -> Unit,
    onLongClick: (FileResult) -> Unit,
    onClick: (FileResult) -> Unit,
) {
    if (files.isEmpty()) {
        OverlayNotice("no matching files", palette)
        return
    }
    LaunchedEffect(selectedIndex, files.firstOrNull()?.uri, files.size) {
        if (selectedIndex in files.indices) listState.keepSelectionVisible(selectedIndex, visibleRowCount)
    }
    LaunchedEffect(listState, files.size) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.map { it.index } }
            .distinctUntilChanged()
            .collect(onVisibleIndicesChanged)
    }
    LazyColumn(
        Modifier.fillMaxWidth().height(resultListHeight(files.size, visibleRowCount)),
        state = listState,
        contentPadding = PaddingValues(bottom = ResultViewportBottomPadding),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
    itemsIndexed(files, key = { index, file -> "${file.uri}:$index" }) { index, file ->
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(palette.tile)
                .border(1.dp, if (index == selectedIndex) OverlayAccent.copy(alpha = .7f) else palette.border, RoundedCornerShape(15.dp))
                .combinedClickable(onClick = { onClick(file) }, onLongClick = { onLongClick(file) }).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (file.thumbnail != null) Image(
                BitmapPainter(file.thumbnail.asImageBitmap()), file.name, Modifier.size(38.dp).clip(RoundedCornerShape(8.dp)),
            ) else Box(
                Modifier.size(38.dp).clip(RoundedCornerShape(8.dp)).background(if (index == selectedIndex) OverlayAccent.copy(alpha = .16f) else palette.panel),
                contentAlignment = Alignment.Center,
            ) {
                if (file.isDirectory) Icon(
                    Icons.Rounded.Folder,
                    contentDescription = "Folder",
                    tint = if (index == selectedIndex) OverlayAccent else palette.secondary,
                    modifier = Modifier.size(22.dp),
                ) else Icon(
                    fileTypeIcon(file),
                    contentDescription = file.mimeType ?: "File",
                    tint = if (index == selectedIndex) OverlayAccent else palette.secondary,
                    modifier = Modifier.size(22.dp),
                )
            }
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text(file.name, color = palette.text, fontFamily = OverlaySans, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(file.location.ifBlank { if (file.isDirectory) "folder" else file.mimeType ?: "file" }, color = palette.secondary, fontFamily = OverlayMono, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (sortMode == FileSortMode.Relevance) {
                if (index == selectedIndex) Text("↵", color = OverlayAccent, fontFamily = OverlayMono, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            } else Column(
                modifier = Modifier.padding(start = 8.dp).widthIn(min = 72.dp, max = 96.dp),
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    modifiedLabel(file.modifiedAtMillis),
                    color = palette.secondary,
                    fontFamily = OverlayMono,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (index == selectedIndex) Text("↵", color = OverlayAccent, fontFamily = OverlayMono, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            }
            Icon(
                Icons.Outlined.MoreVert,
                contentDescription = "File actions",
                tint = if (index == selectedIndex) OverlayAccent else palette.secondary.copy(alpha = .72f),
                modifier = Modifier.padding(start = 5.dp).size(25.dp).clickable { onLongClick(file) },
            )
        }
    }
    }
}

private fun fileTypeIcon(file: FileResult): ImageVector {
    val mime = file.mimeType.orEmpty().lowercase()
    val extension = file.name.substringAfterLast('.', "").lowercase()
    return when {
        mime == "application/pdf" || extension == "pdf" -> Icons.Outlined.PictureAsPdf
        extension in setOf("epub", "mobi", "azw", "azw3", "fb2") -> Icons.Outlined.MenuBook
        mime.startsWith("audio/") -> Icons.Outlined.AudioFile
        mime.startsWith("video/") -> Icons.Outlined.VideoFile
        mime.contains("zip") || mime.contains("compressed") || extension in setOf("zip", "rar", "7z", "tar", "gz", "bz2", "xz") -> Icons.Outlined.Archive
        mime.contains("spreadsheet") || mime.contains("excel") || extension in setOf("xls", "xlsx", "ods", "csv", "tsv") -> Icons.Outlined.TableChart
        mime.contains("presentation") || mime.contains("powerpoint") || extension in setOf("ppt", "pptx", "odp") -> Icons.Outlined.Slideshow
        mime.startsWith("text/") && extension in setOf("kt", "java", "js", "ts", "py", "html", "css", "json", "xml", "md", "sh", "yaml", "yml") -> Icons.Outlined.Code
        mime.startsWith("text/") || mime.contains("document") || mime.contains("word") || extension in setOf("doc", "docx", "odt", "rtf", "txt") -> Icons.Outlined.Description
        else -> Icons.Outlined.InsertDriveFile
    }
}

@Composable
private fun OverlayFileActions(
    target: FileResult,
    actions: List<FileAction>,
    selectedIndex: Int,
    visibleRowCount: Int,
    palette: OverlayPalette,
    onClick: (FileAction) -> Unit,
) {
    if (actions.isEmpty()) {
        OverlayNotice("no matching actions", palette)
        return
    }
    val state = rememberLazyListState()
    LaunchedEffect(selectedIndex, actions.size) {
        if (selectedIndex in actions.indices) state.keepSelectionVisible(selectedIndex, visibleRowCount)
    }
    LazyColumn(
        Modifier.fillMaxWidth().height(resultListHeight(actions.size, visibleRowCount)),
        state = state,
        contentPadding = PaddingValues(bottom = ResultViewportBottomPadding),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(actions, key = { _, action -> action.name }) { index, action ->
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(palette.tile)
                    .border(1.dp, if (index == selectedIndex) OverlayAccent.copy(alpha = .76f) else palette.border, RoundedCornerShape(15.dp))
                    .clickable { onClick(action) }.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(38.dp).clip(RoundedCornerShape(11.dp))
                        .background(if (index == selectedIndex) OverlayAccent.copy(alpha = .16f) else palette.panel),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = when (action) {
                            FileAction.Open -> Icons.Outlined.InsertDriveFile
                            FileAction.OpenWith -> Icons.Outlined.OpenInNew
                            FileAction.Reveal -> Icons.Outlined.FolderOpen
                            FileAction.Preview -> Icons.Outlined.Visibility
                            FileAction.Info -> Icons.Outlined.Info
                            FileAction.Delete -> Icons.Outlined.Delete
                        },
                        contentDescription = action.title,
                        tint = if (action == FileAction.Delete) Color(0xFFFF4D3A) else if (index == selectedIndex) OverlayAccent else palette.secondary,
                        modifier = Modifier.size(21.dp),
                    )
                }
                Column(Modifier.padding(start = 12.dp).weight(1f)) {
                    Text(action.title, color = if (action == FileAction.Delete) Color(0xFFFF4D3A) else palette.text, fontFamily = OverlaySans, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(action.subtitle, color = palette.secondary, fontFamily = OverlayMono, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (index == selectedIndex) Text("↵", color = OverlayAccent, fontFamily = OverlayMono, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            }
        }
    }
}

@Composable
private fun OverlayFileActionDetail(
    target: FileResult,
    detail: String,
    preview: android.graphics.Bitmap?,
    palette: OverlayPalette,
    onConfirmDelete: (() -> Unit)? = null,
) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(palette.tile)
            .border(1.dp, OverlayAccent.copy(alpha = .65f), RoundedCornerShape(15.dp)).padding(14.dp),
    ) {
        Text(target.name, color = palette.text, fontFamily = OverlaySans, fontWeight = FontWeight.Bold, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(8.dp))
        if (preview != null) {
            Image(
                preview.asImageBitmap(),
                target.name,
                Modifier.fillMaxWidth().heightIn(min = 130.dp, max = 250.dp).clip(RoundedCornerShape(11.dp)),
                contentScale = ContentScale.Fit,
            )
        } else {
            Text(detail, color = palette.secondary, fontFamily = OverlayMono, fontSize = 12.sp, lineHeight = 18.sp)
            if (onConfirmDelete != null) {
                Spacer(Modifier.height(14.dp))
                Text(
                    "delete file",
                    color = Color(0xFFFF4D3A),
                    fontFamily = OverlayMono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFFFF4D3A).copy(alpha = .72f), RoundedCornerShape(10.dp))
                        .clickable(onClick = onConfirmDelete)
                        .padding(horizontal = 13.dp, vertical = 8.dp),
                )
            }
        }
    }
}

private fun modifiedLabel(timestamp: Long): String {
    if (timestamp <= 0L) return "date unknown"
    val age = System.currentTimeMillis() - timestamp
    return if (age in 0 until 7L * 24 * 60 * 60 * 1000) {
        android.text.format.DateUtils.getRelativeTimeSpanString(
            timestamp,
            System.currentTimeMillis(),
            android.text.format.DateUtils.MINUTE_IN_MILLIS,
        ).toString()
    } else {
        java.text.SimpleDateFormat("d MMM yy", java.util.Locale.getDefault()).format(java.util.Date(timestamp))
    }
}

@Composable
private fun OverlayFileAccess(palette: OverlayPalette, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(palette.tile)
            .border(1.dp, OverlayAccent.copy(alpha = .7f), RoundedCornerShape(15.dp))
            .clickable(onClick = onClick).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("▤", color = OverlayAccent, fontFamily = OverlayMono, fontSize = 21.sp)
        Column(Modifier.padding(start = 12.dp).weight(1f)) {
            Text("allow file search", color = palette.text, fontFamily = OverlaySans, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("one-time Android storage access", color = palette.secondary, fontFamily = OverlayMono, fontSize = 11.sp)
        }
        Text("↵", color = OverlayAccent, fontFamily = OverlayMono, fontWeight = FontWeight.Bold, fontSize = 24.sp)
    }
}

@Composable
private fun OverlayAliasPreview(match: AliasMatch, icon: Drawable?, palette: OverlayPalette, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp))
            .background(Brush.horizontalGradient(listOf(OverlayAccent.copy(alpha = .28f), palette.tile)))
            .border(1.dp, OverlayAccent, RoundedCornerShape(15.dp)).clickable(onClick = onClick).padding(13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RealAppIcon(icon, match.target.label, palette, Modifier.size(40.dp))
        Column(Modifier.padding(start = 12.dp).weight(1f)) {
            Text(match.previewTitle ?: match.target.label, color = palette.text, fontFamily = OverlaySans, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(match.previewSubtitle ?: "search → ${match.query}", color = palette.secondary, fontFamily = OverlayMono, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text("↵", color = OverlayAccent, fontFamily = OverlayMono, fontWeight = FontWeight.Bold, fontSize = 24.sp)
    }
}

@Composable
private fun OverlayContactAccess(palette: OverlayPalette, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(palette.tile)
            .border(1.dp, OverlayAccent.copy(alpha = .7f), RoundedCornerShape(15.dp))
            .clickable(onClick = onClick).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("@", color = OverlayAccent, fontFamily = OverlayMono, fontWeight = FontWeight.Bold, fontSize = 21.sp)
        Column(Modifier.padding(start = 12.dp).weight(1f)) {
            Text("allow contact search", color = palette.text, fontFamily = OverlaySans, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("needed for names, calls and messages", color = palette.secondary, fontFamily = OverlayMono, fontSize = 11.sp)
        }
        Text("↵", color = OverlayAccent, fontFamily = OverlayMono, fontWeight = FontWeight.Bold, fontSize = 24.sp)
    }
}

@Composable
private fun OverlayContactResults(
    contacts: List<ContactResult>,
    parsed: ParsedCommand,
    selectedIndex: Int,
    visibleRowCount: Int,
    palette: OverlayPalette,
    onSelect: (ContactResult) -> Unit,
) {
    if (contacts.isEmpty()) {
        OverlayNotice("no matching contacts", palette)
        return
    }
    data class ContactGroup(val key: String, val personName: String?, val routes: List<IndexedValue<ContactResult>>)
    val groups = remember(contacts) {
        val commanderIdByName = contacts.asSequence()
            .filter { it.commanderMessagingContactId != null }
            .associate { it.name.lowercase().replace(Regex("[^\\p{L}\\p{N}]+"), " ").trim() to it.commanderMessagingContactId!! }
        val grouped = linkedMapOf<String, MutableList<IndexedValue<ContactResult>>>()
        contacts.forEachIndexed { index, contact ->
            val canonicalName = contact.name.lowercase().replace(Regex("[^\\p{L}\\p{N}]+"), " ").trim()
            val personId = contact.commanderMessagingContactId ?: commanderIdByName[canonicalName]
            val key = personId?.let { "person:$it" }
                ?: "route:${contact.hubNotificationKey ?: contact.messagingPlatformScope ?: contact.messagingIdentity ?: "$index:${contact.name}:${contact.number}"}"
            grouped.getOrPut(key) { mutableListOf() }.add(IndexedValue(index, contact))
        }
        grouped.map { (key, routes) ->
            val orderedRoutes = routes.sortedByDescending { it.value.messagingIsDefault }
            ContactGroup(key, key.takeIf { it.startsWith("person:") }?.let { orderedRoutes.first().value.name }, orderedRoutes)
        }
    }
    val selectedGroupIndex = groups.indexOfFirst { group -> group.routes.any { it.index == selectedIndex } }.coerceAtLeast(0)
    val displayUnits = contacts.size + groups.count { it.personName != null }
    val listState = rememberLazyListState()
    LaunchedEffect(selectedIndex, groups.size) {
        if (selectedIndex in contacts.indices) listState.keepSelectionVisible(selectedGroupIndex, groups.size.coerceAtMost(visibleRowCount))
    }
    LazyColumn(
        Modifier.fillMaxWidth().height(resultListHeight(displayUnits, visibleRowCount)),
        state = listState,
        contentPadding = PaddingValues(bottom = ResultViewportBottomPadding),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
    itemsIndexed(groups, key = { _, group -> group.key }) { groupIndex, group ->
        val first = group.routes.first().value
        val shape = RoundedCornerShape(15.dp)
            if (group.personName != null) {
                Column(Modifier.fillMaxWidth().clip(shape).background(palette.tile).border(1.dp, palette.border, shape).padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 2.dp, top = 0.dp, end = 2.dp, bottom = 6.dp)) {
                        Box(Modifier.size(38.dp).clip(CircleShape).background(palette.border), contentAlignment = Alignment.Center) {
                            first.avatar?.let { Image(BitmapPainter(it.asImageBitmap()), first.name, Modifier.fillMaxSize()) }
                                ?: Text(first.name.take(1).uppercase(), color = Color.White, fontFamily = OverlaySans, fontWeight = FontWeight.Bold)
                        }
                        Column(Modifier.padding(start = 11.dp).weight(1f)) {
                            Text(group.personName, color = palette.text, fontFamily = OverlaySans, fontWeight = FontWeight.Bold, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${group.routes.size} messaging ${if (group.routes.size == 1) "route" else "routes"}", color = palette.secondary, fontFamily = OverlayMono, fontSize = 10.sp)
                        }
                    }
                    group.routes.forEach { indexed ->
                        val contact = indexed.value
                        val selected = indexed.index == selectedIndex
                        val routeShape = RoundedCornerShape(11.dp)
                        Box(Modifier.fillMaxWidth().padding(top = 4.dp)) {
                            if (selected) Box(Modifier.matchParentSize().blur(8.dp, BlurredEdgeTreatment.Unbounded).border(2.dp, OverlayAccent.copy(alpha = .8f), routeShape))
                            Row(
                                Modifier.fillMaxWidth().clip(routeShape)
                                    .background(if (selected) OverlayAccent.copy(alpha = .20f) else Color.Transparent)
                                    .border(1.dp, if (selected) OverlayAccent else palette.border.copy(alpha = .65f), routeShape)
                                    .clickable { onSelect(contact) }.padding(horizontal = 10.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(Modifier.size(30.dp).clip(RoundedCornerShape(8.dp)).background(OverlayAccent.copy(alpha = .13f)), contentAlignment = Alignment.Center) {
                                    MessagingPlatformGlyph(contact)
                                }
                                Column(Modifier.padding(start = 10.dp).weight(1f)) {
                                    Text(contact.messagingPlatform ?: "Message", color = palette.text, fontFamily = OverlaySans, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(contact.label.substringAfter(" · ", contact.label), color = palette.secondary, fontFamily = OverlayMono, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                if (selected) Text("↵", color = OverlayAccent, fontFamily = OverlayMono, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            }
                        }
                    }
                }
            } else {
                val contact = first
                val selected = group.routes.first().index == selectedIndex
                Box(Modifier.fillMaxWidth()) {
                    if (selected) Box(Modifier.matchParentSize().blur(10.dp, BlurredEdgeTreatment.Unbounded).border(3.dp, OverlayAccent.copy(alpha = .82f), shape))
                    Row(
                        Modifier.fillMaxWidth().clip(shape).background(palette.tile)
                            .then(if (selected) Modifier.background(OverlayAccent.copy(alpha = .20f)) else Modifier)
                            .border(1.dp, if (selected) OverlayAccent else palette.border, shape)
                            .clickable { onSelect(contact) }.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val recentDestination = contact.hubNotificationKey == RecentMessagesDestination
                        val platformDestination = contact.messagingPlatformScope != null
                        Box(
                            Modifier.size(40.dp).clip(if (recentDestination || platformDestination) RoundedCornerShape(11.dp) else CircleShape)
                                .background(if (recentDestination || platformDestination) OverlayAccent.copy(alpha = .14f) else if (selected) OverlayAccent else palette.border),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (recentDestination) Icon(painterResource(R.drawable.ic_recent_messages), "Recent messages", tint = OverlayAccent, modifier = Modifier.size(23.dp))
                            else if (platformDestination) MessagingPlatformGlyph(contact)
                            else if (contact.avatar != null) Image(BitmapPainter(contact.avatar.asImageBitmap()), contact.name, Modifier.fillMaxSize())
                            else Text(contact.name.take(1).uppercase(), color = Color.White, fontFamily = OverlaySans, fontWeight = FontWeight.Bold)
                        }
                        Column(Modifier.padding(start = 13.dp).weight(1f)) {
                            Text(contact.name, color = palette.text, fontFamily = OverlaySans, fontWeight = FontWeight.Bold, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(contact.label, color = palette.secondary, fontFamily = OverlayMono, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        if (selected) Text("  ↵", color = OverlayAccent, fontFamily = OverlayMono, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun MessagingPlatformGlyph(contact: ContactResult, compact: Boolean = false) {
    val platform = contact.messagingPlatform
    val iconSize = if (compact) 11.dp else 21.dp
    if (contact.messagingPlatformIcon != null) {
        Image(
            painter = BitmapPainter(contact.messagingPlatformIcon.asImageBitmap()),
            contentDescription = platform ?: "Messaging app",
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(OverlayAccent),
            modifier = Modifier.size(iconSize),
        )
        return
    }
    val icon = when {
        contact.messagingPackageName == "com.instagram.android" || platform.equals("instagram", true) -> Icons.Outlined.CameraAlt
        contact.messagingPackageName == "com.Slack" || platform.equals("slack", true) -> Icons.Outlined.Tag
        contact.messagingPackageName == "com.microsoft.teams" || platform?.contains("teams", true) == true -> Icons.Outlined.Groups
        else -> when (platform?.lowercase()) {
        "sms", "messages" -> Icons.Outlined.ChatBubbleOutline
        "whatsapp" -> Icons.Outlined.PhoneInTalk
        "messenger" -> Icons.Outlined.Bolt
        "signal" -> Icons.Outlined.Sensors
        "telegram" -> Icons.Outlined.Send
        "discord" -> Icons.Outlined.Forum
            else -> null
        }
    }
    if (icon != null) {
        Icon(icon, platform ?: "Messaging app", tint = OverlayAccent, modifier = Modifier.size(iconSize))
    } else {
        Icon(Icons.Outlined.ChatBubbleOutline, platform ?: "Messaging app", tint = OverlayAccent, modifier = Modifier.size(iconSize))
    }
}

private fun resultListHeight(itemCount: Int, visibleRowCount: Int) =
    resultViewportHeight(minOf(itemCount, visibleRowCount).coerceAtLeast(1)) + ResultViewportBottomPadding

private suspend fun LazyListState.keepSelectionVisible(index: Int, visibleRowCount: Int) {
    if (index == 0) {
        scrollToItem(0)
        return
    }
    val visible = layoutInfo.visibleItemsInfo
    if (visible.isEmpty()) {
        scrollToItem(index)
        return
    }
    val selected = visible.firstOrNull { it.index == index }
    val fullyVisible = selected != null && selected.offset >= layoutInfo.viewportStartOffset &&
        selected.offset + selected.size <= layoutInfo.viewportEndOffset
    if (fullyVisible) return
    val target = if (index < firstVisibleItemIndex) index else (index - visibleRowCount + 1).coerceAtLeast(0)
    animateScrollToItem(target)
}

@Composable
private fun OverlaySelectedContact(contact: ContactResult, kind: CommandKind, palette: OverlayPalette) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(150)) + slideInVertically(tween(200)) { it / 3 },
    ) {
        val shape = RoundedCornerShape(15.dp)
        Box(Modifier.fillMaxWidth()) {
        Box(
            Modifier.matchParentSize().blur(10.dp, BlurredEdgeTreatment.Unbounded)
                .border(3.dp, OverlayAccent.copy(alpha = .82f), shape),
        )
        Row(
            Modifier.fillMaxWidth().clip(shape).background(palette.tile)
                .background(OverlayAccent.copy(alpha = .20f))
                .border(1.dp, OverlayAccent.copy(alpha = .7f), shape)
                .padding(horizontal = 13.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(OverlayAccent),
                contentAlignment = Alignment.Center,
            ) {
                if (contact.avatar != null) Image(
                    painter = BitmapPainter(contact.avatar.asImageBitmap()),
                    contentDescription = contact.name,
                    modifier = Modifier.fillMaxSize(),
                ) else Text(contact.name.take(1).uppercase(), color = Color.White, fontFamily = OverlaySans, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                if (contact.messagingPlatform != null || contact.messagingPackageName != null) {
                    Box(
                        Modifier.align(Alignment.BottomEnd).size(16.dp).clip(CircleShape).background(palette.tile)
                            .border(1.dp, OverlayAccent.copy(alpha = .7f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        MessagingPlatformGlyph(contact, compact = true)
                    }
                }
            }
            Column(Modifier.padding(start = 13.dp).weight(1f)) {
                Text(contact.name, color = palette.text, fontFamily = OverlaySans, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(
                    if (kind == CommandKind.Message) "type message${contact.messagingPlatform?.let { " · $it" }.orEmpty()}" else "ready to call",
                    color = palette.secondary,
                    fontFamily = OverlayMono,
                    fontSize = 11.sp,
                )
            }
            Text(contact.label, color = palette.secondary, fontFamily = OverlayMono, fontSize = 12.sp)
        }
        }
    }
}

@Composable
private fun OverlayActionPreview(command: ParsedCommand, palette: OverlayPalette, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(
            Brush.horizontalGradient(listOf(OverlayAccent.copy(alpha = .18f), palette.tile, palette.tile)),
        )
            .border(1.dp, OverlayAccent.copy(alpha = .65f), RoundedCornerShape(15.dp))
            .clickable(onClick = onClick).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(command.text.ifBlank { command.kind.label }, Modifier.weight(1f), color = palette.text, fontFamily = OverlaySans, fontWeight = FontWeight.Bold, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(overlayActionLabel(command.kind), color = palette.secondary, fontFamily = OverlayMono, fontSize = 12.sp)
        Text("  ↵", color = OverlayAccent, fontFamily = OverlayMono, fontWeight = FontWeight.Bold, fontSize = 22.sp)
    }
}

@Composable
private fun OverlayCalculatorResult(result: String, palette: OverlayPalette, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp))
            .background(palette.tile)
            .border(1.dp, OverlayAccent.copy(alpha = .65f), RoundedCornerShape(15.dp))
            .clickable(onClick = onClick).padding(horizontal = 15.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("=", color = OverlayAccent, fontFamily = OverlayMono, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Text(result, Modifier.padding(start = 11.dp).weight(1f), color = palette.text, fontFamily = OverlayMono, fontWeight = FontWeight.Bold, fontSize = 23.sp)
        Text("calculate", color = palette.secondary, fontFamily = OverlayMono, fontSize = 11.sp)
    }
}

@Composable
private fun OverlayNotice(text: String, palette: OverlayPalette) {
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(palette.tile).border(1.dp, palette.border, RoundedCornerShape(15.dp)).padding(14.dp)) {
        Text(text, color = palette.secondary, fontFamily = OverlayMono, fontSize = 13.sp)
    }
}

@Composable
private fun RealAppIcon(drawable: Drawable?, label: String, palette: OverlayPalette, modifier: Modifier = Modifier) {
    val painter = remember(drawable) {
        drawable?.let { BitmapPainter(it.toBitmap(width = 96, height = 96).asImageBitmap()) }
    }
    if (painter != null) {
        Image(painter = painter, contentDescription = label, modifier = modifier.clip(RoundedCornerShape(10.dp)))
    } else {
        Box(modifier.clip(RoundedCornerShape(10.dp)).background(palette.border), contentAlignment = Alignment.Center) {
            Text(label.take(1).uppercase(), color = palette.text, fontFamily = OverlaySans, fontWeight = FontWeight.Bold)
        }
    }
}

private object TodoistRecognitionTransformation : VisualTransformation {
    private const val weekday = "(?:mon(?:day)?|tue(?:s|sday)?|wed(?:s|nesday)?|thu(?:r|rs|rsday)?|fri(?:day)?|sat(?:urday)?|sun(?:day)?)"
    private val patterns = listOf(
        TodoistNaturalLanguage.compactTimeRecognitionPattern,
        Regex("(?i)(?<!\\w)p[1-4](?!\\w)"),
        Regex("(?<!\\w)[#@+][\\p{L}\\p{N}_-]+"),
        Regex("(?<!\\w)/[\\p{L}\\p{N}_-]+"),
        Regex("(?i)![0-9]{1,2}(?::[0-9]{2})?(?: ?(?:am|pm|min|m|h))?(?: before)?"),
        Regex("\\{[^}]+\\}"),
        Regex("(?i)\\b(?:tod|today|tom|tomorrow|tonight|this (?:morning|afternoon|evening)|next (?:week|month|year)|every (?:day|weekday|week|month|year|$weekday)|(?:next|this) $weekday|$weekday|in \\d+ (?:days?|weeks?|months?|years?))\\b"),
        Regex("(?i)\\b(?:at )?(?:[01]?\\d|2[0-3])(?::[0-5]\\d)? ?(?:am|pm)\\b"),
        Regex("(?i)\\b(?:at )?(?:[01]?\\d|2[0-3]):[0-5]\\d\\b"),
        Regex("(?i)\\b(?:jan(?:uary)?|feb(?:ruary)?|mar(?:ch)?|apr(?:il)?|may|jun(?:e)?|jul(?:y)?|aug(?:ust)?|sep(?:tember)?|oct(?:ober)?|nov(?:ember)?|dec(?:ember)?) \\d{1,2}(?:st|nd|rd|th)?\\b"),
    )

    override fun filter(text: AnnotatedString): TransformedText {
        val styled = buildAnnotatedString {
            append(text)
            if (text.startsWith("-")) addStyle(SpanStyle(color = OverlayAccent), 0, 1)
            patterns.forEach { pattern ->
                pattern.findAll(text.text).forEach { match ->
                    addStyle(
                        SpanStyle(color = OverlayAccent, fontWeight = FontWeight.Bold),
                        match.range.first,
                        match.range.last + 1,
                    )
                }
            }
        }
        return TransformedText(styled, OffsetMapping.Identity)
    }
}

private object TimerRecognitionTransformation : VisualTransformation {
    private val patterns = listOf(
        Regex("(?i)\\b(?:1[0-2]|0?[1-9])(?::?[0-5]\\d)?\\s*(?:a|p)\\.?m\\.?\\b"),
        Regex("(?i)\\b\\d+(?:\\.\\d+)?\\s*(?:seconds?|secs?|s|minutes?|mins?|m|hours?|hrs?|h)\\b"),
    )

    override fun filter(text: AnnotatedString): TransformedText {
        val styled = buildAnnotatedString {
            append(text)
            if (text.startsWith("+")) addStyle(SpanStyle(color = OverlayAccent), 0, 1)
            patterns.forEach { pattern ->
                pattern.findAll(text.text).forEach { match ->
                    addStyle(
                        SpanStyle(color = OverlayAccent, fontWeight = FontWeight.Bold),
                        match.range.first,
                        match.range.last + 1,
                    )
                }
            }
        }
        return TransformedText(styled, OffsetMapping.Identity)
    }
}

/**
 * Holds a changing visual rule without changing the VisualTransformation
 * instance passed to BasicTextField. Replacing that instance restarts some
 * hardware IMEs during rapid input.
 */
private class DelegatingVisualTransformation : VisualTransformation {
    var delegate: VisualTransformation = VisualTransformation.None

    override fun filter(text: AnnotatedString): TransformedText = delegate.filter(text)
}

private class SentinelTransformation(
    private val delegate: VisualTransformation,
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val visible = AnnotatedString(text.text.removePrefix(InputSentinel.toString()))
        val transformed = delegate.filter(visible)
        return TransformedText(
            transformed.text,
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int =
                    transformed.offsetMapping.originalToTransformed((offset - 1).coerceAtLeast(0))

                override fun transformedToOriginal(offset: Int): Int =
                    (transformed.offsetMapping.transformedToOriginal(offset) + 1).coerceAtMost(text.length)
            },
        )
    }
}

private object CommandPrefixTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val styled = buildAnnotatedString {
            append(text)
            val first = text.text.firstOrNull()
            if (first != null && (first == TriggerSettings.noteSymbol() || first == FileSearchSettings.trigger.value || CommandKind.entries.any { it != CommandKind.Note && it != CommandKind.Files && it.symbol == first })) {
                addStyle(SpanStyle(color = OverlayAccent, fontWeight = FontWeight.Bold), 0, 1)
            }
        }
        return TransformedText(styled, OffsetMapping.Identity)
    }
}

private class AliasPrefixTransformation(private val prefixLength: Int) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val styled = buildAnnotatedString {
            append(text)
            if (prefixLength > 0 && prefixLength <= text.length) {
                addStyle(
                    SpanStyle(color = OverlayAccent, fontWeight = FontWeight.Bold),
                    0,
                    prefixLength,
                )
            }
        }
        return TransformedText(styled, OffsetMapping.Identity)
    }
}

private class FileFilterPrefixTransformation(private val prefixLength: Int) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val styled = buildAnnotatedString {
            append(text)
            if (text.isNotEmpty()) {
                addStyle(SpanStyle(color = OverlayAccent, fontWeight = FontWeight.Bold), 0, 1)
            }
            val end = (1 + prefixLength).coerceAtMost(text.length)
            if (end > 1) {
                addStyle(SpanStyle(color = OverlayAccent, fontWeight = FontWeight.Bold), 1, end)
            }
        }
        return TransformedText(styled, OffsetMapping.Identity)
    }
}

private fun overlayActionLabel(kind: CommandKind): String = when (kind) {
    CommandKind.Apps -> "open app"
    CommandKind.Files -> "open file"
    CommandKind.Message -> "text contact"
    CommandKind.Call -> "call contact"
    CommandKind.Todo -> "add to todoist"
    CommandKind.Note -> "keep note"
    CommandKind.Event -> "create event"
    CommandKind.Timer -> "set timer or alarm"
    CommandKind.Calculator -> "calculate"
    CommandKind.Web -> "search web"
    CommandKind.Ask -> "ask Gemini AI"
}

private fun overlayPendingLabel(kind: CommandKind): String = when (kind) {
    CommandKind.Apps -> "opening app"
    CommandKind.Files -> "opening file"
    CommandKind.Message -> "message prepared"
    CommandKind.Call -> "opening call"
    CommandKind.Todo -> "sending to todoist"
    CommandKind.Note -> "opening note"
    CommandKind.Event -> "opening event"
    CommandKind.Timer -> "timer added"
    CommandKind.Calculator -> "calculating"
    CommandKind.Web -> "opening browser"
    CommandKind.Ask -> "opening ChatGPT"
}
