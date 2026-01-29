# Reducer Refactoring Plan

## Overview

Decompose large reducers into focused sub-reducers following the same pattern as effect handlers. Each sub-reducer should be ~30-60 lines and handle a specific aspect of the feature. The main reducer becomes a router that delegates to sub-reducers in a deterministic order.

**Execution scope:** All phases (reducer refactoring + view package moves) happen in one refactoring session. Pull request will be created only when all phases are complete, all tests pass, and all checks succeed.

## Current State

| Reducer | Lines | Status |
|---------|-------|--------|
| HabitsReducer | 453 | 🔴 Needs major decomposition |
| MemosReducer | 210 | 🟡 Needs decomposition |
| SettingsReducer | 161 | 🟡 Needs decomposition |
| OnboardingReducer | 124 | 🟢 Borderline, minor refactoring |
| ModeSelectionReducer | 95 | 🟢 OK |
| AppReducer | 73 | 🟢 OK |

## Refactoring Strategy

### 1. Package Structure

Create `presentation/` subdirectories (only these three):
```
feature/<name>/
  presentation/
    reducer/                    # Sub-reducers
      <Name>EditorReducer.kt    # Editor-related actions
      <Name>DialogReducer.kt    # Dialog-related actions
      <Name>CrudReducer.kt      # CRUD operations
      ...
    handler/                    # Effect handlers (existing pattern)
      <Name>CredentialsEffectHandler.kt
      <Name>ApiEffectHandler.kt
      ...
    view/                       # Compose UI (moved from feature/<name>/view)
      <Name>Screen.kt
      <Name>Dialog.kt
      ...
    <Name>Reducer.kt            # Main router reducer
    <Name>Action.kt
    <Name>State.kt
    <Name>Effect.kt
    <Name>Feature.kt
```

### 2. Sub-Reducer Pattern (No `else -> null`)

**Requirement:** all `when` branches must be exhaustive. To achieve this, actions are split into sealed subtypes per reducer group (same approach as effects).

**Action split example:**
```kotlin
sealed interface HabitsAction {
  sealed interface Editor : HabitsAction {
    data object OpenEditor : Editor
    data object CloseEditor : Editor
    data class ToggleHabit(...) : Editor
    // ...
  }

  sealed interface Config : HabitsAction {
    data object OpenConfigDialog : Config
    data object CloseConfigDialog : Config
    // ...
  }

  // ...
}
```

**Sub-reducer signature uses group type:**
```kotlin
internal fun editorReducer(
  action: HabitsAction.Editor,
  state: HabitsState,
): ReducerResult<HabitsAction, HabitsState, HabitsEffect, Nothing> =
  reducer {
    when (action) {
      is HabitsAction.Editor.OpenEditor -> { ... }
      is HabitsAction.Editor.CloseEditor -> { ... }
      is HabitsAction.Editor.ToggleHabit -> { ... }
    }
  }
```

**Main reducer delegates by type and stays exhaustive:**
```kotlin
val habitsReducer: Reducer<HabitsAction, HabitsState, HabitsEffect, Nothing> =
  reducer { action, state ->
    when (action) {
      is HabitsAction.Editor -> editorReducer(action, state)
      is HabitsAction.Config -> configReducer(action, state)
      is HabitsAction.ConfigWarning -> configWarningReducer(action, state)
      is HabitsAction.ConfigDelete -> configDeleteReducer(action, state)
      is HabitsAction.SingleToggle -> singleToggleReducer(action, state)
      is HabitsAction.Selection -> selectionReducer(action, state)
      is HabitsAction.Response -> responseReducer(action, state)
      is HabitsAction.Cache -> cacheReducer(action, state)
    }
  }
```

### 3. Deterministic Routing Rules

With action splitting, routing is type-based and exhaustive; ordering conflicts disappear. Rules:
- A single action type belongs to exactly one group (sealed subtype).
- Do not use catch-all reducers or default branches.
- If a reducer needs to be split further, introduce a nested sealed subtype.

### 4. No Open Questions Policy

Every phase includes a preflight inventory step that extracts all actions currently handled by the reducer and maps them to a target sub-reducer. This removes ambiguity and prevents missed actions.

## Phase 1: HabitsReducer (453 lines → 8 sub-reducers)

