package space.be1ski.vibits.shared.feature.onboarding.presentation

import space.be1ski.vibits.shared.core.elm.test
import space.be1ski.vibits.shared.feature.onboarding.domain.model.HabitPreset
import kotlin.test.Test
import kotlin.test.assertEquals

class OnboardingReducerTest {
  @Test
  fun `when StartOnboarding then sets currentStep to Welcome`() =
    onboardingReducer.test(OnboardingState()) {
      send(OnboardingAction.StartOnboarding)

      assertState { currentStep == OnboardingStep.Welcome }
      assertHasEffect<OnboardingEffect.Command.LoadPresets>()
    }

  @Test
  fun `when Continue from Welcome then moves to ChoosePreset`() =
    onboardingReducer.test(OnboardingState(currentStep = OnboardingStep.Welcome)) {
      send(OnboardingAction.Continue)

      assertState { currentStep == OnboardingStep.ChoosePreset }
      assertNoEffects()
    }

  @Test
  fun `when Continue from ChoosePreset with selected preset then moves to HabitSetup and auto-fills habit name`() =
    onboardingReducer.test(
      OnboardingState(
        currentStep = OnboardingStep.ChoosePreset,
        selectedPresetId = "water",
        presets =
          listOf(
            HabitPreset(id = "water", nameKey = "label_habit_preset_water", nameEn = "Drink water"),
            HabitPreset(id = "custom", nameKey = "label_habit_preset_custom", nameEn = "Custom"),
          ),
      ),
    ) {
      send(OnboardingAction.Continue)

      assertState {
        currentStep == OnboardingStep.HabitSetup &&
          habitName == "Drink water"
      }
      assertNoEffects()
    }

  @Test
  fun `when Continue from ChoosePreset with custom preset then moves to HabitSetup with blank name`() =
    onboardingReducer.test(
      OnboardingState(
        currentStep = OnboardingStep.ChoosePreset,
        selectedPresetId = "custom",
        presets =
          listOf(
            HabitPreset(id = "water", nameKey = "label_habit_preset_water", nameEn = "Drink water"),
            HabitPreset(id = "custom", nameKey = "label_habit_preset_custom", nameEn = "Custom"),
          ),
      ),
    ) {
      send(OnboardingAction.Continue)

      assertState {
        currentStep == OnboardingStep.HabitSetup &&
          habitName == ""
      }
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
      send(OnboardingAction.Continue)

      assertState { currentStep == OnboardingStep.ChoosePreset }
      assertNoEffects()
    }

  @Test
  fun `when PresetsLoaded then updates presets in state`() =
    onboardingReducer.test(OnboardingState()) {
      val presets =
        listOf(
          HabitPreset(id = "water", nameKey = "label_habit_preset_water", nameEn = "Drink water"),
          HabitPreset(id = "walk", nameKey = "label_habit_preset_walk", nameEn = "Take a walk"),
        )
      send(OnboardingAction.PresetsLoaded(presets))

      assertState { this.presets == presets }
      assertNoEffects()
    }

  @Test
  fun `when SelectPreset then updates selectedPresetId`() =
    onboardingReducer.test(OnboardingState()) {
      send(OnboardingAction.SelectPreset("water"))

      assertState { selectedPresetId == "water" }
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
      send(OnboardingAction.UpdateHabitName("Morning exercise"))

      assertState {
        habitName == "Morning exercise" &&
          creationError == null
      }
      assertNoEffects()
    }

  @Test
  fun `when CreateHabit with valid name then starts creation`() =
    onboardingReducer.test(
      OnboardingState(
        currentStep = OnboardingStep.HabitSetup,
        habitName = "Exercise",
        selectedPresetId = "custom",
      ),
    ) {
      send(OnboardingAction.CreateHabit)

      assertState { isCreatingHabit }
      val effect = assertHasEffect<OnboardingEffect.Command.CreateFirstHabit>()
      assertEquals("Exercise", effect.name)
      assertEquals("custom", effect.presetId)
    }

  @Test
  fun `when CreateHabit with blank name then shows error`() =
    onboardingReducer.test(
      OnboardingState(
        currentStep = OnboardingStep.HabitSetup,
        habitName = "   ",
      ),
    ) {
      send(OnboardingAction.CreateHabit)

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
      send(OnboardingAction.HabitCreated)

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
      send(OnboardingAction.HabitCreationFailed("Network error"))

      assertState {
        !isCreatingHabit &&
          creationError == "Network error"
      }
      assertNoEffects()
    }

  @Test
  fun `when Skip then emits Skipped notification`() =
    onboardingReducer.test(OnboardingState()) {
      send(OnboardingAction.Skip)

      assertHasEffect<OnboardingEffect.Notification.Skipped>()
    }

  @Test
  fun `when GoToDashboard then marks completed and emits Completed`() =
    onboardingReducer.test(OnboardingState(currentStep = OnboardingStep.Success)) {
      send(OnboardingAction.GoToDashboard)

      assertHasEffect<OnboardingEffect.Command.MarkOnboardingCompleted>()
      assertHasEffect<OnboardingEffect.Notification.Completed>()
    }

  @Test
  fun `when MarkFirstCheckIn then marks check-in and completes onboarding`() =
    onboardingReducer.test(OnboardingState(currentStep = OnboardingStep.Success)) {
      send(OnboardingAction.MarkFirstCheckIn)

      assertHasEffect<OnboardingEffect.Command.MarkFirstCheckIn>()
      assertHasEffect<OnboardingEffect.Command.MarkOnboardingCompleted>()
    }

  @Test
  fun `when FirstCheckInCreated then emits notification and completes onboarding`() =
    onboardingReducer.test(OnboardingState(currentStep = OnboardingStep.Success)) {
      send(OnboardingAction.FirstCheckInCreated)

      assertHasEffect<OnboardingEffect.Notification.FirstCheckInCreated>()
      assertHasEffect<OnboardingEffect.Notification.Completed>()
    }

  @Test
  fun `when Back from ChoosePreset then moves to Welcome`() =
    onboardingReducer.test(OnboardingState(currentStep = OnboardingStep.ChoosePreset)) {
      send(OnboardingAction.Back)

      assertState { currentStep == OnboardingStep.Welcome }
      assertNoEffects()
    }

  @Test
  fun `when Back from HabitSetup then moves to ChoosePreset`() =
    onboardingReducer.test(OnboardingState(currentStep = OnboardingStep.HabitSetup)) {
      send(OnboardingAction.Back)

      assertState { currentStep == OnboardingStep.ChoosePreset }
      assertNoEffects()
    }
}
