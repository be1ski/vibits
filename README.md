# Vibits

[![CI](https://github.com/be1ski/vibits/actions/workflows/ci.yml/badge.svg)](https://github.com/be1ski/vibits/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/be1ski/vibits/graph/badge.svg)](https://codecov.io/gh/be1ski/vibits)
[![Release](https://img.shields.io/github/v/release/be1ski/vibits)](https://github.com/be1ski/vibits/releases/latest)
[![Live Demo](https://img.shields.io/badge/demo-live-brightgreen)](https://be1ski.github.io/vibits/)

![Vibits](.github/hero.webp)

Habit tracker powered by [Memos](https://github.com/usememos/memos). Kotlin Multiplatform + Compose Multiplatform.

**Platforms:** Android · iOS · Desktop · Web<br>
**Modes:** Online (Memos sync) · Offline · Demo<br>
**Localization:** 🇬🇧 🇪🇸 🇨🇳 🇮🇳 🇸🇦 🇧🇷 🇷🇺 🇺🇦 🇧🇾 🇰🇿 🇺🇿 🇬🇪 🇦🇿 🇰🇬 🇹🇯 🇷🇴 🇹🇲 🇯🇵 🇩🇪 🇫🇷

## Run

```
./gradlew :desktopApp:run
./gradlew :androidApp:installDebug
./gradlew :webApp:wasmJsBrowserDevelopmentRun
```

## Build

```
./gradlew checkAll                                   # lint, detekt, compile, tests
./gradlew koverXmlReport                             # coverage report
./gradlew :androidApp:assembleRelease                # Android APK
./gradlew :iosFramework:assembleSharedReleaseXCFramework  # iOS framework
./gradlew :desktopApp:packageDmg                     # macOS DMG
./gradlew :desktopApp:packageMsi                     # Windows MSI
```

## CI/CD

[One-click release](https://github.com/be1ski/vibits/actions/workflows/release.yml) → builds all platforms in parallel:
- **Android APK** → GitHub Releases + Firebase App Distribution
- **macOS DMG / Windows MSI** → GitHub Releases
- **Web** → GitHub Releases + GitHub Pages

## Modules

```
core/
  elm/           — TEA architecture
  platform/      — Platform abstractions
  strings/       — Localized strings
  ui/            — Compose theme & components
feature/
  auth/          — Authentication
  habits/        — Habits tracking
  main/          — App graph & DI
  memos/         — Memos API
  mode/          — App mode (demo/online)
  onboarding/    — Onboarding flow
  settings/      — Settings
  sync/          — Sync engine
androidApp/      — Android entry point
desktopApp/      — Desktop entry point
iosFramework/    — iOS XCFramework
iosApp/          — iOS Xcode project
webApp/          — Web entry (WASM)
```

## Architecture

TEA (The Elm Architecture) · ~95% shared code
