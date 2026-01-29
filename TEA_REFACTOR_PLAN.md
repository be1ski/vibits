# TEA Refactor Plan (Commands + Notifications)

## Executive Summary

**Goal:** Enforce Command/Notification separation in TEA core to eliminate `emptyFlow()` anti-pattern and empty coordinator branches.

**Current State:**
- Onboarding & Settings already have Command/Notification structure, but TEA core doesn't enforce it
- All 6 features need migration: 3 with notifications (Onboarding, Settings, ModeSelection), 3 commands-only (App, Memos, Habits)

**Effort:**
- Phase 1 (Core TEA): ~4 files, blocking all features
- Phase 2 (Features): ~18 files, parallelizable after Phase 1
- Phase 3-5 (Coordinators, Tests, Cleanup): ~8 files

**Status:** ✅ Planning complete, ready for implementation

---

## Context

### Current State
Some features **already have** Command/Notification separation in their effect types:
- `OnboardingEffect` has `Command` and `Notification` sealed interfaces
- `SettingsEffect` has `Command` and `Notification` sealed interfaces
- `ModeSelectionEffect` is flat but has `NotifyModeSelected` as a notification

However, the TEA core doesn't enforce this separation:
- EffectHandlers must manually return `emptyFlow()` for notifications (see `OnboardingEffectHandler:24`, `SettingsEffectHandler:29`, `ModeSelectionEffectHandler:35`)
- Coordinators must explicitly ignore commands with empty branches (see `AppRoot:216`, `FeatureCoordinator:38-41`)
- All effects are exposed to external observers, even commands that should stay internal

There are also "silent" actions that do nothing in reducers:
- `OnboardingAction.OnboardingCompleted` (OnboardingReducer:124-126) - empty block with comment "Handled by coordinator"

### Goal
Make the Command/Notification separation explicit in the TEA core:
- **Command**: internal side-effects handled by `EffectHandler` only
- **Notification**: external signals for coordinators/UI, never reach `EffectHandler`
- Remove all `emptyFlow()` for notifications in effect handlers
- Remove all empty branches for commands in coordinators

## Target Architecture (Two Handles)

- Reducer emits **commands** and **notifications** separately.
- `EffectHandler` processes **only commands**.
- `Feature` exposes **notifications only** to outside observers.
- Commands never appear in UI coordinators; notifications never reach EffectHandlers.

## Technical Details

### Type Parameter Strategy

**Features with both commands and notifications:**
```kotlin
Feature<OnboardingAction, OnboardingState, OnboardingEffect.Command, OnboardingEffect.Notification>
```

**Features with commands only:**
```kotlin
Feature<AppAction, AppState, AppEffect, Nothing>
```

### `Nothing` Type Behavior

`Nothing` is Kotlin's bottom type that has no instances. Using it as Notification type:
- ✅ **Compile-time safety**: Calling `notify(...)` in reducer results in compile error
- ✅ **Type inference**: Makes it impossible to emit notifications
- ✅ **Flow handling**: `notifications: Flow<Nothing>` never emits (but is a valid, never-emitting flow)
- ✅ **Coordinator code**: No need to collect from `notifications` flow (it will never emit)

Example reducer with `Nothing`:
```kotlin
val appReducer: Reducer<AppAction, AppState, AppEffect, Nothing> = reducer { action, state ->
  when (action) {
    is AppAction.SaveTab -> {
      command(AppEffect.SaveHabitsTimeRangeTab(action.tab))  // ✅ OK
      // notify(...)  // ❌ Compile error: Nothing type
    }
  }
}
```

### ReducerResult Structure

Before (single Effect type):
```kotlin
ReducerResult<State, Effect>(
  state = newState,
  effects = setOf(effect1, effect2)
)
```

After (separate Command and Notification):
```kotlin
ReducerResult<State, Command, Notification>(
  state = newState,
  commands = setOf(command1, command2),
  notifications = setOf(notification1)
)
```

### Test Migration Strategy

**EffectHandler tests:**
- ✅ No changes needed - they already test command handling only
- Signature change: `EffectHandler<Effect, Action>` → `EffectHandler<Command, Action>`
- Test logic remains the same (mock commands, verify actions)

