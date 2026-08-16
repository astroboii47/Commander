# Commander 0.1.0 alpha 2

This alpha expands search hand-offs and makes Android permission requests clearer.

> **Play Protect notice:** Commander is distributed directly through GitHub rather than Google Play. Play Protect may therefore describe the app as unknown or block installation. Commander also offers optional sensitive Android features such as Accessibility, notification access, contacts, SMS, and file access. Download only from this release, verify the attached SHA-256 checksum, and do not disable Play Protect globally. More information is available in the [setup guide](https://github.com/astroboii47/Commander/blob/main/docs/SETUP.md#play-protect-warning).

## New in this build

- Added structured Google Maps previews for place searches and directions.
- Added natural directions parsing for `from`, `to`, driving, walking, cycling, and transit.
- Added Gmail aliases that open Gmail search and enter the query through scoped accessibility automation.
- Added 1Password search hand-off through its published Search shortcut, with local query entry through Commander's accessibility service.
- Added Waze search and current-location navigation previews.
- Removed the root-assisted home-typing option from the public build.
- Added an explained contact-access action when `@` or `#` is first used.
- Added contact permission status and access controls to Commander Settings.
- Kept SMS permission tied only to the optional direct-SMS setting.

## Notes

- Search aliases remain disabled until you assign one or more aliases in Commander Settings.
- Google Maps place suggestions are not included in this build. Searches and directions are handed to Maps after confirmation.
- 1Password must be unlocked before Commander can enter the search query. Its Android shortcut does not accept query extras, so this integration requires Commander's accessibility service.
- Waze can navigate from the current location to a destination. Its public deep links do not accept a custom starting point.
