package space.be1ski.vibits.shared.feature.mode.presentation

import space.be1ski.vibits.shared.core.elm.Reducer
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode

val modeSelectionReducer: Reducer<ModeSelectionAction, ModeSelectionState, ModeSelectionEffect.Command, ModeSelectionEffect.Notification> =
  reducer { action, state ->
    when (action) {
      is ModeSelectionAction.StoredCredentialsFound -> {
        state { copy(hasStoredCredentials = true, showQuickOnlineDialog = true) }
      }

      is ModeSelectionAction.StoredCredentialsNotFound -> {
        state { copy(hasStoredCredentials = false) }
      }

      is ModeSelectionAction.DismissQuickOnlineDialog -> {
        state { copy(showQuickOnlineDialog = false) }
      }

      is ModeSelectionAction.UseStoredCredentials -> {
        state { copy(showQuickOnlineDialog = false, isValidating = true) }
        command(ModeSelectionEffect.Command.UseStoredCredentialsWithValidation)
      }

      is ModeSelectionAction.ShowCredentialsDialog -> {
        state { copy(showCredentialsDialog = true, error = null) }
      }

      is ModeSelectionAction.DismissCredentialsDialog -> {
        state {
          copy(
            showCredentialsDialog = false,
            baseUrl = "",
            token = "",
            isValidating = false,
            error = null,
          )
        }
      }

      is ModeSelectionAction.UpdateBaseUrl -> {
        state { copy(baseUrl = action.value, error = null) }
      }

      is ModeSelectionAction.UpdateToken -> {
        state { copy(token = action.value, error = null) }
      }

      is ModeSelectionAction.Submit -> {
        val baseUrl = state.baseUrl.trim()
        val token = state.token.trim()
        if (baseUrl.isBlank() || token.isBlank()) {
          state { copy(error = ModeSelectionError.FILL_ALL_FIELDS) }
        } else {
          state { copy(isValidating = true, error = null) }
          command(ModeSelectionEffect.Command.ValidateCredentials(baseUrl, token))
        }
      }

      is ModeSelectionAction.ValidationSucceeded -> {
        // Capture credentials before clearing state
        val wasManuallyEntered = state.baseUrl.isNotBlank() && state.token.isNotBlank()
        val capturedBaseUrl = state.baseUrl.trim()
        val capturedToken = state.token.trim()

        state {
          copy(
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
        state { copy(isValidating = false, error = ModeSelectionError.CONNECTION_FAILED) }
      }

      is ModeSelectionAction.SelectMode -> {
        state { copy(showQuickOnlineDialog = false) }
        command(ModeSelectionEffect.Command.SaveMode(action.mode))
        notify(ModeSelectionEffect.Notification.ModeSelected(action.mode))
      }
    }
  }
