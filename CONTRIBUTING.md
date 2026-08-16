# Contributing

Commander is currently focused on reliability, performance, keyboard behaviour, and device compatibility.

## Before opening an issue

- Search existing issues.
- Reproduce the problem on the latest alpha.
- Remove personal information from screenshots and logs.
- Include the device, Android version, launcher, keyboard, and exact steps.

## Pull requests

Keep changes focused. Avoid unrelated formatting or dependency updates. Before submitting:

```bash
./gradlew lintDebug assembleDebug
```

Explain the user-visible behaviour, devices tested, and any new permissions or external services. UI changes should include before-and-after captures with private content removed.

Do not include proprietary assets, third-party credentials, copied marketing media, or code without a compatible licence and attribution.
