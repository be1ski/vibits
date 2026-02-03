package space.be1ski.vibits.feature.habits.presentation.reducer

import space.be1ski.vibits.core.elm.Reducer
import space.be1ski.vibits.core.elm.reducer
import space.be1ski.vibits.feature.habits.domain.buildHabitsConfigContentFromList
import space.be1ski.vibits.feature.habits.presentation.action.HabitsAction
import space.be1ski.vibits.feature.habits.presentation.effect.HabitsEffect
import space.be1ski.vibits.feature.habits.presentation.state.HabitsState

/**
 * Sub-reducer for edit config warning flow.
 */
internal val configWarningReducer: Reducer<HabitsAction.ConfigWarning, HabitsState, HabitsEffect, Nothing> =
  reducer { action, state ->
    when (action) {
      is HabitsAction.ConfigWarning.DismissEditConfigWarning -> {
        state {
          state.copy(
            showEditConfigWarning = false,
            showConfigDialog = false,
            editingHabits = emptyList(),
            editingConfigMemo = null,
            pendingConfigEdit = emptyList(),
            pendingConfigMemo = null,
          )
        }
      }

      is HabitsAction.ConfigWarning.ConfirmEditExistingConfig -> {
        val content = buildHabitsConfigContentFromList(state.pendingConfigEdit)
        val existingMemo = state.editingConfigMemo ?: return@reducer
        state {
          state.copy(
            showEditConfigWarning = false,
            isLoading = true,
            pendingConfigEdit = emptyList(),
            pendingConfigMemo = null,
          )
        }
        command(HabitsEffect.UpdateMemo(existingMemo.name, content))
      }

      is HabitsAction.ConfigWarning.CreateNewConfigInstead -> {
        val content = buildHabitsConfigContentFromList(state.pendingConfigEdit)
        state {
          state.copy(
            showEditConfigWarning = false,
            isLoading = true,
            pendingConfigEdit = emptyList(),
            pendingConfigMemo = null,
          )
        }
        command(HabitsEffect.CreateMemo(content))
      }
    }
  }
