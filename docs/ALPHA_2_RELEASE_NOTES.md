# Commander 0.1.0 alpha 2

This alpha expands search hand-offs and makes Android permission requests clearer.

## New in this build

- Added structured Google Maps previews for place searches and directions.
- Added natural directions parsing for `from`, `to`, driving, walking, cycling, and transit.
- Added Gmail aliases that hand search terms directly to Gmail's Android search activity.
- Added 1Password search hand-off through its published Search shortcut, with local query entry through Commander's accessibility service.
- Added Waze search and current-location navigation previews.
- Added an explained contact-access action when `@` or `#` is first used.
- Added contact permission status and access controls to Commander Settings.
- Kept SMS permission tied only to the optional direct-SMS setting.

## Notes

- Search aliases remain disabled until you assign one or more aliases in Commander Settings.
- Google Maps place suggestions are not included in this build. Searches and directions are handed to Maps after confirmation.
- 1Password must be unlocked before Commander can enter the search query. Its Android shortcut does not accept query extras, so this integration requires Commander's accessibility service.
- Waze can navigate from the current location to a destination. Its public deep links do not accept a custom starting point.

Most testing for this update was completed on the Unihertz Titan 2. Device and app-version reports are welcome.
