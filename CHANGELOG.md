# Changelog

## 0.1.0-alpha.2

- Added structured Google Maps searches and directions with `from`, `to`, and travel-mode parsing.
- Added Gmail search hand-off through Gmail's exact Android search activity.
- Added 1Password search hand-off through its Search shortcut and scoped accessibility query entry.
- Added structured Waze search and current-location navigation hand-off.
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
