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

## Features

- **Cross-platform**: Single codebase for Android, iOS, Desktop (macOS/Windows/Linux), and Web (WASM)
- **Flexible sync**: Work online with Memos backend, offline mode, or try the demo
- **TEA architecture**: The Elm Architecture for predictable state management
- **Type-safe DI**: Compile-time dependency injection with [Metro](https://zacsweers.github.io/metro/)
- **Multi-language**: 17+ language translations built-in
- **High code sharing**: ~95% of code shared across all platforms

## Tech Stack

- **UI**: Compose Multiplatform
- **Networking**: Ktor Client
- **Serialization**: kotlinx.serialization
- **DI**: Metro (compile-time)
- **Architecture**: TEA (The Elm Architecture)
- **Testing**: kotlin.test + JaCoCo coverage

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

## CI/CD

[One-click release](https://github.com/be1ski/vibits/actions/workflows/release.yml) → builds all platforms in parallel:
- **Android APK** → GitHub Releases + Firebase App Distribution
- **macOS DMG / Windows MSI** → GitHub Releases
- **Web** → GitHub Releases + GitHub Pages

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

## Contributing

See [AGENTS.md](AGENTS.md) for detailed development guidelines including:
- Feature architecture and module structure
- Dependency injection patterns
- Testing guidelines and TDD practices
- Code style conventions
- CI/CD workflows
