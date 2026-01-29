package space.be1ski.vibits.shared.feature.settings.presentation.reducer
import space.be1ski.vibits.shared.core.elm.Reducer
import space.be1ski.vibits.shared.feature.settings.presentation.action.SettingsAction
import space.be1ski.vibits.shared.feature.settings.presentation.effect.SettingsEffect
import space.be1ski.vibits.shared.feature.settings.presentation.state.SettingsState

val settingsReducer: Reducer<SettingsAction, SettingsState, SettingsEffect.Command, SettingsEffect.Notification> =
  { action, state ->
    when (action) {
      is SettingsAction.Dialog -> dialogReducer(action, state)
      is SettingsAction.Input -> inputReducer(action, state)
      is SettingsAction.Validation -> validationReducer(action, state)
      is SettingsAction.Reset -> resetReducer(action, state)
      is SettingsAction.SaveAndLogs -> saveAndLogsReducer(action, state)
    }
  }
