package space.be1ski.vibits.shared.feature.habits.presentation.reducer

import space.be1ski.vibits.shared.core.elm.Reducer
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.feature.habits.presentation.action.HabitsAction
import space.be1ski.vibits.shared.feature.habits.presentation.effect.HabitsEffect
import space.be1ski.vibits.shared.feature.habits.presentation.state.HabitsState

/**
 * Sub-reducer for delete config confirmation.
 */
internal val configDeleteReducer: Reducer<HabitsAction.ConfigDelete, HabitsState, HabitsEffect, Nothing> =
  reducer { action, state ->
    when (action) {
      is HabitsAction.ConfigDelete.RequestDeleteConfig -> {
        state { copy(showDeleteConfigConfirm = true) }
      }

      is HabitsAction.ConfigDelete.ConfirmDeleteConfig -> {
        val existingMemo = state.editingConfigMemo ?: return@reducer
        state {
          copy(
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
        state { copy(showDeleteConfigConfirm = false) }
      }
    }
  }
