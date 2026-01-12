# androidApp

Android entry point for the shared KMP UI.

## Key files

- `src/main/kotlin/.../MainActivity.kt` - hosts `MemosApp`
- `src/main/kotlin/.../MemosApplication.kt` - initializes Koin and Android context
- `src/main/AndroidManifest.xml` - app config

## Build

```bash
./gradlew :androidApp:installDebug
```

## Notes

- All UI lives in `shared` module.
- This module should stay thin and platform-specific only.