**Reducer tests:**
- ❌ **Require updates** - need new assertion helpers
- Replace `assertEffects(effect1, effect2)` with:
  - `assertCommands(command1)` - verify commands emitted
  - `assertNotifications(notification1)` - verify notifications emitted
- For commands-only features: use only `assertCommands(...)`
- Example:
  ```kotlin
  @Test
  fun `when action then emit command and notification`() = runReducerTest {
    given { initialState }
    whenever { someAction }
    then {
      assertState { expectedState }
      assertCommands(SomeEffect.Command.DoSomething)
      assertNotifications(SomeEffect.Notification.SomethingHappened)
    }
  }
  ```

## Plan

### 1) Inventory & Classification

**Status: ✅ Complete**

All effects classified. Silent actions identified.

**Onboarding** (already has Command/Notification structure)
- Commands: `LoadPresets`, `CreateFirstHabit`, `MarkOnboardingCompleted`, `MarkFirstCheckIn`
- Notifications: `Completed`, `Skipped`, `FirstCheckInCreated`
- Status: Structure exists, needs core TEA migration

**Settings** (already has Command/Notification structure)
- Commands: `ValidateCredentials`, `SwitchMode`, `SaveCredentials`, `ResetApp`, `SaveLanguage`, `SaveTheme`
- Notifications: `ModeChanged`, `ResetCompleted`, `CredentialsSaved`, `LanguageChanged`, `ThemeChanged`, `DialogClosed`
- Status: Structure exists, needs core TEA migration

**ModeSelection** (flat structure)
- Commands: `InitializeFromLocalConfig`, `CheckStoredCredentials`, `UseStoredCredentialsWithValidation`, `ValidateCredentials`, `SaveCredentials`, `SaveMode`
- Notifications: `NotifyModeSelected`
- Status: Needs restructuring into Command/Notification

**App** (commands only)
- Commands: `SaveHabitsTimeRangeTab`, `SavePostsTimeRangeTab`
- Notifications: None
- Status: Use `Nothing` as Notification type

**Memos** (commands only)
- Commands: `LoadCachedMemos`, `LoadRemoteMemos`, `SaveCredentials`, `LoadCredentials`, `CreateMemo`, `UpdateMemo`, `DeleteMemo`
- Notifications: None
- Status: Use `Nothing` as Notification type

**Habits** (commands only)
- Commands: `CreateMemo`, `UpdateMemo`, `DeleteMemo`, `RefreshMemos`, `RunPrewarmAllRanges`, `RecalculateActivityData`
- Notifications: None
- Status: Use `Nothing` as Notification type

**Silent Actions** (to be removed or converted to notifications):
- `OnboardingAction.OnboardingCompleted` (OnboardingReducer:124-126) - empty block, comment says "Handled by coordinator"
  - Decision: Remove this action. The notification `OnboardingEffect.Notification.Completed` already serves this purpose.

### 2) Core TEA Contract Changes

**Status: 🔲 Pending**

Update the TEA core to enforce Command/Notification separation.

**Elm.kt changes:**
1. Introduce `ReducerResult<State, Command, Notification>` to replace current `ReducerResult<State, Effect>`
2. Update `Reducer` type alias:
   ```kotlin
   typealias Reducer<Action, State, Command, Notification> =
     (Action, State) -> ReducerResult<State, Command, Notification>
   ```
3. Update `EffectHandler` to accept only commands:
   ```kotlin
   typealias EffectHandler<Command, Action> = (Command) -> Flow<Action>
   ```
4. Update `Feature` interface to expose notifications separately:
   ```kotlin
   interface Feature<Action, State, Command, Notification> {
     val state: StateFlow<State>
     val notifications: Flow<Notification>  // New: only notifications exposed
     fun send(action: Action)
     fun launchIn(scope: CoroutineScope)
   }
   ```

**ReducerDsl.kt changes:**
- Keep existing `state { ... }` DSL function unchanged
- Remove `effect(...)` DSL function entirely - no compatibility layer
- Add new DSL functions (canonical API):
  - `command(...)`, `commands(...)` - emit commands for EffectHandler
  - `notify(...)`, `notifications(...)` - emit notifications for coordinators
- Migration path: Find/replace all `effect(...)` calls with `command(...)` or `notify(...)` during feature migrations
- Note: `notify(...)` cannot be called if Notification type is `Nothing` (compile error) - this is desired behavior

