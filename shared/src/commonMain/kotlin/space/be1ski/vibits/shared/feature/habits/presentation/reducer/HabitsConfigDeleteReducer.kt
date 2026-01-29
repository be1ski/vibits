package space.be1ski.vibits.shared.feature.habits.presentation.reducer

import space.be1ski.vibits.shared.core.elm.ReducerResult
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.feature.habits.presentation.action.HabitsAction
import space.be1ski.vibits.shared.feature.habits.presentation.effect.HabitsEffect
import space.be1ski.vibits.shared.feature.habits.presentation.state.HabitsState

/**
 * Sub-reducer for delete config confirmation.
 */
internal fun configDeleteReducer(
  action: HabitsAction.ConfigDelete,
  state: HabitsState,
): ReducerResult<HabitsState, HabitsEffect, Nothing> =
  reducer<HabitsAction.ConfigDelete, HabitsState, HabitsEffect, Nothing> { a, s ->
    when (a) {
      is HabitsAction.ConfigDelete.RequestDeleteConfig -> {
        state { copy(showDeleteConfigConfirm = true) }
      }

      is HabitsAction.ConfigDelete.ConfirmDeleteConfig -> {
        val existingMemo = s.editingConfigMemo ?: return@reducer
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
  }(action, state)
