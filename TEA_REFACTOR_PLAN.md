# TEA EffectHandler Refactor Plan (Polished)

## Goals
- Keep each EffectHandler class ~30 lines by splitting into small, focused handlers.
- Replace all `flow {}` / `emptyFlow()` boilerplate in handlers with a consistent helper API.
- Eliminate `emptyFlow().also { ... }` everywhere (replace with explicit, intentional helpers).
- Move heavy Habits calculations out of presentation into domain use cases.
- Preserve existing TEA contracts and behavior (no feature behavior changes).
- Keep DI rules: only inject individual dependencies, never containers in handlers.

## Current Findings
- Large handlers: Habits (247), ModeSelection (92), Memos (87), Settings (86), Onboarding (67).
- `emptyFlow().also { ... }` appears twice in `SettingsEffectHandler`.
- `emptyFlow()` appears in `AppEffectHandler`.
- Memos/Habits handlers still use the “single flow { when (...) }” pattern.

## Decisions (No Open Questions)

### 1) Standard Flow helpers (core/elm)
Create `FlowHelpers.kt` in `shared/src/commonMain/kotlin/space/be1ski/vibits/shared/core/elm`:

```kotlin
fun <A> actions(block: suspend FlowCollector<A>.() -> Unit): Flow<A> = flow(block)

fun <A> action(value: A): Flow<A> = flow { emit(value) }

fun <A> sideEffect(block: suspend () -> Unit): Flow<A> = flow { block() }

fun <A> noActions(): Flow<A> = emptyFlow()
```

Usage policy:
- `action(...)` for a single emission.
- `actions { ... }` for multiple emissions or branching logic.
- `sideEffect { ... }` when **no actions are emitted** (replaces `emptyFlow().also { ... }`).
- `noActions()` only for true no-op commands (rare).

### 2) Split handlers into small classes (router + sub-handlers)
Each feature gets:
- A **router** `*EffectHandler` (~30 lines) with a single `when` and delegation.
- Several **sub-handlers** (also ~20–30 lines each) that focus on one responsibility.

Sub-handlers are real “EffectHandlers” (not just private functions), created in the FeatureFactory.
This directly addresses “small effect handlers” and keeps files short.

### 3) Command grouping for type-safe sub-handlers
To avoid `else -> noActions()` boilerplate, add nested command groups inside each feature’s Effect:

Example (Settings):
```kotlin
sealed interface SettingsEffect {
  sealed interface Command : SettingsEffect {
    sealed interface Credentials : Command
    sealed interface Mode : Command
    sealed interface Preferences : Command

    data class ValidateCredentials(...) : Credentials
    data class SaveCredentials(...) : Credentials
    data class SwitchMode(...) : Mode
    data object ResetApp : Mode
    data object ResetAppWithMemos : Mode
    data class SaveLanguage(...) : Preferences
    data class SaveTheme(...) : Preferences
  }
}
```

Then sub-handlers can be **typed**:
```kotlin
class SettingsCredentialsEffectHandler(...) : EffectHandler<SettingsEffect.Command.Credentials, SettingsAction>
```

This keeps routing exhaustive and avoids dead `else` branches.

### 4) Habits: move logic into domain use cases
HabitsEffectHandler must become orchestration-only. All calculation and range building moves to domain use cases.

New domain models (no `*Data` suffix):
- `ActivitySnapshot`
- `PrewarmedActivitySnapshot`
- `ActivityRangeSet`

New use cases:
- `GenerateActivityRangesUseCase` (pure object, uses `startOfWeek` / `quarterIndex`)
- `CalculateActivitySnapshotUseCase` (`@Inject`, wraps existing calculation)
- `PrewarmActivitySnapshotsUseCase` (`@Inject`, handles parallel computation)

Platform values (`currentLocalDate`, `TimeZone.currentSystemDefault`) stay in EffectHandler and are passed into use cases.

---

## Target Handler Layout (per feature)

### Settings
- Router: `SettingsEffectHandler`
- Sub-handlers:
  - `SettingsCredentialsEffectHandler` (ValidateCredentials, SaveCredentials)
  - `SettingsModeEffectHandler` (SwitchMode, ResetApp, ResetAppWithMemos)
  - `SettingsPreferencesEffectHandler` (SaveLanguage, SaveTheme)

`SettingsEffect.Command` gains `Credentials`, `Mode`, `Preferences` groups.

### ModeSelection
- Router: `ModeSelectionEffectHandler`
- Sub-handlers:
  - `ModeSelectionCredentialsEffectHandler` (InitializeFromLocalConfig, CheckStoredCredentials, UseStoredCredentialsWithValidation, ValidateCredentials, SaveCredentials)
  - `ModeSelectionModeEffectHandler` (SaveMode)

`ModeSelectionEffect.Command` gains `Credentials`, `Mode` groups.

