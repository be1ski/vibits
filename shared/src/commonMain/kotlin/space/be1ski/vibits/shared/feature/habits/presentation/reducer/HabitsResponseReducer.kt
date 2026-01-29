package space.be1ski.vibits.shared.feature.habits.presentation.reducer

import space.be1ski.vibits.shared.core.elm.ReducerResult
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsAction
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsEffect
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsState

/**
 * Sub-reducer for API response handling.
 */
internal fun responseReducer(
  action: HabitsAction.Response,
  state: HabitsState,
): ReducerResult<HabitsState, HabitsEffect, Nothing> =
  reducer<HabitsAction.Response, HabitsState, HabitsEffect, Nothing> { a, s ->
    when (a) {
      is HabitsAction.Response.MemoCreated, is HabitsAction.Response.MemoUpdated -> {
        state {
          copy(
            isLoading = false,
            editorDay = null,
            editorConfig = emptyList(),
            editorSelections = emptyMap(),
            editorExisting = null,
            editorError = null,
            showConfigDialog = false,
            editingHabits = emptyList(),
            singleToggleDay = null,
            singleToggleHabitTag = null,
            singleToggleHabitLabel = null,
            singleToggleConfig = emptyList(),
          )
        }
        command(HabitsEffect.RefreshMemos)
      }

      is HabitsAction.Response.MemoDeleted -> {
        state {
          copy(
            isLoading = false,
            editorDay = null,
            editorConfig = emptyList(),
            editorSelections = emptyMap(),
            editorExisting = null,
            editorError = null,
            showDeleteConfirm = false,
            singleToggleDay = null,
            singleToggleHabitTag = null,
            singleToggleHabitLabel = null,
            singleToggleConfig = emptyList(),
          )
        }
        command(HabitsEffect.RefreshMemos)
      }

      is HabitsAction.Response.MemoOperationFailed -> {
        state {
          copy(
            isLoading = false,
            editorError = a.error,
            singleToggleDay = null,
            singleToggleHabitTag = null,
            singleToggleHabitLabel = null,
            singleToggleConfig = emptyList(),
          )
        }
      }
    }
  }(action, state)
