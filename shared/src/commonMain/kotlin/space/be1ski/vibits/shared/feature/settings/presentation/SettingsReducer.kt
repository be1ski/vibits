@file:Suppress("CyclomaticComplexMethod")

package space.be1ski.vibits.shared.feature.settings.presentation

import space.be1ski.vibits.shared.core.elm.Reducer
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode

val settingsReducer: Reducer<SettingsAction, SettingsState, SettingsEffect> = reducer { action, state ->
  when (action) {
    // Dialog lifecycle
    is SettingsAction.Open -> {
      state {
        copy(
          isOpen = true,
          editBaseUrl = action.baseUrl,
          editToken = action.token,
          appMode = action.appMode,
          isValidating = false,
          validationError = null,
          showResetConfirmation = false,
          isResetting = false,
          showLogsDialog = false
        )
      }
    }

    is SettingsAction.Close -> {
      state {
        copy(
          isOpen = false,
          isValidating = false,
          validationError = null,
          showResetConfirmation = false,
          showLogsDialog = false
        )
      }
      effect(SettingsEffect.NotifyDialogClosed)
    }

    is SettingsAction.Dismiss -> {
      state {
        copy(
          isOpen = false,
          isValidating = false,
          validationError = null,
          showResetConfirmation = false,
          showLogsDialog = false
        )
      }
      effect(SettingsEffect.NotifyDialogClosed)
    }

    // Credentials
    is SettingsAction.UpdateBaseUrl -> {
      state { copy(editBaseUrl = action.value, validationError = null) }
      effect(SettingsEffect.SaveCredentials(action.value, state.editToken))
    }

    is SettingsAction.UpdateToken -> {
      state { copy(editToken = action.value, validationError = null) }
      effect(SettingsEffect.SaveCredentials(state.editBaseUrl, action.value))
    }

    // Mode selection
    is SettingsAction.SelectMode -> {
      if (action.mode == AppMode.ONLINE) {
        val baseUrl = state.editBaseUrl.trim()
        val token = state.editToken.trim()
        if (baseUrl.isBlank() || token.isBlank()) {
          // Just show the fields, don't validate yet - user needs to fill them first
          state { copy(appMode = AppMode.ONLINE, validationError = null) }
        } else {
          state { copy(isValidating = true, validationError = null) }
          effect(SettingsEffect.ValidateCredentials(baseUrl, token, action.mode))
        }
      } else {
        state { copy(appMode = action.mode, validationError = null) }
        effect(SettingsEffect.SwitchMode(action.mode))
      }
    }

    // Validation responses
    is SettingsAction.ValidationSucceeded -> {
      if (state.pendingSave) {
        // Validation succeeded after Save - close dialog and notify
        state { copy(isValidating = false, isOpen = false, pendingSave = false, appMode = AppMode.ONLINE) }
        effect(SettingsEffect.SwitchMode(AppMode.ONLINE))
        effect(SettingsEffect.NotifyCredentialsSaved(state.editBaseUrl, state.editToken))
      } else {
        // Validation succeeded after mode selection - just switch mode
        state { copy(isValidating = false, appMode = AppMode.ONLINE) }
        effect(SettingsEffect.SwitchMode(AppMode.ONLINE))
      }
    }

    is SettingsAction.ValidationFailed -> {
      state { copy(isValidating = false, validationError = action.error, pendingSave = false) }
    }

    is SettingsAction.ModeSwitched -> {
      effect(SettingsEffect.NotifyModeChanged(state.appMode))
    }

    // Reset flow
    is SettingsAction.RequestReset -> {
      state { copy(showResetConfirmation = true) }
    }

    is SettingsAction.ConfirmReset -> {
      state { copy(showResetConfirmation = false, isResetting = true) }
      effect(SettingsEffect.ResetApp)
    }

    is SettingsAction.CancelReset -> {
      state { copy(showResetConfirmation = false) }
    }

    is SettingsAction.ResetCompleted -> {
      state { copy(isOpen = false, isResetting = false) }
      effect(SettingsEffect.NotifyResetCompleted)
    }

    // Logs
    is SettingsAction.OpenLogs -> {
      state { copy(showLogsDialog = true) }
    }

    is SettingsAction.CloseLogs -> {
      state { copy(showLogsDialog = false) }
    }

    // Save
    is SettingsAction.Save -> {
      if (state.appMode == AppMode.ONLINE) {
        val baseUrl = state.editBaseUrl.trim()
        val token = state.editToken.trim()
        if (baseUrl.isBlank() || token.isBlank()) {
          state { copy(validationError = "fill_all_fields") }
        } else {
          // Validate before saving in Online mode
          state { copy(isValidating = true, validationError = null, pendingSave = true) }
          effect(SettingsEffect.ValidateCredentials(baseUrl, token, AppMode.ONLINE))
        }
      } else {
        state { copy(isOpen = false) }
        effect(SettingsEffect.NotifyCredentialsSaved(state.editBaseUrl, state.editToken))
      }
    }
  }
}