### Memos
- Router: `MemosEffectHandler`
- Sub-handlers:
  - `MemosCredentialsEffectHandler` (LoadCredentials, SaveCredentials)
  - `MemosLoadEffectHandler` (LoadCachedMemos, LoadRemoteMemos)
  - `MemosWriteEffectHandler` (CreateMemo, UpdateMemo, DeleteMemo)

`MemosEffect` gains nested groups: `Credentials`, `Load`, `Write`.

### Onboarding
- Router: `OnboardingEffectHandler`
- Sub-handlers:
  - `OnboardingPresetsEffectHandler` (LoadPresets)
  - `OnboardingSetupEffectHandler` (CreateFirstHabit, MarkFirstCheckIn)
  - `OnboardingCompletionEffectHandler` (MarkOnboardingCompleted)

`OnboardingEffect.Command` gains `Presets`, `Setup`, `Completion` groups.

### Habits
- Router: `HabitsEffectHandler`
- Sub-handlers:
  - `HabitsMemoEffectHandler` (CreateMemo, UpdateMemo, DeleteMemo)
  - `HabitsRefreshEffectHandler` (RefreshMemos)
  - `HabitsActivityEffectHandler` (RunPrewarmAllRanges, RecalculateActivityData)

`HabitsEffect` gains nested groups: `Memo`, `Refresh`, `Activity`.

### App
- Router: `AppEffectHandler` (small already)
- Update to use `sideEffect` (not `emptyFlow`).

---

## Implementation Plan

### Phase 1 — Flow helpers
1. Add `FlowHelpers.kt` in `core/elm` (helpers above).
2. Add `FlowHelpersTest.kt` in `shared/src/commonTest/.../core/elm`.
3. Replace `emptyFlow().also { ... }` in Settings with `sideEffect { ... }`.
4. Replace `emptyFlow()` in `AppEffectHandler` with `noActions()` or `sideEffect { ... }` (prefer `sideEffect` since work is done).

### Phase 2 — Command grouping + handler splits (Settings, Onboarding, ModeSelection, Memos)
For each feature:
1. Add nested command group interfaces in `*Effect.kt` (as described above).
2. Introduce sub-handler classes in the same `presentation` package.
3. Update router `*EffectHandler` to delegate to sub-handlers.
4. Update FeatureFactory to instantiate sub-handlers with **individual dependencies**.
5. Replace `flow {}` with `action`/`actions`/`sideEffect` helpers.

### Phase 3 — Habits domain extraction + handler split
1. Add new domain models:
   - `ActivitySnapshot`
   - `PrewarmedActivitySnapshot`
   - `ActivityRangeSet`
2. Add new use cases:
   - `GenerateActivityRangesUseCase`
   - `CalculateActivitySnapshotUseCase`
   - `PrewarmActivitySnapshotsUseCase`
3. Update HabitsEffectHandler to use new use cases and split into sub-handlers.
4. Update HabitsFeatureFactory to construct the new use cases and handlers.

### Phase 4 — Cleanup and verification
1. Remove unused imports (`flow`, `emptyFlow`) from handlers.
2. Ensure **no** `emptyFlow().also { ... }` remains.
3. Run `./gradlew :shared:desktopTest`.
4. Run `./gradlew :shared:koverHtmlReport` and check coverage for new use cases/helpers.

---

## Test Plan

### New tests
- `FlowHelpersTest` for all helpers.
- `GenerateActivityRangesUseCaseTest` (edge cases: single day, year boundary, quarter boundary).
- `CalculateActivitySnapshotUseCaseTest` (habits vs posts, config present/absent).
- `PrewarmActivitySnapshotsUseCaseTest` (empty memos, ranges x modes count).

### Existing tests (update factories only)
- `SettingsEffectHandlerTest`
- `ModeSelectionEffectHandlerTest`
- `MemosEffectHandlerTest`
- `OnboardingEffectHandlerTest`
- `HabitsEffectHandlerTest`

Behavioral assertions should remain unchanged.

---

## Success Criteria
- All EffectHandler classes (router + sub-handlers) are ~20–30 lines.
- No `emptyFlow().also {}` anywhere in `shared/src/commonMain`.
- All handlers use `action`/`actions`/`sideEffect`/`noActions` helpers.
- HabitsEffectHandler contains **no** range generation or calculation logic.
- All tests pass and coverage remains ≥95%.

---

## File List Summary
- New: `shared/src/commonMain/kotlin/space/be1ski/vibits/shared/core/elm/FlowHelpers.kt`
- New: `shared/src/commonTest/kotlin/space/be1ski/vibits/shared/core/elm/FlowHelpersTest.kt`
- Updated: all `*Effect.kt` (command grouping)
- New: sub-handler classes in each `feature/*/presentation` package
- Updated: all `*EffectHandler.kt` routers + FeatureFactories
- New: Habits domain models + use cases

---

**Status**: Ready to implement
**Last updated**: 2026-01-29
