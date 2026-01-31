package space.be1ski.vibits.shared.feature.onboarding.presentation

import space.be1ski.vibits.shared.core.elm.test
import space.be1ski.vibits.shared.core.ui.theme.DefaultHabitColor
import space.be1ski.vibits.shared.feature.onboarding.domain.model.HabitPreset
import space.be1ski.vibits.shared.feature.onboarding.presentation.action.OnboardingAction
import space.be1ski.vibits.shared.feature.onboarding.presentation.effect.OnboardingEffect.Command
import space.be1ski.vibits.shared.feature.onboarding.presentation.effect.OnboardingEffect.Notification
import space.be1ski.vibits.shared.feature.onboarding.presentation.reducer.onboardingReducer
import space.be1ski.vibits.shared.feature.onboarding.presentation.state.OnboardingState
import space.be1ski.vibits.shared.feature.onboarding.presentation.state.OnboardingStep
import kotlin.test.Test
import kotlin.test.assertEquals

class OnboardingReducerTest {
  @Test
  fun `when StartOnboarding then sets currentStep to Welcome`() =
    onboardingReducer.test(OnboardingState()) {
      send(OnboardingAction.Navigation.StartOnboarding)

      assertState { currentStep == OnboardingStep.Welcome }
      assertHasCommand<Command.LoadPresets>()
    }

  @Test
  fun `when Continue from Welcome then moves to ChoosePreset`() =
    onboardingReducer.test(OnboardingState(currentStep = OnboardingStep.Welcome)) {
      send(OnboardingAction.Navigation.Continue)

      assertState { currentStep == OnboardingStep.ChoosePreset }
      assertNoEffects()
    }

  @Test
  fun `when Continue from ChoosePreset with regular preset then creates habit directly`() =
    onboardingReducer.test(
      OnboardingState(
        currentStep = OnboardingStep.ChoosePreset,
        selectedPresetId = "water",
        selectedPresetName = "Drink Water",
        selectedColor = DefaultHabitColor,
      ),
    ) {
      send(OnboardingAction.Navigation.Continue)

      assertState {
        isCreatingHabit &&
          habitName == "Drink Water" &&
          creationError == null
      }
      val effect = assertHasCommand<Command.CreateFirstHabit>()
      assertEquals("Drink Water", effect.name)
      assertEquals("water", effect.presetId)
      assertEquals(DefaultHabitColor, effect.color)
    }

  @Test
  fun `when Continue from ChoosePreset with custom preset then moves to HabitSetup`() =
    onboardingReducer.test(
      OnboardingState(
        currentStep = OnboardingStep.ChoosePreset,
        selectedPresetId = "custom",
        selectedPresetName = "Create your own",
      ),
    ) {
      send(OnboardingAction.Navigation.Continue)

      assertState { currentStep == OnboardingStep.HabitSetup }
      assertNoEffects()
    }

  @Test
  fun `when Continue from ChoosePreset without preset then stays on ChoosePreset`() =
    onboardingReducer.test(
      OnboardingState(
        currentStep = OnboardingStep.ChoosePreset,
        selectedPresetId = null,
      ),
    ) {
      send(OnboardingAction.Navigation.Continue)

      assertState { currentStep == OnboardingStep.ChoosePreset }
      assertNoEffects()
    }

  @Test
  fun `when PresetsLoaded then updates presets in state`() =
    onboardingReducer.test(OnboardingState()) {
      val presets =
        listOf(
          HabitPreset(id = "water", nameKey = "demo_habit_water"),
          HabitPreset(id = "walking", nameKey = "demo_habit_walking"),
        )
      send(OnboardingAction.Preset.PresetsLoaded(presets))

      assertState { this.presets == presets }
      assertNoEffects()
    }

  @Test
  fun `when SelectPreset then updates selectedPresetId and selectedPresetName`() =
    onboardingReducer.test(OnboardingState()) {
      send(OnboardingAction.Preset.SelectPreset("water", "Drink Water"))

      assertState {
        selectedPresetId == "water" &&
          selectedPresetName == "Drink Water"
      }
      assertNoEffects()
    }

  @Test
  fun `when UpdateHabitName then updates habitName and clears error`() =
    onboardingReducer.test(
      OnboardingState(
        habitName = "",
        creationError = "some error",
      ),
    ) {
      send(OnboardingAction.Habit.UpdateHabitName("Morning exercise"))

      assertState {
        habitName == "Morning exercise" &&
          creationError == null
      }
      assertNoEffects()
    }

  @Test
  fun `when UpdateHabitColor then updates selectedColor`() =
    onboardingReducer.test(OnboardingState()) {
      send(OnboardingAction.Habit.UpdateHabitColor(0xFF2196F3L))

      assertState { selectedColor == 0xFF2196F3L }
      assertNoEffects()
    }

  @Test
  fun `when CreateHabit with valid name then starts creation`() =
    onboardingReducer.test(
      OnboardingState(
        currentStep = OnboardingStep.HabitSetup,
        habitName = "Exercise",
        selectedPresetId = "custom",
        selectedColor = 0xFF4CAF50L,
      ),
    ) {
      send(OnboardingAction.Habit.CreateHabit)

      assertState { isCreatingHabit }
      val effect = assertHasCommand<Command.CreateFirstHabit>()
      assertEquals("Exercise", effect.name)
      assertEquals("custom", effect.presetId)
      assertEquals(0xFF4CAF50L, effect.color)
    }

