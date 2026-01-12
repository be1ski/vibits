# Memos KMP

Cross-platform client for Memos built with Kotlin Multiplatform and Compose Multiplatform.

## Modules

- `shared/` - KMP shared code: networking, models, UI, DI, and platform abstractions.
- `androidApp/` - Android app wrapper for shared UI.
- `desktopApp/` - Desktop app wrapper for shared UI.

## Structure overview

```
.
├── shared/
│   ├── src/commonMain/kotlin/... (shared code)
│   ├── src/androidMain/kotlin/... (Android specifics)
│   ├── src/desktopMain/kotlin/... (Desktop specifics)
│   └── src/iosMain/kotlin/... (iOS specifics)
├── androidApp/ (Android entry)
├── desktopApp/ (Desktop entry)
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
