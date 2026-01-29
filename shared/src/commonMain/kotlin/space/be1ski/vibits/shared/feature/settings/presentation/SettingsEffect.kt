package space.be1ski.vibits.shared.feature.settings.presentation

import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.settings.domain.model.AppLanguage
import space.be1ski.vibits.shared.feature.settings.domain.model.AppTheme

sealed interface SettingsEffect {
  /**
   * Commands handled by SettingsEffectHandler (async operations).
   */
  sealed interface Command : SettingsEffect {
    data class ValidateCredentials(
      val baseUrl: String,
      val token: String,
      val targetMode: AppMode,
    ) : Command

    data class SwitchMode(
      val mode: AppMode,
    ) : Command

    data class SaveCredentials(
      val baseUrl: String,
      val token: String,
    ) : Command

    data object ResetApp : Command

    data object ResetAppWithMemos : Command

    data class SaveLanguage(
      val language: AppLanguage,
    ) : Command

    data class SaveTheme(
      val theme: AppTheme,
    ) : Command
  }

  /**
   * Notifications observed by FeatureCoordinator for cross-feature coordination.
   */
  sealed interface Notification : SettingsEffect {
    data class ModeChanged(
      val newMode: AppMode,
    ) : Notification

    data object ResetCompleted : Notification

    data class CredentialsSaved(
      val baseUrl: String,
      val token: String,
    ) : Notification

    data class LanguageChanged(
      val language: AppLanguage,
    ) : Notification

    data class ThemeChanged(
      val theme: AppTheme,
    ) : Notification

    data object DialogClosed : Notification
  }
}
