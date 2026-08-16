# Commander setup

## Back up or migrate settings

Open **Commander Settings**, then use **Export settings** to save a versioned Commander JSON backup with Android's document picker. On another installation, choose **Import settings** and select that file.

The backup contains preferences, aliases, app mappings, Tasker mappings, appearance, sounds, custom icons, and feature toggles. It intentionally excludes Gemini and Todoist credentials, notification state, drafts, and other temporary Hub data. Enter API credentials again after importing.

Commander is modular. Enable only the access required by the features you use.

## 1. Install

Download the APK from GitHub Releases and install it. Android may ask you to allow installations from your browser or file manager.

### Play Protect warning

Play Protect may describe Commander as unknown or block installation because the alpha is distributed directly through GitHub rather than Google Play. Commander also provides optional features that use sensitive Android access, including Accessibility, notification access, contacts, SMS, files, and root assistance.

This warning should not be bypassed blindly. Download the APK only from the official Commander Releases page and compare it with the SHA-256 checksum attached to that release. Do not disable Play Protect globally. If Android does not provide an informed install-anyway option, wait for a verified distribution route or build the app from the public source.

Updates must be signed with the same key as the installed build. If Android reports an incompatible signature, uninstall the previous development build before installing the public alpha. Uninstalling clears Commander settings.

## 2. Open Commander Bar

Commander Bar can be launched from:

- its Android shortcut;
- a launcher gesture;
- a hardware-key mapping app such as Key Mapper;
- optional typing from the launcher home screen.

The normal Android launcher entry opens Commander Hub. Commander Home is experimental and hidden in the alpha build.

## 3. Home-screen typing

1. Open **Commander Settings**.
2. Enable **Type from home screen**.
3. Tap **Open Command app info**.
4. If Android shows the option, open the three-dot menu and choose **Allow restricted settings**.
5. Return to Commander Settings and tap **Open Accessibility settings**.
6. Enable **Commander home typing**.

This service listens for physical key events while a launcher home screen is active. It does not enable Commander over every app; use the normal Commander Bar shortcut outside the launcher.

The optional root setting can restore this Accessibility service after reboot on supported rooted devices. It invokes `su` only when that setting is enabled.

## 4. Commander Hub

Open Commander Hub and grant Notification access when prompted. Hub displays active notifications known to Android’s notification listener.

- Tap a notification to open its content intent when one exists.
- Swipe or use the configured keyboard action to dismiss it.
- Reply is available only when the notification exposes a direct-reply action.
- Aggregate notification summaries are hidden by default and can be enabled in Settings.

## 5. Contacts and messaging

Grant Contacts permission to search names after `@` or `#`. SMS sending requires the SMS permission and confirmation flow provided by Commander. Messenger and other third-party messaging behaviour depends on the shortcuts or active reply notifications exposed by the installed app.

## 6. Files and folders

File search requires Android’s full-file access on supported versions. The default trigger is `Space` and can be changed in Settings.

- `f ` filters to files with the default configuration.
- `fo ` filters to folders with the default configuration.
- Tap the sort label to switch between relevance and modification-time order.

Opening a folder is delegated to an installed file manager. Solid Explorer is the primary tested file manager.

## 7. Todoist

Commander can either open Todoist Quick Add or send a task directly through Todoist’s API.

For direct add:

1. Obtain a personal API token from Todoist.
2. Enter it in Commander Settings.
3. Enable direct Todoist add.

The token is encrypted with Android Keystore. Natural-language recognition is an indicator before submission; Todoist remains responsible for final parsing.

## 8. Gemini and ChatGPT

- `?` uses the Gemini API inside Commander and requires your own Google AI Studio key.
- `??` hands the prompt to the installed ChatGPT app.

The Gemini key is encrypted with Android Keystore. Queries sent to Gemini are governed by Google’s service terms and privacy policy. ChatGPT handoff and notification preview behaviour can vary between app versions.

## 9. Search aliases and Tasker

Settings can assign one or more aliases to supported search targets. Enter the alias, a space, then the query. Tasker aliases use Tasker’s task-selection and execution interfaces and require Tasker to be installed and configured.

## 10. Keyboard navigation

Commander Bar supports arrow-key selection and Enter to activate the selected result. Backspace on an empty field moves back one command stage or closes the overlay.

Commander Hub supports arrow keys and Enter. Optional quick navigation adds:

- `I` / `K`: previous or next notification
- `J` / `L`: previous or next category
- `O`: open or reply
- `U`: dismiss

## Troubleshooting

When reporting a problem, include:

- device and Android version;
- launcher and keyboard app;
- whether a physical or on-screen keyboard was used;
- the relevant Commander setting;
- exact steps to reproduce;
- a screen recording with private content removed, when practical.
