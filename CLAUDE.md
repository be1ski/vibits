# Vibits

Kotlin Multiplatform habit tracker powered by Memos. Compose Multiplatform UI, Metro DI, TEA (Elm Architecture).

## Commands

```
./gradlew checkAll                                  # All checks (ktlint, detekt, compile, tests)
./gradlew checkJvm                                  # JVM-only checks (Linux-safe)
./gradlew ktlintFormat                              # Auto-fix code style
./gradlew :app:desktop:run                          # Run desktop app
./gradlew :feature:<name>:presentation:desktopTest  # Module tests
./gradlew koverXmlReport                            # Coverage → build/reports/kover/report.xml
./gradlew screenshotTests                           # UI screenshots → build/ui-screenshots/
```

## Structure

```
core/{elm, platform, strings, ui, utils}       — shared foundations
feature/{auth, habits, homescreen, memos,      — each: domain/ → data/ → presentation/ → di/
         mode, onboarding, settings, sync}
app/{android, desktop, ios, web}               — entry points
build-logic/                                   — convention plugins (vibits.kmp.*, vibits.checks.*)
```

Each feature: `domain/model/`, `domain/usecase/`, `domain/repository/` → `data/` (repos, DTOs) → `presentation/{action,state,effect,reducer,view}/` → `di/`. Reference: `feature/habits/`.

## Module Dependencies

- `core/` → other core + external libs only
- `feature/domain` → core + other features' domain
- `feature/data` → core + own domain + other features' domain
- `feature/presentation` → core + own domain + other features' domain
- Cross-feature data/presentation deps **forbidden**. `homescreen` exempt (DI coordinator).

## Package Placement (enforced by `checkConventions`)

- `*.platform.*` — expect/actual only (excluded from coverage)
- `*.ui.*`, `*.view.*` — @Composable only (excluded from coverage)
- `*.room.*`, `*.di.*`, `*.test.*`, `*.testing.*` — excluded from coverage
- `data/platform/` — ONLY expect/actual. Non-expect/actual helpers → `data/internal/`
- Pure business logic must NOT contain @Composable or expect/actual

## Coroutine Cancellation (enforced by `checkConventions`)

- **Never** `runCatching` in suspend code → use `runSuspendCatching` from `core/utils`
- **Never** `catch (e: Exception)` without preceding `catch (ce: CancellationException) { throw ce }`
- `TimeoutCancellationException` always rethrown

## TEA Pattern

Actions split into sealed subtypes (one per sub-reducer). Sub-reducers are `internal val` (~30-60 lines), exhaustive `when`, no `else`. Main reducer routes by action type. Reference: `feature/habits/presentation/reducer/`.

`*Dependencies` in `di/` are transport containers — consumers inject individual deps, NOT the container. Reference: `feature/settings/di/`.

## DI (Metro)

- `@Inject` for auto-created classes, `@Provides` only for platform-specific, `@Binds` for interface bindings
- `@SingleIn(AppScope::class)` for singletons
- Use cases: stateless → `object` + `operator fun invoke`; with deps → `@Inject class` + `operator fun invoke`
- `fun interface` for single-method DI interfaces (NOT for repos, multi-method, or expect/actual)

## Coding Style

- 2-space indent, official Kotlin style, no `!!`, no `@Suppress` for new code
- Naming: `Foo` interface / `FooImpl` implementation; variable names match types
- Avoid meaningless suffixes (`Info`, `Data`, `Model`). Rename classes when scope changes.
- Use `Indent` design system values, not hardcoded dp. Keep `libs.versions.toml` sorted alphabetically.
- Self-documenting code over comments. No unnecessary default parameter values.

## Localization

- **Never** hardcode user-facing strings → `composeResources/values/strings.xml`
- Add translations for ALL locales immediately (native translations, not English fallbacks)
- Russian: "Memo" = «Воспоминание» (not «Заметка»)
- String names: `snake_case` with prefixes: `action_`, `label_`, `msg_`, `title_`, `hint_`, `format_`

## Testing

- TDD, ~95% coverage, manual fakes only (no MockK/Mockito — KMP)
- Test names: backticks `` `when X then Y` ``. Hardcode strings (no production constants).
- Fakes: `Fake*` prefix. Cross-module → `<module>/testing/` submodule. Same-module → `commonTest`.
- Fake classes in production `commonMain` **fail** `checkConventions`
- Test State computed properties with business logic even though `*State` is excluded from coverage
- When touching UI: run `checkJvm`, verify screenshots in `build/ui-screenshots/`
- Screenshots: always both themes via `captureInBothThemes`. Reference: `feature/homescreen/.../AppScreenshotTest.kt`

## Git & PRs

- All changes via PRs, never commit to main. **ALWAYS** `git fetch origin main && git rebase origin/main` before first
  commit on any branch. This is mandatory — never skip it, even if you think the branch is up to date.
- Squash merge: `gh pr merge --auto --squash --delete-branch`
- PR updates: push new commits to the same branch (don't recreate PRs). Amend + force-push only when explicitly asked.
- Commit messages: imperative, concise, English. No claude.ai session links.
- PR titles: English only. Descriptions: brief summary + changes bullets, no bloat.
- Release: `gh workflow run Release` (auto-increments version, builds all platforms)
- Do not commit tokens or secrets. Logs must not include auth tokens.
