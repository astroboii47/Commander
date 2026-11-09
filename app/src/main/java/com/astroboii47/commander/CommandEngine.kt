package com.astroboii47.commander

import android.Manifest
import android.app.Activity
import android.app.SearchManager
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
import android.provider.OpenableColumns
import android.telephony.SmsManager
import android.app.PendingIntent
import android.app.ActivityOptions
import android.content.BroadcastReceiver
import android.content.IntentFilter
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.io.File
import android.util.Size
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
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
    fun hasContactsPermission(): Boolean =
        ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED

    fun requestContactsPermission() {
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.READ_CONTACTS), 4302)
    }

    fun openAppInfo(app: AppResult): Boolean {
        if (app.packageName.startsWith("internal:")) return false
        return runCatching {
            activity.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${app.packageName}")
                },
            )
            true
        }.getOrDefault(false)
    }

    fun hasGeminiKey(): Boolean = GeminiSettings.apiKey(activity).isNotBlank()

    suspend fun askGemini(history: List<AskMessage>): Result<String> =
        withContext(Dispatchers.IO) { GeminiApi.generate(activity.applicationContext, history) }

    fun searchApps(query: String, limit: Int = 7, includeWebFallback: Boolean = true): List<AppResult> {
        val needle = query.trim().lowercase()
        if (needle == "all") {
            return listOf(AppResult("All apps", INTERNAL_ALL_APPS, null))
        }
        if (needle == "rec" || needle == "recents") {
            return listOf(AppResult("Recent apps", INTERNAL_RECENT_APPS, null))
        }
        val destinations = buildList {
            if ("commander hub".contains(needle) || "hub".startsWith(needle)) {
                add(AppResult("Commander Hub", INTERNAL_HUB, null))
            }
            if (ModuleSettings.homeAvailable(activity) && ModuleSettings.homeEnabled.value &&
                ("commander home".contains(needle) || "home".startsWith(needle))) {
                add(AppResult("Commander Home", INTERNAL_HOME, null))
            }
            if ("commander settings".contains(needle) || "settings".startsWith(needle)) {
                add(AppResult("Commander Settings", INTERNAL_SETTINGS, null))
            }
            if ("switcher".startsWith(needle) || "app switcher".contains(needle)) {
                add(AppResult("App Switcher", INTERNAL_SWITCHER, null))
            }
            if ("key control".contains(needle) || "control screen".contains(needle)) {
                add(AppResult("Key Control", INTERNAL_KEY_CONTROL, null))
            }
            if ("tasks".startsWith(needle) || "todoist tasks".contains(needle)) {
                add(AppResult("Tasks", INTERNAL_TASKS, null))
            }
            if ("quick controls".contains(needle) || "device controls".contains(needle)) {
                add(AppResult("Quick Controls", INTERNAL_QUICK_CONTROLS, null))
            }
            if ("app shortcuts".contains(needle) || "shortcuts".startsWith(needle)) {
                add(AppResult("App Shortcuts", INTERNAL_APP_SHORTCUTS, null))
            }
        }
        val matches = (destinations + AppCatalog.search(activity.applicationContext, query, limit).map {
            AppResult(label = it.label, packageName = it.packageName, icon = null)
        }).distinctBy { it.packageName }.take(limit)
        return if (matches.isEmpty() && needle.isNotBlank() && includeWebFallback && AppSearchSettings.webFallback.value) {
            listOf(AppResult("Search the web", INTERNAL_WEB + Uri.encode(query.trim()), null))
        } else matches
    }

    fun allApps(query: String, limit: Int = Int.MAX_VALUE): List<AppResult> {
        val needle = query.trim().lowercase()
        return AppCatalog.all(activity.applicationContext).asSequence()
            .filter { needle.isBlank() || needle in it.searchable }
            .take(limit)
            .map { AppResult(it.label, it.packageName, null) }
            .toList()
    }

    fun commanderRecents(query: String, limit: Int = 10): List<AppResult> {
        val needle = query.trim().lowercase()
        return recentApps(10).filter {
            needle.isBlank() || needle in it.label.lowercase() || needle in it.packageName.lowercase()
        }.take(limit)
    }

    private fun recentApps(limit: Int): List<AppResult> {
        val launchable = AppCatalog.all(activity.applicationContext).associateBy { it.packageName }
        return AppLaunchHistory.packages(activity.applicationContext)
            .mapNotNull(launchable::get)
            .take(limit.coerceAtMost(10))
            .map { AppResult(it.label, it.packageName, null) }
    }

    fun loadVisibleAppIcons(apps: List<AppResult>): List<AppResult> {
        return apps.map { app ->
            val icon = when (app.packageName) {
                INTERNAL_HOME -> ContextCompat.getDrawable(activity, R.drawable.ic_commander_home)
            INTERNAL_SETTINGS -> ContextCompat.getDrawable(activity, R.drawable.ic_commander_bar)
                INTERNAL_HUB -> ContextCompat.getDrawable(activity, R.drawable.ic_commander_hub)
            INTERNAL_ALL_APPS, INTERNAL_RECENT_APPS -> ContextCompat.getDrawable(activity, R.drawable.ic_commander_bar)
            INTERNAL_SWITCHER -> ContextCompat.getDrawable(activity, R.drawable.ic_commander_bar)
            INTERNAL_KEY_CONTROL -> null
            INTERNAL_QUICK_CONTROLS -> ContextCompat.getDrawable(activity, R.drawable.ic_quick_controls)
            INTERNAL_APP_SHORTCUTS -> ContextCompat.getDrawable(activity, R.drawable.ic_app_shortcuts)
            else -> if (app.packageName.startsWith(INTERNAL_WEB)) null else AppIconOverrides.load(activity, app.packageName)
                    ?: runCatching { packageManager.getApplicationIcon(app.packageName) }.getOrNull()
            }
            app.copy(icon = icon, adaptiveColor = icon?.let(::extractIconColor))
        }
    }

    fun webFallbackResult(query: String): AppResult? {
        val value = query.trim()
        return if (value.isNotEmpty() && AppSearchSettings.webFallback.value) {
            AppResult("Search the web", INTERNAL_WEB + Uri.encode(value), null)
        } else null
    }

    fun loadPackageIcon(packageName: String) =
        runCatching { packageManager.getApplicationIcon(packageName) }.getOrNull()

    fun loadSystemSettingsIcon() = runCatching {
        val packageName = Intent(Settings.ACTION_SETTINGS).resolveActivity(packageManager)?.packageName
            ?: return@runCatching null
        packageManager.getApplicationIcon(packageName)
    }.getOrNull()

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

    fun openFileWith(file: FileResult): String? {
        if (file.isDirectory) return openFolder(file)
        val view = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(file.uri, file.mimeType ?: "*/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return startOrFallback(Intent.createChooser(view, "Open ${file.name} with"), null, "No app can open this file")
    }

    fun revealFile(file: FileResult): String? {
        val path = file.location.trim('/')
        if (path.isBlank()) return "Containing folder is unavailable"
        val folder = FileResult(
            name = path.substringAfterLast('/'),
            uri = DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", "primary:$path"),
            mimeType = DocumentsContract.Document.MIME_TYPE_DIR,
            location = path.substringBeforeLast('/', "Internal storage"),
            isDirectory = true,
        )
        return openFolder(folder)
    }

    fun fileInfo(file: FileResult): String {
        var size: Long? = null
        if (!file.isDirectory) runCatching {
            activity.contentResolver.query(file.uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst() && !cursor.isNull(0)) size = cursor.getLong(0)
            }
        }
        val details = buildList {
            add(if (file.isDirectory) "Folder" else file.mimeType ?: "File")
            size?.let { add(formatFileSize(it)) }
            if (file.location.isNotBlank()) add(file.location)
            if (file.modifiedAtMillis > 0L) add(java.text.DateFormat.getDateTimeInstance().format(java.util.Date(file.modifiedAtMillis)))
        }
        return details.joinToString("\n")
    }

    fun filePreview(file: FileResult): android.graphics.Bitmap? {
        if (file.isDirectory || Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
        return runCatching { activity.contentResolver.loadThumbnail(file.uri, Size(720, 480), null) }.getOrNull()
    }

    fun deleteFile(file: FileResult): String? {
        if (file.isDirectory) return "Folder deletion is not supported"
        return runCatching {
            if (activity.contentResolver.delete(file.uri, null, null) > 0) null
            else "File could not be deleted"
        }.getOrElse { "File could not be deleted" }
    }

    private fun formatFileSize(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val units = arrayOf("KB", "MB", "GB", "TB")
        var value = bytes / 1024.0
        var unit = 0
        while (value >= 1024 && unit < units.lastIndex) { value /= 1024; unit++ }
        return String.format(java.util.Locale.getDefault(), "%.1f %s", value, units[unit])
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
                startActivityAndFinish(solidIntent)
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
                return runCatching { startActivityAndFinish(targeted); null }
                    .getOrElse { "Folder could not be opened" }
            }
        }
        if (view.resolveActivity(packageManager) != null) {
            return runCatching { startActivityAndFinish(view); null }
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

    fun searchSystemSettings(query: String): List<SystemSettingResult> = SystemSettingsCatalog.search(query)

    fun openSystemSetting(result: SystemSettingResult): String? = startOrFallback(
        Intent(result.action),
        Intent(Settings.ACTION_SETTINGS),
        "Settings could not be opened",
    )

    fun calculatorPreview(text: String): String? = UnitConverter.convert(text) ?: evaluateCalculation(text)?.second

    fun openAlias(match: AliasMatch): String? {
        if (match.query.isBlank()) return "Type a search after the alias"
        if (match.target.id == "gmail") {
            val launch = packageManager.getLaunchIntentForPackage("com.google.android.gm")
                ?: return "Gmail is unavailable"
            if (HomeTypingAccessibilityService.isConnected()) {
                GmailSearchBridge.arm(activity.applicationContext, match.query)
                return startOrFallback(launch, null, "Gmail is unavailable")
            }
            val search = Intent(Intent.ACTION_SEARCH).apply {
                setClassName("com.google.android.gm", "com.google.android.gm.ui.MailActivityGmail")
                putExtra(SearchManager.QUERY, match.query)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            val encoded = Uri.encode(match.query)
            return startOrFallback(
                search,
                Intent(Intent.ACTION_VIEW, Uri.parse("https://mail.google.com/mail/u/0/#search/$encoded")),
                "Gmail is unavailable",
            )
        }
        if (match.target.id == "onepassword") {
            val search = Intent("onepassword.action.SEARCH").apply {
                setClassName("com.onepassword.android", "com.onepassword.android.app.StartActivity")
            }
            val launch = packageManager.getLaunchIntentForPackage("com.onepassword.android")
                ?: packageManager.getLaunchIntentForPackage("com.agilebits.onepassword")
            OnePasswordSearchBridge.arm(activity.applicationContext, match.query)
            return startOrFallback(search, launch, "1Password is unavailable")
        }
        if (match.target.id == "waze" && match.isDirections) {
            val destination = match.routeDestination
                ?: return "Waze routes must have a destination"
            val uri = Uri.parse("https://www.waze.com/ul").buildUpon()
                .appendQueryParameter("q", destination)
                .appendQueryParameter("navigate", "yes")
                .appendQueryParameter("utm_source", "commander")
                .build()
            val targeted = Intent(Intent.ACTION_VIEW, uri).setPackage("com.waze")
            return startOrFallback(targeted, Intent(Intent.ACTION_VIEW, uri), "Waze is unavailable")
        }
        if (match.target.id == "maps" && match.isDirections) {
            val builder = Uri.parse("https://www.google.com/maps/dir/").buildUpon()
                .appendQueryParameter("api", "1")
            match.routeOrigin?.let { builder.appendQueryParameter("origin", it) }
            match.routeDestination?.let { builder.appendQueryParameter("destination", it) }
            match.travelMode?.let { builder.appendQueryParameter("travelmode", it) }
            val uri = builder.build()
            val targeted = Intent(Intent.ACTION_VIEW, uri).setPackage("com.google.android.apps.maps")
            return startOrFallback(targeted, Intent(Intent.ACTION_VIEW, uri), "Google Maps is unavailable")
        }
        val encoded = URLEncoder.encode(match.query, StandardCharsets.UTF_8.toString()).replace("+", "%20")
        val uri = Uri.parse(match.target.urlTemplate.replace("%s", encoded))
        val targeted = Intent(Intent.ACTION_VIEW, uri).apply { match.target.packageName?.let(::setPackage) }
        val fallback = Intent(Intent.ACTION_VIEW, uri)
        return startOrFallback(targeted, fallback, "${match.target.label} is unavailable")
    }

    fun searchContacts(
        query: String,
        limit: Int = 5,
        includeMessenger: Boolean = false,
        platform: MessagingPlatformOption? = null,
    ): List<ContactResult> {
        if (query.isBlank() && platform == null) return emptyList()
        val messenger = if (includeMessenger) CommanderMessagingContacts.search(activity, query, limit, platform) else emptyList()
        if (platform != null && platform.key != "sms") return messenger
        val directNumber = if (!includeMessenger && query.trim().matches(Regex("^\\+?[0-9() .-]{3,}$")) &&
            query.count(Char::isDigit) >= 3
        ) {
            PhoneNumberUtils.normalizeNumber(query).takeIf(String::isNotBlank)?.let { number ->
                ContactResult(name = number, number = number, label = "call typed number")
            }
        } else null
        // A phone-shaped call query is an explicit direct dial. Do not mix in
        // contact-provider matches, which could make a nearby contact the
        // selected row while the user is still entering a number.
        if (directNumber != null) return listOf(directNumber)
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return listOfNotNull(directNumber) + messenger
        }
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE,
        )
        val commanderContactsByName = if (includeMessenger) {
            CommanderMessagingContacts.contacts(activity).associateBy { canonicalPersonName(it.name) }
        } else emptyMap()
        val messagingDiscoveries = if (includeMessenger) {
            CommanderMessagingContacts.discoveries(activity).associateBy { it.identity }
        } else emptyMap()
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
                    val phoneBookName = cursor.getString(nameColumn)
                    val commanderContact = if (includeMessenger) {
                        commanderContactsByName[canonicalPersonName(phoneBookName)]
                    } else null
                    add(
                        ContactResult(
                            name = commanderContact?.name ?: phoneBookName,
                            number = cursor.getString(numberColumn),
                            label = ContactsContract.CommonDataKinds.Phone.getTypeLabel(activity.resources, type, "phone").toString()
                                .lowercase().let { typeLabel ->
                                    if (includeMessenger) listOf(
                                        "Messages",
                                        phoneBookName,
                                        typeLabel,
                                        if (directSmsEnabled()) "direct send" else "open composer",
                                    ).joinToString(" · ")
                                    else typeLabel
                                },
                            messagingPlatform = if (includeMessenger) "SMS" else null,
                            messagingRouteTitle = if (includeMessenger) phoneBookName else null,
                            messagingPlatformIcon = null,
                            commanderMessagingContactId = commanderContact?.id,
                            messagingIsDefault = commanderContact?.defaultIdentity?.let { defaultIdentity ->
                                val route = messagingDiscoveries[defaultIdentity]
                                route?.packageName == "sms" || route?.platform.equals("SMS", true) ||
                                    route?.packageName in buildSet {
                                        android.provider.Telephony.Sms.getDefaultSmsPackage(activity)?.let(::add)
                                        Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"))
                                            .resolveActivity(activity.packageManager)?.packageName?.let(::add)
                                    }
                            } == true,
                        ),
                    )
                }
            }.distinctBy { contact ->
                contact.name.trim().lowercase() to PhoneNumberUtils.normalizeNumber(contact.number)
            }
        } ?: emptyList()
        val directSmsNames = phoneContacts.mapTo(hashSetOf()) { canonicalPersonName(it.name) }
        val smsPackages = buildSet {
            android.provider.Telephony.Sms.getDefaultSmsPackage(activity)?.let(::add)
            Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:")).resolveActivity(activity.packageManager)?.packageName?.let(::add)
        }
        val distinctNotificationRoutes = messenger.filterNot { contact ->
            canonicalPersonName(contact.name) in directSmsNames && (
                contact.messagingPlatform.equals("SMS", true) ||
                    contact.messagingPlatform.equals("Messages", true) ||
                    contact.messagingPackageName in smsPackages
                )
        }
        return (listOfNotNull(directNumber) + phoneContacts + distinctNotificationRoutes).distinctBy { contact ->
            contact.messengerShortcutId
                ?: contact.messagingIdentity
                ?: "${contact.messagingPlatform.orEmpty().lowercase()}:${contact.name.trim().lowercase()}:${PhoneNumberUtils.normalizeNumber(contact.number)}"
        }.sortedWith(
            compareBy<ContactResult> { contactNameMatchRank(it.name, query) }
                .thenBy { it.name.length }
                .thenBy { it.name.lowercase() }
                .thenByDescending { it.messagingIsDefault }
        ).take(limit)
    }

    private fun contactNameMatchRank(name: String, query: String): Int {
        val candidate = canonicalPersonName(name)
        val needle = canonicalPersonName(query)
        return when {
            candidate == needle -> 0
            candidate.startsWith(needle) -> 1
            candidate.split(' ').any { it.startsWith(needle) } -> 2
            else -> 3
        }
    }

    private fun canonicalPersonName(name: String): String = name
        .lowercase()
        .replace(Regex("[^\\p{L}\\p{N}]+"), " ")
        .trim()
        .replace(Regex("\\s+"), " ")

    fun recentReplyContacts(limit: Int = Int.MAX_VALUE): List<ContactResult> =
        HubRepository.items.asSequence()
            .filter { !it.isSummary && it.replyAction != null }
            .distinctBy { it.key }
            .sortedByDescending { it.time }
            .map { item ->
                ContactResult(
                    name = item.conversationTitle?.takeIf(String::isNotBlank) ?: item.title,
                    number = "",
                    label = listOf(item.appName, "reply available", item.text.take(48).takeIf(String::isNotBlank))
                        .filterNotNull()
                        .joinToString(" · "),
                    avatar = HubImageCache.get(item.displayImageKey),
                    hubNotificationKey = item.key,
                    messagingIdentity = CommanderMessagingContacts.identity(
                        item.packageName,
                        item.shortcutId,
                        item.conversationTitle ?: item.title,
                    ),
                    messagingPlatform = item.appName,
                    messagingPackageName = item.packageName,
                    messagingPlatformIcon = CommanderMessagingContacts.loadCachedPlatformIcon(activity, item.packageName),
                )
            }
            .take(limit)
            .toList()

    fun replyToRecentMessage(notificationKey: String, body: String): String? {
        val item = HubRepository.items.firstOrNull {
            it.key == notificationKey || notificationKey in it.sourceKeys
        } ?: return "Message is no longer available"
        return if (item.sendReply(activity, body)) null else "Message could not be sent"
    }

    fun sendCommanderMessage(contact: ContactResult, body: String): CommanderMessageDelivery {
        if (body.isBlank()) return CommanderMessageDelivery("Type a message first", status = "message not sent")
        val contactId = contact.commanderMessagingContactId
            ?: return CommanderMessageDelivery("Commander contact is unavailable", status = "message not sent")
        val (route, liveReply) = CommanderMessagingContacts.bestRoute(activity, contactId, contact.messagingIdentity)
        if (liveReply != null) {
            val accepted = liveReply.sendReply(activity, body)
            return if (accepted) CommanderMessageDelivery(sent = true, status = "message sent")
            else CommanderMessageDelivery("Message could not be sent", status = "message not sent")
        }
        CommanderMessagingContacts.retainedReply(activity, contactId)?.let { (identity, reply) ->
            val accepted = reply.sendReply(activity, "retained::$identity", route?.packageName.orEmpty(), body)
            if (accepted) return CommanderMessageDelivery(sent = true, status = "message sent")
            CommanderMessagingContacts.forgetRetainedReply(identity)
        }
        if (route?.packageName == "com.facebook.orca" && route.shortcutId != null) {
            val error = MessengerChatStore.openDraft(
                activity,
                ContactResult(route.title, "", "Messenger", messengerShortcutId = route.shortcutId),
                body,
            )
            return if (error == null) CommanderMessageDelivery(status = "message ready · confirm in Messenger")
            else CommanderMessageDelivery(error, status = "message not sent")
        }
        return CommanderMessageDelivery(
            "${route?.platform ?: "This conversation"} needs a new replyable notification",
            status = "waiting for notification",
        )
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
            val liveReply = HubRepository.items.firstOrNull {
                it.shortcutId == contact.messengerShortcutId && it.replyAction != null
            }
            if (liveReply != null) {
                return if (liveReply.sendReply(activity, body)) null else "Message could not be sent"
            }
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
        CommandKind.Note -> {
            RecentNoteStore.save(activity.applicationContext, command.text)
            shareToPackage(command.text, "com.google.android.keep", "Google Keep")
        }
        CommandKind.Call -> call(command.text)
        CommandKind.Message -> message(command.text)
        CommandKind.Ask -> if (command.text.startsWith('?')) askChatGpt(command.text) else "Ask is handled in Command"
    }

    private fun setTimer(text: String): String? {
        if (text.isBlank()) return "Type a duration after +"
        parseAlarmTime(text)?.let { alarm ->
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, alarm.hour24)
                putExtra(AlarmClock.EXTRA_MINUTES, alarm.minute)
                putExtra(AlarmClock.EXTRA_MESSAGE, "Command alarm")
                putExtra(AlarmClock.EXTRA_SKIP_UI, true)
            }
            if (intent.resolveActivity(packageManager) == null) return "No alarm app is installed"
            return runCatching {
                activity.startActivity(intent)
                null
            }.getOrElse { error ->
                if (error is SecurityException) "Permission denied by the alarm app" else "Alarm could not be set"
            }
        }
        val match = Regex("(?i)\\b(\\d+(?:\\.\\d+)?)\\s*(seconds?|secs?|s|minutes?|mins?|m|hours?|hrs?|h)\\b").find(text)
            ?: return "Try +10 minutes tea or +6:30am"
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

    fun alarmTimeLabel(text: String): String? = parseAlarmTime(text)?.label

    fun timerInputPreview(text: String): String? = alarmTimeLabel(text)?.let { "alarm · $it" }
        ?: timerDurationSeconds(text)?.let { seconds ->
            val hours = seconds / 3600
            val minutes = (seconds % 3600) / 60
            val remainder = seconds % 60
            "timer · ${if (hours > 0) "%02d:%02d:%02d".format(hours, minutes, remainder) else "%02d:%02d".format(minutes, remainder)}"
        }

    private fun timerDurationSeconds(text: String): Long? {
        val match = Regex("(?i)\\b(\\d+(?:\\.\\d+)?)\\s*(seconds?|secs?|s|minutes?|mins?|m|hours?|hrs?|h)\\b").find(text)
            ?: return null
        val amount = match.groupValues[1].toDoubleOrNull() ?: return null
        return when {
            match.groupValues[2].startsWith("h", true) -> (amount * 3600).toLong()
            match.groupValues[2].startsWith("m", true) -> (amount * 60).toLong()
            else -> amount.toLong()
        }.coerceAtLeast(1)
    }

    private data class AlarmTime(val hour24: Int, val minute: Int, val label: String)

    private fun parseAlarmTime(text: String): AlarmTime? {
        val match = Regex("(?i)^\\s*(\\d{1,2})(?::?(\\d{2}))?\\s*([ap])\\.?m\\.?\\s*$").matchEntire(text)
            ?: return null
        val hour12 = match.groupValues[1].toIntOrNull()?.takeIf { it in 1..12 } ?: return null
        val minute = match.groupValues[2].takeIf(String::isNotBlank)?.toIntOrNull() ?: 0
        if (minute !in 0..59) return null
        val afternoon = match.groupValues[3].equals("p", ignoreCase = true)
        val hour24 = when {
            afternoon && hour12 != 12 -> hour12 + 12
            !afternoon && hour12 == 12 -> 0
            else -> hour12
        }
        val suffix = if (afternoon) "PM" else "AM"
        return AlarmTime(hour24, minute, "%d:%02d %s".format(hour12, minute, suffix))
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
        return WebSearchLauncher.openQuick(activity, text)
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
            startActivityAndFinish(intent)
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
            INTERNAL_SWITCHER -> return "Open App Switcher from Commander"
            INTERNAL_KEY_CONTROL -> internalDestination(KeyControlActivity::class.java)
            INTERNAL_QUICK_CONTROLS, INTERNAL_APP_SHORTCUTS -> return "Open this category from Commander"
            else -> if (app.packageName.startsWith(INTERNAL_WEB)) {
                return searchWeb(Uri.decode(app.packageName.removePrefix(INTERNAL_WEB)))
            } else if (app.packageName.startsWith(INTERNAL_WEB_PRIVATE)) {
                return WebSearchLauncher.openPrivately(activity, Uri.decode(app.packageName.removePrefix(INTERNAL_WEB_PRIVATE)))
            } else if (app.packageName.startsWith(INTERNAL_WEB_FULL)) {
                return WebSearchLauncher.openNormally(activity, Uri.decode(app.packageName.removePrefix(INTERNAL_WEB_FULL)))
            } else packageManager.getLaunchIntentForPackage(app.packageName) ?: return "App cannot be opened"
        }
        startActivityAndFinish(intent)
        if (!app.packageName.startsWith("internal:")) {
            AppLaunchHistory.record(activity.applicationContext, app.packageName)
        }
        return null
    }

    private fun internalDestination(destination: Class<out Activity>) =
        Intent(activity, destination).addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT,
        )

    companion object {
        const val INTERNAL_HUB = "internal:minimal-hub"
        const val INTERNAL_HOME = "internal:command-home"
        const val INTERNAL_SETTINGS = "internal:command-settings"
        const val INTERNAL_WEB = "internal:web-search:"
        const val INTERNAL_WEB_PRIVATE = "internal:web-search-private:"
        const val INTERNAL_WEB_FULL = "internal:web-search-full:"
        const val INTERNAL_ALL_APPS = "internal:all-apps"
        const val INTERNAL_RECENT_APPS = "internal:recent-apps"
        const val INTERNAL_SWITCHER = "internal:switcher"
        const val INTERNAL_KEY_CONTROL = "internal:key-control"
        const val INTERNAL_TASKS = "internal:todoist-tasks"
        const val INTERNAL_QUICK_CONTROLS = "internal:quick-controls"
        const val INTERNAL_APP_SHORTCUTS = "internal:app-shortcuts"
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
            startActivityAndFinish(chosen)
            null
        }.getOrElse { error ->
            if (error is SecurityException) "Permission denied by the target app" else unavailable
        }
    }

    private fun startActivityAndFinish(intent: Intent) {
        val launchOptions = ActivityOptions.makeCustomAnimation(activity, 0, 0).toBundle()
        activity.startActivity(intent, launchOptions)
        activity.finish()
        @Suppress("DEPRECATION")
        activity.overridePendingTransition(0, 0)
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
