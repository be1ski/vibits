package space.be1ski.vibits.shared.feature.settings.presentation.reducer

import space.be1ski.vibits.shared.core.elm.ReducerResult
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.feature.settings.presentation.action.SettingsAction
import space.be1ski.vibits.shared.feature.settings.presentation.effect.SettingsEffect
import space.be1ski.vibits.shared.feature.settings.presentation.state.SettingsState

@Suppress("LongMethod")
internal fun resetReducer(
  action: SettingsAction.Reset,
  state: SettingsState,
): ReducerResult<SettingsState, SettingsEffect.Command, SettingsEffect.Notification> =
  reducer<SettingsAction.Reset, SettingsState, SettingsEffect.Command, SettingsEffect.Notification> { a, s ->
    when (a) {
      is SettingsAction.Reset.RequestReset -> {
        state { copy(showResetConfirmation = true) }
      }

      is SettingsAction.Reset.ConfirmReset -> {
        state { copy(showResetConfirmation = false, isResetting = true) }
        command(SettingsEffect.Command.ResetApp)
      }

      is SettingsAction.Reset.ConfirmResetWithMemos -> {
        state { copy(showResetConfirmation = false, isResetting = true) }
        command(SettingsEffect.Command.ResetAppWithMemos)
      }

      is SettingsAction.Reset.CancelReset -> {
        state { copy(showResetConfirmation = false) }
      }

      is SettingsAction.Reset.ResetCompleted -> {
        state { copy(isOpen = false, isResetting = false) }
        notify(SettingsEffect.Notification.ResetCompleted)
      }
    }
  }(action, state)