**FeatureImpl.kt changes:**
1. Remove old `effects: Flow<Effect>` property - no longer exposed
2. Add `commandQueue: Channel<Command>` - internal, processed by EffectHandler
3. Add `notificationBroadcast: MutableSharedFlow<Notification>` - exposed as `notifications: Flow<Notification>`
4. Split effect processing in reducer loop: commands → commandQueue, notifications → notificationBroadcast
5. Update constructor to accept `EffectHandler<Command, Action>` instead of `EffectHandler<Effect, Action>`
6. Commands from commandQueue are processed by EffectHandler, results dispatched as actions
7. Notifications from notificationBroadcast are exposed to external observers only

### 3) Feature Migrations

**Status: 🔲 Pending (blocked by step 2)**

Migrate each feature to the new contract, removing empty branches.

**Onboarding** (minimal changes needed)
- ✅ Effect structure already correct (Command/Notification)
- Update: Reducer to use `command(...)` and `notify(...)` DSL instead of `effect(...)`
- Update: EffectHandler signature from `EffectHandler<OnboardingEffect, ...>` to `EffectHandler<OnboardingEffect.Command, ...>`
- Update: Factory function signature from `Feature<..., OnboardingEffect>` to `Feature<..., OnboardingEffect.Command, OnboardingEffect.Notification>`
- Remove: `emptyFlow()` branch for Notification (OnboardingEffectHandler:24)
- Remove: `OnboardingAction.OnboardingCompleted` action and its usage (OnboardingAction.kt, OnboardingReducer.kt:124-126, OnboardingEffectHandler.kt:59)
- Files: OnboardingReducer.kt, OnboardingEffectHandler.kt, OnboardingAction.kt, OnboardingFeatureFactory.kt

**Settings** (minimal changes needed)
- ✅ Effect structure already correct (Command/Notification)
- Update: Reducer to use `command(...)` and `notify(...)` DSL instead of `effect(...)`
- Update: EffectHandler signature from `EffectHandler<SettingsEffect, ...>` to `EffectHandler<SettingsEffect.Command, ...>`
- Update: Factory function signature from `Feature<..., SettingsEffect>` to `Feature<..., SettingsEffect.Command, SettingsEffect.Notification>`
- Remove: `emptyFlow()` branch for Notification (SettingsEffectHandler:29)
- Files: SettingsReducer.kt, SettingsEffectHandler.kt, SettingsFeatureFactory.kt

**ModeSelection** (requires restructuring)
- Restructure: `ModeSelectionEffect` into Command/Notification sealed interfaces (similar to OnboardingEffect)
- Update: Reducer to use `command(...)` and `notify(...)` DSL instead of `effect(...)`
- Move: `NotifyModeSelected` from flat effect to `ModeSelectionEffect.Notification.ModeSelected`
- Update: EffectHandler signature to `EffectHandler<ModeSelectionEffect.Command, ...>`
- Update: EffectHandler to handle only Command branch, remove `emptyFlow()` for NotifyModeSelected (line 35)
- Update: Factory function signature from `Feature<..., ModeSelectionEffect>` to `Feature<..., ModeSelectionEffect.Command, ModeSelectionEffect.Notification>`
- Update: AppRoot.kt coordinator to collect from `notifications` instead of `effects`
- Files: ModeSelectionEffect.kt, ModeSelectionReducer.kt, ModeSelectionEffectHandler.kt, ModeSelectionFeatureFactory.kt, AppRoot.kt

**App** (commands only, minimal changes)
- Keep: Flat `AppEffect` structure (no notifications needed)
- Update: Use `Nothing` as Notification type parameter in Feature signature
- Update: Reducer to use `command(...)` DSL instead of `effect(...)` (never call `notify(...)`)
- Update: Factory function signature from `Feature<..., AppEffect>` to `Feature<..., AppEffect, Nothing>`
- Update: AppFeatures.kt container to use new signature
- Files: AppReducer.kt, AppEffectHandler.kt, AppFeatureFactory.kt, AppFeatures.kt

