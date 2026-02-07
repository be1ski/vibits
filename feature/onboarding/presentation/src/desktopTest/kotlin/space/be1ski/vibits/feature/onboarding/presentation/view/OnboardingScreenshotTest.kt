package space.be1ski.vibits.feature.onboarding.presentation.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import space.be1ski.vibits.core.ui.test.runAppUiTest
import space.be1ski.vibits.core.ui.test.saveScreenshot
import space.be1ski.vibits.core.ui.test.setThemedContent
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
    runAppUiTest {
      setThemedContent {
        OnboardingScreen(
          state = OnboardingState(currentStep = OnboardingStep.Welcome),
          onAction = {},
        )
      }

      onNodeWithTag(OnboardingTestTags.WELCOME_SCREEN).assertIsDisplayed()
      saveScreenshot("onboarding", "OnboardingScreenshotTest", "onboarding_welcome")
    }

  @Test
  fun `when choose preset step then captures preset selection screen`() =
    runAppUiTest {
      setThemedContent {
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

      onNodeWithTag(OnboardingTestTags.CHOOSE_PRESET_SCREEN).assertIsDisplayed()
      saveScreenshot("onboarding", "OnboardingScreenshotTest", "onboarding_choose_preset")
    }

  @Test
  fun `when habit setup step then captures habit setup screen`() =
    runAppUiTest {
      setThemedContent {
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

      onNodeWithTag(OnboardingTestTags.HABIT_SETUP_SCREEN).assertIsDisplayed()
      saveScreenshot("onboarding", "OnboardingScreenshotTest", "onboarding_habit_setup")
    }

  @Test
  fun `when habit setup has error then captures error state`() =
    runAppUiTest {
      setThemedContent {
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

      onNodeWithTag(OnboardingTestTags.HABIT_SETUP_SCREEN).assertIsDisplayed()
      saveScreenshot("onboarding", "OnboardingScreenshotTest", "onboarding_habit_setup_error")
    }

  @Test
  fun `when success step then captures success screen`() =
    runAppUiTest {
      setThemedContent {
        OnboardingScreen(
          state = OnboardingState(currentStep = OnboardingStep.Success),
          onAction = {},
        )
      }

      onNodeWithTag(OnboardingTestTags.SUCCESS_SCREEN).assertIsDisplayed()
      saveScreenshot("onboarding", "OnboardingScreenshotTest", "onboarding_success")
    }
}
