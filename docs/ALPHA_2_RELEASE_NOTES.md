# Commander 0.1.0 alpha 2

This alpha expands search hand-offs and makes Android permission requests clearer.

## New in this build

- Added structured Google Maps previews for place searches and directions.
- Added natural directions parsing for `from`, `to`, driving, walking, cycling, and transit.
- Added Gmail aliases that hand search terms directly to Gmail's Android search activity.
- Added a best-effort 1Password search alias. Commander opens 1Password when the installed version does not expose Android search.
- Added an explained contact-access action when `@` or `#` is first used.
- Added contact permission status and access controls to Commander Settings.
- Kept SMS permission tied only to the optional direct-SMS setting.

## Notes

- Search aliases remain disabled until you assign one or more aliases in Commander Settings.
- Google Maps place suggestions are not included in this build. Searches and directions are handed to Maps after confirmation.
- 1Password 8 currently does not expose a searchable Android activity on the tested Titan 2 build, so its fallback opens the app without inserting the query.

Most testing for this update was completed on the Unihertz Titan 2. Device and app-version reports are welcome.