**Memos** (commands only, minimal changes)
- Keep: Flat `MemosEffect` structure (no notifications needed)
- Update: Use `Nothing` as Notification type parameter in Feature signature
- Update: Reducer to use `command(...)` DSL instead of `effect(...)` (never call `notify(...)`)
- Update: Factory function signature from `Feature<..., MemosEffect>` to `Feature<..., MemosEffect, Nothing>`
- Update: AppFeatures.kt container to use new signature
- Files: MemosReducer.kt, MemosEffectHandler.kt, MemosFeatureFactory.kt, AppFeatures.kt

**Habits** (commands only, minimal changes)
- Keep: Flat `HabitsEffect` structure (no notifications needed)
- Update: Use `Nothing` as Notification type parameter in Feature signature
- Update: Reducer to use `command(...)` DSL instead of `effect(...)` (never call `notify(...)`)
- Update: Factory function signature from `Feature<..., HabitsEffect>` to `Feature<..., HabitsEffect, Nothing>`
- Update: AppFeatures.kt container to use new signature
- Files: HabitsReducer.kt, HabitsEffectHandler.kt, HabitsFeatureFactory.kt, AppFeatures.kt

### 4) Coordinator Updates

**Status: 🔲 Pending (blocked by steps 2 and 3)**

**AppRoot** (AppRoot.kt:187-221)
- Update: Change from `feature.effects.collect` to `feature.notifications.collect`
- Remove: Empty branch for `OnboardingEffect.Command` (line 216)
- Before:
  ```kotlin
  feature.effects.collect { effect ->
    when (effect) {
      is OnboardingEffect.Notification.Completed,
      is OnboardingEffect.Notification.Skipped -> onOnboardingCompleted()
      is OnboardingEffect.Notification.FirstCheckInCreated -> { ... }
      is OnboardingEffect.Command -> {} // Commands handled by EffectHandler
    }
  }
  ```
- After:
  ```kotlin
  feature.notifications.collect { notification ->
    when (notification) {
      is OnboardingEffect.Notification.Completed,
      is OnboardingEffect.Notification.Skipped -> onOnboardingCompleted()
      is OnboardingEffect.Notification.FirstCheckInCreated -> { ... }
    }
  }
  ```

**FeatureCoordinator** (FeatureCoordinator.kt:35-43)
- Update: Change from `feature.effects.collect` to `feature.notifications.collect`
- Remove: Empty branch for `SettingsEffect.Command` (line 40)
- Before:
  ```kotlin
  features.settings.effects.collect { effect ->
    when (effect) {
      is SettingsEffect.Notification -> handleNotification(...)
      is SettingsEffect.Command -> Unit
    }
  }
  ```
- After:
  ```kotlin
  features.settings.notifications.collect { notification ->
    handleNotification(notification, ...)
  }
  ```

### 5) Testing Updates

**Status: 🔲 Pending (blocked by steps 2 and 3)**

**ReducerTestDsl** (update test helpers)
- Add `assertCommands(...)` to check emitted commands
- Add `assertNotifications(...)` to check emitted notifications
- Deprecate or adapt `assertEffects(...)` for backwards compatibility
- Example:
  ```kotlin
  @Test
  fun `when Skip then emit Skipped notification`() = runReducerTest(onboardingReducer) {
    given { OnboardingState(...) }
    whenever { OnboardingAction.Skip }
    then {
      assertNotifications(OnboardingEffect.Notification.Skipped)
      assertCommands() // empty
    }
  }
  ```

**Feature-specific test updates:**
- **OnboardingReducerTest**: Replace `assertEffects(...)` with `assertCommands(...)` or `assertNotifications(...)`
- **SettingsReducerTest**: Same as above
- **ModeSelectionReducerTest**: Same as above, update effect names after restructuring
- **AppReducerTest, MemosReducerTest, HabitsReducerTest**: Replace with `assertCommands(...)`

**EffectHandler tests:**
- No changes needed (they already test only command handling)

### 6) Cleanup Checklist

**Status: 🔲 Pending (integrated into Phases 2-5)**

This is a checklist of anti-patterns to eliminate during migration. Each item should be addressed in the relevant phase.

**`emptyFlow()` branches to remove (Phase 2):**
- `OnboardingEffectHandler.kt:24` - `is OnboardingEffect.Notification -> emptyFlow()`
- `SettingsEffectHandler.kt:29` - `is SettingsEffect.Notification -> emptyFlow()`
- `ModeSelectionEffectHandler.kt:35` - `is ModeSelectionEffect.NotifyModeSelected -> emptyFlow()`

