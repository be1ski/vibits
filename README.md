# Vibits

[![CI](https://github.com/be1ski/vibits/actions/workflows/ci.yml/badge.svg)](https://github.com/be1ski/vibits/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/be1ski/vibits/graph/badge.svg)](https://codecov.io/gh/be1ski/vibits)
[![Release](https://img.shields.io/github/v/release/be1ski/vibits)](https://github.com/be1ski/vibits/releases/latest)
[![Live Demo](https://img.shields.io/badge/demo-live-brightgreen)](https://be1ski.github.io/vibits/)

![Vibits](.github/hero.webp)

Habit tracker powered by [Memos](https://github.com/usememos/memos)

- **Platforms:** Android · iOS · macOS · Windows · Web
- **Modes:** Online (Memos sync) · Offline · Demo
- **Locales:** 🇬🇧 🇪🇸 🇨🇳 🇮🇳 🇸🇦 🇧🇷 🇷🇺 🇺🇦 🇧🇾 🇰🇿 🇺🇿 🇬🇪 🇦🇿 🇰🇬 🇹🇯 🇷🇴 🇹🇲 🇯🇵 🇩🇪 🇫🇷

## Highlights
- Kotlin Multiplatform with platform entry points for Android, iOS, desktop, and web
- Compose Multiplatform UI with TEA (Elm Architecture) state management
- Stack — Metro DI, Ktor, Room, kotlinx serialization/datetime
- Quality gates: ktlint, detekt, and Kover coverage reports

## Build & Test
- `./gradlew checkAll` — lint, detekt, compile, tests
- `./gradlew checkJvm` — JVM-only checks
- `./gradlew checkIos` — iOS checks
- `./gradlew koverXmlReport` — coverage report
- `./gradlew screenshotTests` — UI screenshot tests
- `./gradlew :androidApp:assembleRelease` — Android APK
- `./gradlew :desktopApp:packageDmg` — macOS DMG
- `./gradlew :desktopApp:packageMsi` — Windows MSI
- iOS — Xcode project at `iosApp/vibits/vibits.xcodeproj`

## Repo Layout
```
core/         — TEA foundation, UI, strings, platform abstractions
feature/      — feature modules (auth, habits, memos, sync, settings, onboarding, mode)
build-logic/  — Gradle convention plugins
androidApp/   — Android entry point
desktopApp/   — Desktop entry point
iosApp/       — iOS wrapper (Xcode project + framework)
webApp/       — Web entry point
```

## CI/CD
CI runs `checkJvm` (Linux) and `checkIos` (macOS). [Release workflow](https://github.com/be1ski/vibits/actions/workflows/release.yml) builds and publishes in parallel:
- **Android APK** → GitHub Releases + Firebase App Distribution
- **macOS DMG / Windows MSI** → GitHub Releases
- **Web** → GitHub Releases + GitHub Pages
