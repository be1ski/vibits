package space.be1ski.vibits.shared.feature.settings.presentation.reducer

import space.be1ski.vibits.shared.core.elm.Reducer
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.settings.presentation.action.SettingsAction
import space.be1ski.vibits.shared.feature.settings.presentation.effect.SettingsEffect
import space.be1ski.vibits.shared.feature.settings.presentation.state.SettingsState

@Suppress("LongMethod")
internal val saveAndLogsReducer: Reducer<SettingsAction.SaveAndLogs, SettingsState, SettingsEffect.Command, SettingsEffect.Notification> =
  reducer { action, state ->
    when (action) {
      is SettingsAction.SaveAndLogs.OpenLogs -> {
        state { copy(showLogsDialog = true) }
      }

      is SettingsAction.SaveAndLogs.CloseLogs -> {
        state { copy(showLogsDialog = false) }
      }

      is SettingsAction.SaveAndLogs.Save -> {
        if (state.appMode == AppMode.ONLINE) {
          val baseUrl = state.editBaseUrl.trim()
          val token = state.editToken.trim()
          if (baseUrl.isBlank() || token.isBlank()) {
            state { copy(validationError = "fill_all_fields") }
          } else {
            state { copy(isValidating = true, validationError = null, pendingSave = true) }
            command(SettingsEffect.Command.ValidateCredentials(baseUrl, token, AppMode.ONLINE))
          }
        } else {
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