**Coordinator comments to remove (Phase 3):**
- `OnboardingReducer.kt:125` - `// Handled by coordinator`
- `AppRoot.kt:216` - `// Commands handled by EffectHandler`
- `FeatureCoordinator.kt:40` - comment about ignoring commands

**Silent actions to remove (Phase 2):**
- `OnboardingAction.OnboardingCompleted` (OnboardingAction.kt, OnboardingReducer.kt:124-126)
- `OnboardingEffectHandler.kt:59` - don't emit this action after `MarkOnboardingCompleted`

**Empty coordinator branches to remove (Phase 3):**
- `AppRoot.kt:216` - `is OnboardingEffect.Command -> {}`
- `FeatureCoordinator.kt:40` - `is SettingsEffect.Command -> Unit`

**Final verification (Phase 5):**
- No actions that do nothing in any reducer
- No empty coordinator branches for commands
- No `emptyFlow()` in any effect handler
- All Feature types use 4-type-parameter signature

## Implementation Order

**Phase 1: Core TEA (BLOCKING)**
1. Update `Elm.kt` - new contracts for Command/Notification separation
2. Update `FeatureImpl.kt` - split command/notification processing
3. Update `ReducerDsl.kt` - add `command(...)`/`notify(...)` DSL functions
4. Update `ReducerTestDsl` - add assertion helpers for commands/notifications

**Phase 2: Feature Migrations (can be done in parallel after Phase 1)**
5. Migrate Onboarding (smallest surface, already has structure)
   - Remove `OnboardingAction.OnboardingCompleted` action and its usage
   - Files: OnboardingReducer.kt, OnboardingEffectHandler.kt, OnboardingAction.kt, OnboardingFeatureFactory.kt
6. Migrate Settings (already has structure)
   - Files: SettingsReducer.kt, SettingsEffectHandler.kt, SettingsFeatureFactory.kt
7. Migrate ModeSelection (needs restructuring + AppRoot coordinator)
   - Files: ModeSelectionEffect.kt, ModeSelectionReducer.kt, ModeSelectionEffectHandler.kt, ModeSelectionFeatureFactory.kt, AppRoot.kt
8. Migrate App, Memos, Habits (commands-only features + AppFeatures container)
   - App: AppReducer.kt, AppEffectHandler.kt, AppFeatureFactory.kt
   - Memos: MemosReducer.kt, MemosEffectHandler.kt, MemosFeatureFactory.kt
   - Habits: HabitsReducer.kt, HabitsEffectHandler.kt, HabitsFeatureFactory.kt
   - Shared: AppFeatures.kt (container with all Feature types), AppFeaturesFactory.kt (factory that creates all features)

**Phase 3: Coordinators (after Phase 2)**
9. Update AppRoot coordinator - switch to `notifications` flow
10. Update FeatureCoordinator - switch to `notifications` flow

**Phase 4: Tests (after Phase 2)**
11. Update all reducer tests - use new assertion helpers
12. Verify all tests pass

**Phase 5: Cleanup & Verification (final pass)**
13. Remove coordinator comments (e.g., `// Handled by coordinator`, `// Commands handled by EffectHandler`)
14. Verify no `emptyFlow()` remains for notifications in any EffectHandler
15. Verify no silent actions remain in any reducer
16. Verify all Feature types use new 4-type-parameter signature
17. Final code review and documentation check

## Risks / Notes

**Breaking Changes:**
- This is a **breaking change** to TEA core affecting all features
- All `Feature` type signatures will change from `Feature<Action, State, Effect>` to `Feature<Action, State, Command, Notification>`
- All `EffectHandler` type signatures will change from `EffectHandler<Effect, Action>` to `EffectHandler<Command, Action>`
- All reducers will need type updates and DSL changes

**Migration Strategy:**
- Phase 1 is BLOCKING - all features must wait for core changes
- After Phase 1, features can migrate in parallel (good for team distribution)
- Keep migrations focused: no UI, domain, or business logic changes during migration
- Test each feature after migration before moving to next

**Type Parameter Decisions (RESOLVED):**
- Features with only commands (App, Memos, Habits): Use `Nothing` as Notification type
- Example: `Feature<AppAction, AppState, AppEffect, Nothing>`
- `Nothing` is Kotlin's bottom type - perfect for "this will never happen"

