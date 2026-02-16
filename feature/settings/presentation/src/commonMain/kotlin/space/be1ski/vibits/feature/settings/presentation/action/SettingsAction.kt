package space.be1ski.vibits.feature.settings.presentation.action

import space.be1ski.vibits.core.elm.Action
import space.be1ski.vibits.core.platform.locale.AppLanguage
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.core.ui.form.CredentialValidationError
import space.be1ski.vibits.feature.settings.domain.model.AppTheme

sealed interface SettingsAction : Action {
  /**
   * Dialog lifecycle.
   */
  sealed interface Dialog : SettingsAction {
    data class Open(
      val baseUrl: String,
      val token: String,
      val appMode: AppMode,
      val language: AppLanguage,
      val theme: AppTheme,
      val syncDebounceSeconds: Int,
    ) : Dialog

    data object Close : Dialog

    data object Dismiss : Dialog
  }

  /**
   * Input (credentials, mode, language, theme).
   */
  sealed interface Input : SettingsAction {
    data class UpdateBaseUrl(
      val value: String,
    ) : Input

    data class UpdateToken(
      val value: String,
    ) : Input

    data class SelectMode(
      val mode: AppMode,
    ) : Input

    data class SelectLanguage(
      val language: AppLanguage,
    ) : Input

    data class SelectTheme(
      val theme: AppTheme,
    ) : Input

    data class SelectSyncDebounce(
      val seconds: Int,
    ) : Input

    data object ModeSwitched : Input
  }

  /**
   * Validation flow.
   */
  sealed interface Validation : SettingsAction {
    data object ValidationSucceeded : Validation

    data class ValidationFailed(
      val error: CredentialValidationError,
    ) : Validation
  }

  /**
   * Reset flow.
   */
  sealed interface Reset : SettingsAction {
    data object RequestReset : Reset

    data object ConfirmReset : Reset

    data object ConfirmResetWithMemos : Reset

    data object CancelReset : Reset

    data object ResetCompleted : Reset
  }

  /**
   * Save and logs.
   */
  sealed interface SaveAndLogs : SettingsAction {
    data object OpenLogs : SaveAndLogs

    data object CloseLogs : SaveAndLogs

    data object Save : SaveAndLogs
  }
}
