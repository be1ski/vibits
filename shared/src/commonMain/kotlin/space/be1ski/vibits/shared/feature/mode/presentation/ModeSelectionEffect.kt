package space.be1ski.vibits.shared.feature.mode.presentation

import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode

sealed interface ModeSelectionEffect {
  /**
   * Commands handled by ModeSelectionEffectHandler (async operations).
   */
  sealed interface Command : ModeSelectionEffect {
    data object InitializeFromLocalConfig : Command

    data object CheckStoredCredentials : Command

    data object UseStoredCredentialsWithValidation : Command

    data class ValidateCredentials(
      val baseUrl: String,
      val token: String,
    ) : Command

    data class SaveCredentials(
      val baseUrl: String,
      val token: String,
    ) : Command

    data class SaveMode(
      val mode: AppMode,
    ) : Command
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
