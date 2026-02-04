package space.be1ski.vibits.feature.mode.presentation.reducer

import space.be1ski.vibits.core.elm.Reducer
import space.be1ski.vibits.core.elm.reducer
import space.be1ski.vibits.feature.mode.presentation.action.ModeSelectionAction
import space.be1ski.vibits.feature.mode.presentation.effect.ModeSelectionEffect.Command
import space.be1ski.vibits.feature.mode.presentation.effect.ModeSelectionEffect.Notification
import space.be1ski.vibits.feature.mode.presentation.state.ModeSelectionState

internal val quickOnlineReducer: Reducer<ModeSelectionAction.QuickOnline, ModeSelectionState, Command, Notification> =
  reducer { action, state ->
    when (action) {
      is ModeSelectionAction.QuickOnline.Dismiss -> {
        state { state.copy(showQuickOnlineDialog = false) }
      }

      is ModeSelectionAction.QuickOnline.UseStoredCredentials -> {
        state { state.copy(showQuickOnlineDialog = false, isValidating = true) }
        command(Command.UseStoredCredentialsWithValidation)
      }
    }
  }
