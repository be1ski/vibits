package space.be1ski.vibits.shared.feature.settings.presentation

import space.be1ski.vibits.shared.domain.model.app.AppDetails
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.preferences.domain.model.AppLanguage
import space.be1ski.vibits.shared.feature.preferences.domain.model.AppTheme

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

data class SettingsState(
  val isOpen: Boolean = false,
  val editBaseUrl: String = "",
  val editToken: String = "",
  val appMode: AppMode = AppMode.NOT_SELECTED,
  val selectedLanguage: AppLanguage = AppLanguage.SYSTEM,
  val languageChanged: Boolean = false,
  val selectedTheme: AppTheme = AppTheme.SYSTEM,
  val isValidating: Boolean = false,
  val validationError: String? = null,
  val showResetConfirmation: Boolean = false,
  val isResetting: Boolean = false,
  val showLogsDialog: Boolean = false,
  val appDetails: AppDetails? = null,
  val pendingSave: Boolean = false,
)

sealed interface SettingsEffect {
  // Async operations (handled by EffectHandler)
  data class ValidateCredentials(
    val baseUrl: String,
    val token: String,
    val targetMode: AppMode,
  ) : SettingsEffect

  data class SwitchMode(
    val mode: AppMode,
  ) : SettingsEffect

  data class SaveCredentials(
    val baseUrl: String,
    val token: String,
  ) : SettingsEffect

  data object ResetApp : SettingsEffect

  data class SaveLanguage(
    val language: AppLanguage,
  ) : SettingsEffect

  data class SaveTheme(
    val theme: AppTheme,
  ) : SettingsEffect

  // Parent notifications (observed by VibitsApp)
  data class NotifyModeChanged(
    val newMode: AppMode,
  ) : SettingsEffect

  data object NotifyResetCompleted : SettingsEffect

  data class NotifyCredentialsSaved(
    val baseUrl: String,
    val token: String,
  ) : SettingsEffect

  data class NotifyLanguageChanged(
    val requiresRestart: Boolean,
  ) : SettingsEffect

  data class NotifyThemeChanged(
    val theme: AppTheme,
  ) : SettingsEffect

  data object NotifyDialogClosed : SettingsEffect
}
