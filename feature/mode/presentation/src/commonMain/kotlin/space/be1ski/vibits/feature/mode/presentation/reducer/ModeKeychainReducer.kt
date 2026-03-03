package space.be1ski.vibits.feature.mode.presentation.reducer

import space.be1ski.vibits.core.elm.Reducer
import space.be1ski.vibits.core.elm.reducer
import space.be1ski.vibits.core.ui.form.CredentialValidationError
import space.be1ski.vibits.feature.mode.presentation.action.ModeSelectionAction
import space.be1ski.vibits.feature.mode.presentation.effect.ModeSelectionEffect.Command
import space.be1ski.vibits.feature.mode.presentation.effect.ModeSelectionEffect.Notification
import space.be1ski.vibits.feature.mode.presentation.state.ModeSelectionState

internal val keychainReducer:
  Reducer<ModeSelectionAction.Keychain, ModeSelectionState, Command, Notification> =
  reducer { action, state ->
    when (action) {
      is ModeSelectionAction.Keychain.Restore -> {
        state { state.copy(isValidating = true, error = null) }
        command(Command.LoadFromKeychain)
      }

      is ModeSelectionAction.Keychain.Loaded -> {
        state {
          state.copy(
            baseUrl = action.baseUrl,
            token = action.token,
            isValidating = false,
            error = null,
          )
        }
      }

      is ModeSelectionAction.Keychain.NotFound -> {
        state { state.copy(isValidating = false, error = CredentialValidationError.CONNECTION_FAILED) }
      }
    }
  }
