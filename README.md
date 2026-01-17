# Vibits

[![CI](https://github.com/be1ski/vibits/actions/workflows/ci.yml/badge.svg)](https://github.com/be1ski/vibits/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/be1ski/vibits/graph/badge.svg?token=63WZCYQE5I)](https://codecov.io/gh/be1ski/vibits)
[![Release](https://img.shields.io/github/v/release/be1ski/vibits)](https://github.com/be1ski/vibits/releases/latest)
[![Live Demo](https://img.shields.io/badge/demo-live-brightgreen)](https://be1ski.github.io/vibits/)

![Vibits](.github/hero.webp)

Habit tracker powered by [Memos](https://github.com/usememos/memos). Kotlin Multiplatform + Compose Multiplatform.

**Platforms:** Android · iOS · Desktop · [Web](https://be1ski.github.io/vibits/)<br>
**Modes:** Online ([Memos](https://github.com/usememos/memos) sync) · Offline · Demo<br>
**Localization:** 🇬🇧 🇷🇺

## Run

```
./gradlew :desktopApp:run
./gradlew :androidApp:installDebug
./gradlew :webApp:wasmJsBrowserDevelopmentRun
```

## Build

```
./gradlew checkAll                                  # lint, detekt, compile, tests
./gradlew :shared:desktopTest                       # unit tests
./gradlew :androidApp:assembleRelease               # Android APK
./gradlew :shared:assembleSharedReleaseXCFramework  # iOS framework
./gradlew :desktopApp:packageDmg                    # macOS DMG
./gradlew :desktopApp:packageMsi                    # Windows MSI
```

## CI

One-click release → builds all platforms in parallel → uploads to [Releases](https://github.com/be1ski/vibits/releases)

## Modules

```
shared/      — UI, networking, models, DI
androidApp/  — Android entry point
desktopApp/  — Desktop entry point
iosApp/      — iOS wrapper
webApp/      — Web entry (WASM)
```

## Architecture

TEA (The Elm Architecture) · ~95% shared code
