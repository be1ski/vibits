package space.be1ski.vibits.feature.onboarding.presentation.state

import space.be1ski.vibits.feature.habits.domain.model.DefaultHabitColor
import space.be1ski.vibits.feature.habits.domain.model.HabitColor
import space.be1ski.vibits.feature.onboarding.domain.model.HabitPreset

data class OnboardingState(
  val currentStep: OnboardingStep = OnboardingStep.Welcome,
  val presets: List<HabitPreset> = emptyList(),
  val selectedPresetId: String? = null,
  val selectedPresetName: String? = null,
  val habitName: String = "",
  val selectedColor: HabitColor = DefaultHabitColor,
  val isCreatingHabit: Boolean = false,
  val creationError: String? = null,
  val habitCreated: Boolean = false,
)

enum class OnboardingStep {
  Welcome,
  ChoosePreset,
  HabitSetup,
  Success,
}
