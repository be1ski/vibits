package space.be1ski.vibits.shared.feature.settings.presentation

import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.settings.domain.model.AppLanguage
import space.be1ski.vibits.shared.feature.settings.domain.model.AppTheme

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
    val language: AppLanguage,
  ) : SettingsEffect

  data class NotifyThemeChanged(
    val theme: AppTheme,
  ) : SettingsEffect

  data object NotifyDialogClosed : SettingsEffect
}
