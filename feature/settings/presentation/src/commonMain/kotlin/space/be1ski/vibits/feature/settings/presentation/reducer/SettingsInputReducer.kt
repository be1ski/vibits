package space.be1ski.vibits.feature.settings.presentation.reducer

import space.be1ski.vibits.core.elm.Reducer
import space.be1ski.vibits.core.elm.reducer
import space.be1ski.vibits.feature.settings.presentation.action.SettingsAction
import space.be1ski.vibits.feature.settings.presentation.effect.SettingsEffect
import space.be1ski.vibits.feature.settings.presentation.state.SettingsState

internal val inputReducer: Reducer<SettingsAction.Input, SettingsState, SettingsEffect.Command, SettingsEffect.Notification> =
  reducer { action, state ->
    when (action) {
      is SettingsAction.Input.UpdateBaseUrl -> {
        state { state.copy(editBaseUrl = action.value, validationError = null) }
      }

      is SettingsAction.Input.UpdateToken -> {
        state { state.copy(editToken = action.value, validationError = null) }
      }

      is SettingsAction.Input.SelectMode -> {
        state { state.copy(appMode = action.mode, validationError = null) }
      }

      is SettingsAction.Input.SelectLanguage -> {
        state { state.copy(selectedLanguage = action.language, languageChanged = true) }
      }

      is SettingsAction.Input.SelectTheme -> {
        state { state.copy(selectedTheme = action.theme) }
      }

      is SettingsAction.Input.ModeSwitched -> {
        notify(SettingsEffect.Notification.ModeChanged(state.appMode))
      }
    }
  }