**Target files:**
- `HabitsEditorReducer.kt` (~50 lines) - Editor lifecycle and interactions
- `HabitsConfigReducer.kt` (~70 lines) - Config dialog management
- `HabitsConfigWarningReducer.kt` (~40 lines) - Edit config warning flow
- `HabitsConfigDeleteReducer.kt` (~30 lines) - Delete config confirmation
- `HabitsSingleToggleReducer.kt` (~60 lines) - Single habit toggle from matrix
- `HabitsSelectionReducer.kt` (~35 lines) - Day/week selection
- `HabitsResponseReducer.kt` (~50 lines) - API response handling
- `HabitsCacheReducer.kt` (~60 lines) - Cache management

**Action groups:**
1. **Editor** (7 actions): OpenEditor, CloseEditor, ToggleHabit, ConfirmEditor, RequestDelete, ConfirmDelete, CancelDelete
2. **Config** (7 actions): OpenConfigDialog, CloseConfigDialog, AddHabit, UpdateHabitLabel, UpdateHabitColor, DeleteHabit, SaveConfigDialog
3. **Config Warning** (3 actions): DismissEditConfigWarning, ConfirmEditExistingConfig, CreateNewConfigInstead
4. **Config Delete** (3 actions): RequestDeleteConfig, ConfirmDeleteConfig, CancelDeleteConfig
5. **Single Toggle** (3 actions): RequestSingleHabitToggle, ConfirmSingleHabitToggle, CancelSingleHabitToggle
6. **Selection** (3 actions): SelectDay, SelectWeek, ClearSelection
7. **Response** (4 actions): MemoCreated, MemoUpdated, MemoDeleted, MemoOperationFailed
8. **Cache** (5 actions): RequestPrewarmAllRanges, UpdateActivityData, PrewarmCompleted, InvalidateAllCache, InvalidateCache

### Phase 1 Preflight Inventory

Complete mapping of all 35 actions in HabitsReducer to their target groups:

**Group 1: Editor (7 actions)**
- Line 25: `OpenEditor(day, memo, config)` → HabitsAction.Editor.OpenEditor
- Line 63: `CloseEditor` → HabitsAction.Editor.CloseEditor
- Line 76: `ToggleHabit(tag, checked)` → HabitsAction.Editor.ToggleHabit
- Line 82: `ConfirmEditor` → HabitsAction.Editor.ConfirmEditor
- Line 107: `RequestDelete` → HabitsAction.Editor.RequestDelete
- Line 111: `ConfirmDelete` → HabitsAction.Editor.ConfirmDelete
- Line 117: `CancelDelete` → HabitsAction.Editor.CancelDelete

**Group 2: Config (7 actions)**
- Line 121: `OpenConfigDialog(currentConfig, existingMemo)` → HabitsAction.Config.OpenConfigDialog
- Line 129: `CloseConfigDialog` → HabitsAction.Config.CloseConfigDialog
- Line 133: `AddHabit` → HabitsAction.Config.AddHabit
- Line 145: `UpdateHabitLabel(id, label)` → HabitsAction.Config.UpdateHabitLabel
- Line 157: `UpdateHabitColor(id, color)` → HabitsAction.Config.UpdateHabitColor
- Line 169: `DeleteHabit(id)` → HabitsAction.Config.DeleteHabit
- Line 174: `SaveConfigDialog` → HabitsAction.Config.SaveConfigDialog

**Group 3: ConfigWarning (3 actions)**
- Line 195: `DismissEditConfigWarning` → HabitsAction.ConfigWarning.DismissEditConfigWarning
- Line 208: `ConfirmEditExistingConfig` → HabitsAction.ConfigWarning.ConfirmEditExistingConfig
- Line 222: `CreateNewConfigInstead` → HabitsAction.ConfigWarning.CreateNewConfigInstead

**Group 4: ConfigDelete (3 actions)**
- Line 235: `RequestDeleteConfig` → HabitsAction.ConfigDelete.RequestDeleteConfig
- Line 239: `ConfirmDeleteConfig` → HabitsAction.ConfigDelete.ConfirmDeleteConfig
- Line 253: `CancelDeleteConfig` → HabitsAction.ConfigDelete.CancelDeleteConfig

**Group 5: SingleToggle (3 actions)**
- Line 257: `RequestSingleHabitToggle(day, habitTag, habitLabel, config)` → HabitsAction.SingleToggle.RequestSingleHabitToggle
- Line 268: `ConfirmSingleHabitToggle` → HabitsAction.SingleToggle.ConfirmSingleHabitToggle
- Line 316: `CancelSingleHabitToggle` → HabitsAction.SingleToggle.CancelSingleHabitToggle

