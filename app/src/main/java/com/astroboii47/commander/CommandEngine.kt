package com.astroboii47.commander

import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import android.provider.MediaStore
import android.provider.AlarmClock
import android.os.Build
import android.os.Environment
import android.os.StrictMode
import android.provider.Settings
import android.provider.DocumentsContract
import android.telephony.SmsManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.IntentFilter
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.io.File
import android.util.Size
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CommandEngine(private val activity: Activity) {
    private val packageManager = activity.packageManager
    val context: Context get() = activity

    fun warmAppIndex() {
        AppCatalog.initialize(activity.applicationContext)
    }

    fun directTodoistEnabled(): Boolean = TodoistSettings.directEnabled(activity)
    fun directSmsEnabled(): Boolean = SmsSettings.directEnabled(activity)

    fun hasGeminiKey(): Boolean = GeminiSettings.apiKey(activity).isNotBlank()

    suspend fun askGemini(history: List<AskMessage>): Result<String> =
        withContext(Dispatchers.IO) { GeminiApi.generate(activity.applicationContext, history) }

    fun searchApps(query: String, limit: Int = 7): List<AppResult> {
        val needle = query.trim().lowercase()
        val destinations = buildList {
            if ("commander hub".contains(needle) || "hub".startsWith(needle)) {
                add(AppResult("Commander Hub", INTERNAL_HUB, null))
            }
            if ("commander home".contains(needle) || "home".startsWith(needle)) {
                add(AppResult("Commander Home", INTERNAL_HOME, null))
            }
            if ("commander settings".contains(needle) || "settings".startsWith(needle)) {
                add(AppResult("Commander Settings", INTERNAL_SETTINGS, null))
            }
        }
        return (destinations + AppCatalog.search(activity.applicationContext, query, limit).map {
            AppResult(label = it.label, packageName = it.packageName, icon = null)
        }).distinctBy { it.packageName }.take(limit)
    }

    fun loadVisibleAppIcons(apps: List<AppResult>): List<AppResult> {
        return apps.map { app ->
            val icon = when (app.packageName) {
                INTERNAL_HOME -> ContextCompat.getDrawable(activity, R.drawable.ic_commander_home)
                INTERNAL_SETTINGS -> ContextCompat.getDrawable(activity, R.drawable.ic_commander_bar)
                INTERNAL_HUB -> ContextCompat.getDrawable(activity, R.drawable.ic_commander_hub)
                else -> AppIconOverrides.load(activity, app.packageName)
                    ?: runCatching { packageManager.getApplicationIcon(app.packageName) }.getOrNull()
            }
            app.copy(icon = icon, adaptiveColor = icon?.let(::extractIconColor))
        }
    }

    fun loadPackageIcon(packageName: String) =
        runCatching { packageManager.getApplicationIcon(packageName) }.getOrNull()

    private fun extractIconColor(drawable: android.graphics.drawable.Drawable): Int {
        val bitmap = drawable.toBitmap(32, 32)
        var bestScore = -1f
        var best = android.graphics.Color.rgb(255, 70, 35)
        val hsv = FloatArray(3)
        for (y in 0 until bitmap.height step 2) for (x in 0 until bitmap.width step 2) {
            val pixel = bitmap.getPixel(x, y)
            if (android.graphics.Color.alpha(pixel) < 150) continue
            android.graphics.Color.colorToHSV(pixel, hsv)
            val score = hsv[1] * (.45f + hsv[2])
            if (score > bestScore && hsv[2] > .22f) {
                bestScore = score
                val tuned = floatArrayOf(hsv[0], hsv[1].coerceIn(.5f, .88f), hsv[2].coerceIn(.62f, .9f))
                best = android.graphics.Color.HSVToColor(tuned)
            }
        }
        return best
    }

    fun searchFiles(query: String, limit: Int = 7, mode: FileSearchMode = FileSearchMode.Default, sortMode: FileSortMode = FileSortMode.Relevance): List<FileResult> {
        if (query.isBlank()) return emptyList()
        val collection = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.RELATIVE_PATH,
            MediaStore.Files.FileColumns.DATE_MODIFIED,
        )
        val fileResults = if (mode == FileSearchMode.FoldersOnly) emptyList() else runCatching {
            activity.contentResolver.query(
                collection,
                projection,
                "${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE ?",
                arrayOf("%$query%"),
                "${MediaStore.Files.FileColumns.DATE_MODIFIED} ${if (sortMode == FileSortMode.Oldest) "ASC" else "DESC"}",
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
                val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.RELATIVE_PATH)
                val modifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
                buildList {
                    while (cursor.moveToNext() && size < limit) {
                        val id = cursor.getLong(idColumn)
                        val mimeType = cursor.getString(mimeColumn)
                        if (mimeType == DocumentsContract.Document.MIME_TYPE_DIR ||
                            mimeType == "resource/folder"
                        ) continue
                        add(
                            FileResult(
                                name = cursor.getString(nameColumn).orEmpty(),
                                uri = ContentUris.withAppendedId(collection, id),
                                mimeType = mimeType,
                                location = cursor.getString(pathColumn).orEmpty().trimEnd('/'),
                                modifiedAtMillis = cursor.getLong(modifiedColumn) * 1000L,
                            ),
                        )
                    }
                }
            }.orEmpty()
        }.getOrDefault(emptyList())
        val includeFolders = mode == FileSearchMode.FoldersOnly ||
            (mode == FileSearchMode.Default && FileSearchSettings.showFolders.value)
        if (!includeFolders) return fileResults
        val combined = searchFolders(query, limit) + fileResults
        return when (sortMode) {
            FileSortMode.Relevance -> combined.sortedWith(compareBy<FileResult>({ !it.name.startsWith(query, ignoreCase = true) }, { it.name.length }))
            FileSortMode.Newest -> combined.sortedByDescending { it.modifiedAtMillis }
            FileSortMode.Oldest -> combined.sortedBy { it.modifiedAtMillis }
        }.take(limit)
    }

    private fun searchFolders(query: String, limit: Int): List<FileResult> {
        val folders = FolderIndex.get(activity.applicationContext)
        return folders.asSequence()
            .filter { it.path.substringAfterLast('/').contains(query, ignoreCase = true) }
            .sortedWith(compareBy({ !it.path.substringAfterLast('/').startsWith(query, ignoreCase = true) }, { it.path.length }))
            .take(limit)
            .map { entry ->
                val path = entry.path
                val cleanPath = path.trim('/')
                FileResult(
                    name = cleanPath.substringAfterLast('/'),
                    uri = DocumentsContract.buildDocumentUri(
                        "com.android.externalstorage.documents",
                        "primary:$cleanPath",
                    ),
                    mimeType = DocumentsContract.Document.MIME_TYPE_DIR,
                    location = cleanPath.substringBeforeLast('/', "Internal storage"),
                    isDirectory = true,
                    modifiedAtMillis = entry.modifiedAtMillis,
                )
            }
            .toList()
    }

    fun hasFileSearchAccess(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.R || Environment.isExternalStorageManager()

    fun openFileSearchAccess(): String? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return null
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
            data = Uri.parse("package:${activity.packageName}")
        }
        return startOrFallback(intent, Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION), "File access settings are unavailable")
    }

    fun openFile(file: FileResult): String? {
        if (file.isDirectory) return openFolder(file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(file.uri, file.mimeType ?: "*/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return startOrFallback(intent, null, "No app can open this file")
    }

    private fun openFolder(folder: FileResult): String? {
        // Solid Explorer explicitly advertises file:// directory handling. A
        // document-provider URI can resolve to Solid but still be rejected
        // because our app did not receive a grant for that provider document.
        val relativePath = if (folder.location == "Internal storage") folder.name
            else "${folder.location.trimEnd('/')}/${folder.name}"
        val solidIntent = Intent(Intent.ACTION_VIEW).apply {
            setClassName("pl.solidexplorer2", "pl.solidexplorer.SolidExplorer")
            setDataAndType(Uri.fromFile(File(Environment.getExternalStorageDirectory(), relativePath)), "resource/folder")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (solidIntent.resolveActivity(packageManager) != null) {
            val opened = runCatching {
                StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().build())
                activity.startActivity(solidIntent)
                activity.finish()
            }.isSuccess
            if (opened) return null
        }
        val view = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(folder.uri, DocumentsContract.Document.MIME_TYPE_DIR)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val packageCandidates = listOf("com.google.android.documentsui")
        for (packageName in packageCandidates) {
            val targeted = Intent(view).setPackage(packageName)
            if (targeted.resolveActivity(packageManager) != null) {
                return runCatching { activity.startActivity(targeted); activity.finish(); null }
                    .getOrElse { "Folder could not be opened" }
            }
        }
        if (view.resolveActivity(packageManager) != null) {
            return runCatching { activity.startActivity(view); activity.finish(); null }
                .getOrElse { "Folder could not be opened" }
        }
        val picker = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, folder.uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return startOrFallback(picker, null, "No file manager can open this folder")
    }

    private data class FolderEntry(val path: String, val modifiedAtMillis: Long)

    private object FolderIndex {
        @Volatile private var cached: List<FolderEntry>? = null

        fun get(context: Context): List<FolderEntry> = cached ?: synchronized(this) {
            cached ?: load(context).also { cached = it }
        }

        private fun load(context: Context): List<FolderEntry> {
            val paths = linkedMapOf<String, Long>()
            val collection = MediaStore.Files.getContentUri("external")
            runCatching {
                context.contentResolver.query(
                    collection,
                    arrayOf(MediaStore.Files.FileColumns.RELATIVE_PATH, MediaStore.Files.FileColumns.DATE_MODIFIED),
                    null,
                    null,
                    null,
                )?.use { cursor ->
                    val column = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.RELATIVE_PATH)
                    val modifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
                    while (cursor.moveToNext()) {
                        val relative = cursor.getString(column).orEmpty().trim('/')
                        if (relative.isBlank()) continue
                        val segments = relative.split('/')
                        val modified = cursor.getLong(modifiedColumn) * 1000L
                        for (end in 1..segments.size) {
                            val path = segments.take(end).joinToString("/")
                            paths[path] = maxOf(paths[path] ?: 0L, modified)
                        }
                    }
                }
            }
            return paths.map { FolderEntry(it.key, it.value) }
        }
    }

    fun loadFilePreviews(files: List<FileResult>): List<FileResult> = files.map { file ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            (file.mimeType?.startsWith("image/") == true || file.mimeType?.startsWith("video/") == true)
        ) {
            file.copy(thumbnail = runCatching { activity.contentResolver.loadThumbnail(file.uri, Size(96, 96), null) }.getOrNull())
        } else file
    }

    fun matchAlias(input: String): AliasMatch? = AliasSettings.match(activity, input)

    fun calculatorPreview(text: String): String? = UnitConverter.convert(text) ?: evaluateCalculation(text)?.second

    fun openAlias(match: AliasMatch): String? {
        if (match.query.isBlank()) return "Type a search after the alias"
        val encoded = URLEncoder.encode(match.query, StandardCharsets.UTF_8.toString()).replace("+", "%20")
        val uri = Uri.parse(match.target.urlTemplate.replace("%s", encoded))
        val targeted = Intent(Intent.ACTION_VIEW, uri).apply { match.target.packageName?.let(::setPackage) }
        val fallback = Intent(Intent.ACTION_VIEW, uri)
        return startOrFallback(targeted, fallback, "${match.target.label} is unavailable")
    }

    fun searchContacts(query: String, limit: Int = 5, includeMessenger: Boolean = false): List<ContactResult> {
        if (query.isBlank()) return emptyList()
        val messenger = if (includeMessenger) MessengerChatStore.search(activity, query, limit) else emptyList()
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) return messenger
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE,
        )
        val phoneContacts = activity.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
            arrayOf("%$query%"),
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} COLLATE NOCASE ASC",
        )?.use { cursor ->
            val nameColumn = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberColumn = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val typeColumn = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.TYPE)
            buildList {
                while (cursor.moveToNext() && size < limit) {
                    val type = cursor.getInt(typeColumn)
                    add(
                        ContactResult(
                            name = cursor.getString(nameColumn),
                            number = cursor.getString(numberColumn),
                            label = ContactsContract.CommonDataKinds.Phone.getTypeLabel(activity.resources, type, "phone").toString().lowercase(),
                        ),
                    )
                }
            }.distinctBy { contact ->
                contact.name.trim().lowercase() to PhoneNumberUtils.normalizeNumber(contact.number)
            }
        } ?: emptyList()
        return (messenger + phoneContacts).distinctBy { contact ->
            contact.messengerShortcutId
                ?: "${contact.name.trim().lowercase()}:${PhoneNumberUtils.normalizeNumber(contact.number)}"
        }.take(limit)
    }

    fun messageContact(contact: ContactResult, body: String = ""): String? {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${Uri.encode(contact.number)}")).apply {
            putExtra("sms_body", body)
        }
        return startOrFallback(intent, null, "No messaging app is installed")
    }

    suspend fun sendMessageContact(contact: ContactResult, body: String): String? {
        if (contact.messengerShortcutId != null) {
            if (body.isBlank()) return "Type a message first"
            return MessengerChatStore.openDraft(activity, contact, body)
        }
        if (!SmsSettings.directEnabled(activity)) return messageContact(contact, body)
        if (body.isBlank()) return "Type a message first"
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            return "SMS permission required · enable it in Command settings"
        }
        return suspendCancellableCoroutine { continuation ->
            val action = "${activity.packageName}.SMS_SENT.${System.nanoTime()}"
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    runCatching { activity.unregisterReceiver(this) }
                    if (continuation.isActive) continuation.resume(
                        if (resultCode == Activity.RESULT_OK) null else "Message could not be sent · code $resultCode",
                    )
                }
            }
            ContextCompat.registerReceiver(activity, receiver, IntentFilter(action), ContextCompat.RECEIVER_NOT_EXPORTED)
            continuation.invokeOnCancellation { runCatching { activity.unregisterReceiver(receiver) } }
            val sentIntent = PendingIntent.getBroadcast(
                activity,
                action.hashCode(),
                Intent(action).setPackage(activity.packageName),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            runCatching {
                val manager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) activity.getSystemService(SmsManager::class.java)
                    else @Suppress("DEPRECATION") SmsManager.getDefault()
                manager.sendTextMessage(contact.number, null, body, sentIntent, null)
            }.onFailure {
                runCatching { activity.unregisterReceiver(receiver) }
                if (continuation.isActive) continuation.resume("Message could not be sent")
            }
        }
    }

    fun callContact(contact: ContactResult): String? = startOrFallback(
        Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(contact.number)}")),
        null,
        "No phone app is installed",
    )

    fun openCamera(): String? = startOrFallback(
        Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA), null, "No camera app is installed",
    )

    fun openTimer(): String? = startOrFallback(
        Intent(AlarmClock.ACTION_SET_TIMER), null, "No timer app is installed",
    )

    fun openMemo(): String? = startOrFallback(
        Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION), null, "No recorder app is installed",
    )

    suspend fun execute(command: ParsedCommand, firstApp: AppResult?): String? = when (command.kind) {
        CommandKind.Apps -> if (firstApp == null) "No matching app" else launchApp(firstApp)
        CommandKind.Files -> "Choose a file result"
        CommandKind.Todo -> if (TodoistSettings.directEnabled(activity)) {
            if (command.text.isBlank()) "Type a task after -"
            else withContext(Dispatchers.IO) {
                TodoistApi.quickAdd(activity.applicationContext, TodoistNaturalLanguage.expandCompactTimes(command.text))
            }
        } else {
            shareToPackage(TodoistNaturalLanguage.expandCompactTimes(command.text), "com.todoist", "Todoist")
        }
        CommandKind.Event -> createCalendarEvent(command.text)
        CommandKind.Timer -> setTimer(command.text)
        CommandKind.Calculator -> calculate(command.text)
        CommandKind.Web -> searchWeb(command.text)
        CommandKind.Note -> shareToPackage(command.text, "com.google.android.keep", "Google Keep")
        CommandKind.Call -> call(command.text)
        CommandKind.Message -> message(command.text)
        CommandKind.Ask -> if (command.text.startsWith('?')) askChatGpt(command.text) else "Ask is handled in Command"
    }

    private fun setTimer(text: String): String? {
        if (text.isBlank()) return "Type a duration after +"
        val match = Regex("(?i)\\b(\\d+(?:\\.\\d+)?)\\s*(seconds?|secs?|s|minutes?|mins?|m|hours?|hrs?|h)\\b").find(text)
            ?: return "Try +10 minutes tea"
        val amount = match.groupValues[1].toDoubleOrNull() ?: return "Invalid timer duration"
        val unit = match.groupValues[2].lowercase()
        val multiplier = when {
            unit.startsWith("h") -> 3600.0
            unit.startsWith("m") -> 60.0
            else -> 1.0
        }
        val seconds = (amount * multiplier).toInt().coerceAtLeast(1)
        val label = text.removeRange(match.range).trim().ifBlank { "Command timer" }
        val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
            putExtra(AlarmClock.EXTRA_LENGTH, seconds)
            putExtra(AlarmClock.EXTRA_MESSAGE, label)
            putExtra(AlarmClock.EXTRA_SKIP_UI, true)
        }
        if (intent.resolveActivity(packageManager) == null) return "No timer app is installed"
        // This is a background command. Unlike normal app launches, keep the
        // overlay activity alive so its timer confirmation can finish.
        return runCatching {
            activity.startActivity(intent)
            null
        }.getOrElse { error ->
            if (error is SecurityException) "Permission denied by the timer app" else "Timer could not be started"
        }
    }

    private fun calculate(text: String): String? {
        if (text.isBlank()) return "Type a calculation after ,"
        val rendered = UnitConverter.convert(text) ?: evaluateCalculation(text)?.second ?: return "Invalid calculation"
        return "$text = $rendered"
    }

    private fun evaluateCalculation(text: String): Pair<Double, String>? {
        val normalized = normalizeCalculation(text)
        val value = runCatching { ExpressionParser(normalized).parse() }.getOrNull() ?: return null
        if (!value.isFinite()) return null
        val rendered = if (value % 1.0 == 0.0) value.toLong().toString() else "%.10f".format(value).trimEnd('0').trimEnd('.')
        return value to rendered
    }

    private fun normalizeCalculation(text: String): String = text
        .lowercase()
        .replace(Regex("\\bmultiplied\\s+by\\b"), "*")
        .replace(Regex("\\btimes\\b"), "*")
        .replace(Regex("\\bdivided\\s+by\\b"), "/")
        .replace(Regex("\\bdivide\\b"), "/")
        .replace(Regex("\\bover\\b"), "/")
        .replace(Regex("\\bplus\\b|\\badd\\b"), "+")
        .replace(Regex("\\bminus\\b|\\bsubtract\\b"), "-")
        .replace('x', '*')
        .replace('×', '*')
        .replace('÷', '/')

    private fun searchWeb(text: String): String? {
        if (text.isBlank()) return "Type a search after /"
        val target = if (Regex("(?i)^https?://").containsMatchIn(text)) text else {
            "https://www.google.com/search?q=" + URLEncoder.encode(text, StandardCharsets.UTF_8.toString())
        }
        return startOrFallback(Intent(Intent.ACTION_VIEW, Uri.parse(target)), null, "No browser is installed")
    }

    private fun askChatGpt(text: String): String? {
        if (text.isBlank()) return "Type a question after ?"
        val direct = text.startsWith('?')
        val prompt = if (direct) text.drop(1).trim() else text.trim()
        if (prompt.isBlank()) return "Type a question after ${if (direct) "??" else "?"}"
        if (!direct) ChatGptNotificationBridge.arm(activity.applicationContext, prompt)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, prompt)
            setPackage("com.openai.chatgpt")
        }
        if (direct) return startOrFallback(intent, null, "ChatGPT is not installed")
        if (intent.resolveActivity(packageManager) == null) return "ChatGPT is not installed"
        return runCatching {
            activity.startActivity(intent)
            activity.finish()
            null
        }.getOrElse { "Could not open ChatGPT" }
    }

    fun openChatGpt(): String? {
        val intent = packageManager.getLaunchIntentForPackage("com.openai.chatgpt")
            ?: return "ChatGPT is not installed"
        return startOrFallback(intent, null, "ChatGPT is not installed")
    }

    private fun launchApp(app: AppResult): String? {
        val intent = when (app.packageName) {
            INTERNAL_HUB -> internalDestination(HubActivity::class.java)
            INTERNAL_HOME -> internalDestination(MainActivity::class.java)
            INTERNAL_SETTINGS -> internalDestination(SettingsActivity::class.java)
            else -> packageManager.getLaunchIntentForPackage(app.packageName) ?: return "App cannot be opened"
        }
        activity.startActivity(intent)
        activity.finish()
        return null
    }

    private fun internalDestination(destination: Class<out Activity>) =
        Intent(activity, destination).addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT,
        )

    private companion object {
        const val INTERNAL_HUB = "internal:minimal-hub"
        const val INTERNAL_HOME = "internal:command-home"
        const val INTERNAL_SETTINGS = "internal:command-settings"
    }

    private fun shareToPackage(text: String, packageName: String, label: String): String? {
        if (text.isBlank()) return "Type something after the command"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            setPackage(packageName)
        }
        return startOrFallback(intent, Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }, "Send to…"), "$label is not installed")
    }

    private fun createCalendarEvent(text: String): String? {
        if (text.isBlank()) return "Type an event after *"
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, text)
        }
        return startOrFallback(intent, null, "No calendar app is installed")
    }

    private fun call(text: String): String? {
        if (text.isBlank()) return "Type a name or number after #"
        val number = findContact(text)?.number ?: text.filter { it.isDigit() || it == '+' }
        if (number.isBlank()) return "Contact not found"
        return startOrFallback(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(number)}")), null, "No phone app is installed")
    }

    private fun message(text: String): String? {
        if (text.isBlank()) return "Type a name or number after @"
        val contact = findContactPrefix(text)
        val directNumber = text.substringBefore(' ').filter { it.isDigit() || it == '+' }
        val number = contact?.number ?: directNumber
        if (number.isBlank()) return "Start with a contact name or phone number"
        val body = if (contact != null) text.removePrefix(contact.matchedText).trim() else text.substringAfter(' ', "")
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${Uri.encode(number)}")).apply {
            putExtra("sms_body", body)
        }
        return startOrFallback(intent, null, "No messaging app is installed")
    }

    private data class ContactMatch(val number: String, val matchedText: String)

    private fun findContact(query: String): ContactMatch? = findContactPrefix(query, exactOnly = true)

    private fun findContactPrefix(input: String, exactOnly: Boolean = false): ContactMatch? {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) return null
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
        )
        return activity.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            null,
        )?.use { cursor ->
            val nameColumn = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberColumn = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
            var best: ContactMatch? = null
            while (cursor.moveToNext()) {
                val name = cursor.getString(nameColumn)
                val matches = if (exactOnly) name.equals(input, true) else input.startsWith(name, true)
                if (matches && (best == null || name.length > best!!.matchedText.length)) {
                    best = ContactMatch(cursor.getString(numberColumn), name)
                }
            }
            best
        }
    }

    private fun startOrFallback(primary: Intent, fallback: Intent?, unavailable: String): String? {
        val chosen = if (primary.resolveActivity(packageManager) != null) primary else fallback
        if (chosen == null || chosen.resolveActivity(packageManager) == null) return unavailable
        return runCatching {
            activity.startActivity(chosen)
            activity.finish()
            null
        }.getOrElse { error ->
            if (error is SecurityException) "Permission denied by the target app" else unavailable
        }
    }
}

private class ExpressionParser(private val source: String) {
    private var index = 0
    fun parse(): Double {
        val result = expression()
        skipSpaces()
        require(index == source.length)
        return result
    }
    private fun expression(): Double {
        var value = term()
        while (true) {
            skipSpaces()
            value = when {
                take('+') -> value + term()
                take('-') -> value - term()
                else -> return value
            }
        }
    }
    private fun term(): Double {
        var value = factor()
        while (true) {
            skipSpaces()
            value = when {
                take('*') -> value * factor()
                take('/') -> value / factor()
                else -> return value
            }
        }
    }
    private fun factor(): Double {
        skipSpaces()
        if (take('+')) return factor()
        if (take('-')) return -factor()
        if (take('(')) return expression().also { skipSpaces(); require(take(')')) }
        val start = index
        while (index < source.length && (source[index].isDigit() || source[index] == '.')) index++
        require(index > start)
        return source.substring(start, index).toDouble()
    }
    private fun take(char: Char): Boolean = if (index < source.length && source[index] == char) { index++; true } else false
    private fun skipSpaces() { while (index < source.length && source[index].isWhitespace()) index++ }
}
