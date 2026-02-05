# Repository Guidelines

Vibits is a habit tracker powered by Memos, built with Kotlin Multiplatform (KMP) and Compose Multiplatform.

## Project Structure & Module Organization

```
core/
  elm/           — TEA architecture foundation
  elm/test/      — Test utilities for reducers
  platform/      — Platform abstractions (expect/actual)
  strings/       — Localized string resources
  ui/            — Compose UI components and theme
  utils/         — Shared utilities (date, logging)
feature/
  auth/          — Authentication (domain, data)
  habits/        — Habits tracking (domain, presentation)
  homescreen/    — App graph, DI wiring, coordinator
  memos/         — Memos API (domain, data, presentation)
  mode/          — App mode selection (domain, data, presentation)
  onboarding/    — Onboarding flow (domain, data, presentation)
  settings/      — Settings (domain, data, presentation)
  sync/          — Sync engine (domain, data)
androidApp/      — Android entry point
desktopApp/      — Desktop entry point
iosApp/          — iOS wrapper
webApp/          — Web entry (WASM)
build-logic/     — Convention plugins (vibits.kmp.*)
```

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

- **`core.date.*`** — Date/time utilities (module: `core/utils`):
  - `DateConstants` — Calendar constants (`DAYS_IN_WEEK`, `MONTHS_IN_QUARTER`, etc.)
  - `DateUtils` — Pure functions: `startOfWeek(LocalDate)`, `quarterIndex(LocalDate|Month)`
  - Uses `kotlinx-datetime`; no platform-specific code

- **`core.logging.*`** — In-memory logging (module: `core/utils`):
  - `Log` — Thread-safe singleton log store (max 500 entries), delegates to `platformLog()`
  - `LogEntry` — Data class: timestamp, level, tag, message
  - `String.maskUrl()` — Truncates URLs for safe logging
  - Uses `kotlinx-atomicfu` for synchronization

- **`core.*` (other)** — Pure business logic:
  - Must not contain @Composable functions
  - Must not contain expect/actual declarations
  - Should be fully testable with unit tests

### Feature Data Layer

Within `feature/<name>/data/`:

- **`data/room/`** — Room database implementations (platform-specific):
  - DAO interfaces, entities, database classes
  - Excluded from coverage (platform-specific, tested via integration tests)

- **`data/platform/`** — **ONLY expect/actual declarations**:
  - expect interface in `commonMain`, actual implementations in platform source sets
  - Factory functions like `expect fun createMemoCache(): MemoCache`
  - **NEVER put non-expect/actual code here** — use `data/internal/` instead
  - Excluded from coverage

- **`data/internal/`** — Platform-specific implementation details (NOT expect/actual):
  - Singleton holders (e.g., `AndroidDatabaseHolder`, `DesktopDatabaseHolder`)
  - Platform-specific helpers used by actual implementations
  - Code that exists only in platform source sets but isn't an expect/actual declaration

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
    platform/         # ONLY expect/actual declarations
    internal/         # Platform-specific helpers (NOT expect/actual)
    room/             # Room database (DAO, entities, database)
  presentation/       # TEA components
    action/           # Action sealed interfaces with grouped subtypes
      <Name>Action.kt # Main action sealed interface
    state/            # State data classes and related types
      <Name>State.kt  # Main state data class
      *Extensions.kt  # State extension functions (if needed)
      *.kt            # State-related types (cache keys, editable models, etc.)
    effect/           # Effects and effect handlers
      <Name>Effect.kt # Effect sealed interface (Command/Notification)
      *EffectHandler.kt # Effect handler implementations
    reducer/          # Reducers (main + sub-reducers)
      <Name>Reducer.kt # Main reducer (router that delegates to sub-reducers)
      <Name>*Reducer.kt # Sub-reducers (focused, ~30-60 lines each)
    view/             # Compose UI (screens, components, dialogs)
    <Name>Feature.kt  # Feature interface and implementation
```

### Layer Responsibilities

- **domain/** — Pure business logic. No UI or framework dependencies.
- **data/** — Data layer implementations:
  - Repository implementations with business logic
  - DTOs and mappers
  - API clients
  - **data/platform/** — ONLY expect/actual declarations
  - **data/internal/** — Platform-specific helpers (NOT expect/actual)
  - **data/room/** — Room database implementations (DAO, entities)
- **presentation/** — The Elm Architecture (TEA) components:
  - **presentation/action/** — Action sealed interfaces:
    - `*Action.kt` — Main action sealed interface with grouped subtypes (e.g., `Editor`, `Config`, `Loading`)
    - Actions are split into sealed subtypes, one per sub-reducer
    - Each action belongs to exactly one group
  - **presentation/state/** — State data classes and related types:
    - `*State.kt` — Main state data class
    - State extension functions (e.g., `*StateExtensions.kt`)
    - State-related types (cache keys, editable models, etc.)
  - **presentation/effect/** — Effects and effect handlers:
    - `*Effect.kt` — Effect sealed interface (Command/Notification separation)
    - `*EffectHandler.kt` — Effect handler implementations
    - Split by effect category (e.g., `ApiEffectHandler`, `StorageEffectHandler`)
  - **presentation/reducer/** — Main reducer + sub-reducers:
    - `*Reducer.kt` — Main reducer that acts as a router, delegating to sub-reducers
    - `<Name><Group>Reducer.kt` — Sub-reducers (~30-60 lines each)
    - Each sub-reducer handles a specific action group (e.g., `HabitsEditorReducer`, `HabitsConfigReducer`)
    - Main reducer uses exhaustive `when` to route actions by type (no `else` branches)
    - Sub-reducers are `internal val` with group-specific action types (top-level values, not functions)
    - Example: `internal val editorReducer: Reducer<HabitsAction.Editor, HabitsState, HabitsEffect, Nothing>`
  - **presentation/view/** — Compose UI only. Screens, components, dialogs. Excluded from unit test coverage.
  - **presentation/*.kt** — Feature interface and implementation only:
    - `*Feature.kt` — Feature interface and implementation
    - No TEA contract files in presentation root
  - **`*State` placement rules:**
    - Pure domain state (only domain models + business logic, no UI concerns) → move to `domain/model/`
    - Mixed state (domain + UI concerns like dialog flags, loading states, credentials for dialogs) → keep in `presentation/state/`
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

**Action Splitting** — Actions are organized into sealed subtypes (groups) in `presentation/action/`:
```kotlin
// presentation/action/HabitsAction.kt
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

