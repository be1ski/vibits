package space.be1ski.vibits.shared.feature.settings.presentation.reducer

import space.be1ski.vibits.shared.core.elm.Reducer
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.feature.settings.presentation.action.SettingsAction
import space.be1ski.vibits.shared.feature.settings.presentation.effect.SettingsEffect
import space.be1ski.vibits.shared.feature.settings.presentation.state.SettingsState

internal val dialogReducer: Reducer<SettingsAction.Dialog, SettingsState, SettingsEffect.Command, SettingsEffect.Notification> =
  reducer { action, state ->
    when (action) {
      is SettingsAction.Dialog.Open -> {
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

      is SettingsAction.Dialog.Close -> {
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

      is SettingsAction.Dialog.Dismiss -> {
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
    }
  }
