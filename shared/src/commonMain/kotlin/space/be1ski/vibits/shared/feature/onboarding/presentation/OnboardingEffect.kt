package space.be1ski.vibits.shared.feature.onboarding.presentation

sealed interface OnboardingEffect {
  /**
   * Commands handled by OnboardingEffectHandler (async operations).
   */
  sealed interface Command : OnboardingEffect {
    data object LoadPresets : Command

    data class CreateFirstHabit(
      val name: String,
      val presetId: String?,
    ) : Command

    data object MarkOnboardingCompleted : Command

    data object MarkFirstCheckIn : Command
  }

  /**
   * Notifications observed by FeatureCoordinator for cross-feature coordination.
   */
  sealed interface Notification : OnboardingEffect {
    data object Completed : Notification

    data object Skipped : Notification

    data object FirstCheckInCreated : Notification
  }
}
