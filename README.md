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

## Key files

### shared

- `shared/src/commonMain/kotlin/space/be1ski/memos/shared/ui/MemosApp.kt` - shared UI root
- `shared/src/commonMain/kotlin/space/be1ski/memos/shared/ui/MemosViewModel.kt` - state and actions
- `shared/src/commonMain/kotlin/space/be1ski/memos/shared/data/MemosRepository.kt` - API access with pagination
- `shared/src/commonMain/kotlin/space/be1ski/memos/shared/ui/components/ContributionGrid.kt` - activity charts
- `shared/src/commonMain/kotlin/space/be1ski/memos/shared/di/SharedModule.kt` - DI wiring

### androidApp

- `androidApp/src/main/kotlin/space/be1ski/memos/android/MainActivity.kt` - Android entry activity
- `androidApp/src/main/kotlin/space/be1ski/memos/android/MemosApplication.kt` - app initialization

### desktopApp

- `desktopApp/src/desktopMain/kotlin/space/be1ski/memos/desktop/DesktopMain.kt` - desktop entry point

### app (legacy)

- `app/src/main/java/space/be1ski/memos/MainActivity.kt` - legacy activity template

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

## Docs

- `ARCHITECTURE.md` - high-level design
- `CONTRIBUTING.md` - workflow and expectations
- `SECURITY.md` - security policies
- `CHANGELOG.md` - release notes
- `CODE_OF_CONDUCT.md` - community guidelines
- `CONTEXT.md` - project context and decisions

## License

TBD.