**Group 6: Selection (3 actions)**
- Line 327: `SelectDay(day, selectionId)` → HabitsAction.Selection.SelectDay
- Line 336: `SelectWeek(week)` → HabitsAction.Selection.SelectWeek
- Line 340: `ClearSelection` → HabitsAction.Selection.ClearSelection

**Group 7: Response (4 actions)**
- Line 350: `MemoCreated(memo)` → HabitsAction.Response.MemoCreated
- Line 350: `MemoUpdated(memo)` → HabitsAction.Response.MemoUpdated (combined handling)
- Line 370: `MemoDeleted(name)` → HabitsAction.Response.MemoDeleted
- Line 389: `MemoOperationFailed(error)` → HabitsAction.Response.MemoOperationFailed

**Group 8: Cache (5 actions)**
- Line 403: `RequestPrewarmAllRanges(memos, appMode)` → HabitsAction.Cache.RequestPrewarmAllRanges
- Line 413: `UpdateActivityData(range, mode, appMode, weekData, configTimeline, successRate)` → HabitsAction.Cache.UpdateActivityData
- Line 425: `PrewarmCompleted` → HabitsAction.Cache.PrewarmCompleted
- Line 433: `InvalidateAllCache` → HabitsAction.Cache.InvalidateAllCache
- Line 443: `InvalidateCache(range, mode, appMode, memos)` → HabitsAction.Cache.InvalidateCache

**Total: 35 actions mapped to 8 groups. ✅ All actions accounted for.**

**Phase 1 execution checklist:**
1. Inventory all actions currently handled by `HabitsReducer` and assign each to a sealed subtype group. No action may remain ungrouped.
2. Create `feature/habits/presentation/reducer/` and add 8 sub-reducer files with matching names.
3. Split `HabitsAction` into sealed subtypes matching the 8 groups.
4. Move action handlers group-by-group into the matching file and keep logic unchanged.
5. Convert `HabitsReducer.kt` into a router using `when (action)` on subgroup types (exhaustive, no `else`).
6. Update or add tests for each sub-reducer, ensuring existing behavior stays identical.

## Phase 2: MemosReducer (210 lines → 5 sub-reducers)

**Target files:**
- `MemosCredentialsReducer.kt` (~30 lines) - Credentials input
- `MemosLoadingReducer.kt` (~60 lines) - Loading and filtering
- `MemosCrudReducer.kt` (~50 lines) - CRUD operations
- `MemosCreateDialogReducer.kt` (~35 lines) - Create dialog
- `MemosEditDialogReducer.kt` (~35 lines) - Edit dialog

**Action groups:**
1. **Credentials** (4 actions): UpdateBaseUrl, UpdateToken, EditCredentials, CredentialsLoaded
2. **Loading** (6 actions): LoadMemos, LoadCachedMemos, ResetForModeChange, ChangePostFilter, CachedMemosLoaded, MemosLoaded
3. **CRUD** (7 actions): CreateMemo, UpdateMemo, DeleteMemo, MemoCreated, MemoUpdated, MemoDeleted, OperationFailed
4. **Create Dialog** (4 actions): ShowCreateDialog, UpdateCreateContent, DismissCreateDialog, ConfirmCreateDialog
5. **Edit Dialog** (4 actions): ShowEditDialog, UpdateEditContent, DismissEditDialog, ConfirmEditDialog

**Note:** Keep `sortedMemos()` helper in main reducer file.

### Phase 2 Preflight Inventory

Complete mapping of all 25 actions in MemosReducer to their target groups:

**Group 1: Credentials (4 actions)**
- Line 17: `UpdateBaseUrl(value)` → MemosAction.Credentials.UpdateBaseUrl
- Line 21: `UpdateToken(value)` → MemosAction.Credentials.UpdateToken
- Line 25: `EditCredentials` → MemosAction.Credentials.EditCredentials
- Line 30: `CredentialsLoaded(baseUrl, token)` → MemosAction.Credentials.CredentialsLoaded

