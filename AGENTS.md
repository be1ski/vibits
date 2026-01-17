# Repository Guidelines

Vibits is a habit tracker powered by Memos, built with Kotlin Multiplatform (KMP) and Compose Multiplatform.

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
- **Avoid meaningless suffixes** like `Info`, `Data`, `Model`, `Object` in class names — they add no semantic value. Use descriptive names that reflect purpose (e.g., `AppDetails` not `AppInfo`, `Credentials` not `CredentialsData`).
- **When extending a class, verify the name still fits.** If you add a field that changes the class's scope (e.g., adding `version` to `StorageInfo`), rename the class to reflect its new purpose.
- Avoid `!!`; keep composables small and focused.
- **Self-documenting code over comments.** Don't add KDoc/comments that restate function names or obvious logic.
- Prefer clean refactors over quick reuse: avoid introducing or keeping code smells, and leave the codebase cleaner than you found it.
- Keep Gradle dependencies and `gradle/libs.versions.toml` entries alphabetically sorted within each block.

## Localization

- **Never hardcode user-facing strings.** All text displayed to users must use string resources from `composeResources/values/strings.xml`.
- **Always add translations immediately.** When adding a new string resource, add the Russian translation in `values-ru/strings.xml` at the same time.
- String resource names use `snake_case` with semantic prefixes: `action_`, `label_`, `msg_`, `title_`, `hint_`, `format_`, etc.

## Testing Guidelines

We follow TDD for business logic and aim for high coverage.

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

1. **Never test constants.** Tests like `assertEquals(200, PAGE_SIZE)` provide no value — they just duplicate the constant.
2. **Delete tests when deleting code.** If you remove a function/class, remove its tests too.
3. **New features need tests.** Every new public API should have corresponding tests.
4. **Test behavior, not implementation.** Focus on what code does, not how it does it.

## Linting

**MANDATORY:** Run lints before every commit and fix all issues.

- Run detekt: `./gradlew detekt`
- All detekt issues must be resolved before committing. Zero tolerance for lint warnings.
- Use `@Suppress` annotations only when the lint rule doesn't apply (e.g., `LongParameterList` for Composables with many parameters is acceptable).

## Commit & Pull Request Guidelines

- Commit messages follow the current history: imperative, concise, single topic (e.g., "Simplify README").
- Use a transparent flow: make changes, run the relevant app command, verify behavior, then commit and push.
- **Pre-commit checklist:**
  1. Run tests: `./gradlew :shared:desktopTest`
  2. Run lints: `./gradlew detekt` — fix all issues
  3. Verify the app runs: `./gradlew :desktopApp:run`
- PRs (if used) should include a summary, testing performed, and screenshots for UI changes.

## Security & Configuration Tips

- Do not commit tokens or secrets; use the app UI to store credentials locally.
- Logs must not include auth tokens or request bodies.
