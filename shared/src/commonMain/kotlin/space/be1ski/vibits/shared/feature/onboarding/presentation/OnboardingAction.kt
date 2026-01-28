package space.be1ski.vibits.shared.feature.onboarding.presentation

sealed interface OnboardingAction {
  // Navigation
  data object StartOnboarding : OnboardingAction

  data object Continue : OnboardingAction

  data object Back : OnboardingAction

  data object Skip : OnboardingAction

  // Preset selection
  data class SelectPreset(
    val presetId: String,
  ) : OnboardingAction

  data class PresetsLoaded(
    val presets: List<space.be1ski.vibits.shared.feature.onboarding.domain.model.HabitPreset>,
  ) : OnboardingAction

  // Habit setup
  data class UpdateHabitName(
    val name: String,
  ) : OnboardingAction

  data object CreateHabit : OnboardingAction

  // Responses
  data object HabitCreated : OnboardingAction

  data class HabitCreationFailed(
    val error: String,
  ) : OnboardingAction

  // Completion
  data object MarkFirstCheckIn : OnboardingAction

  data object GoToDashboard : OnboardingAction

  data object OnboardingCompleted : OnboardingAction
}
