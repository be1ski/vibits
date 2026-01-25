package space.be1ski.vibits.shared.feature.mode.presentation

import space.be1ski.vibits.shared.core.elm.Reducer
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode

val modeSelectionReducer: Reducer<ModeSelectionAction, ModeSelectionState, ModeSelectionEffect> =
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
        effect(ModeSelectionEffect.UseStoredCredentialsWithValidation)
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
          effect(ModeSelectionEffect.ValidateCredentials(baseUrl, token))
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
          effect(ModeSelectionEffect.SaveCredentials(capturedBaseUrl, capturedToken))
        }
        effect(ModeSelectionEffect.SaveMode(AppMode.ONLINE))
        effect(ModeSelectionEffect.NotifyModeSelected(AppMode.ONLINE))
      }

      is ModeSelectionAction.ValidationFailed -> {
        state { copy(isValidating = false, error = ModeSelectionError.CONNECTION_FAILED) }
      }

      is ModeSelectionAction.SelectMode -> {
        state { copy(showQuickOnlineDialog = false) }
        effect(ModeSelectionEffect.SaveMode(action.mode))
        effect(ModeSelectionEffect.NotifyModeSelected(action.mode))
      }
    }
  }