  @Test
  fun `when CreateHabit with blank name then shows error`() =
    onboardingReducer.test(
      OnboardingState(
        currentStep = OnboardingStep.HabitSetup,
        habitName = "   ",
      ),
    ) {
      send(OnboardingAction.Habit.CreateHabit)

      assertState {
        !isCreatingHabit &&
          creationError == "habit_name_required"
      }
      assertNoEffects()
    }

  @Test
  fun `when HabitCreated then moves to Success step`() =
    onboardingReducer.test(
      OnboardingState(
        currentStep = OnboardingStep.HabitSetup,
        isCreatingHabit = true,
      ),
    ) {
      send(OnboardingAction.Habit.HabitCreated)

      assertState {
        !isCreatingHabit &&
          habitCreated &&
          currentStep == OnboardingStep.Success
      }
      assertNoEffects()
    }

  @Test
  fun `when HabitCreationFailed then shows error`() =
    onboardingReducer.test(
      OnboardingState(
        isCreatingHabit = true,
      ),
    ) {
      send(OnboardingAction.Habit.HabitCreationFailed("Network error"))

      assertState {
        !isCreatingHabit &&
          creationError == "Network error"
      }
      assertNoEffects()
    }

  @Test
  fun `when Skip then emits Skipped notification`() =
    onboardingReducer.test(OnboardingState()) {
      send(OnboardingAction.Navigation.Skip)

      assertHasNotification<Notification.Skipped>()
    }

  @Test
  fun `when GoToDashboard then marks completed and emits Completed`() =
    onboardingReducer.test(OnboardingState(currentStep = OnboardingStep.Success)) {
      send(OnboardingAction.Completion.GoToDashboard)

      assertHasCommand<Command.MarkOnboardingCompleted>()
      assertHasNotification<Notification.Completed>()
    }

  @Test
  fun `when MarkFirstCheckIn then marks check-in and completes onboarding`() =
    onboardingReducer.test(OnboardingState(currentStep = OnboardingStep.Success)) {
      send(OnboardingAction.Completion.MarkFirstCheckIn)

      assertHasCommand<Command.MarkFirstCheckIn>()
      assertHasCommand<Command.MarkOnboardingCompleted>()
    }

  @Test
  fun `when FirstCheckInCreated then emits notification and completes onboarding`() =
    onboardingReducer.test(OnboardingState(currentStep = OnboardingStep.Success)) {
      send(OnboardingAction.Completion.FirstCheckInCreated)

      assertHasNotification<Notification.FirstCheckInCreated>()
      assertHasNotification<Notification.Completed>()
    }

  @Test
  fun `when Back from ChoosePreset then moves to Welcome`() =
    onboardingReducer.test(OnboardingState(currentStep = OnboardingStep.ChoosePreset)) {
      send(OnboardingAction.Navigation.Back)

      assertState { currentStep == OnboardingStep.Welcome }
      assertNoEffects()
    }

  @Test
  fun `when Back from HabitSetup then moves to ChoosePreset`() =
    onboardingReducer.test(OnboardingState(currentStep = OnboardingStep.HabitSetup)) {
      send(OnboardingAction.Navigation.Back)

      assertState { currentStep == OnboardingStep.ChoosePreset }
      assertNoEffects()
    }

  @Test
  fun `when Back from Welcome then stays on Welcome`() =
    onboardingReducer.test(OnboardingState(currentStep = OnboardingStep.Welcome)) {
      send(OnboardingAction.Navigation.Back)

      assertState { currentStep == OnboardingStep.Welcome }
      assertNoEffects()
    }

  @Test
  fun `when Back from Success then stays on Success`() =
    onboardingReducer.test(OnboardingState(currentStep = OnboardingStep.Success)) {
      send(OnboardingAction.Navigation.Back)

      assertState { currentStep == OnboardingStep.Success }
      assertNoEffects()
    }

  @Test
  fun `when Continue from HabitSetup with valid name then creates habit`() =
    onboardingReducer.test(
      OnboardingState(
        currentStep = OnboardingStep.HabitSetup,
        habitName = "Exercise",
        selectedPresetId = "custom",
        selectedColor = 0xFF4CAF50L,
      ),
    ) {
      send(OnboardingAction.Navigation.Continue)

      assertState { isCreatingHabit && creationError == null }
      val effect = assertHasCommand<Command.CreateFirstHabit>()
      assertEquals("Exercise", effect.name)
      assertEquals("custom", effect.presetId)
      assertEquals(0xFF4CAF50L, effect.color)
    }

  @Test
  fun `when Continue from HabitSetup with blank name then shows error`() =
    onboardingReducer.test(
      OnboardingState(
        currentStep = OnboardingStep.HabitSetup,
        habitName = "  ",
      ),
    ) {
      send(OnboardingAction.Navigation.Continue)

      assertState {
        !isCreatingHabit &&
          creationError == "habit_name_required"
      }
      assertNoEffects()
    }

  @Test
  fun `when Continue from Success then does nothing`() =
    onboardingReducer.test(OnboardingState(currentStep = OnboardingStep.Success)) {
      send(OnboardingAction.Navigation.Continue)

      assertState { currentStep == OnboardingStep.Success }
      assertNoEffects()
    }
}
