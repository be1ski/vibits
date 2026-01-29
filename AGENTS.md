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
  presentation/       # TEA components
    reducer/          # Sub-reducers (focused, ~30-60 lines each)
    handler/          # Effect handlers (focused, handle specific effects)
    view/             # Compose UI (screens, components, dialogs)
    <Name>Action.kt   # Action sealed interface with grouped subtypes
    <Name>State.kt    # State data class
    <Name>Effect.kt   # Effect sealed interface
    <Name>Reducer.kt  # Main reducer (router that delegates to sub-reducers)
    <Name>Feature.kt  # Feature interface and implementation
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
  - **Root files** (TEA contracts):
    - `*Action.kt` — Action sealed interface with grouped subtypes (e.g., `Editor`, `Config`, `Loading`)
    - `*State.kt` — State data class
    - `*Effect.kt` — Effect sealed interface
    - `*Reducer.kt` — Main reducer that acts as a router, delegating to sub-reducers
    - `*Feature.kt` — Feature interface and implementation
  - **presentation/reducer/** — Sub-reducers (~30-60 lines each):
    - Each sub-reducer handles a specific action group (e.g., `EditorReducer`, `ConfigReducer`)
    - Actions are split into sealed subtypes per reducer group (same pattern as effects)
    - Main reducer uses exhaustive `when` to route actions by type (no `else` branches)
    - Sub-reducers are `internal` functions with group-specific action types
    - Example: `editorReducer(action: HabitsAction.Editor, state: HabitsState): ReducerResult`
  - **presentation/handler/** — Effect handlers (focused, handle specific effects):
    - Split by effect category (e.g., `ApiEffectHandler`, `StorageEffectHandler`)
    - Same decomposition pattern as reducers
  - **presentation/view/** — Compose UI only. Screens, components, dialogs. Excluded from unit test coverage.
  - **`*State` placement rules:**
    - Pure domain state (only domain models + business logic, no UI concerns) → move to `domain/model/`
    - Mixed state (domain + UI concerns like dialog flags, loading states, credentials for dialogs) → keep in `presentation/`
    - When State has computed properties with business logic → add tests regardless of location
    - Example: `AppState` moved to domain (pure domain), `MemosState` stays in presentation (contains UI concerns)
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

### Sub-Reducer Pattern

Large reducers are decomposed into focused sub-reducers (~30-60 lines each) following the same pattern as effect handlers. Each sub-reducer handles a specific aspect of the feature.

**Action Splitting** — Actions are organized into sealed subtypes (groups):
```kotlin
sealed interface HabitsAction : Action {
  sealed interface Editor : HabitsAction {
    data class OpenEditor(...) : Editor
    data object CloseEditor : Editor
    // ...
  }

  sealed interface Config : HabitsAction {
    data class OpenConfigDialog(...) : Config
    data object SaveConfigDialog : Config
    // ...
  }

  // More groups...
}
```

**Sub-Reducer Signature** — Each sub-reducer is an `internal` function that takes a specific action group:
```kotlin
internal fun editorReducer(
  action: HabitsAction.Editor,
  state: HabitsState,
): ReducerResult<HabitsState, HabitsEffect, Nothing> =
  reducer<HabitsAction.Editor, HabitsState, HabitsEffect, Nothing> { a, s ->
    when (a) {
      is HabitsAction.Editor.OpenEditor -> { ... }
      is HabitsAction.Editor.CloseEditor -> { ... }
      // All branches exhaustive, no else
    }
  }(action, state)
```

**Main Reducer Router** — The main reducer delegates to sub-reducers using exhaustive `when`:
```kotlin
val habitsReducer: Reducer<HabitsAction, HabitsState, HabitsEffect, Nothing> =
  reducer { action, state ->
    when (action) {
      is HabitsAction.Editor -> editorReducer(action, state)
      is HabitsAction.Config -> configReducer(action, state)
      is HabitsAction.Cache -> cacheReducer(action, state)
      // Exhaustive, type-based routing
    }
  }
```

**Key Principles:**
- A single action type belongs to exactly one group (sealed subtype)
- No catch-all reducers or default branches
- Sub-reducers live in `presentation/reducer/` directory
- Naming: `<Feature><Group>Reducer.kt` (e.g., `HabitsEditorReducer.kt`)
- Same pattern as effect handlers for consistency

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
- **Always add translations for ALL supported locales immediately.** When adding a new string resource, add proper native translations to all locale files in `composeResources/values-*/strings.xml`. Use professional native translations, not English fallbacks.
- **Terminology:** In this app, “Memo” must be translated as **«Воспоминание»** in Russian. Do not use «Заметка» for memos.
- String resource names use `snake_case` with semantic prefixes: `action_`, `label_`, `msg_`, `title_`, `hint_`, `format_`, etc.

## Testing Guidelines

We follow TDD for business logic and aim for high coverage.

**CRITICAL: Always add tests for new code.** When implementing new features, actions, commands, reducers, or any business logic:
1. Write tests immediately, not as an afterthought
2. Ensure all new code paths are covered
3. Verify tests pass before committing
4. Never skip tests "to do later" — do them now

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
- **Hardcode strings in tests.** Use literal string values instead of constants like `PostTags.HABITS_CONFIG`. Tests should verify real behavior with real data, not automatically adjust when constants change.

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
- **State classes with computed properties** — even though `*State` is excluded from coverage, test computed properties containing business logic (e.g., `MemosState.hasCredentials`, `AppState.isDemoMode`)

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

### Coverage Configuration

**Kover is the single source of truth** for coverage exclusions. Configure exclusions in `shared/build.gradle.kts`:

```kotlin
kover {
  reports {
    filters {
      excludes {
        classes(
          "*.view.*",      // View layer
          "*.ui.*",        // UI components
          "*State",        // TEA State classes
          // etc.
        )
      }
    }
  }
}
```

**Codecov** (`codecov.yml`) only checks overall project coverage (90% target), not per-patch coverage. This prevents false failures when adding UI code or other excluded files. Codecov receives coverage data from Kover XML.

## Linting & Formatting

**Pre-commit hook handles all checks automatically.** Install it once with `./gradlew installGitHooks`.

- `./gradlew ktlintFormat` — auto-fix code style issues before committing.
- `./gradlew checkAll` — manually run all checks if needed.

Use `@Suppress` annotations only when the lint rule doesn't apply (e.g., `LongParameterList` for DI containers, `ktlint:standard:function-naming` for factory functions).

## Commit & Pull Request Guidelines

**All changes must go through pull requests** — never commit directly to `main`. This ensures CI checks pass before merging and keeps the main branch green.

- Create a feature branch, push, and open a PR.
- Use auto-merge with squash (`gh pr merge --auto --squash --delete-branch`).
- **When user requests changes to existing PR:** amend commits and force-push to the same branch. Do NOT close and recreate PRs.
- Commit messages: imperative, concise, single topic (e.g., "Simplify README").
- PR titles: use English only (no Cyrillic or other non-ASCII characters).
- **PR descriptions must be concise and focused:**
  - Brief summary explaining what changed and why
  - Changes section with bullet points of what was added/updated/deleted
  - For refactoring: short before/after comparison (code snippets if needed)
  - Test plan with checkboxes only if relevant
  - **NO unnecessary headers, footers, badges, or promotional content**
  - Keep it clean and to the point
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
