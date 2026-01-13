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
- Avoid `// given`, `// when`, `// then` comments in tests; use clear test names and structure instead.
- Prefer clean refactors over quick reuse: avoid introducing or keeping code smells, and leave the codebase cleaner than you found it.
- Keep Gradle dependencies and `gradle/libs.versions.toml` entries alphabetically sorted within each block.

## Testing Guidelines

We follow TDD for business logic and aim for high coverage (100% when practical).

- Unit tests live in `shared/src/commonTest` for shared KMP logic.
- Android-specific tests belong under `androidApp/src/test` or `androidApp/src/androidTest`.
- Test names use backticks with `when ... then ...` phrasing; structure tests with `given/when/then`.
- Run shared unit tests with `./gradlew :shared:desktopTest`.
- Coverage reports come from `./gradlew :shared:jacocoDesktopTestReport`.
- Before every commit: run all tests, generate coverage, and update the coverage numbers in `README.md`.

## Commit & Pull Request Guidelines

- Commit messages follow the current history: imperative, concise, single topic (e.g., “Simplify README”).
- Use a transparent flow: make changes, run the relevant app command, verify behavior, then commit and push.
- PRs (if used) should include a summary, testing performed, and screenshots for UI changes.

## Security & Configuration Tips

- Do not commit tokens or secrets; use the app UI to store credentials locally.
- Logs must not include auth tokens or request bodies.