**Group 2: Loading (6 actions)**
- Line 35: `LoadMemos` → MemosAction.Loading.LoadMemos
- Line 47: `LoadCachedMemos` → MemosAction.Loading.LoadCachedMemos
- Line 51: `ResetForModeChange` → MemosAction.Loading.ResetForModeChange
- Line 63: `ChangePostFilter(filter)` → MemosAction.Loading.ChangePostFilter
- Line 67: `CachedMemosLoaded(memos)` → MemosAction.Loading.CachedMemosLoaded
- Line 92: `MemosLoaded(memos)` → MemosAction.Loading.MemosLoaded

**Group 3: CRUD (7 actions)**
- Line 105: `CreateMemo(content)` → MemosAction.Crud.CreateMemo
- Line 110: `UpdateMemo(name, content)` → MemosAction.Crud.UpdateMemo
- Line 115: `DeleteMemo(name)` → MemosAction.Crud.DeleteMemo
- Line 120: `MemoCreated(memo)` → MemosAction.Crud.MemoCreated
- Line 125: `MemoUpdated(memo)` → MemosAction.Crud.MemoUpdated
- Line 135: `MemoDeleted(name)` → MemosAction.Crud.MemoDeleted
- Line 140: `OperationFailed(error)` → MemosAction.Crud.OperationFailed

**Group 4: CreateDialog (4 actions)**
- Line 145: `ShowCreateDialog` → MemosAction.CreateDialog.ShowCreateDialog
- Line 149: `UpdateCreateContent(content)` → MemosAction.CreateDialog.UpdateCreateContent
- Line 153: `DismissCreateDialog` → MemosAction.CreateDialog.DismissCreateDialog
- Line 157: `ConfirmCreateDialog` → MemosAction.CreateDialog.ConfirmCreateDialog

**Group 5: EditDialog (4 actions)**
- Line 166: `ShowEditDialog(memo)` → MemosAction.EditDialog.ShowEditDialog
- Line 176: `UpdateEditContent(content)` → MemosAction.EditDialog.UpdateEditContent
- Line 180: `DismissEditDialog` → MemosAction.EditDialog.DismissEditDialog
- Line 184: `ConfirmEditDialog` → MemosAction.EditDialog.ConfirmEditDialog

**Total: 25 actions mapped to 5 groups. ✅ All actions accounted for.**

**Phase 2 execution checklist:**
1. Inventory all actions in `MemosReducer` and assign each to a sealed subtype group.
2. Create `feature/memos/presentation/reducer/` and add the 5 sub-reducer files.
3. Split `MemosAction` into sealed subtypes matching the 5 groups.
4. Move action handlers with no logic changes.
5. Reduce `MemosReducer.kt` to router-only and keep `sortedMemos()` in it.
6. Add/update tests per sub-reducer.

## Phase 3: SettingsReducer (161 lines → 5 sub-reducers)

**Target files:**
- `SettingsDialogReducer.kt` (~30 lines) - Dialog lifecycle
- `SettingsInputReducer.kt` (~40 lines) - Credentials, mode, language, theme
- `SettingsValidationReducer.kt` (~30 lines) - Validation flow
- `SettingsResetReducer.kt` (~40 lines) - Reset flow
- `SettingsSaveReducer.kt` (~45 lines) - Save logic (OpenLogs, CloseLogs, Save)

**Action groups:**
1. **Dialog** (3 actions): Open, Close, Dismiss
2. **Input** (6 actions): UpdateBaseUrl, UpdateToken, SelectMode, SelectLanguage, SelectTheme, ModeSwitched
3. **Validation** (2 actions): ValidationSucceeded, ValidationFailed
4. **Reset** (5 actions): RequestReset, ConfirmReset, ConfirmResetWithMemos, CancelReset, ResetCompleted
5. **Save & Logs** (3 actions): OpenLogs, CloseLogs, Save

### Phase 3 Preflight Inventory

Complete mapping of all 19 actions in SettingsReducer to their target groups:

**Group 1: Dialog (3 actions)**
- Line 13: `Open(baseUrl, token, appMode, language, theme)` → SettingsAction.Dialog.Open
- Line 32: `Close` → SettingsAction.Dialog.Close
- Line 45: `Dismiss` → SettingsAction.Dialog.Dismiss

**Group 2: Input (6 actions)**
- Line 59: `UpdateBaseUrl(value)` → SettingsAction.Input.UpdateBaseUrl
- Line 63: `UpdateToken(value)` → SettingsAction.Input.UpdateToken
- Line 68: `SelectMode(mode)` → SettingsAction.Input.SelectMode
- Line 73: `SelectLanguage(language)` → SettingsAction.Input.SelectLanguage
- Line 78: `SelectTheme(theme)` → SettingsAction.Input.SelectTheme
- Line 99: `ModeSwitched` → SettingsAction.Input.ModeSwitched

