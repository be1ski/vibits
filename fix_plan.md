# Fix Plan - Vibits Codebase Improvements

## Priority 1: HIGH IMPACT - Test Coverage Gaps

### 1.1 Settings Presentation - Missing Reducer Tests
**Status:** DONE ✓
**Effort:** N/A
**Impact:** N/A

Analysis shows `SettingsReducerTest.kt` already covers ALL 5 sub-reducers through the main reducer:
- [x] `SettingsInputReducer` - 6 test cases cover all actions
- [x] `SettingsValidationReducer` - 2 test cases cover all actions
- [x] `SettingsSaveAndLogsReducer` - 4 test cases cover all scenarios
- [x] `SettingsResetReducer` - 5 test cases cover all actions
- [x] `SettingsDialogReducer` - 3 test cases cover all actions

Total: 20 tests covering all action types and edge cases.

### 1.2 Mode Selection Presentation - Effect Handler Tests
**Status:** DONE ✓
**Effort:** N/A
**Impact:** N/A

Analysis shows `ModeSelectionEffectHandlerTest.kt` covers ALL 6 commands:
- [x] `InitializeFromLocalConfig` - 2 test cases (with/without credentials)
- [x] `CheckStoredCredentials` - 2 test cases (with/without credentials)
- [x] `UseStoredCredentialsWithValidation` - 2 test cases (success/failure)
- [x] `ValidateCredentials` - 2 test cases (success/failure)
- [x] `SaveCredentials` - 1 test case
- [x] `SaveMode` - 1 test case

Total: 10 tests covering all commands and edge cases.

### 1.3 Habits Presentation - Reducer Coverage
**Status:** DONE ✓
**Effort:** N/A
**Impact:** N/A

Analysis shows `HabitsReducerTest.kt` already covers ALL 8 sub-reducers through the main reducer:
- [x] `HabitsEditorReducer` - 13 test cases
- [x] `HabitsConfigReducer` - 15 test cases
- [x] `HabitsConfigWarningReducer` - 3 test cases
- [x] `HabitsConfigDeleteReducer` - 4 test cases
- [x] `HabitsSingleToggleReducer` - 5 test cases
- [x] `HabitsSelectionReducer` - 3 test cases
- [x] `HabitsResponseReducer` - 4 test cases
- [x] `HabitsCacheReducer` - 6 test cases

Total: 53+ tests covering all action types and edge cases.

---

## Priority 2: Code Duplication

### 2.1 Duplicate Credentials Validation Logic
**Status:** WONTFIX
**Effort:** Low
**Impact:** Low (revised)

Analysis shows the "duplication" is minimal and acceptable:
- `SettingsCredentialsEffectHandler.kt` - 3 lines, emits `SettingsAction`
- `ModeSelectionCredentialsEffectHandler.kt` - 3 lines, emits `ModeSelectionAction`
- `MemosCredentialsEffectHandler.kt` - **No validation logic** (only load/save)

**Why not extract:**
1. Core validation logic (`ConnectionTester`) is already abstracted at domain level
2. Each feature needs its own action types per TEA architecture
3. Extracting 3 lines to a shared utility adds complexity without benefit
4. Feature isolation is maintained correctly

### 2.2 Inconsistent URL Log Masking Constants
**Status:** DONE ✓
**Effort:** Low
**Impact:** Low

~~- `CredentialsRepositoryImpl.kt`: `URL_LOG_MAX_LENGTH = 50`~~
~~- `ModeSelectionCredentialsEffectHandler.kt`: `LOG_URL_MAX_LENGTH = 20`~~

**Solution:** Extracted `maskUrl()` extension function to `core/logging/Log.kt` with 50-char default. All callers updated to use centralized utility.

---

## Priority 3: Code Smells

### 3.1 LongMethod Suppression
**Status:** DONE ✓
**Effort:** Low
**Impact:** Low (revised)

Analysis shows the `@Suppress("LongMethod")` was unnecessary:
- `SettingsSaveAndLogsReducer.kt` - 44 lines, well under 60-line threshold
- `SettingsResetReducer.kt` - 36 lines, well under 60-line threshold

Removed unnecessary suppressions. Detekt passes without them.

### 3.2 Hardcoded Validation Error Strings
**Status:** WONTFIX
**Effort:** Medium (revised - requires refactoring State, Action, Reducers, EffectHandlers, tests)
**Impact:** Low

Analysis shows this is a minor inconsistency, not a bug:
- Settings uses string codes (`"fill_all_fields"`, `"connection_failed"`) as error identifiers
- ModeSelection uses enum `ModeSelectionError` for the same purpose (cleaner)
- Both approaches work correctly - strings are properly mapped to localized resources in view layer
- All 17 locale files have proper translations

Enum approach is cleaner but refactoring would touch many files for minimal benefit.

---

## Completed

- [x] **2.2 URL Log Masking** - Centralized `maskUrl()` to `core/logging/Log.kt` (commit b753891)
- [x] **3.1 LongMethod Suppression** - Removed unnecessary `@Suppress("LongMethod")` from Settings reducers
