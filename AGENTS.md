# Repository Guidelines

Vibits is a habit tracker powered by Memos, built with Kotlin Multiplatform (KMP) and Compose Multiplatform.

## Project Structure & Module Organization

- `shared/` — shared UI, models, networking, DI, and platform abstractions.
- `androidApp/` — Android entry point and app manifest.
- `desktopApp/` — Compose Desktop entry point.
- `gradle/` and root Gradle files — build configuration and wrapper.

Kotlin sources live under `src/<sourceSet>/kotlin/...`. Platform resources (if any) live under module `src/.../res`.

## Package Organization Rules

The project follows strict package organization to maintain clean architecture and enable automatic test coverage exclusions:

### Core Packages

- **`core.platform.*`** — Platform-specific code (expect/actual declarations):
  - `core.platform.locale.LocaleProvider`
  - `core.platform.storage.KeyValueStore`
  - `core.platform.logging.platformLog`
  - All expect/actual interfaces and implementations
  - **Excluded from coverage** (untestable in unit tests)

- **`core.ui.*`** — UI layer with Compose code:
  - `core.ui.theme.*` — Theming, colors, typography
  - `core.ui.date.DateFormatter` — UI helpers with @Composable functions
  - Any code containing @Composable functions
  - **Excluded from coverage** (@Composable cannot be unit tested)

- **`core.*` (other)** — Pure business logic:
  - Must not contain @Composable functions
  - Must not contain expect/actual declarations
  - Should be fully testable with unit tests

### Feature Data Layer

Within `feature/<name>/data/`:

- **`data/room/`** — Room database implementations (platform-specific):
  - DAO interfaces, entities, database classes
  - Excluded from coverage (platform-specific, tested via integration tests)

- **`data/platform/`** — Platform-specific data implementations:
  - expect/actual storage implementations
  - Platform-specific caches
  - Excluded from coverage

- **`data/` (root)** — Testable repository implementations:
  - Repository implementations with business logic
  - DTOs and mappers
  - Should have comprehensive unit tests

## Feature Architecture

Features are autonomous and isolated units that can be run separately and disabled easily. Each feature follows this package structure:

```
feature/<name>/
  di/                 # Dependencies containers and FeatureFactory
  domain/
    model/            # Domain models and types
    usecase/          # Business logic (use cases)
    repository/       # Repository interfaces
  data/               # Repository implementations, DTOs, mappers
    platform/         # Platform-specific implementations (expect/actual)
    room/             # Room database (DAO, entities, database)
  presentation/       # TEA components (Feature, Reducer, EffectHandler)
  view/               # Compose UI (screens, components, dialogs)
```

### Layer Responsibilities

