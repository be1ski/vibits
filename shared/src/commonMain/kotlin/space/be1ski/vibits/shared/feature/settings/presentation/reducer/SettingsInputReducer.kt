package space.be1ski.vibits.shared.feature.settings.presentation.reducer

import space.be1ski.vibits.shared.core.elm.ReducerResult
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.feature.settings.presentation.SettingsAction
import space.be1ski.vibits.shared.feature.settings.presentation.SettingsEffect
import space.be1ski.vibits.shared.feature.settings.presentation.SettingsState

internal fun inputReducer(
  action: SettingsAction.Input,
  state: SettingsState,
): ReducerResult<SettingsState, SettingsEffect.Command, SettingsEffect.Notification> =
  reducer<SettingsAction.Input, SettingsState, SettingsEffect.Command, SettingsEffect.Notification> { a, s ->
    when (a) {
      is SettingsAction.Input.UpdateBaseUrl -> {
        state { copy(editBaseUrl = a.value, validationError = null) }
      }

      is SettingsAction.Input.UpdateToken -> {
        state { copy(editToken = a.value, validationError = null) }
      }

      is SettingsAction.Input.SelectMode -> {
        state { copy(appMode = a.mode, validationError = null) }
      }

      is SettingsAction.Input.SelectLanguage -> {
        state { copy(selectedLanguage = a.language, languageChanged = true) }
      }

      is SettingsAction.Input.SelectTheme -> {
        state { copy(selectedTheme = a.theme) }
      }

      is SettingsAction.Input.ModeSwitched -> {
        notify(SettingsEffect.Notification.ModeChanged(s.appMode))
      }
    }
  }(action, state)
