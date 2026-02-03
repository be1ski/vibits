package space.be1ski.vibits.feature.habits.presentation.reducer

import space.be1ski.vibits.core.elm.Reducer
import space.be1ski.vibits.core.elm.reducer
import space.be1ski.vibits.feature.habits.presentation.action.HabitsAction
import space.be1ski.vibits.feature.habits.presentation.effect.HabitsEffect
import space.be1ski.vibits.feature.habits.presentation.state.HabitsState

/**
 * Sub-reducer for delete config confirmation.
 */
internal val configDeleteReducer: Reducer<HabitsAction.ConfigDelete, HabitsState, HabitsEffect, Nothing> =
  reducer { action, state ->
    when (action) {
      is HabitsAction.ConfigDelete.RequestDeleteConfig -> {
        state { state.copy(showDeleteConfigConfirm = true) }
      }

      is HabitsAction.ConfigDelete.ConfirmDeleteConfig -> {
        val existingMemo = state.editingConfigMemo ?: return@reducer
        state {
          state.copy(
            showDeleteConfigConfirm = false,
            showConfigDialog = false,
            editingHabits = emptyList(),
            editingConfigMemo = null,
            isLoading = true,
          )
        }
        command(HabitsEffect.DeleteMemo(existingMemo.name))
      }

      is HabitsAction.ConfigDelete.CancelDeleteConfig -> {
        state { state.copy(showDeleteConfigConfirm = false) }
      }
    }
  }
