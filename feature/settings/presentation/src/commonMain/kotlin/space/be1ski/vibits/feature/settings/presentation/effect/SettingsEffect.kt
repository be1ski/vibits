package space.be1ski.vibits.feature.settings.presentation.effect

import space.be1ski.vibits.core.platform.locale.AppLanguage
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.settings.domain.model.AppTheme

sealed interface SettingsEffect {
  /**
   * Commands handled by SettingsEffectHandler (async operations).
   */
  sealed interface Command : SettingsEffect {
    sealed interface Credentials : Command

    sealed interface Mode : Command

    sealed interface Preferences : Command

    data class ValidateCredentials(
      val baseUrl: String,
      val token: String,
      val targetMode: AppMode,
    ) : Credentials

    data class SaveCredentials(
      val baseUrl: String,
      val token: String,
    ) : Credentials

    data class SwitchMode(
      val mode: AppMode,
    ) : Mode

    data object ResetApp : Mode

    data object ResetAppWithMemos : Mode

    data class SaveLanguage(
      val language: AppLanguage,
    ) : Preferences

    data class SaveTheme(
      val theme: AppTheme,
    ) : Preferences

    data class SaveSyncDebounce(
      val seconds: Int,
    ) : Preferences
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
