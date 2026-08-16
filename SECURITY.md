# Security policy

Commander is alpha software and has not received an independent security audit.

## Reporting a vulnerability

Do not post API keys, access tokens, private messages, contact details, notification content, or a working exploit in a public issue.

Until private GitHub vulnerability reporting is configured, open a minimal public issue stating that you have a security report and wait for a private contact channel. Include no sensitive technical details in that issue.

## Sensitive configuration

- Never commit `local.properties`, signing stores, signing passwords, Gemini keys, or Todoist tokens.
- Use a dedicated release-signing key and keep it outside the repository.
- Revoke a credential immediately if it appears in a log, screenshot, issue, commit, or shared build configuration.

## Privileged features

Notification access, Accessibility, full-file access, SMS, contacts, and optional root assistance are powerful capabilities. They are feature-specific and should remain disabled unless required by the user.
