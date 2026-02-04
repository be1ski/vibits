package space.be1ski.vibits.feature.mode.presentation.reducer

import space.be1ski.vibits.core.elm.Reducer
import space.be1ski.vibits.core.elm.reducer
import space.be1ski.vibits.feature.mode.presentation.action.ModeSelectionAction
import space.be1ski.vibits.feature.mode.presentation.effect.ModeSelectionEffect.Command
import space.be1ski.vibits.feature.mode.presentation.effect.ModeSelectionEffect.Notification
import space.be1ski.vibits.feature.mode.presentation.state.ModeSelectionState

internal val inputReducer: Reducer<ModeSelectionAction.Input, ModeSelectionState, Command, Notification> =
  reducer { action, state ->
    when (action) {
      is ModeSelectionAction.Input.UpdateBaseUrl -> {
        state { state.copy(baseUrl = action.value, error = null) }
      }

      is ModeSelectionAction.Input.UpdateToken -> {
        state { state.copy(token = action.value, error = null) }
      }
    }
  }
