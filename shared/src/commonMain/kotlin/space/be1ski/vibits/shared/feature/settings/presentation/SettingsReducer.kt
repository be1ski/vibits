@file:Suppress("CyclomaticComplexMethod")

package space.be1ski.vibits.shared.feature.settings.presentation

import space.be1ski.vibits.shared.core.elm.Reducer
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode

val settingsReducer: Reducer<SettingsAction, SettingsState, SettingsEffect> =
  reducer { action, state ->
    when (action) {
      // Dialog lifecycle
      is SettingsAction.Open -> {
        state {
          copy(
            isOpen = true,
            editBaseUrl = action.baseUrl,
            editToken = action.token,
            appMode = action.appMode,
            selectedLanguage = action.language,
            languageChanged = false,
            selectedTheme = action.theme,
            isValidating = false,
            validationError = null,
            showResetConfirmation = false,
            isResetting = false,
            showLogsDialog = false,
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
            showLogsDialog = false,
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
            showLogsDialog = false,
          )
        }
        effect(SettingsEffect.NotifyDialogClosed)
      }

      // Credentials - just update local state, save on Save action
      is SettingsAction.UpdateBaseUrl -> {
        state { copy(editBaseUrl = action.value, validationError = null) }
      }

      is SettingsAction.UpdateToken -> {
        state { copy(editToken = action.value, validationError = null) }
      }

      // Mode selection - update local state only, actual switch happens on Save
      is SettingsAction.SelectMode -> {
        state { copy(appMode = action.mode, validationError = null) }
      }

      // Language selection - just update local state, save on Save action
      is SettingsAction.SelectLanguage -> {
        state { copy(selectedLanguage = action.language, languageChanged = true) }
      }

      // Theme selection - just update local state, save on Save action
      is SettingsAction.SelectTheme -> {
        state { copy(selectedTheme = action.theme) }
      }

      // Validation responses
      is SettingsAction.ValidationSucceeded -> {
        // Validation succeeded - save all settings and close dialog
        state { copy(isValidating = false, isOpen = false, pendingSave = false, appMode = AppMode.ONLINE) }
        effect(SettingsEffect.SaveCredentials(state.editBaseUrl, state.editToken))
        effect(SettingsEffect.SwitchMode(AppMode.ONLINE))
        effect(SettingsEffect.SaveLanguage(state.selectedLanguage))
        effect(SettingsEffect.SaveTheme(state.selectedTheme))
        effect(SettingsEffect.NotifyLanguageChanged(state.selectedLanguage))
        effect(SettingsEffect.NotifyThemeChanged(state.selectedTheme))
        effect(SettingsEffect.NotifyCredentialsSaved(state.editBaseUrl, state.editToken))
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

      // Save - apply all pending changes
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
          // Save all settings and close dialog
          state { copy(isOpen = false) }
          effect(SettingsEffect.SaveCredentials(state.editBaseUrl, state.editToken))
          effect(SettingsEffect.SwitchMode(state.appMode))
          effect(SettingsEffect.SaveLanguage(state.selectedLanguage))
          effect(SettingsEffect.SaveTheme(state.selectedTheme))
          effect(SettingsEffect.NotifyLanguageChanged(state.selectedLanguage))
          effect(SettingsEffect.NotifyThemeChanged(state.selectedTheme))
          effect(SettingsEffect.NotifyCredentialsSaved(state.editBaseUrl, state.editToken))
        }
      }
    }
  }