- **domain/** — Pure business logic. No UI or framework dependencies.
- **data/** — Data layer implementations:
  - Repository implementations with business logic
  - DTOs and mappers
  - API clients
  - **data/platform/** — Platform-specific implementations (expect/actual)
  - **data/room/** — Room database implementations (DAO, entities)
- **presentation/** — The Elm Architecture (TEA) components:
  - `*Feature.kt` — State, Action, Effect sealed classes
  - `*Reducer.kt` — Pure state transitions
  - `*EffectHandler.kt` — Side effects (API calls, DB operations)
- **view/** — Compose UI only. Screens, components, dialogs. Excluded from unit test coverage.
- **di/** — Dependency containers and feature factories:
  - `*Dependencies.kt` — Transport containers for composable dependency passing
  - `*FeatureFactory.kt` — Feature instantiation with dependencies

### Dependencies Pattern

`*Dependencies` classes are **transport containers** for passing groups of dependencies through the composable hierarchy. They should NOT be used directly in consumers.

**In `di/` package** — define the container:
```kotlin
@Inject
class SettingsDependencies(
  val connectionTester: ConnectionTester,
  val switchAppMode: SwitchAppModeUseCase,
  val saveCredentials: SaveCredentialsUseCase,
)
```

**In consumers (EffectHandler, Factory)** — inject individual dependencies, not the container:
```kotlin
// CORRECT: Individual dependencies in constructor
class SettingsEffectHandler(
  private val connectionTester: ConnectionTester,
  private val switchAppMode: SwitchAppModeUseCase,
  private val saveCredentials: SaveCredentialsUseCase,
) : EffectHandler<...>

// In Factory: unpack Dependencies into individual params
fun createSettingsFeature(dependencies: SettingsDependencies) =
  FeatureImpl(
    effectHandler = SettingsEffectHandler(
      connectionTester = dependencies.connectionTester,
      switchAppMode = dependencies.switchAppMode,
      saveCredentials = dependencies.saveCredentials,
    ),
  )
```

**WRONG: Using Dependencies directly in consumer:**
```kotlin
// DON'T DO THIS
class SettingsEffectHandler(
  private val deps: SettingsDependencies,  // Bad: consumer knows about container
)
```

## Dependency Injection (Metro)

We use [Metro](https://zacsweers.github.io/metro/) for compile-time DI.

- Add `@Inject` to classes that should be created by Metro.
- Use `@Provides` in `AppGraph` only for platform-specific classes (expect/actual).
- Use `@Binds` to bind implementations to interfaces.
- Scope singletons with `@SingleIn(AppScope::class)`.
- **Use cases:**
  - Stateless use cases without dependencies: use `object` with `operator fun invoke`. Example: `FilterPostsUseCase(memos)`.
  - Use cases with dependencies: use `@Inject class` with `operator fun invoke`.
  - Simple pure utility functions (e.g., date calculations): use top-level functions in `*Utils.kt` files.
  - Operations on data classes: use extension functions in the same file as the data class.
- **Feature dependencies:** Instead of passing many parameters through composables, create a `*Dependencies` class (e.g., `HabitsDependencies`) that bundles all dependencies for a feature. Use `@Inject` so Metro wires it automatically. See `AppDependencies` for reference.

## Build, Test, and Development Commands

- `./gradlew checkAll` — run all checks (ktlint, detekt, compile, tests).
- `./gradlew installGitHooks` — install pre-commit hook that runs `checkAll`.
- `./gradlew :desktopApp:run` — run the desktop app locally.
- `./gradlew :androidApp:installDebug` — build and install the Android app on a device/emulator.

## Coding Style & Naming Conventions

- Kotlin style: official (`kotlin.code.style=official`).
- Indentation: 2 spaces (match existing files).
- Naming: PascalCase types, camelCase functions/vars, UPPER_SNAKE_CASE constants.
- **Avoid meaningless suffixes** like `Info`, `Data`, `Model`, `Object` in class names — they add no semantic value. Use descriptive names that reflect purpose (e.g., `AppDetails` not `AppInfo`, `Credentials` not `CredentialsData`).
- **When extending a class, verify the name still fits.** If you add a field that changes the class's scope (e.g., adding `version` to `StorageInfo`), rename the class to reflect its new purpose.
- **Interface/Implementation naming:** Use classic naming: `Foo` for interface, `FooImpl` for implementation. Never use inconsistent patterns like `FooAction` interface with `FooActioner` implementation. Specific implementations can have descriptive names (e.g., `DemoMemosRepository`, `OfflineMemosRepository` for `MemosRepository`). Variable names must match types: `connectionTester: ConnectionTester`, not `testConnection: ConnectionTester`.
- **Single-method interfaces (fun interface):** For interfaces with a single method that have DI-provided implementations, use `fun interface` with explicit method signature:
  ```kotlin
  fun interface ConnectionTester {
    suspend operator fun invoke(baseUrl: String, token: String): Result<Unit>
  }

  @Inject
  @SingleIn(AppScope::class)
  @ContributesBinding(AppScope::class)
  class ConnectionTesterImpl(...) : ConnectionTester {
    override suspend fun invoke(baseUrl: String, token: String): Result<Unit> { ... }
  }
  ```
  Note: Avoid shorthand `fun interface Foo : (String, String) -> Result` — it loses parameter names and hurts readability.
  This pattern is for single-purpose functional interfaces with DI. Do NOT use it for: multi-method interfaces, platform-specific interfaces with `expect/actual`, or repository interfaces.
- Avoid `!!`; keep composables small and focused.
- **No unnecessary default values.** Don't add default parameter values that nobody uses — required parameters catch missing arguments at compile time.
- **Use design system values.** Use `Indent` object values instead of hardcoding dp. If a value doesn't exist, add it to the design system.
- **Self-documenting code over comments.** Don't add KDoc/comments that restate function names or obvious logic.
- **Package placement matters for coverage:**
  - @Composable functions must be in `*.ui.*` or `*.view.*` packages
  - expect/actual declarations must be in `*.platform.*` packages
  - Pure business logic must not contain @Composable or expect/actual
  - When moving code between packages, verify coverage impact
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
- Coverage reports:
  - HTML: `./gradlew :shared:koverHtmlReport` (opens at `shared/build/reports/kover/html/index.html`)
  - XML (for CI): `./gradlew :shared:koverXmlReport` (outputs to `shared/build/reports/kover/report.xml`)

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

### Coverage Guidelines

We maintain **~95% test coverage** using Kover. Coverage is automatically measured and reported to Codecov on every PR.

**What is excluded from coverage:**
- **TEA architecture classes:** `*State`, `*Action`, `*Effect`, `*Features` (data classes)
- **DI modules:** `*.di.*` (dependency wiring)
- **Generated code:** `*.generated.*` (Compose resources, Metro factories)
- **Platform-specific code:** `*.platform.*` (expect/actual declarations)
- **Room database:** `*.room.*` (platform-specific persistence)
- **UI layer:** `*.ui.*`, `*.view.*` (Compose UI, @Composable functions)
- **DTOs:** `*Dto` (serialization data classes)

**What must be tested:**
- Domain logic (use cases, business rules)
- Repository implementations with business logic
- Pure functions and utilities
- Data transformations and mappers

**Important rules:**
- UI code (@Composable functions) belongs in `*.ui.*` or `*.view.*` packages
- expect/actual code belongs in `*.platform.*` packages
- Pure business logic must not contain @Composable or expect/actual
- When moving code, verify it doesn't affect coverage exclusions

Check coverage locally:
```bash
./gradlew :shared:koverHtmlReport
open shared/build/reports/kover/html/index.html
```

## Linting & Formatting

**Pre-commit hook handles all checks automatically.** Install it once with `./gradlew installGitHooks`.

- `./gradlew ktlintFormat` — auto-fix code style issues before committing.
- `./gradlew checkAll` — manually run all checks if needed.

Use `@Suppress` annotations only when the lint rule doesn't apply (e.g., `LongParameterList` for DI containers, `ktlint:standard:function-naming` for factory functions).

## Commit & Pull Request Guidelines

**All changes must go through pull requests** — never commit directly to `main`. This ensures CI checks pass before merging and keeps the main branch green.

- Create a feature branch, push, and open a PR.
- Use auto-merge with squash (`gh pr merge --auto --squash --delete-branch`).
- Commit messages: imperative, concise, single topic (e.g., "Simplify README").
- PR titles: use English only (no Cyrillic or other non-ASCII characters).
- **PR descriptions must be concise and focused:**
  - Brief summary explaining what changed and why
  - Changes section with bullet points of what was added/updated/deleted
  - For refactoring: short before/after comparison (code snippets if needed)
  - Test plan with checkboxes only if relevant
  - **NO unnecessary headers, footers, badges, or promotional content**
  - **NO "Generated with..." footers or similar fluff**
  - Keep it professional and to the point
- Pre-commit hook runs `checkAll` automatically — no manual checks needed.

## CI/CD

### CI Workflow

Runs automatically on push to `main` and on pull requests:
- Runs `./gradlew checkAll` (ktlint, detekt, compile, tests)
- Generates coverage report and uploads to Codecov
- Uploads test results as artifacts

### Release Workflow

Builds and publishes all platforms in parallel. Run manually:

```bash
gh workflow run Release
```

What it does:
1. Auto-increments patch version (v1.0.39 → v1.0.40)
2. Creates GitHub release with auto-generated notes
3. Builds in parallel:
   - **Android APK** → uploads to GitHub Release + Firebase App Distribution
   - **macOS DMG** → uploads to GitHub Release
   - **Windows MSI** → uploads to GitHub Release
   - **Web** → uploads tarball to GitHub Release + deploys to GitHub Pages

Monitor release progress:
```bash
gh run list --workflow=Release --limit=1
gh run watch <run-id>
```

## Security & Configuration Tips

- Do not commit tokens or secrets; use the app UI to store credentials locally.
- Logs must not include auth tokens or request bodies.
