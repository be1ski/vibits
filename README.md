# Memos KMP

Cross-platform client for Memos built with Kotlin Multiplatform and Compose Multiplatform.

## Modules

- `shared/` - KMP shared code: networking, models, UI, DI, and platform abstractions.
- `androidApp/` - Android app wrapper for shared UI.
- `desktopApp/` - Desktop app wrapper for shared UI.
- `app/` - Legacy Android-only module (kept for reference; not wired into builds).

## Structure overview

```
.
├── shared/
│   ├── src/commonMain/kotlin/... (models, repository, UI, charts, DI)
│   ├── src/androidMain/kotlin/... (OkHttp + SharedPreferences)
│   ├── src/desktopMain/kotlin/... (CIO + Preferences)
│   └── src/iosMain/kotlin/... (Darwin + NSUserDefaults)
├── androidApp/ (Android entry activity + Application)
├── desktopApp/ (Desktop entry main)
└── app/ (legacy Android-only module)
```

## Quick start

### Desktop

```bash
./gradlew :desktopApp:run
```

### Android

```bash
./gradlew :androidApp:installDebug
```

## Features

- Memos API integration via Ktor with pagination
- Token auth
- Activity grid + weekly bar chart
- Profile and posts tabs
- Cross-platform credential storage

## Configuration

Credentials are stored after the first successful load:

- Android: `SharedPreferences`
- Desktop: `java.util.prefs.Preferences`
- iOS: `NSUserDefaults`

To edit credentials later, use the "Edit credentials" button in the UI.
