package space.be1ski.vibits.feature.onboarding.presentation.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import space.be1ski.vibits.core.ui.test.captureAllVariants
import space.be1ski.vibits.core.utils.habits.DemoHabit
import space.be1ski.vibits.feature.onboarding.domain.model.HabitPreset
import space.be1ski.vibits.feature.onboarding.presentation.state.OnboardingState
import space.be1ski.vibits.feature.onboarding.presentation.state.OnboardingStep
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class OnboardingScreenshotTest {
  private val testPresets = DemoHabit.entries.map { HabitPreset(it) } + HabitPreset(null)

  @Test
  fun `when welcome step then captures welcome screen`() =
    captureAllVariants(
      "onboarding_welcome",
      assertions = { onNodeWithTag(OnboardingTestTags.WELCOME_SCREEN).assertIsDisplayed() },
    ) {
      OnboardingScreen(state = OnboardingState(currentStep = OnboardingStep.Welcome), onAction = {})
    }

  @Test
  fun `when choose preset step then captures preset selection screen`() =
    captureAllVariants(
      "onboarding_choose_preset",
      assertions = { onNodeWithTag(OnboardingTestTags.CHOOSE_PRESET_SCREEN).assertIsDisplayed() },
    ) {
      OnboardingScreen(
        state =
          OnboardingState(
            currentStep = OnboardingStep.ChoosePreset,
            presets = testPresets,
            selectedPresetId = "exercise",
          ),
        onAction = {},
      )
    }

  @Test
  fun `when habit setup step then captures habit setup screen`() =
    captureAllVariants(
      "onboarding_habit_setup",
      assertions = { onNodeWithTag(OnboardingTestTags.HABIT_SETUP_SCREEN).assertIsDisplayed() },
    ) {
      OnboardingScreen(
        state =
          OnboardingState(
            currentStep = OnboardingStep.HabitSetup,
            presets = testPresets,
            selectedPresetId = "exercise",
            habitName = "Exercise",
          ),
        onAction = {},
      )
    }

  @Test
  fun `when habit setup has error then captures error state`() =
    captureAllVariants(
      "onboarding_habit_setup_error",
      assertions = { onNodeWithTag(OnboardingTestTags.HABIT_SETUP_SCREEN).assertIsDisplayed() },
    ) {
      OnboardingScreen(
        state =
          OnboardingState(
            currentStep = OnboardingStep.HabitSetup,
            presets = testPresets,
            selectedPresetId = "exercise",
            habitName = "",
            creationError = "habit_name_required",
          ),
        onAction = {},
      )
    }

  @Test
  fun `when success step then captures success screen`() =
    captureAllVariants(
      "onboarding_success",
      assertions = { onNodeWithTag(OnboardingTestTags.SUCCESS_SCREEN).assertIsDisplayed() },
    ) {
      OnboardingScreen(state = OnboardingState(currentStep = OnboardingStep.Success), onAction = {})
    }
}
