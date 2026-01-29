package space.be1ski.vibits.shared.feature.settings.presentation

import space.be1ski.vibits.shared.core.elm.Reducer
import space.be1ski.vibits.shared.feature.settings.presentation.reducer.dialogReducer
import space.be1ski.vibits.shared.feature.settings.presentation.reducer.inputReducer
import space.be1ski.vibits.shared.feature.settings.presentation.reducer.resetReducer
import space.be1ski.vibits.shared.feature.settings.presentation.reducer.saveAndLogsReducer
import space.be1ski.vibits.shared.feature.settings.presentation.reducer.validationReducer

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
