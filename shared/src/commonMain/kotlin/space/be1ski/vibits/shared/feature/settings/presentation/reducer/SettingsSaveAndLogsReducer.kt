package space.be1ski.vibits.shared.feature.settings.presentation.reducer

import space.be1ski.vibits.shared.core.elm.ReducerResult
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.settings.presentation.SettingsAction
import space.be1ski.vibits.shared.feature.settings.presentation.SettingsEffect
import space.be1ski.vibits.shared.feature.settings.presentation.SettingsState

@Suppress("LongMethod")
internal fun saveAndLogsReducer(
  action: SettingsAction.SaveAndLogs,
  state: SettingsState,
): ReducerResult<SettingsState, SettingsEffect.Command, SettingsEffect.Notification> =
  reducer<SettingsAction.SaveAndLogs, SettingsState, SettingsEffect.Command, SettingsEffect.Notification> { a, s ->
    when (a) {
      is SettingsAction.SaveAndLogs.OpenLogs -> {
        state { copy(showLogsDialog = true) }
      }

      is SettingsAction.SaveAndLogs.CloseLogs -> {
        state { copy(showLogsDialog = false) }
      }

      is SettingsAction.SaveAndLogs.Save -> {
        if (s.appMode == AppMode.ONLINE) {
          val baseUrl = s.editBaseUrl.trim()
          val token = s.editToken.trim()
          if (baseUrl.isBlank() || token.isBlank()) {
            state { copy(validationError = "fill_all_fields") }
          } else {
            state { copy(isValidating = true, validationError = null, pendingSave = true) }
            command(SettingsEffect.Command.ValidateCredentials(baseUrl, token, AppMode.ONLINE))
          }
        } else {
          state { copy(isOpen = false) }
          command(SettingsEffect.Command.SaveCredentials(s.editBaseUrl, s.editToken))
          command(SettingsEffect.Command.SwitchMode(s.appMode))
          command(SettingsEffect.Command.SaveLanguage(s.selectedLanguage))
          command(SettingsEffect.Command.SaveTheme(s.selectedTheme))
          notify(SettingsEffect.Notification.LanguageChanged(s.selectedLanguage))
          notify(SettingsEffect.Notification.ThemeChanged(s.selectedTheme))
          notify(SettingsEffect.Notification.CredentialsSaved(s.editBaseUrl, s.editToken))
        }
      }
    }
  }(action, state)
