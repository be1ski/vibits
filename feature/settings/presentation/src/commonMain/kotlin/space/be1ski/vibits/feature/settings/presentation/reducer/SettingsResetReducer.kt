package space.be1ski.vibits.feature.settings.presentation.reducer

import space.be1ski.vibits.core.elm.Reducer
import space.be1ski.vibits.core.elm.reducer
import space.be1ski.vibits.feature.settings.presentation.action.SettingsAction
import space.be1ski.vibits.feature.settings.presentation.effect.SettingsEffect
import space.be1ski.vibits.feature.settings.presentation.state.SettingsState

@Suppress("LongMethod")
internal val resetReducer: Reducer<SettingsAction.Reset, SettingsState, SettingsEffect.Command, SettingsEffect.Notification> =
  reducer { action, state ->
    when (action) {
      is SettingsAction.Reset.RequestReset -> {
        state { state.copy(showResetConfirmation = true) }
      }

      is SettingsAction.Reset.ConfirmReset -> {
        state { state.copy(showResetConfirmation = false, isResetting = true) }
        command(SettingsEffect.Command.ResetApp)
      }

      is SettingsAction.Reset.ConfirmResetWithMemos -> {
        state { state.copy(showResetConfirmation = false, isResetting = true) }
        command(SettingsEffect.Command.ResetAppWithMemos)
      }

      is SettingsAction.Reset.CancelReset -> {
        state { state.copy(showResetConfirmation = false) }
      }

      is SettingsAction.Reset.ResetCompleted -> {
        state { state.copy(isOpen = false, isResetting = false) }
        notify(SettingsEffect.Notification.ResetCompleted)
      }
    }
  }
