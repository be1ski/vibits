package space.be1ski.vibits.feature.habits.presentation.reducer

import space.be1ski.vibits.core.elm.Reducer
import space.be1ski.vibits.core.elm.reducer
import space.be1ski.vibits.feature.habits.presentation.action.HabitsAction
import space.be1ski.vibits.feature.habits.presentation.effect.HabitsEffect
import space.be1ski.vibits.feature.habits.presentation.state.HabitsState

/**
 * Sub-reducer for API response handling.
 */
internal val responseReducer: Reducer<HabitsAction.Response, HabitsState, HabitsEffect, Nothing> =
  reducer { action, state ->
    when (action) {
      is HabitsAction.Response.MemoCreated, is HabitsAction.Response.MemoUpdated -> {
        state {
          state.copy(
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
          state.copy(
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
          state.copy(
            isLoading = false,
            editorError = action.error,
            singleToggleDay = null,
            singleToggleHabitTag = null,
            singleToggleHabitLabel = null,
            singleToggleConfig = emptyList(),
          )
        }
      }
    }
  }
