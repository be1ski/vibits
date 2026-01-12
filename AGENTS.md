# Repository Guidelines

This repository is a Kotlin Multiplatform (KMP) client for Memos with shared Compose UI and platform launchers.

## Project Structure & Module Organization

- `shared/` — shared UI, models, networking, DI, and platform abstractions.
- `androidApp/` — Android entry point and app manifest.
- `desktopApp/` — Compose Desktop entry point.
- `gradle/` and root Gradle files — build configuration and wrapper.

Kotlin sources live under `src/<sourceSet>/kotlin/...`. Platform resources (if any) live under module `src/.../res`.

## Build, Test, and Development Commands

- `./gradlew :desktopApp:run` — run the desktop app locally.
- `./gradlew :androidApp:installDebug` — build and install the Android app on a device/emulator.
- `./gradlew :shared:compileKotlinDesktop` — fast compile check for shared code.

## Coding Style & Naming Conventions

- Kotlin style: official (`kotlin.code.style=official`).
- Indentation: 2 spaces (match existing files).
- Naming: PascalCase types, camelCase functions/vars, UPPER_SNAKE_CASE constants.
- Avoid `!!`; keep composables small and focused.
- Add KDoc to public types/functions.

## Testing Guidelines

No enforced test suite yet. If adding tests, place them under:

- `shared/src/commonTest` for shared KMP tests.
- `androidApp/src/test` or `androidApp/src/androidTest` for Android.

## Commit & Pull Request Guidelines

- Commit messages follow the current history: imperative, concise, single topic (e.g., “Simplify README”).
- Use a transparent flow: make changes, run the relevant app command, verify behavior, then commit and push.
- PRs (if used) should include a summary, testing performed, and screenshots for UI changes.

## Security & Configuration Tips

- Do not commit tokens or secrets; use the app UI to store credentials locally.
- Logs must not include auth tokens or request bodies.
