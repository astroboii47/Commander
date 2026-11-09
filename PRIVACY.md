# Privacy

Commander does not contain advertising, analytics, telemetry, or crash-reporting SDKs.

## Data stored on the device

Commander stores preferences, aliases, selected integrations, notification-read state, saved messaging people and routes, and optional API credentials in its private app storage. Gemini and Todoist credentials are encrypted using a key held by Android Keystore.

Notification history is off by default. If enabled, Commander stores a limited snapshot containing the notification title, text, app, category, and time for the selected retention period. History is encrypted with a key held by Android Keystore. Notification images, reply handles, and notification action handles are not retained. History can be cleared from its screen or disabled from Settings.

Uninstalling Commander removes its private app data. Android backup behaviour can depend on device settings and firmware.

## Data accessed by optional features

- **Notification access:** reads active notifications so Commander Hub can display, dismiss, open, categorise, and reply to them. It also discovers replyable messaging routes and can retain notification history when the user enables that option. This information remains in private app storage.
- **Accessibility:** receives physical key events for optional launcher home-screen typing and performs narrowly scoped automation for supported handoff features.
- **Contacts:** searches local contacts for calling and messaging commands.
- **SMS:** sends an SMS only after the user invokes the corresponding messaging workflow.
- **Files:** searches shared storage when full-file access is granted.

## External services

Commander makes network requests only when the user invokes a network-backed feature:

- Gemini prompts and conversation history are sent to Google’s Gemini API using the user’s API key.
- Direct Todoist tasks are sent to Todoist using the user’s personal token.
- Search aliases and web search open the selected app or service with the typed query.
- When optional web-search previews are enabled, the current web query is sent to Google Suggest to retrieve live query suggestions. This is disabled by default.
- Quick web search opens the result in a browser Custom Tab. Private search is handed to a supported installed privacy browser; Commander does not implement or claim its own private-browsing session.
- ChatGPT prompts are handed to the installed ChatGPT app when that command is used.

Those services process data under their own terms and privacy policies. Commander does not operate a server or receive a copy of these requests.

## Logs

Release builds do not intentionally log typed command text, API keys, message bodies, or notification content. Non-sensitive integration failures may be recorded in Android system logs for troubleshooting.

## Questions

For a privacy concern, open a GitHub issue without including private content. Security-sensitive reports should follow [SECURITY.md](SECURITY.md).
