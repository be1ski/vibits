package space.be1ski.vibits.feature.mode.presentation.effect

import space.be1ski.vibits.core.platform.mode.AppMode

sealed interface ModeSelectionEffect {
  /**
   * Commands handled by ModeSelectionEffectHandler (async operations).
   */
  sealed interface Command : ModeSelectionEffect {
    sealed interface Credentials : Command

    sealed interface Mode : Command

    data object InitializeFromLocalConfig : Credentials

    data object CheckStoredCredentials : Credentials

    data object UseStoredCredentialsWithValidation : Credentials

    data class ValidateCredentials(
      val baseUrl: String,
      val token: String,
    ) : Credentials

    data class SaveCredentials(
      val baseUrl: String,
      val token: String,
    ) : Credentials

    data object LoadFromKeychain : Credentials

    data class SaveMode(
      val mode: AppMode,
    ) : Mode
  }

  /**
   * Notifications observed by AppRoot for cross-feature coordination.
   */
  sealed interface Notification : ModeSelectionEffect {
    data class ModeSelected(
      val mode: AppMode,
    ) : Notification
  }
}
