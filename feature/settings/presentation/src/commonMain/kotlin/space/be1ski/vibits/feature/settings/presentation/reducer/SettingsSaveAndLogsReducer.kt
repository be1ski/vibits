package space.be1ski.vibits.feature.settings.presentation.reducer

import space.be1ski.vibits.core.elm.Reducer
import space.be1ski.vibits.core.elm.reducer
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.auth.domain.model.CredentialValidationError
import space.be1ski.vibits.feature.settings.presentation.action.SettingsAction
import space.be1ski.vibits.feature.settings.presentation.effect.SettingsEffect
import space.be1ski.vibits.feature.settings.presentation.state.SettingsState

internal val saveAndLogsReducer: Reducer<SettingsAction.SaveAndLogs, SettingsState, SettingsEffect.Command, SettingsEffect.Notification> =
  reducer { action, state ->
    when (action) {
      is SettingsAction.SaveAndLogs.OpenLogs -> {
        state { state.copy(showLogsDialog = true) }
      }

      is SettingsAction.SaveAndLogs.CloseLogs -> {
        state { state.copy(showLogsDialog = false) }
      }

      is SettingsAction.SaveAndLogs.Save -> {
        if (state.appMode == AppMode.ONLINE) {
          val baseUrl = state.editBaseUrl.trim()
          val token = state.editToken.trim()
          if (baseUrl.isBlank() || token.isBlank()) {
            state { state.copy(validationError = CredentialValidationError.FILL_ALL_FIELDS) }
          } else {
            state { state.copy(isValidating = true, validationError = null, pendingSave = true) }
            command(SettingsEffect.Command.ValidateCredentials(baseUrl, token, AppMode.ONLINE))
          }
        } else {
          state { state.copy(isOpen = false) }
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
