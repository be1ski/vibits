package space.be1ski.vibits.feature.habits.presentation.reducer

import space.be1ski.vibits.core.elm.Reducer
import space.be1ski.vibits.core.elm.reducer
import space.be1ski.vibits.feature.habits.presentation.action.HabitsAction
import space.be1ski.vibits.feature.habits.presentation.effect.HabitsEffect
import space.be1ski.vibits.feature.habits.presentation.state.EditorError
import space.be1ski.vibits.feature.habits.presentation.state.HabitsState

internal val responseReducer: Reducer<HabitsAction.Response, HabitsState, HabitsEffect, Nothing> =
  reducer { action, state ->
    when (action) {
      is HabitsAction.Response.MemoCreated, is HabitsAction.Response.MemoUpdated -> {
        state { state.resetAfterOperation().copy(showConfigDialog = false, editingHabits = emptyList(), needsCacheRefresh = true) }
        command(HabitsEffect.RefreshMemos)
      }

      is HabitsAction.Response.MemoDeleted -> {
        state { state.resetAfterOperation().copy(showDeleteConfirm = false, needsCacheRefresh = true) }
        command(HabitsEffect.RefreshMemos)
      }

      is HabitsAction.Response.MemoOperationFailed -> {
        state { state.resetToggle().copy(isLoading = false, editorError = EditorError.OperationFailed(action.error)) }
      }
    }
  }

private fun HabitsState.resetAfterOperation() =
  copy(
    isLoading = false,
    editorDay = null,
    editorConfig = emptyList(),
    editorSelections = emptyMap(),
    editorExisting = null,
    editorError = null,
    singleToggleDay = null,
    singleToggleHabitTag = null,
    singleToggleHabitLabel = null,
    singleToggleConfig = emptyList(),
  )

private fun HabitsState.resetToggle() =
  copy(
    singleToggleDay = null,
    singleToggleHabitTag = null,
    singleToggleHabitLabel = null,
    singleToggleConfig = emptyList(),
  )