**Sub-Reducer Pattern** — Each sub-reducer is an `internal val` (top-level value, not a function):
```kotlin
// presentation/reducer/HabitsEditorReducer.kt
internal val editorReducer: Reducer<HabitsAction.Editor, HabitsState, HabitsEffect, Nothing> =
  reducer { action, state ->
    when (action) {
      is HabitsAction.Editor.OpenEditor -> { ... }
      is HabitsAction.Editor.CloseEditor -> { ... }
      is HabitsAction.Editor.ToggleHabit -> { ... }
      // All branches exhaustive, no else
    }
  }
```

**Main Reducer Router** — The main reducer delegates to sub-reducers using exhaustive `when`:
```kotlin
// presentation/reducer/HabitsReducer.kt
val habitsReducer: Reducer<HabitsAction, HabitsState, HabitsEffect, Nothing> =
  { action, state ->
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
- Sub-reducers are **top-level values** (`val`), not functions
- Sub-reducers use `Reducer<ActionGroup, State, Command, Notification>` type
- Main reducer and sub-reducers both live in `presentation/reducer/` directory
- Naming: `<Feature><Group>Reducer.kt` (e.g., `HabitsEditorReducer.kt`)
- State-related files (extensions, cache keys, editable models) live in `presentation/state/`
- Same decomposition pattern as effect handlers for consistency

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

- `./gradlew checkAll` — run all checks (ktlint, detekt, compile, tests). Requires macOS for iOS.
- `./gradlew checkJvm` — run JVM-only checks (Linux-safe).
- `./gradlew checkIos` — run iOS checks (requires macOS).
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

- Run all checks: `./gradlew checkAll` (includes ktlint, detekt, compile, all module tests)
- Run tests for specific module: `./gradlew :feature:habits:presentation:desktopTest`
- Coverage reports:
  - XML (aggregated): `./gradlew koverXmlReport` (outputs to `build/reports/kover/report.xml`)
  - HTML (per module): `./gradlew :feature:memos:presentation:koverHtmlReport`

### Test Organization

- Unit tests live in each module's `src/commonTest/kotlin/...` directory.
- Desktop-specific tests in `src/desktopTest/kotlin/...`.
- Test utilities in `core/elm/test` (package `*.test.*`, excluded from coverage).
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
./gradlew koverXmlReport
# Aggregated report at build/reports/kover/report.xml
```

### Coverage Configuration

**Codecov is the single source of truth** for coverage exclusions. Configure in `codecov.yml`:

```yaml
ignore:
  - "**/di/**"
  - "**/view/**"
  - "**/ui/**"
  - "**/platform/**"
  - "**/*State.kt"
  # etc.
```

Kover is applied to all modules via `vibits.kmp.library` convention plugin and generates full coverage reports without exclusions. Codecov applies ignore rules when calculating coverage percentage.

## Linting & Formatting

**Pre-commit hook handles all checks automatically.** Install it once with `./gradlew installGitHooks`.

- `./gradlew ktlintFormat` — auto-fix code style issues before committing.
- `./gradlew checkAll` — manually run all checks if needed.

**Never use `@Suppress` for new code** — always refactor to fix the underlying issue. When touching existing code that triggers lint warnings, refactor it immediately rather than suppressing. Only use `@Suppress` when the lint rule fundamentally doesn't apply (e.g., `LongParameterList` for DI containers, `ktlint:standard:function-naming` for factory functions).

## Commit & Pull Request Guidelines

**All changes must go through pull requests** — never commit directly to `main`. This ensures CI checks pass before merging and keeps the main branch green.

- Create a feature branch, push, and open a PR.
- Use auto-merge with squash (`gh pr merge --auto --squash --delete-branch`).
- **When user requests changes to existing PR:** amend commits and force-push to the same branch. Do NOT close and recreate PRs.
- Commit messages: imperative, concise, single topic (e.g., "Simplify README").
- **Never include session links** — do not add `claude.ai/code/session_*` or any other claude.ai links to commits, PRs, or code.
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
- `check-jvm` job (Linux): runs `./gradlew checkJvm` (detekt, compile, tests)
- `check-ios` job (macOS): runs `./gradlew checkIos` (iOS compile, ktlint)
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
