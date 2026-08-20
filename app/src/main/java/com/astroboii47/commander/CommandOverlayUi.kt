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
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardReturn
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
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

private val OverlayAccent: Color get() = AccentSettings.color.value
private val OverlayMono = FontFamily(
    Font(R.font.courier_prime_regular, FontWeight.Normal),
    Font(R.font.courier_prime_bold, FontWeight.Bold),
)
private val OverlaySans = FontFamily(Font(R.font.inter_variable))
private val OverlaySerif = FontFamily(Font(R.font.instrument_serif_regular))
private const val InputSentinel = '\u2060'

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
    val invertedBlurDarkness = AppearanceSettings.invertBlurDarkness.value
    val palette = if (dark && invertedBlurDarkness) {
        OverlayPalette(
            // Both layers must remain translucent: the glass tile is drawn over
            // this panel, so an opaque panel would make it look flat grey.
            panel = Color.Transparent,
            tile = Color(0x20F2EEE6),
            text = Color(0xFFF6F3EA),
            secondary = Color(0xFFB8B4AC),
            border = Color(0x26F2EEE6),
        )
    } else if (dark) {
        OverlayPalette(Color(0x98070707), Color(0x70111111), Color(0xFFF6F3EA), Color(0xFFAAA7A0), Color(0x604F4F4F))
    } else if (invertedBlurDarkness) {
        OverlayPalette(Color(0xD8D3D0C8), Color(0xE8F6F3EC), Color(0xFF161616), Color(0xFF64615B), Color(0x99BDB9AF))
    } else {
        OverlayPalette(Color(0xB8ECE9E1), Color(0x9AF4F2EC), Color(0xFF161616), Color(0xFF6D6B65), Color(0x88CAC7BE))
    }
    var query by remember { mutableStateOf(initialQuery) }
    var editorValue by remember { mutableStateOf(TextFieldValue(InputSentinel + initialQuery, TextRange(initialQuery.length + 1))) }
    var notice by remember { mutableStateOf<String?>(null) }
    var revealed by remember { mutableStateOf(false) }
    var settledQuery by remember { mutableStateOf("") }
    var selectedContact by remember { mutableStateOf<ContactResult?>(null) }
    var selectedContactKind by remember { mutableStateOf<CommandKind?>(null) }
    var previousStageQuery by remember { mutableStateOf<String?>(null) }
    var selectedIndex by remember { mutableStateOf(0) }
    var executing by remember { mutableStateOf(false) }
    var askPreview by remember { mutableStateOf(initialAskPreview) }
    var askWaiting by remember { mutableStateOf(initialAskWaiting) }
    var askTimedOut by remember { mutableStateOf(false) }
    var askMessages by remember { mutableStateOf<List<AskMessage>>(emptyList()) }
    var askLoading by remember { mutableStateOf(false) }
    var editorFocused by remember { mutableStateOf(false) }
    var automaticallyOpenedAppKey by remember { mutableStateOf<String?>(null) }
    var heldOverlayFirstKey by remember { mutableStateOf(android.view.KeyEvent.KEYCODE_UNKNOWN) }
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
    val aliasSuggestions = remember(query, AppSearchSettings.aliasSuggestions.value) {
        AliasSettings.suggestions(engine.context, query)
    }
    val shortcutQuery = remember(query, aliasSuggestions) {
        if (aliasSuggestions.isEmpty()) AppShortcutSettings.query(engine.context, query) else null
    }
    val taskerResults = remember(query, maxVisibleResults, aliasSuggestions) {
        if (aliasSuggestions.isEmpty()) TaskerAliases.matches(engine.context, query).take(maxVisibleResults) else emptyList()
    }
    val calculatorResult = remember(parsed.kind, parsed.text) {
        if (parsed.kind == CommandKind.Calculator) engine.calculatorPreview(parsed.text) else null
    }
    val inGeminiConversation = askMessages.isNotEmpty()
    val activeKind = if (inGeminiConversation) CommandKind.Ask else selectedContactKind ?: parsed.kind
    val baseApps = remember(query, activeKind, aliasMatch, maxVisibleResults) {
        if (activeKind == CommandKind.Apps && aliasMatch == null && aliasSuggestions.isEmpty()) engine.searchApps(parsed.text, maxVisibleResults) else emptyList()
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
    val contacts = remember(activeKind, contactQuery, maxVisibleResults) {
        if ((activeKind == CommandKind.Message || activeKind == CommandKind.Call) && selectedContact == null) {
            engine.searchContacts(contactQuery, maxVisibleResults, includeMessenger = activeKind == CommandKind.Message)
        } else emptyList()
    }
    val actionReady = when {
        aliasSuggestions.isNotEmpty() -> true
        aliasMatch?.target?.id == "settings" -> settingsResults.isNotEmpty()
        shortcutQuery != null -> shortcutResults.isNotEmpty()
        taskerResults.isNotEmpty() -> true
        else -> when (activeKind) {
        CommandKind.Apps -> if (aliasMatch != null) aliasMatch.query.isNotBlank() else apps.isNotEmpty()
        CommandKind.Files -> !hasFileAccess || files.isNotEmpty()
        CommandKind.Message -> if (!hasContactsPermission) true else if (selectedContact != null) query.isNotBlank() else contacts.isNotEmpty()
        CommandKind.Call -> if (!hasContactsPermission) true else selectedContact != null || contacts.isNotEmpty()
        CommandKind.Ask -> if (inGeminiConversation) query.isNotBlank() && !askLoading
            else parsed.text.isNotBlank() && settledQuery == query
        else -> parsed.text.isNotBlank()
        }
    }
    val selectedFileIsDirectory = activeKind == CommandKind.Files &&
        files.getOrNull(selectedIndex.coerceIn(0, files.lastIndex.coerceAtLeast(0)))?.isDirectory == true
    val showActionAccent = actionReady && (query.isNotBlank() || selectedContact != null)
    val enterColor by animateColorAsState(
        targetValue = if (executing) Color.White else if (showActionAccent) OverlayAccent else palette.secondary,
        animationSpec = tween(150),
        label = "commandEnterColor",
    )
    val glowAlpha by animateFloatAsState(
        targetValue = if (showActionAccent) .34f else if (query.isNotBlank()) .10f else 0f,
        animationSpec = tween(190),
        label = "commandGlow",
    )
    val executionFill by animateFloatAsState(
        targetValue = if (executing) 1f else 0f,
        animationSpec = tween(125),
        label = "commandExecutionFill",
    )
    val executionPulse = remember { Animatable(0f) }
    val usePulseConfirmation = AppearanceSettings.pulseConfirmation.value
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    val homeHandoffText = HomeTypingHandoff.text.value
    val completionVisible = isCompletionNotice(notice)
    val commandBarShape = RoundedCornerShape(17.dp)
    // The bar is inset by 6 dp, so the outer radius must also be 6 dp larger
    // for both curves to remain optically concentric.
    val resultsPanelShape = RoundedCornerShape(23.dp)

    fun setEditorQuery(value: String) {
        query = value
        editorValue = TextFieldValue(
            InputSentinel + value,
            TextRange(value.length + 1),
        )
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

    LaunchedEffect(executing, usePulseConfirmation) {
        if (!usePulseConfirmation) {
            executionPulse.snapTo(0f)
        } else if (executing) {
            executionPulse.snapTo(0f)
            executionPulse.animateTo(1f, tween(115))
            executionPulse.animateTo(.48f, tween(165))
        } else {
            executionPulse.animateTo(0f, tween(140))
        }
    }

    LaunchedEffect(baseApps) {
        val retained = apps.associateBy { it.packageName }
        val stable = baseApps.map { fresh ->
            retained[fresh.packageName]?.takeIf { it.icon != null }?.copy(label = fresh.label) ?: fresh
        }
        // Never blank and reload an icon that is already visible. Apart from
        // avoiding needless package-manager work, this keeps an unchanged app
        // in an unchanged slot visually stable while the query evolves.
        apps = stable
        val missing = stable.filter { it.icon == null }
        if (missing.isNotEmpty()) {
            val loaded = withContext(Dispatchers.IO) { engine.loadVisibleAppIcons(missing) }
                .associateBy { it.packageName }
            apps = stable.map { loaded[it.packageName] ?: it }
        }
    }
    LaunchedEffect(query, baseApps, activeKind, aliasMatch, shortcutQuery, taskerResults, AppSearchSettings.openSingleResult.value) {
        val app = baseApps.singleOrNull()
        val eligible = AppSearchSettings.openSingleResult.value &&
            query.isNotBlank() && activeKind == CommandKind.Apps && aliasMatch == null &&
            shortcutQuery == null && taskerResults.isEmpty() && app != null &&
            !app.packageName.startsWith("internal:web-search:")
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
        aliasIcon = if (aliasMatch?.target?.id == "settings") {
            withContext(Dispatchers.IO) { engine.loadSystemSettingsIcon() }
        } else aliasMatch?.target?.packageName?.let { packageName ->
            withContext(Dispatchers.IO) { engine.loadPackageIcon(packageName) }
        }
    }
    LaunchedEffect(fileSearchQuery, activeKind, fileSortMode, maxVisibleResults) {
        rawFiles = if (activeKind == CommandKind.Files && fileSearchQuery.query.isNotBlank()) {
            delay(140)
            withContext(Dispatchers.IO) { engine.loadFilePreviews(engine.searchFiles(fileSearchQuery.query, maxVisibleResults, fileSearchQuery.mode, fileSortMode)) }
        } else emptyList()
    }
    LaunchedEffect(shortcutQuery, maxVisibleResults) {
        shortcutResults = if (shortcutQuery != null) {
            withContext(Dispatchers.IO) { AppShortcutCatalog.search(engine.context, shortcutQuery, maxVisibleResults) }
        } else emptyList()
    }

    fun playOutcome(error: String?) {
        SoundFeedback.play(engine.context, if (error == null) CommandSound.Confirm else CommandSound.Error)
    }

    fun execute() {
        if (executing) return
        if (aliasSuggestions.isNotEmpty()) {
            aliasSuggestions.getOrNull(selectedIndex.coerceIn(0, aliasSuggestions.lastIndex))?.let { suggestion ->
                SoundFeedback.play(engine.context, CommandSound.Step)
                setEditorQuery("${suggestion.alias} ")
                selectedIndex = 0
                notice = null
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
        if (taskerResults.isNotEmpty()) {
            taskerResults.getOrNull(selectedIndex.coerceIn(0, taskerResults.lastIndex.coerceAtLeast(0)))?.let { task ->
                executing = true
                scope.launch {
                    delay(135)
                    val error = TaskerAliases.run(engine.context, task)
                    executing = false
                    playOutcome(error)
                    notice = error ?: "task started · ${task.label}"
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
        if ((activeKind == CommandKind.Message || activeKind == CommandKind.Call) && !engine.hasContactsPermission()) {
            engine.requestContactsPermission()
            return
        }
        if ((activeKind == CommandKind.Message || activeKind == CommandKind.Call) && selectedContact == null) {
            contacts.getOrNull(selectedIndex.coerceIn(0, contacts.lastIndex.coerceAtLeast(0)))?.let { contact ->
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
                val error = engine.sendMessageContact(contact, body)
                executing = false
                playOutcome(error)
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
            notice = error ?: if (parsed.kind == CommandKind.Todo && engine.directTodoistEnabled()) {
                "task added · todoist"
            } else {
                overlayPendingLabel(parsed.kind)
            }
        }
    }
    fun emptyBackspace(): Boolean {
        if (previousStageQuery != null) {
            val previousQuery = previousStageQuery!!
            previousStageQuery = null
            selectedContact = null
            selectedContactKind = null
            notice = null
            setEditorQuery(previousQuery)
        } else {
            onDismiss()
        }
        return true
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
        if (isHomeTypingLaunch && editorFocused && HomeTypingHandoff.isCollecting()) {
            // Android reports editor focus before the physical input connection
            // is dependable on Titan 2. Keep the accessibility buffer as the
            // sole key owner until the user pauses after the current burst.
            delay(700)
            if (HomeTypingHandoff.isCollecting() && HomeTypingHandoff.text.value == homeHandoffText) {
                val finalText = HomeTypingHandoff.text.value
                // Do not reconstruct an unchanged TextFieldValue: doing so
                // replaces a fast Ctrl+A selection with a cursor-at-end range.
                if (query != finalText) setEditorQuery(finalText)
                HomeTypingHandoff.finish()
            }
        }
    }
    LaunchedEffect(query) {
        selectedIndex = 0
        if (query.isBlank()) {
            settledQuery = ""
        } else {
            delay(320)
            settledQuery = query
        }
    }
    LaunchedEffect(notice) {
        if (isCompletionNotice(notice)) {
            delay(if (notice == "timer added") 2_500 else 950)
            onDismiss()
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(if (invertedBlurDarkness) Color(0x6C000000) else Color.Transparent)
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
                .clip(resultsPanelShape)
                .background(palette.panel)
                .border(
                    1.dp,
                    if (invertedBlurDarkness) Color.Transparent else palette.border,
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
                OverlayCompletion(notice!!, palette, focusRequester)
            } else {
            when {
                notice != null -> OverlayNotice(notice!!, palette)
                aliasSuggestions.isNotEmpty() -> OverlayAliasSuggestions(aliasSuggestions, selectedIndex, maxVisibleResults, palette) { suggestion ->
                    SoundFeedback.play(engine.context, CommandSound.Step)
                    setEditorQuery("${suggestion.alias} ")
                    selectedIndex = 0
                    notice = null
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
                aliasMatch != null -> OverlayAliasPreview(aliasMatch, aliasIcon, palette, ::execute)
                activeKind == CommandKind.Calculator && calculatorResult != null -> OverlayCalculatorResult(calculatorResult, palette, ::execute)
                shortcutQuery != null -> OverlayShortcutResults(shortcutResults, selectedIndex, palette) { shortcut ->
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
                taskerResults.isNotEmpty() -> OverlayTaskerResults(taskerResults, selectedIndex, palette) { task ->
                    executing = true
                    scope.launch {
                        delay(135)
                        val error = TaskerAliases.run(engine.context, task)
                        executing = false
                        playOutcome(error)
                        notice = error ?: "task started · ${task.label}"
                    }
                }
                activeKind == CommandKind.Apps && query.isNotBlank() -> OverlayAppResults(
                    apps = apps,
                    selectedIndex = selectedIndex,
                    palette = palette,
                    onClick = { app ->
                        executing = true
                        scope.launch {
                            delay(135)
                            val error = engine.execute(ParsedCommand(CommandKind.Apps, app.label), app)
                            executing = false
                            playOutcome(error)
                            if (error != null) notice = error
                        }
                    },
                    onLongClick = { app ->
                        if (engine.openAppInfo(app)) {
                            SoundFeedback.play(engine.context, CommandSound.Step)
                            onDismiss()
                        }
                    },
                )
                activeKind == CommandKind.Files && !hasFileAccess -> OverlayFileAccess(palette) { engine.openFileSearchAccess() }
                activeKind == CommandKind.Files && parsed.text.isNotBlank() -> OverlayFileResults(files, selectedIndex, palette, fileSortMode) { file ->
                    executing = true
                    scope.launch {
                        delay(135)
                        val error = engine.openFile(file)
                        executing = false
                        playOutcome(error)
                        if (error != null) notice = error
                    }
                }
                (activeKind == CommandKind.Message || activeKind == CommandKind.Call) && !hasContactsPermission -> {
                    OverlayContactAccess(palette) { engine.requestContactsPermission() }
                }
                (activeKind == CommandKind.Message || activeKind == CommandKind.Call) && query.length > 1 && selectedContact == null -> {
                    OverlayContactResults(contacts, parsed, selectedIndex, palette) { contact ->
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

            if (query.isNotBlank() || selectedContact != null || inGeminiConversation) {
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
                            selectedContact!!.name,
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
                                text = "▸ ${if (selectedFileIsDirectory) "open folder" else "open file"}",
                                color = palette.secondary,
                                fontFamily = OverlayMono,
                                fontSize = 12.sp,
                                maxLines = 1,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = fileSortMode.label,
                                color = OverlayAccent,
                                fontFamily = OverlayMono,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                maxLines = 1,
                                modifier = Modifier.clickable { fileSortMode = fileSortMode.next() }
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                            )
                        }
                    } else Text(
                        text = "▸ ${when {
                            shortcutQuery != null -> "search app shortcuts"
                            taskerResults.isNotEmpty() -> "run Tasker task"
                            activeKind == CommandKind.Apps && apps.getOrNull(selectedIndex)?.packageName?.startsWith("internal:web-search:") == true -> "search web"
                            activeKind == CommandKind.Ask && query.startsWith("??") -> "ask ChatGPT"
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
                    .background(palette.tile),
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
                    .border(1.dp, if (executing) Color.White.copy(alpha = .5f) else if (showActionAccent) OverlayAccent.copy(alpha = .72f) else palette.border, commandBarShape)
                    .padding(horizontal = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (query.isEmpty()) {
                            Text(
                                if (inGeminiConversation) "reply…"
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
                                    editorValue = next
                                    query = next.text.drop(1)
                                    notice = null
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                                .onFocusChanged { editorFocused = it.isFocused }
                                .onPreviewKeyEvent { event ->
                                    val native = event.nativeKeyEvent
                                    val isBackspace = event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DEL
                                    val resultCount = when {
                                        aliasSuggestions.isNotEmpty() -> aliasSuggestions.size
                                        aliasMatch?.target?.id == "settings" -> settingsResults.size
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
                                        execute()
                                        true
                                    } else if (event.type == KeyEventType.KeyDown && isBackspace && query.isEmpty()) {
                                        emptyBackspace()
                                    } else {
                                        false
                                    }
                                },
                            textStyle = TextStyle(color = palette.text, fontFamily = OverlayMono, fontWeight = FontWeight.Bold, fontSize = 24.sp),
                            cursorBrush = SolidColor(OverlayAccent),
                            visualTransformation = SentinelTransformation(when {
                                taskerResults.isNotEmpty() -> AliasPrefixTransformation(taskerResults.first().alias.length)
                                aliasMatch != null -> AliasPrefixTransformation(aliasMatch.alias.length)
                                activeKind == CommandKind.Todo -> TodoistRecognitionTransformation
                                else -> CommandPrefixTransformation
                            }),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(onGo = { execute() }),
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
                                .clickable(enabled = actionReady) { execute() },
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
private fun AskWaitingPreview(
    prompt: String,
    palette: OverlayPalette,
    timedOut: Boolean,
    onOpenChatGpt: () -> Unit,
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
    notice == "timer added" ||
    notice?.startsWith("task started ·") == true

@Composable
private fun OverlayCompletion(text: String, palette: OverlayPalette, focusRequester: FocusRequester) {
    val visible = remember(text) { MutableTransitionState(false).apply { targetState = true } }
    LaunchedEffect(text) { focusRequester.requestFocus() }
    Box(Modifier.fillMaxWidth()) {
        AnimatedVisibility(
            visibleState = visible,
            enter = fadeIn(tween(120)),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .background(palette.tile)
                    .border(1.dp, OverlayAccent.copy(alpha = .72f), RoundedCornerShape(17.dp))
                    .padding(horizontal = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
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
private fun OverlayAppResults(
    apps: List<AppResult>,
    selectedIndex: Int,
    palette: OverlayPalette,
    onClick: (AppResult) -> Unit,
    onLongClick: (AppResult) -> Unit,
) {
    if (apps.isEmpty()) {
        OverlayNotice("no apps found", palette)
        return
    }
    val animationMode = AppearanceSettings.listAnimation.value
    val targetHeight = (apps.size * 66 + (apps.size - 1) * 8).dp
    val listHeight by animateDpAsState(
        targetValue = targetHeight,
        animationSpec = when (animationMode) {
            ListAnimationMode.Off -> tween(0)
            ListAnimationMode.Quick -> tween(100)
            ListAnimationMode.Smooth -> tween(240)
        },
        label = "appListHeight",
    )
    Column(Modifier.fillMaxWidth().height(listHeight)) {
        apps.forEachIndexed { index, app ->
            if (index > 0) Spacer(Modifier.height(8.dp))
            if (animationMode == ListAnimationMode.Off) {
                OverlayAppResultRow(app, index == selectedIndex, palette, onClick, onLongClick)
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
                    OverlayAppResultRow(shownApp, index == selectedIndex, palette, onClick, onLongClick)
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
    onClick: (AppResult) -> Unit,
    onLongClick: (AppResult) -> Unit,
) {
    val glowMode = AppearanceSettings.appGlowMode.value
    val appColor = app.adaptiveColor?.let { Color(it) } ?: OverlayAccent
    val visualMode = if (glowMode == AppGlowMode.Off) AppGlowMode.Reduced else glowMode
    val shape = RoundedCornerShape(15.dp)
    val selectedBrush = when (visualMode) {
        AppGlowMode.Off -> SolidColor(palette.tile)
        AppGlowMode.Outline -> SolidColor(palette.tile)
        AppGlowMode.Reduced -> SolidColor((if (glowMode == AppGlowMode.Off) OverlayAccent else appColor).copy(alpha = .20f))
        AppGlowMode.Full -> SolidColor(appColor.copy(alpha = .62f))
    }
    val selectionColor = if (glowMode == AppGlowMode.Off) OverlayAccent else appColor
    Box(Modifier.fillMaxWidth()) {
        if (selected) {
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
                        color = selectionColor.copy(alpha = when (visualMode) {
                            AppGlowMode.Outline -> .72f
                            AppGlowMode.Reduced -> .82f
                            else -> .94f
                        }),
                        shape = shape,
                    ),
            )
        }
        Row(
            Modifier.fillMaxWidth().clip(shape).background(palette.tile)
                .then(if (selected && visualMode != AppGlowMode.Outline) Modifier.background(selectedBrush) else Modifier)
                .border(if (selected && visualMode == AppGlowMode.Outline) 2.dp else 1.dp, if (selected) selectionColor else palette.border, shape)
                .combinedClickable(
                    onClick = { onClick(app) },
                    onLongClick = { if (!app.packageName.startsWith("internal:")) onLongClick(app) },
                ).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RealAppIcon(app.icon, app.label, palette, Modifier.size(42.dp))
            Text(app.label, Modifier.padding(start = 13.dp).weight(1f), color = palette.text, fontFamily = OverlaySans, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            if (selected) Text("↵", color = if (visualMode == AppGlowMode.Full) Color.White else selectionColor, fontFamily = OverlayMono, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        }
    }
}

@Composable
private fun OverlayShortcutResults(
    shortcuts: List<AppShortcutResult>,
    selectedIndex: Int,
    palette: OverlayPalette,
    onClick: (AppShortcutResult) -> Unit,
) {
    if (shortcuts.isEmpty()) {
        OverlayNotice("no app shortcuts found", palette)
        return
    }
    shortcuts.forEachIndexed { index, shortcut ->
        if (index > 0) Spacer(Modifier.height(8.dp))
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
    val scrollState = rememberScrollState()
    Column(
        Modifier.fillMaxWidth()
            .heightIn(max = (visibleRowCount * 66 + (visibleRowCount - 1).coerceAtLeast(0) * 8).dp)
            .verticalScroll(scrollState),
    ) {
        results.forEachIndexed { index, result ->
            if (index > 0) Spacer(Modifier.height(8.dp))
            val bringIntoViewRequester = remember { BringIntoViewRequester() }
            LaunchedEffect(index == selectedIndex) {
                if (index == selectedIndex) bringIntoViewRequester.bringIntoView()
            }
            Row(
                Modifier.fillMaxWidth().bringIntoViewRequester(bringIntoViewRequester)
                    .clip(RoundedCornerShape(15.dp))
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
private fun OverlayAliasSuggestions(
    suggestions: List<AliasSuggestion>,
    selectedIndex: Int,
    visibleRowCount: Int,
    palette: OverlayPalette,
    onClick: (AliasSuggestion) -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scrollState = rememberScrollState()
    Column(
        Modifier.fillMaxWidth()
            .heightIn(max = (visibleRowCount * 66 + (visibleRowCount - 1).coerceAtLeast(0) * 8).dp)
            .verticalScroll(scrollState),
    ) {
    suggestions.forEachIndexed { index, suggestion ->
        if (index > 0) Spacer(Modifier.height(8.dp))
        val bringIntoViewRequester = remember { BringIntoViewRequester() }
        LaunchedEffect(index == selectedIndex) {
            if (index == selectedIndex) bringIntoViewRequester.bringIntoView()
        }
        val icon = remember(suggestion.packageName, suggestion.taskerAlias?.iconBase64) {
            suggestion.taskerAlias?.let { TaskerAliases.icon(context, it) }
                ?: suggestion.packageName?.let { packageName ->
                    runCatching { context.packageManager.getApplicationIcon(packageName) }.getOrNull()
                }
        }
        Row(
            Modifier.fillMaxWidth().bringIntoViewRequester(bringIntoViewRequester).clip(RoundedCornerShape(15.dp))
                .background(if (index == selectedIndex) OverlayAccent.copy(alpha = .22f) else palette.tile)
                .border(1.dp, if (index == selectedIndex) OverlayAccent else palette.border, RoundedCornerShape(15.dp))
                .clickable { onClick(suggestion) }.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RealAppIcon(icon, suggestion.label, palette, Modifier.size(42.dp))
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
            if (index == selectedIndex) Text("↵", color = OverlayAccent, fontFamily = OverlayMono, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        }
    }
    }
}

@Composable
private fun OverlayTaskerResults(
    tasks: List<TaskerAlias>,
    selectedIndex: Int,
    palette: OverlayPalette,
    onClick: (TaskerAlias) -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    tasks.forEachIndexed { index, task ->
        if (index > 0) Spacer(Modifier.height(8.dp))
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

@Composable
private fun OverlayFileResults(files: List<FileResult>, selectedIndex: Int, palette: OverlayPalette, sortMode: FileSortMode, onClick: (FileResult) -> Unit) {
    if (files.isEmpty()) {
        OverlayNotice("no matching files", palette)
        return
    }
    files.forEachIndexed { index, file ->
        if (index > 0) Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(palette.tile)
                .border(1.dp, if (index == selectedIndex) OverlayAccent.copy(alpha = .7f) else palette.border, RoundedCornerShape(15.dp))
                .clickable { onClick(file) }.padding(12.dp),
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
                ) else Text("▧", color = if (index == selectedIndex) OverlayAccent else palette.secondary, fontFamily = OverlayMono, fontSize = 20.sp)
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
    palette: OverlayPalette,
    onSelect: (ContactResult) -> Unit,
) {
    if (contacts.isEmpty()) {
        OverlayNotice("no matching contacts", palette)
        return
    }
    contacts.forEachIndexed { index, contact ->
        if (index > 0) Spacer(Modifier.height(8.dp))
        val visibleState = remember(contact.name, contact.number) { MutableTransitionState(false).apply { targetState = true } }
        AnimatedVisibility(
            visibleState = visibleState,
            enter = fadeIn(tween(170, delayMillis = index * 45)) + slideInVertically(tween(210, delayMillis = index * 45)) { it / 2 },
        ) {
        val selected = index == selectedIndex
        val shape = RoundedCornerShape(15.dp)
        Box(Modifier.fillMaxWidth()) {
        if (selected) Box(
            Modifier.matchParentSize().blur(10.dp, BlurredEdgeTreatment.Unbounded)
                .border(3.dp, OverlayAccent.copy(alpha = .82f), shape),
        )
        Row(
            Modifier.fillMaxWidth().clip(shape).background(palette.tile)
                .then(if (selected) Modifier.background(OverlayAccent.copy(alpha = .20f)) else Modifier)
                .border(1.dp, if (selected) OverlayAccent else palette.border, shape)
                .clickable { onSelect(contact) }.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(if (selected) OverlayAccent else palette.border),
                contentAlignment = Alignment.Center,
            ) {
                if (contact.avatar != null) Image(
                    painter = BitmapPainter(contact.avatar.asImageBitmap()),
                    contentDescription = contact.name,
                    modifier = Modifier.fillMaxSize(),
                ) else Text(contact.name.take(1).uppercase(), color = Color.White, fontFamily = OverlaySans, fontWeight = FontWeight.Bold)
            }
            Text(contact.name, Modifier.padding(start = 13.dp).weight(1f), color = palette.text, fontFamily = OverlaySans, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Text(contact.label, color = palette.secondary, fontFamily = OverlayMono, fontSize = 12.sp)
            if (selected) Text("  ↵", color = OverlayAccent, fontFamily = OverlayMono, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        }
        }
        }
    }
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
                Text(contact.name.take(1).uppercase(), color = Color.White, fontFamily = OverlaySans, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
            Column(Modifier.padding(start = 13.dp).weight(1f)) {
                Text(contact.name, color = palette.text, fontFamily = OverlaySans, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(
                    if (kind == CommandKind.Message) "type message" else "ready to call",
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

private fun overlayActionLabel(kind: CommandKind): String = when (kind) {
    CommandKind.Apps -> "open app"
    CommandKind.Files -> "open file"
    CommandKind.Message -> "text contact"
    CommandKind.Call -> "call contact"
    CommandKind.Todo -> "add to todoist"
    CommandKind.Note -> "keep note"
    CommandKind.Event -> "create event"
    CommandKind.Timer -> "set timer"
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
