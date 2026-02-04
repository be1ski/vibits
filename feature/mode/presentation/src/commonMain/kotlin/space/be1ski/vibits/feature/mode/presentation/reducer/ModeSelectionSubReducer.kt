package space.be1ski.vibits.feature.mode.presentation.reducer

import space.be1ski.vibits.core.elm.Reducer
import space.be1ski.vibits.core.elm.reducer
import space.be1ski.vibits.feature.mode.presentation.action.ModeSelectionAction
import space.be1ski.vibits.feature.mode.presentation.effect.ModeSelectionEffect.Command
import space.be1ski.vibits.feature.mode.presentation.effect.ModeSelectionEffect.Notification
import space.be1ski.vibits.feature.mode.presentation.state.ModeSelectionState

internal val selectionReducer: Reducer<ModeSelectionAction.Selection, ModeSelectionState, Command, Notification> =
  reducer { action, state ->
    when (action) {
      is ModeSelectionAction.Selection.SelectMode -> {
        state { state.copy(showQuickOnlineDialog = false) }
        command(Command.SaveMode(action.mode))
        notify(Notification.ModeSelected(action.mode))
      }
    }
  }
