@file:Suppress("CyclomaticComplexMethod")

package space.be1ski.vibits.shared.feature.settings.presentation

import space.be1ski.vibits.shared.core.elm.Reducer
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode

val settingsReducer: Reducer<SettingsAction, SettingsState, SettingsEffect.Command, SettingsEffect.Notification> =
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
        notify(SettingsEffect.Notification.DialogClosed)
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
        notify(SettingsEffect.Notification.DialogClosed)
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
        command(SettingsEffect.Command.SaveCredentials(state.editBaseUrl, state.editToken))
        command(SettingsEffect.Command.SwitchMode(AppMode.ONLINE))
        command(SettingsEffect.Command.SaveLanguage(state.selectedLanguage))
        command(SettingsEffect.Command.SaveTheme(state.selectedTheme))
        notify(SettingsEffect.Notification.LanguageChanged(state.selectedLanguage))
        notify(SettingsEffect.Notification.ThemeChanged(state.selectedTheme))
        notify(SettingsEffect.Notification.CredentialsSaved(state.editBaseUrl, state.editToken))
      }

      is SettingsAction.ValidationFailed -> {
        state { copy(isValidating = false, validationError = action.error, pendingSave = false) }
      }

      is SettingsAction.ModeSwitched -> {
        notify(SettingsEffect.Notification.ModeChanged(state.appMode))
      }

      // Reset flow
      is SettingsAction.RequestReset -> {
        state { copy(showResetConfirmation = true) }
      }

      is SettingsAction.ConfirmReset -> {
        state { copy(showResetConfirmation = false, isResetting = true) }
        command(SettingsEffect.Command.ResetApp)
      }

      is SettingsAction.ConfirmResetWithMemos -> {
        state { copy(showResetConfirmation = false, isResetting = true) }
        command(SettingsEffect.Command.ResetAppWithMemos)
      }

      is SettingsAction.CancelReset -> {
        state { copy(showResetConfirmation = false) }
      }

      is SettingsAction.ResetCompleted -> {
        state { copy(isOpen = false, isResetting = false) }
        notify(SettingsEffect.Notification.ResetCompleted)
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
            command(SettingsEffect.Command.ValidateCredentials(baseUrl, token, AppMode.ONLINE))
          }
        } else {
          // Save all settings and close dialog
          state { copy(isOpen = false) }
          command(SettingsEffect.Command.SaveCredentials(state.editBaseUrl, state.editToken))
          command(SettingsEffect.Command.SwitchMode(state.appMode))
          command(SettingsEffect.Command.SaveLanguage(state.selectedLanguage))
          command(SettingsEffect.Command.SaveTheme(state.selectedTheme))
          notify(SettingsEffect.Notification.LanguageChanged(state.selectedLanguage))
          notify(SettingsEffect.Notification.ThemeChanged(state.selectedTheme))
          notify(SettingsEffect.Notification.CredentialsSaved(state.editBaseUrl, state.editToken))
        }
      }
    }
  }
