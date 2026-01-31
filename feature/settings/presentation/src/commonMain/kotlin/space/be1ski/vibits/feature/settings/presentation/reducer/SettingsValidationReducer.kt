package space.be1ski.vibits.feature.settings.presentation.reducer

import space.be1ski.vibits.core.elm.Reducer
import space.be1ski.vibits.core.elm.reducer
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.settings.presentation.action.SettingsAction
import space.be1ski.vibits.feature.settings.presentation.effect.SettingsEffect
import space.be1ski.vibits.feature.settings.presentation.state.SettingsState

internal val validationReducer: Reducer<SettingsAction.Validation, SettingsState, SettingsEffect.Command, SettingsEffect.Notification> =
  reducer { action, state ->
    when (action) {
      is SettingsAction.Validation.ValidationSucceeded -> {
        state { state.copy(isValidating = false, isOpen = false, pendingSave = false, appMode = AppMode.ONLINE) }
        command(SettingsEffect.Command.SaveCredentials(state.editBaseUrl, state.editToken))
        command(SettingsEffect.Command.SwitchMode(AppMode.ONLINE))
        command(SettingsEffect.Command.SaveLanguage(state.selectedLanguage))
        command(SettingsEffect.Command.SaveTheme(state.selectedTheme))
        notify(SettingsEffect.Notification.LanguageChanged(state.selectedLanguage))
        notify(SettingsEffect.Notification.ThemeChanged(state.selectedTheme))
        notify(SettingsEffect.Notification.CredentialsSaved(state.editBaseUrl, state.editToken))
      }

      is SettingsAction.Validation.ValidationFailed -> {
        state { state.copy(isValidating = false, validationError = action.error, pendingSave = false) }
      }
    }
  }
