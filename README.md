# Vibits

[![CI](https://github.com/be1ski/vibits/actions/workflows/ci.yml/badge.svg)](https://github.com/be1ski/vibits/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/be1ski/vibits/graph/badge.svg?token=63WZCYQE5I)](https://codecov.io/gh/be1ski/vibits)
[![Release](https://img.shields.io/github/v/release/be1ski/vibits)](https://github.com/be1ski/vibits/releases/latest)
[![Live Demo](https://img.shields.io/badge/demo-live-brightgreen)](https://be1ski.github.io/vibits/)

![Vibits](.github/hero.webp)

Habit tracker powered by [Memos](https://github.com/usememos/memos). Built with Kotlin Multiplatform + Compose Multiplatform.

## Modules

- `shared/` - UI, networking, models, DI, platform abstractions.
- `androidApp/` - Android launcher.
- `desktopApp/` - Desktop launcher.
- `iosApp/` - iOS wrapper (XCFramework).
- `webApp/` - Web entry (Wasm).

## Run

- Desktop: `./gradlew :desktopApp:run`
- Android: `./gradlew :androidApp:installDebug`
- Web: `./gradlew :webApp:wasmJsBrowserDevelopmentRun`
- iOS XCFramework: `./gradlew :shared:assembleSharedDebugXCFramework`
- Desktop DMG: `./gradlew :desktopApp:packageDmg -Dorg.gradle.java.home=$(/usr/libexec/java_home -v 21)`
- DMG output: `desktopApp/build/compose/binaries/main/dmg/Vibits-1.0.0.dmg`

## Tests

We use TDD for business logic and target high coverage.

- Unit tests: `shared/src/commonTest`
- Test naming: backticks with `when ... then ...`
- Run tests: `./gradlew :shared:desktopTest`
- Coverage: `./gradlew :shared:jacocoDesktopTestReport`
- Coverage report: `shared/build/reports/jacoco/jacocoDesktopTestReport/html/index.html`
