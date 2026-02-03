package space.be1ski.vibits.feature.mode.presentation.reducer

import space.be1ski.vibits.core.elm.Reducer
import space.be1ski.vibits.core.elm.reducer
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.mode.presentation.action.ModeSelectionAction
import space.be1ski.vibits.feature.mode.presentation.effect.ModeSelectionEffect
import space.be1ski.vibits.feature.mode.presentation.state.ModeSelectionError
import space.be1ski.vibits.feature.mode.presentation.state.ModeSelectionState

val modeSelectionReducer: Reducer<ModeSelectionAction, ModeSelectionState, ModeSelectionEffect.Command, ModeSelectionEffect.Notification> =
  reducer { action, state ->
    when (action) {
      is ModeSelectionAction.StoredCredentialsFound -> {
        state { state.copy(hasStoredCredentials = true, showQuickOnlineDialog = true) }
      }

      is ModeSelectionAction.StoredCredentialsNotFound -> {
        state { state.copy(hasStoredCredentials = false) }
      }

      is ModeSelectionAction.DismissQuickOnlineDialog -> {
        state { state.copy(showQuickOnlineDialog = false) }
      }

      is ModeSelectionAction.UseStoredCredentials -> {
        state { state.copy(showQuickOnlineDialog = false, isValidating = true) }
        command(ModeSelectionEffect.Command.UseStoredCredentialsWithValidation)
      }

      is ModeSelectionAction.ShowCredentialsDialog -> {
        state { state.copy(showCredentialsDialog = true, error = null) }
      }

      is ModeSelectionAction.DismissCredentialsDialog -> {
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

      is ModeSelectionAction.UpdateBaseUrl -> {
        state { state.copy(baseUrl = action.value, error = null) }
      }

      is ModeSelectionAction.UpdateToken -> {
        state { state.copy(token = action.value, error = null) }
      }

      is ModeSelectionAction.Submit -> {
        val baseUrl = state.baseUrl.trim()
        val token = state.token.trim()
        if (baseUrl.isBlank() || token.isBlank()) {
          state { state.copy(error = ModeSelectionError.FILL_ALL_FIELDS) }
        } else {
          state { state.copy(isValidating = true, error = null) }
          command(ModeSelectionEffect.Command.ValidateCredentials(baseUrl, token))
        }
      }

      is ModeSelectionAction.ValidationSucceeded -> {
        // Capture credentials before clearing state
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
        // Only save credentials if they were manually entered
        if (wasManuallyEntered) {
          command(ModeSelectionEffect.Command.SaveCredentials(capturedBaseUrl, capturedToken))
        }
        command(ModeSelectionEffect.Command.SaveMode(AppMode.ONLINE))
        notify(ModeSelectionEffect.Notification.ModeSelected(AppMode.ONLINE))
      }

      is ModeSelectionAction.ValidationFailed -> {
        state { state.copy(isValidating = false, error = ModeSelectionError.CONNECTION_FAILED) }
      }

      is ModeSelectionAction.SelectMode -> {
        state { state.copy(showQuickOnlineDialog = false) }
        command(ModeSelectionEffect.Command.SaveMode(action.mode))
        notify(ModeSelectionEffect.Notification.ModeSelected(action.mode))
      }
    }
  }