**Group 3: Validation (2 actions)**
- Line 83: `ValidationSucceeded` → SettingsAction.Validation.ValidationSucceeded
- Line 95: `ValidationFailed(error)` → SettingsAction.Validation.ValidationFailed

**Group 4: Reset (5 actions)**
- Line 104: `RequestReset` → SettingsAction.Reset.RequestReset
- Line 108: `ConfirmReset` → SettingsAction.Reset.ConfirmReset
- Line 113: `ConfirmResetWithMemos` → SettingsAction.Reset.ConfirmResetWithMemos
- Line 118: `CancelReset` → SettingsAction.Reset.CancelReset
- Line 122: `ResetCompleted` → SettingsAction.Reset.ResetCompleted

**Group 5: SaveAndLogs (3 actions)**
- Line 128: `OpenLogs` → SettingsAction.SaveAndLogs.OpenLogs
- Line 132: `CloseLogs` → SettingsAction.SaveAndLogs.CloseLogs
- Line 137: `Save` → SettingsAction.SaveAndLogs.Save

**Total: 19 actions mapped to 5 groups. ✅ All actions accounted for.**

**Phase 3 execution checklist:**
1. Inventory all actions in `SettingsReducer` and assign each to a sealed subtype group.
2. Create `feature/settings/presentation/reducer/` and add the 5 sub-reducer files.
3. Split `SettingsAction` into sealed subtypes matching the 5 groups.
4. Move action handlers; keep logic intact.
5. Router uses exhaustive `when (action)` on subgroup types.
6. Add/update tests per sub-reducer.

## Phase 4: OnboardingReducer (124 lines → 4 sub-reducers)

**Target files:**
- `OnboardingNavigationReducer.kt` (~40 lines) - Navigation flow
- `OnboardingPresetReducer.kt` (~25 lines) - Preset selection
- `OnboardingHabitReducer.kt` (~35 lines) - Habit setup
- `OnboardingCompletionReducer.kt` (~30 lines) - Completion flow

**Action groups:**
1. **Navigation** (4 actions): StartOnboarding, Continue, Back, Skip
2. **Preset** (2 actions): PresetsLoaded, SelectPreset
3. **Habit** (5 actions): UpdateHabitName, UpdateHabitColor, CreateHabit, HabitCreated, HabitCreationFailed
4. **Completion** (3 actions): MarkFirstCheckIn, FirstCheckInCreated, GoToDashboard

### Phase 4 Preflight Inventory

Complete mapping of all 14 actions in OnboardingReducer to their target groups:

**Group 1: Navigation (4 actions)**
- Line 10: `StartOnboarding` → OnboardingAction.Navigation.StartOnboarding
- Line 15: `Continue` → OnboardingAction.Navigation.Continue
- Line 43: `Back` → OnboardingAction.Navigation.Back
- Line 51: `Skip` → OnboardingAction.Navigation.Skip

**Group 2: Preset (2 actions)**
- Line 56: `PresetsLoaded(presets)` → OnboardingAction.Preset.PresetsLoaded
- Line 60: `SelectPreset(presetId)` → OnboardingAction.Preset.SelectPreset

**Group 3: Habit (5 actions)**
- Line 65: `UpdateHabitName(name)` → OnboardingAction.Habit.UpdateHabitName
- Line 69: `UpdateHabitColor(color)` → OnboardingAction.Habit.UpdateHabitColor
- Line 73: `CreateHabit` → OnboardingAction.Habit.CreateHabit
- Line 89: `HabitCreated` → OnboardingAction.Habit.HabitCreated
- Line 99: `HabitCreationFailed(error)` → OnboardingAction.Habit.HabitCreationFailed

**Group 4: Completion (3 actions)**
- Line 109: `MarkFirstCheckIn` → OnboardingAction.Completion.MarkFirstCheckIn
- Line 114: `FirstCheckInCreated` → OnboardingAction.Completion.FirstCheckInCreated
- Line 119: `GoToDashboard` → OnboardingAction.Completion.GoToDashboard

**Total: 14 actions mapped to 4 groups. ✅ All actions accounted for.**