**Testing Strategy:**
- Run full test suite after Phase 1 (expect failures, that's normal)
- Fix feature tests incrementally in Phase 4
- Keep CI green or clearly mark as WIP in PRs

## Open Questions

**Status: ✅ All Resolved**

All open questions have been answered:

1. ✅ **What type to use for Notification in commands-only features?**
   - Decision: Use `Nothing` (Kotlin's bottom type)
   - Applies to: App, Memos, Habits features
   - Benefit: Compile-time prevention of `notify(...)` calls
   - See: Technical Details section for `Nothing` behavior

2. ✅ **What to do with `OnboardingAction.OnboardingCompleted`?**
   - Decision: Remove this silent action in Phase 2
   - Rationale: `OnboardingEffect.Notification.Completed` already serves this purpose
   - Location: OnboardingAction.kt, OnboardingReducer.kt:124-126, OnboardingEffectHandler.kt:59

3. ✅ **Which features have Command/Notification structure?**
   - Already structured: Onboarding, Settings
   - Needs restructuring: ModeSelection
   - Commands only: App, Memos, Habits

4. ✅ **Are there other silent actions?**
   - Audit complete: Only `OnboardingAction.OnboardingCompleted` found

5. ✅ **Implementation order and blocking?**
   - Phase 1 (Core TEA) blocks everything
   - Phase 2 (Features) can be parallelized
   - Phase 3 (Coordinators) depends on Phase 2
   - All files identified including factories and containers

6. ✅ **How to handle `effect(...)` DSL migration?**
   - Decision: Remove `effect(...)` entirely, no compatibility layer
   - Find/replace with `command(...)` or `notify(...)` during feature migrations
   - Compile errors will guide the migration

7. ✅ **What happens to old `effects: Flow<Effect>`?**
   - Remove from Feature interface
   - Replace with `notifications: Flow<Notification>`
   - Commands stay internal, never exposed

8. ✅ **Which factory files need updates?**
   - All `*FeatureFactory.kt` files (return type changes)
   - `AppFeatures.kt` container (all Feature properties)
   - `AppFeaturesFactory.kt` factory (creates all features)

**The plan is now ready for implementation.**

## Optional Follow-ups (Post-Migration)

These are **not required** for the core refactor but may improve clarity:

1. **Naming consistency across features:**
   - Current: `OnboardingEffect.Command`, `SettingsEffect.Command`
   - Alternative: Separate top-level types like `OnboardingCommand`, `OnboardingNotification`
   - Decision: Keep current naming (nested sealed interfaces) for consistency with existing codebase patterns

2. **Action audit for completion events:**
   - Look for actions that exist only to signal "effect completed" (like `OnboardingCompleted`)
   - Evaluate if they should be notifications instead
   - This refactor already addresses the known case (`OnboardingAction.OnboardingCompleted`)

3. **Documentation updates:**
   - Update CLAUDE.md with new TEA patterns
   - Add examples of command-only vs notification-emitting features
   - Document when to use `Nothing` vs actual Notification types

4. **Performance monitoring:**
   - Measure if separating command/notification flows improves performance
   - Current implementation processes all effects through single channel
   - New implementation may have better throughput with separate channels

---

## PR Summary (for description)

**Why**
- TEA core currently routes all effects through EffectHandler and exposes them to UI, forcing `emptyFlow()` and no‑op branches.
- This refactor enforces Command/Notification separation, removing anti‑patterns and clarifying intent.

**What changed (high level)**
- TEA core updated to split commands and notifications in reducer results.
- EffectHandler now accepts only commands; Feature exposes only notifications.
- All features migrated to the new contract; ModeSelection restructured.
- Coordinators now observe notifications only.
- Reducer test DSL updated to assert commands/notifications separately.

**Key cleanups**
- Removed `emptyFlow()` branches for notifications in effect handlers.
- Removed silent actions (e.g., `OnboardingAction.OnboardingCompleted`).
- Removed coordinator branches that ignore commands.

**Testing**
- Update reducer tests to use `assertCommands(...)` / `assertNotifications(...)`.
- Run `./gradlew :shared:desktopTest` (or full `./gradlew checkAll`).
