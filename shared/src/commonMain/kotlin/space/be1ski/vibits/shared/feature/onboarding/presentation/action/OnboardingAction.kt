package space.be1ski.vibits.shared.feature.onboarding.presentation.action

import space.be1ski.vibits.shared.core.elm.Action
import space.be1ski.vibits.shared.feature.onboarding.domain.model.HabitPreset

sealed interface OnboardingAction : Action {
  /**
   * Navigation flow.
   */
  sealed interface Navigation : OnboardingAction {
    data object StartOnboarding : Navigation

    data object Continue : Navigation

    data object Back : Navigation

    data object Skip : Navigation
  }

  /**
   * Preset selection.
   */
  sealed interface Preset : OnboardingAction {
    data class PresetsLoaded(
      val presets: List<HabitPreset>,
    ) : Preset

    data class SelectPreset(
      val presetId: String,
      val localizedName: String,
    ) : Preset
  }

  /**
   * Habit setup.
   */
  sealed interface Habit : OnboardingAction {
    data class UpdateHabitName(
      val name: String,
    ) : Habit

    data class UpdateHabitColor(
      val color: Long,
    ) : Habit

    data object CreateHabit : Habit

    data object HabitCreated : Habit

    data class HabitCreationFailed(
      val error: String,
    ) : Habit
  }

  /**
   * Completion flow.
   */
  sealed interface Completion : OnboardingAction {
    data object MarkFirstCheckIn : Completion

    data object FirstCheckInCreated : Completion

    data object GoToDashboard : Completion
  }
}
