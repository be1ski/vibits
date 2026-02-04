package space.be1ski.vibits.feature.mode.presentation.reducer

import space.be1ski.vibits.core.elm.Reducer
import space.be1ski.vibits.core.elm.reducer
import space.be1ski.vibits.feature.mode.presentation.action.ModeSelectionAction
import space.be1ski.vibits.feature.mode.presentation.effect.ModeSelectionEffect.Command
import space.be1ski.vibits.feature.mode.presentation.effect.ModeSelectionEffect.Notification
import space.be1ski.vibits.feature.mode.presentation.state.ModeSelectionState

internal val dialogReducer: Reducer<ModeSelectionAction.Dialog, ModeSelectionState, Command, Notification> =
  reducer { action, state ->
    when (action) {
      is ModeSelectionAction.Dialog.Show -> {
        state { state.copy(showCredentialsDialog = true, error = null) }
      }

      is ModeSelectionAction.Dialog.Dismiss -> {
        state {
          state.copy(
            showCredentialsDialog = false,
            baseUrl = "",
            token = "",
            isValidating = false,
            error = null,
          )
        }
      }
    }
  }
