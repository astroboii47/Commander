# Commander 0.1.0 Alpha 6

Alpha 6 is Commander's largest update so far. It expands the keyboard-first command palette into a broader navigation and control layer while continuing to prioritise fast launch, physical-key input, and compact phone displays.

## Highlights

- Key Control adds keyboard hints and searchable actions over supported Android interfaces.
- Commander hardware shortcuts can open Command Bar, Hub, App Switcher, or Key Control without a separate key-mapping app.
- App Switcher gains faster previews, hold-to-switch controls, sticky mode, filtering, keyboard navigation, and swipe-to-close.
- Quick Controls now cover common device, audio, display, media, battery, and device-specific actions where Android or the device ROM exposes them.
- Todoist gains Tasks, Upcoming, and Calendar pages with filtering, keyboard completion, due times, and live refresh.
- Commander Contacts groups SMS contacts and supported app conversations into clearer person-based routes.
- Each Commander Contact can have a default messaging route that appears first when searching by person.
- `@recents` provides fast access to recent replyable conversations and their pending message context.
- Hub gains encrypted notification history with selectable retention and a dedicated History view.
- Settings gains search, category shortcuts, and a clearly visible Commander Contacts entry.

Signal and Telegram use Commander's matching fallback messaging icons until an appropriate notification glyph has been captured on the device. This does not affect messaging behaviour.

See [CHANGELOG.md](../CHANGELOG.md) for the complete list of additions, improvements, and fixes.

## Getting started with the new features

### Key Control

Search for **Key Control** or assign its dedicated Android or Commander hardware shortcut. Letter hints represent actions exposed by the current screen. Type a hint to activate it, or press Shift to search the available control labels. Key Control depends on the accessibility information supplied by the app, so coverage varies between screens.

### App Switcher

Search for **App Switcher** or assign its shortcut. Use Left and Right to navigate, Enter to switch, type to filter, or swipe a card upward to close it. Hold-to-switch mode selects an app while a modifier is held and opens it when released. Sticky mode leaves the switcher open for filtering.

### Tasks

Search for **Tasks** or enter `tasks `. Use Left and Right to move between Tasks, Upcoming, and Calendar. Use Up and Down to select a task, Enter to complete it, and Shift+Enter to open it in Todoist.

### Quick Controls

Search for **Quick Controls** or use the configured alias. Controls can also be mixed into ordinary app results from Settings. Some controls toggle immediately, while controls such as brightness, volume, ringer mode, and battery protection accept a value or mode.

### Commander Contacts

Open **Commander Contacts** from the top of Settings. Create a person from an unassigned messaging notification, attach additional app identities or an SMS contact, then choose a default route. Search with `@name`, scope the search with inputs such as `@Messenger `, or use `@recents` for recent replyable conversations.

### Hub history

History is off by default. Choose a retention period under **Hub & command bar**, then use the History icon beside Manage in Hub. Stored snapshots are encrypted and do not retain notification images or live reply and action handles.

## Compatibility

Commander now requires Android 10 or newer.

Most development and testing has been performed on the Unihertz Titan 2 and Zinwa Q25. Other Android devices and keyboard phones are welcome, but behaviour can vary between manufacturers, Android versions, launchers, keyboards, and third-party app versions.

Some capabilities require optional Android access:

- Accessibility for home typing, hardware shortcuts, Key Control, and selected hand-off flows
- Notification access for Hub, recent conversations, notification actions, and notification-derived messaging routes
- Contacts for phone contact lookup
- SMS for optional direct carrier SMS
- File access for device file and folder search
- Shizuku for supported privileged App Switcher and device-control features

## Installation notice

> **Play Protect may warn that Commander is unknown or block the installation.** Commander is distributed directly through GitHub rather than Google Play, so new releases do not yet have Play Store reputation. Commander also offers optional Accessibility, notification, contact, SMS, and file features, which Android correctly treats as sensitive.

Download Commander only from the official [GitHub Releases page](https://github.com/astroboii47/Commander/releases). Do not disable Play Protect globally. If Android does not offer an informed install-anyway option, wait for a verified distribution route or build Commander from the public source.

Before installing an alpha update, exporting Commander settings is recommended. Future updates must be signed with the same official Commander signing key.
