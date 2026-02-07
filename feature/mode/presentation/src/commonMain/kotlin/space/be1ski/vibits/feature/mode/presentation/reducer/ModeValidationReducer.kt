package space.be1ski.vibits.feature.mode.presentation.reducer

import space.be1ski.vibits.core.elm.Reducer
import space.be1ski.vibits.core.elm.reducer
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.core.ui.form.CredentialValidationError
import space.be1ski.vibits.feature.mode.presentation.action.ModeSelectionAction
import space.be1ski.vibits.feature.mode.presentation.effect.ModeSelectionEffect.Command
import space.be1ski.vibits.feature.mode.presentation.effect.ModeSelectionEffect.Notification
import space.be1ski.vibits.feature.mode.presentation.state.ModeSelectionState

internal val validationReducer: Reducer<ModeSelectionAction.Validation, ModeSelectionState, Command, Notification> =
  reducer { action, state ->
    when (action) {
      is ModeSelectionAction.Validation.Submit -> {
        val baseUrl = state.baseUrl.trim()
        val token = state.token.trim()
        if (baseUrl.isBlank() || token.isBlank()) {
          state { state.copy(error = CredentialValidationError.FILL_ALL_FIELDS) }
        } else {
          state { state.copy(isValidating = true, error = null) }
          command(Command.ValidateCredentials(baseUrl, token))
        }
      }

      is ModeSelectionAction.Validation.Succeeded -> {
        val wasManuallyEntered = state.baseUrl.isNotBlank() && state.token.isNotBlank()
        val capturedBaseUrl = state.baseUrl.trim()
        val capturedToken = state.token.trim()

        state {
          state.copy(
            showCredentialsDialog = false,
            isValidating = false,
            baseUrl = "",
            token = "",
            error = null,
          )
        }
        if (wasManuallyEntered) {
          command(Command.SaveCredentials(capturedBaseUrl, capturedToken))
        }
        command(Command.SaveMode(AppMode.ONLINE))
        notify(Notification.ModeSelected(AppMode.ONLINE))
      }

      is ModeSelectionAction.Validation.Failed -> {
        state { state.copy(isValidating = false, error = CredentialValidationError.CONNECTION_FAILED) }
      }
    }
  }