**Phase 4 execution checklist:**
1. Inventory all actions in `OnboardingReducer` and assign each to a sealed subtype group.
2. Create `feature/onboarding/presentation/reducer/` and add the 4 sub-reducer files.
3. Split `OnboardingAction` into sealed subtypes matching the 4 groups.
4. Move action handlers; keep logic intact.
5. Router uses exhaustive `when (action)` on subgroup types.
6. Add/update tests per sub-reducer.

## Implementation Guidelines

### 1. Sub-Reducer Signature

All sub-reducers use the group action type and are exhaustive:

```kotlin
internal fun xxxReducer(
  action: FeatureAction.Group,
  state: FeatureState,
): ReducerResult<FeatureAction, FeatureState, FeatureEffect, Notification> =
  reducer {
    when (action) {
      is FeatureAction.Group.SomeAction -> { ... }
      is FeatureAction.Group.OtherAction -> { ... }
    }
  }
```

### 2. Main Reducer Router

The main reducer file becomes a simple router:

```kotlin
package space.be1ski.vibits.shared.feature.xxx.presentation

import space.be1ski.vibits.shared.core.elm.Reducer
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.feature.xxx.presentation.reducer.editorReducer
import space.be1ski.vibits.shared.feature.xxx.presentation.reducer.configReducer
// ... other imports

/**
 * Main reducer that delegates to sub-reducers.
 */
val xxxReducer: Reducer<XxxAction, XxxState, XxxEffect, Notification> =
  reducer { action, state ->
    when (action) {
      is XxxAction.Editor -> editorReducer(action, state)
      is XxxAction.Config -> configReducer(action, state)
      is XxxAction.Cache -> cacheReducer(action, state)
    }
  }
```

### 3. Testing Strategy

- Each sub-reducer gets its own test file
- Main reducer tests verify delegation works correctly
- Keep existing test coverage (ensure ~95% maintained)
- Hardcode strings in tests (do not use constants)

### 4. File Organization

```
presentation/
  reducer/
    HabitsEditorReducer.kt
    HabitsConfigReducer.kt
    ...
  handler/
    ...
  view/
    ...                         # UI moved from feature/<name>/view
  HabitsAction.kt
  HabitsState.kt
  HabitsEffect.kt
  HabitsReducer.kt  # Router
```

### 5. View Package Move

Move `feature/<name>/view/` to `feature/<name>/presentation/view/`:
- Keep `@Composable` code under `*.view.*` packages to preserve coverage exclusions.
- Update package declarations and imports.
- Update Kover excludes if they rely on path-based patterns (verify current config).

### 6. Presentation Root Contents

`presentation/` root stays for TEA contracts only:
- `*Action`
- `*State`
- `*Effect`
- `*Feature`

No additional subpackages under `presentation/` beyond `handler`, `reducer`, `view` unless a concrete need is documented in the plan.

## Success Criteria

- ✅ No reducer over 100 lines
- ✅ Each sub-reducer handles related actions (~30-60 lines)
- ✅ All tests pass with same coverage
- ✅ Clear separation of concerns
- ✅ Easy to locate and modify specific behaviors

## Order of Execution

1. Phase 1: HabitsReducer (largest, most complex)
2. Phase 2: MemosReducer
3. Phase 3: SettingsReducer
4. Phase 4: OnboardingReducer (optional polish)
5. **Documentation Update**: Update CLAUDE.md with new architecture patterns

Total: ~30 new sub-reducer files, 4 main reducers refactored.

## Documentation Updates

After all phases are complete, update `CLAUDE.md` to reflect the new architecture:

1. **Feature Architecture section** - Update package structure to show `reducer/`, `handler/`, `view/` subdirectories
2. **Layer Responsibilities** - Document sub-reducer pattern and router pattern
3. **Testing Guidelines** - Add examples for testing sub-reducers
4. **File naming conventions** - Document `*Reducer.kt` (router) vs `*XxxReducer.kt` (sub-reducer)

**Key points to document:**
- Actions are split into sealed subtypes (groups)
- Each group handled by dedicated sub-reducer
- Main reducer is exhaustive router
- Sub-reducers are internal functions with group-specific action types
- Same pattern as effect handlers (consistency across TEA components)

## Verification Steps (per phase)

1. Run unit tests for shared logic:
   - `./gradlew :shared:desktopTest`
2. Check coverage remains stable:
   - `./gradlew :shared:koverHtmlReport`
   - Open `shared/build/reports/kover/html/index.html` and confirm no new exclusions required.
