# Changelog

## 0.1.0-alpha.5.1

- Fixed generic Commander and keyboard shortcut launches sometimes opening App Switcher instead of Commander Hub.
- Restored Commander Hub as the unambiguous primary launcher destination.
- Added a dedicated App Switcher shortcut for keyboard mappers, launchers, and automation tools that support Android app shortcuts.
- Fixed Recent Apps occasionally restoring `rec` in the filter field after opening from home typing.

## 0.1.0-alpha.5

### Added

- Added a native Todoist Tasks view that opens directly from the searchable Tasks result or the `tasks ` command.
- Added separate Today and Overdue sections with task counts, live text filtering, project names, labels, priorities, recurring indicators, due dates, and scheduled times.
- Added keyboard-first task actions. Arrow keys navigate, Enter completes the selected task, and Shift+Enter opens it in Todoist.
- Added task-card completion feedback with an accent glow and checked state before the completed task leaves the list.
- Added Quick Controls for flashlight, rotation, brightness, volume, media playback, ringer mode, theme, screen recording, QR scanning, caffeine, screen dimming, Mini Mode, Scroll Assistant, and battery protection where supported.
- Added typed Quick Control values and modes, including brightness percentage or auto, volume percentage, ringer state, theme state, and battery protection level.
- Added All Apps and Recent Apps views with searchable, fully navigable app lists.
- Added App Switcher with recent-task previews, app-name filtering, Left and Right navigation, Enter to switch, and swipe up to close a task.
- Added a keyboard-driven file actions menu with Open, Open in app, Preview, Reveal in folder, File info, and Delete.
- Added file-type icons for documents, PDFs, ebooks, audio, video, code, spreadsheets, presentations, folders, and generic files.
- Added app-provided notification actions and Android Snooze support to Commander Hub, with matching icons and keyboard navigation.
- Added expandable Hub notification details for longer messages, emails, and notifications.
- Added notification profile images and notification-supplied icons, with optional circular presentation, accent tinting, and left or right placement.
- Added swipe-right flagging, inline Hub action confirmation, and Commander sound feedback for navigation and completed actions.
- Added minimal scroll indicators for long Hub lists.

### Improved

- Improved Quick Control state feedback so successful toggles remain visible and repeatable without consuming the command text.
- Improved App Switcher preview caching and task ordering for faster repeated launches and more natural app switching.
- Improved file and folder preview loading so newly visible rows populate as the list scrolls without loading every result at once.
- Improved plain Tasker aliases so partial matches rank naturally alongside apps and exact matches run immediately.
- Improved Hub notification title and app-name extraction for weather, Todoist, messaging, email, and other expanded notification layouts.
- Improved Hub deduplication for group summaries, updated notifications, missed calls, and notifications changed by flagging or app-provided actions.
- Improved Hub reply continuity so supported conversations can remain open after sending when the source notification is replaced.
- Improved Hub layout alignment for headers, timestamps, app names, unread indicators, icons, expanded content, and action menus.
- Improved return-to-Hub behaviour after opening notification destinations where Android preserves the caller task.

### Fixed

- Fixed keyboard navigation and selection visibility across long app, shortcut, alias, Settings, file, action, Tasks, Hub, and App Switcher lists.
- Fixed Hub category tabs and bottom rows being clipped on smaller displays.
- Fixed action menus with many entries clipping their final action.
- Fixed App Switcher dismissal leaving empty gaps and excluded hidden system tasks from normal results.
- Fixed file actions losing the previous query and selection when returning from previews or external apps.
- Fixed filtered Todoist results being vertically clipped when only one or a few tasks matched.
- Fixed selected task rows shifting their due-date column when the Enter indicator moved between rows.
- Fixed task completion using the command-bar confirmation glow instead of the selected task card.

## 0.1.0-alpha.4.1

