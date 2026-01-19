package space.be1ski.vibits.shared.feature.settings.presentation

import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.settings.domain.model.AppLanguage
import space.be1ski.vibits.shared.feature.settings.domain.model.AppTheme

sealed interface SettingsAction {
  // Dialog lifecycle
  data class Open(
    val baseUrl: String,
    val token: String,
    val appMode: AppMode,
    val language: AppLanguage,
    val theme: AppTheme,
  ) : SettingsAction

  data object Close : SettingsAction

  data object Dismiss : SettingsAction

  // Credentials
  data class UpdateBaseUrl(
    val value: String,
  ) : SettingsAction

  data class UpdateToken(
    val value: String,
  ) : SettingsAction

  // Mode selection
  data class SelectMode(
    val mode: AppMode,
  ) : SettingsAction

  // Language selection
  data class SelectLanguage(
    val language: AppLanguage,
  ) : SettingsAction

  // Theme selection
  data class SelectTheme(
    val theme: AppTheme,
  ) : SettingsAction

  // Validation responses
  data object ValidationSucceeded : SettingsAction

  data class ValidationFailed(
    val error: String,
  ) : SettingsAction

  data object ModeSwitched : SettingsAction

  // Reset flow
  data object RequestReset : SettingsAction

  data object ConfirmReset : SettingsAction

  data object CancelReset : SettingsAction

  data object ResetCompleted : SettingsAction

  // Logs
  data object OpenLogs : SettingsAction

  data object CloseLogs : SettingsAction

  // Save
  data object Save : SettingsAction
}
