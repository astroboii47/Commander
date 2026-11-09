# Commander 0.1.0 alpha 7

Alpha 7 makes Commander substantially more usable on conventional touch-screen phones while preserving its fast keyboard-first workflows.

## Highlights

- **Faster Commander Bar:** launch, focus, keyboard hand-off, and first-character handling have been tightened to reduce intermittent cold-start delay.
- **Flexible triggers:** commands can have multiple custom triggers, including words and symbols, and supported triggers work at the start, end, or inline in a query.
- **Quick web lookup:** optional live suggestions can open in a dismissible browser sheet, a privacy browser, or the full default browser.
- **Split-screen workflows:** choose two apps, split the active app, or use a fast Recents-assisted flow on compatible Android builds.
- **Better touch support:** result actions, file previews, app pickers, trigger pills, and a 1×1 Commander Bar widget make physical keys optional for more workflows.
- **Assistant routing:** supported assistant invocation types can route separately to Commander Bar or another selected app.

## Experimental Home status

Commander Home is still under active development. Its code remains in the project, but its launcher entry, shortcut, search result, and Settings UI are disabled in this public build. Commander Bar and Commander Hub continue to work independently.

## Privacy note

Live Google query suggestions are optional and disabled by default. Enabling them sends the current web query to Google Suggest. Quick search uses the installed browser’s Custom Tab; private search is delegated to a supported installed privacy browser such as Firefox Focus.

## Compatibility

This release adds testing and refinements for Nothing OS on CMF Phone 2 Pro alongside the existing Titan 2 and Zinwa Q25 testing. Android OEM behaviour still varies for assistant gestures, blur, split screen, Accessibility, and background app launches.
