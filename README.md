# Memos KMP

Cross-platform client for Memos built with Kotlin Multiplatform and Compose Multiplatform.

## Modules

- `shared/` - KMP shared code: networking, models, UI, DI, and platform abstractions.
- `androidApp/` - Android app wrapper for shared UI.
- `desktopApp/` - Desktop app wrapper for shared UI.
- `iosApp/` - iOS app wrapper that hosts the shared UI via XCFramework.
- `webApp/` - Web entry point for the shared UI (Wasm).

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
├── iosApp/ (iOS entry)
├── webApp/ (Web entry)
```

## Quick start

### Desktop

```bash
./gradlew :desktopApp:run
```

### Desktop (DMG)

```bash
./gradlew :desktopApp:packageDmg -Dorg.gradle.java.home=$(/usr/libexec/java_home -v 21)
```

Output: `desktopApp/build/compose/binaries/main/dmg/Memos-1.0.0.dmg`

### Android

```bash
./gradlew :androidApp:installDebug
```

### Web

```bash
./gradlew :webApp:wasmJsBrowserDevelopmentRun
```

### iOS

```bash
./gradlew :shared:assembleSharedDebugXCFramework
```

Then add `shared/build/XCFrameworks/debug/shared.xcframework` to the Xcode target and run from Xcode.

## Testing and TDD

We follow TDD for business logic and aim for high coverage (100% when practical).

- Unit tests live in `shared/src/commonTest`.
- Test names use backticks with `when ... then ...` phrasing and follow `given/when/then`.
- Run unit tests with:
  ```bash
  ./gradlew :shared:desktopTest
  ```
- Generate a coverage report (JVM/desktop target):
  ```bash
  ./gradlew :shared:jacocoDesktopTestReport
  ```
  Output: `shared/build/reports/jacoco/jacocoDesktopTestReport/html/index.html`
- Before every commit: run all tests, generate coverage, and update the coverage numbers below.

## Dependencies

- Keep Gradle dependencies and `gradle/libs.versions.toml` entries alphabetically sorted within each block.

## Coverage

Last updated: 2026-01-13
Desktop (Jacoco):
- Instructions: 22%
- Branches: 11%
- Lines: 768/2,646 (29%)