- Added a unified Hub actions menu with Open, Reply, Dismiss, app-provided notification actions, and notification ignore controls.
- Added keyboard access to Hub actions with P, arrow-key navigation, Enter or O to activate, and U to return to the Hub list.
- Added destructive-action confirmation for actions such as Delete.
- Added selected-action glow feedback and Commander success or error states for notification actions.
- Added Hub navigation, launch, and confirmation sounds using the existing Commander sound and volume settings.
- Added adaptive scrolling for long Hub action lists so every option remains visible on smaller displays.
- Added support for returning to Hub after opening a notification destination where Android permits normal caller navigation.
- Added conservative notification deduplication for equivalent group headers, message rows, and missed-call notifications.
- Added suppression of short-lived action receipts such as "message archived" after the result is confirmed inside Commander.
- Improved Todoist reminder titles so Hub shows the task name when Todoist supplies it.
- Improved plain-text Tasker aliases so exact tasks are ranked alongside apps and execute immediately when selected.

## 0.1.0-alpha.4

- Added complete, keyboard-navigable result lists for apps, app shortcuts, Settings, aliases, Tasker tasks, files, folders, and contacts.
- Added viewport-aware app icon and file preview loading so newly visible rows populate without loading every result in advance.
- Added notification ignore rules for individual conversations, notification types, and entire apps, with restoration through Hub Manage.
- Added an option to keep or close the Hub reply popup after sending. The popup now remains available if the originating notification is replaced or removed.
- Improved Hub reply reconciliation to prevent duplicate outgoing messages and repeated sends.
- Improved alias acceptance from physical keyboards and prevented duplicate Enter callbacks from executing an alias prematurely.
- Fixed app-search crashes caused by temporary duplicate lazy-list row identities.
- Fixed result positioning after returning from a search and kept bottom clearance without adding unwanted top padding.
- Fixed long result lists so keyboard selection remains visible and no longer stops at the initial viewport.

## 0.1.0-alpha.3.1

- Added keyboard-driven seen state in Commander Hub. A notification becomes seen after remaining selected for 300 ms.
- Fixed reply popups opened with O or Enter so the notification is marked seen in Commander.
- Kept rapidly passed notifications unseen and preserved Android's original notification state.

## 0.1.0-alpha.3

- Added optional web-search fallback when app search has no matches.
- Added alias suggestions with app and Tasker icons, keyboard navigation, and touch scrolling.
- Added a configurable Android Settings alias with direct, searchable destinations for common system settings.
- Added touch long-press on app-search results to open Android App info.
- Added Finance and Tasks notification categories with dedicated icons.
- Added Auto, Always, and Hidden visibility modes for every optional Hub tab.
- Improved Hub summary detection so ordinary notifications used as Android group headers remain in their normal category.
- Improved incoming and outgoing message detection for supported messaging notifications.
- Fixed Hub unread highlighting so closing Hub or dismissing one item does not mark every notification as read.
- Fixed keyboard navigation through long Hub and alias lists so the selected item remains visible.
- Improved spacing at the bottom of long Hub lists.
- Reorganised Commander Settings into clearer feature categories.

## 0.1.0-alpha.2

- Added structured Google Maps searches and directions with `from`, `to`, and travel-mode parsing.
- Added Gmail search hand-off through Gmail's exact Android search activity.
- Added 1Password search hand-off through its Search shortcut and scoped accessibility query entry.
- Added structured Waze search and current-location navigation hand-off.
- Reworked Gmail search to open the stable launcher activity and enter the query through scoped accessibility automation.
- Removed root-assisted Accessibility persistence from the public app.
- Reworked contact permission prompts so access is requested with context from Settings or first use.

## 0.1.0-alpha.1

- Introduced Commander Bar and Commander Hub as a keyboard-first Android command palette.
- Added app, shortcut, contact, file, folder, Tasker, alias, calculator, unit conversion, timer, task, calendar, web, and AI workflows.
- Added notification filtering, keyboard navigation, dismissal, opening, and supported direct replies in Hub.
- Added appearance, glow, blur, sound, trigger, alias, and integration settings.
- Added versioned settings export and import for device and package migration.
- Added phone-number normalisation to remove duplicate contact results caused only by formatting.
- Fixed files-only and folders-only prefixes so filter state remains stable while typing.
- Prevented directory-like storage records from appearing as files.
- Prepared the permanent `com.astroboii47.commander` application identity.

Commander Home is present internally but hidden from the alpha launcher build while it remains experimental.
