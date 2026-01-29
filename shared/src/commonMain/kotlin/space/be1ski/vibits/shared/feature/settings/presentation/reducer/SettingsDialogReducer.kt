package space.be1ski.vibits.shared.feature.settings.presentation.reducer

import space.be1ski.vibits.shared.core.elm.ReducerResult
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.feature.settings.presentation.action.SettingsAction
import space.be1ski.vibits.shared.feature.settings.presentation.effect.SettingsEffect
import space.be1ski.vibits.shared.feature.settings.presentation.state.SettingsState

internal fun dialogReducer(
  action: SettingsAction.Dialog,
  state: SettingsState,
): ReducerResult<SettingsState, SettingsEffect.Command, SettingsEffect.Notification> =
  reducer<SettingsAction.Dialog, SettingsState, SettingsEffect.Command, SettingsEffect.Notification> { a, s ->
    when (a) {
      is SettingsAction.Dialog.Open -> {
        state {
          copy(
            isOpen = true,
            editBaseUrl = a.baseUrl,
            editToken = a.token,
            appMode = a.appMode,
            selectedLanguage = a.language,
            languageChanged = false,
            selectedTheme = a.theme,
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
  }(action, state)
