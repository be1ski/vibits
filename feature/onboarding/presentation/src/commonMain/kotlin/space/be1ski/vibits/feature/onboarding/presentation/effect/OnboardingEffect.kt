package space.be1ski.vibits.feature.onboarding.presentation.effect

import space.be1ski.vibits.feature.habits.domain.model.HabitColor

sealed interface OnboardingEffect {
  /**
   * Commands handled by OnboardingEffectHandler (async operations).
   */
  sealed interface Command : OnboardingEffect {
    sealed interface Presets : Command

    sealed interface Setup : Command

    sealed interface Completion : Command

    data object LoadPresets : Presets

    data class CreateFirstHabit(
      val name: String,
      val presetId: String?,
      val color: HabitColor,
    ) : Setup

    data object MarkFirstCheckIn : Setup

    data object MarkOnboardingCompleted : Completion
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
