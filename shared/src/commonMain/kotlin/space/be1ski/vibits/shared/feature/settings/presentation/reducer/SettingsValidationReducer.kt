package space.be1ski.vibits.shared.feature.settings.presentation.reducer

import space.be1ski.vibits.shared.core.elm.ReducerResult
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.settings.presentation.action.SettingsAction
import space.be1ski.vibits.shared.feature.settings.presentation.effect.SettingsEffect
import space.be1ski.vibits.shared.feature.settings.presentation.state.SettingsState

internal fun validationReducer(
  action: SettingsAction.Validation,
  state: SettingsState,
): ReducerResult<SettingsState, SettingsEffect.Command, SettingsEffect.Notification> =
  reducer<SettingsAction.Validation, SettingsState, SettingsEffect.Command, SettingsEffect.Notification> { a, s ->
    when (a) {
      is SettingsAction.Validation.ValidationSucceeded -> {
        state { copy(isValidating = false, isOpen = false, pendingSave = false, appMode = AppMode.ONLINE) }
        command(SettingsEffect.Command.SaveCredentials(s.editBaseUrl, s.editToken))
        command(SettingsEffect.Command.SwitchMode(AppMode.ONLINE))
        command(SettingsEffect.Command.SaveLanguage(s.selectedLanguage))
        command(SettingsEffect.Command.SaveTheme(s.selectedTheme))
        notify(SettingsEffect.Notification.LanguageChanged(s.selectedLanguage))
        notify(SettingsEffect.Notification.ThemeChanged(s.selectedTheme))
        notify(SettingsEffect.Notification.CredentialsSaved(s.editBaseUrl, s.editToken))
      }

      is SettingsAction.Validation.ValidationFailed -> {
        state { copy(isValidating = false, validationError = a.error, pendingSave = false) }
      }
    }
  }(action, state)
