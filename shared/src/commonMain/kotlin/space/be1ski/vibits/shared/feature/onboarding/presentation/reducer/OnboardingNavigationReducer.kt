package space.be1ski.vibits.shared.feature.onboarding.presentation.reducer

import space.be1ski.vibits.shared.core.elm.ReducerResult
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.feature.onboarding.presentation.action.OnboardingAction
import space.be1ski.vibits.shared.feature.onboarding.presentation.effect.OnboardingEffect
import space.be1ski.vibits.shared.feature.onboarding.presentation.state.OnboardingState
import space.be1ski.vibits.shared.feature.onboarding.presentation.state.OnboardingStep

internal fun navigationReducer(
  action: OnboardingAction.Navigation,
  state: OnboardingState,
): ReducerResult<OnboardingState, OnboardingEffect.Command, OnboardingEffect.Notification> =
  reducer<OnboardingAction.Navigation, OnboardingState, OnboardingEffect.Command, OnboardingEffect.Notification> { a, s ->
    when (a) {
      is OnboardingAction.Navigation.StartOnboarding -> {
        state { copy(currentStep = OnboardingStep.Welcome) }
        command(OnboardingEffect.Command.LoadPresets)
      }

      is OnboardingAction.Navigation.Continue -> {
        when (s.currentStep) {
          OnboardingStep.Welcome -> state { copy(currentStep = OnboardingStep.ChoosePreset) }
          OnboardingStep.ChoosePreset -> {
            if (s.selectedPresetId != null) {
              state { copy(currentStep = OnboardingStep.HabitSetup) }
            }
          }
          OnboardingStep.HabitSetup -> {
            if (s.habitName.isNotBlank()) {
              state { copy(isCreatingHabit = true, creationError = null) }
              command(
                OnboardingEffect.Command.CreateFirstHabit(
                  s.habitName,
                  s.selectedPresetId,
                  s.selectedColor,
                ),
              )
            } else {
              state { copy(creationError = "habit_name_required") }
            }
          }
          OnboardingStep.Success -> {
            // Do nothing, should use GoToDashboard
          }
        }
      }

      is OnboardingAction.Navigation.Back -> {
        when (s.currentStep) {
          OnboardingStep.ChoosePreset -> state { copy(currentStep = OnboardingStep.Welcome) }
          OnboardingStep.HabitSetup -> state { copy(currentStep = OnboardingStep.ChoosePreset) }
          else -> {}
        }
      }

      is OnboardingAction.Navigation.Skip -> {
        notify(OnboardingEffect.Notification.Skipped)
      }
    }
  }(action, state)
