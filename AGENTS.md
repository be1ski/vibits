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

### Running Tests

- Run shared unit tests: `./gradlew :shared:desktopTest`
- Run iOS simulator tests: `./gradlew :shared:iosSimulatorArm64Test`
- Coverage reports: `./gradlew :shared:jacocoDesktopTestReport`

### Test Organization

- Unit tests live in `shared/src/commonTest` for shared KMP logic.
- Desktop-specific tests in `shared/src/desktopTest`.
- Android-specific tests belong under `androidApp/src/test` or `androidApp/src/androidTest`.
- Test names use backticks with `when ... then ...` phrasing.

### Avoiding Test Rot

Tests must stay useful and up-to-date. Follow these rules:

1. **Run tests before every commit.** If tests don't compile or pass, fix them before pushing.
2. **Never test constants.** Tests like `assertEquals(200, PAGE_SIZE)` provide no value — they just duplicate the constant.
3. **Delete tests when deleting code.** If you remove a function/class, remove its tests too.
4. **New features need tests.** Every new public API should have corresponding tests.
5. **Test behavior, not implementation.** Focus on what code does, not how it does it.

## Commit & Pull Request Guidelines

- Commit messages follow the current history: imperative, concise, single topic (e.g., “Simplify README”).
- Use a transparent flow: make changes, run the relevant app command, verify behavior, then commit and push.
- PRs (if used) should include a summary, testing performed, and screenshots for UI changes.

## Security & Configuration Tips

- Do not commit tokens or secrets; use the app UI to store credentials locally.
- Logs must not include auth tokens or request bodies.
