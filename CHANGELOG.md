# Changelog

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
