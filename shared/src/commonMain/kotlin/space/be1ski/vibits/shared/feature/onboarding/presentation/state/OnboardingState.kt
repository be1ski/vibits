package space.be1ski.vibits.shared.feature.onboarding.presentation.state

import space.be1ski.vibits.shared.core.ui.theme.DefaultHabitColor
import space.be1ski.vibits.shared.feature.onboarding.domain.model.HabitPreset

data class OnboardingState(
  val currentStep: OnboardingStep = OnboardingStep.Welcome,
  val presets: List<HabitPreset> = emptyList(),
  val selectedPresetId: String? = null,
  val habitName: String = "",
  val selectedColor: Long = DefaultHabitColor,
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
