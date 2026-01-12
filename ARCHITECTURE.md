# Architecture

## Overview

This project is a Kotlin Multiplatform (KMP) client for Memos.
UI is built with Compose Multiplatform and shared across desktop and Android.

## Modules

- `shared/`
  - `commonMain`: domain models, API client, DI, shared UI
  - `androidMain` / `desktopMain` / `iosMain`: platform-specific HTTP engine and credential storage
- `androidApp/`
  - Android entry point for shared UI
- `desktopApp/`
  - Desktop entry point for shared UI
- `app/`
  - Legacy Android-only app template (not used in current builds)

## Data flow

1. UI invokes `MemosViewModel.loadMemos()`
2. `MemosRepository` fetches paginated memos via Ktor
3. UI renders activity charts and posts list

## Networking

- Ktor client in `shared` with per-platform engines
- Pagination uses `pageSize` and `pageToken`
- Authorization uses Bearer token

## Dependency injection

- Koin in `shared` provides `HttpClient`, `MemosRepository`, `MemosViewModel`, `CredentialsStore`

## Credential storage

- Android: `SharedPreferences`
- Desktop: `java.util.prefs.Preferences`
- iOS: `NSUserDefaults`

## UI

- Profile tab: activity grid + weekly bar chart
- Posts tab: list of memos

## Build

- Gradle KMP with Compose Multiplatform
- Version catalog in `gradle/libs.versions.toml`
