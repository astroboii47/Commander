# Release checklist

This document is for maintainers. Never commit a signing store or password.

## One-time signing setup

1. Create a dedicated Commander release-signing key and store it in a secure backup location.
2. Copy `keystore.properties.example` to `keystore.properties`.
3. Enter the absolute signing-store path, alias, and passwords.
4. Confirm both the signing store and `keystore.properties` are ignored by Git.

As an alternative to `keystore.properties`, the build accepts `COMMANDER_STORE_FILE`, `COMMANDER_STORE_PASSWORD`, `COMMANDER_KEY_ALIAS`, and `COMMANDER_KEY_PASSWORD` environment variables. This is useful when credentials are stored in a password manager or macOS Keychain.

Every public Commander update must use the same signing key. Losing it prevents compatible updates; exposing it allows unauthorised builds to impersonate the project.

## Prepare a release

1. Update `versionCode` and `versionName` in `app/build.gradle.kts`.
2. Update `CHANGELOG.md`.
3. Ensure the Commander Home launcher flag has the intended value.
4. Run:

   ```bash
   ./gradlew clean lintDebug testDebugUnitTest assembleRelease
   ```

5. Confirm `app-release.apk` is signed with the expected certificate.
6. Install the release APK over the previous public build and complete the smoke test below.
7. Generate a SHA-256 checksum.
8. Create a GitHub prerelease and attach the signed APK, checksum, release notes, and optional MP4 demo.

## Smoke test

- Commander Hub launcher entry opens Hub.
- Commander Home is absent from the launcher for the alpha.
- Commander Bar shortcut opens over another app.
- App search and keyboard navigation work.
- Backspace moves back or closes an empty bar.
- Todoist fallback works without a token; direct add works with a test token.
- Timer, calculator, unit conversion, web search, and one alias work.
- File search handles denied and granted storage access.
- Hub handles missing and granted notification access.
- Hub can open and dismiss a test notification.
- A supported direct-reply notification sends successfully.
- Settings scroll to the final controls on the smallest supported display.
- Light and dark appearance remain readable.
- No private test credentials or notification content appear in the APK, screenshots, logs, or release notes.

## Alpha release notes

Mark `0.1.0-alpha.1` as a prerelease. State the tested devices, keyboard-first focus, required permissions, experimental integrations, known limitations, and upgrade-signing expectations plainly.
