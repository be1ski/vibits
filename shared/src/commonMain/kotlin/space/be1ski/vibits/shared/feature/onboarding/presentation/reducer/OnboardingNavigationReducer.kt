package space.be1ski.vibits.shared.feature.onboarding.presentation.reducer

import space.be1ski.vibits.shared.core.elm.Reducer
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.feature.onboarding.domain.model.CUSTOM_PRESET_ID
import space.be1ski.vibits.shared.feature.onboarding.presentation.action.OnboardingAction
import space.be1ski.vibits.shared.feature.onboarding.presentation.effect.OnboardingEffect
import space.be1ski.vibits.shared.feature.onboarding.presentation.state.OnboardingState
import space.be1ski.vibits.shared.feature.onboarding.presentation.state.OnboardingStep

internal val navigationReducer:
  Reducer<OnboardingAction.Navigation, OnboardingState, OnboardingEffect.Command, OnboardingEffect.Notification> =
  reducer { action, state ->
    when (action) {
      is OnboardingAction.Navigation.StartOnboarding -> {
        state { copy(currentStep = OnboardingStep.Welcome) }
        command(OnboardingEffect.Command.LoadPresets)
      }

      is OnboardingAction.Navigation.Continue -> {
        when (state.currentStep) {
          OnboardingStep.Welcome -> state { copy(currentStep = OnboardingStep.ChoosePreset) }
          OnboardingStep.ChoosePreset -> {
            val presetId = state.selectedPresetId
            val presetName = state.selectedPresetName
            if (presetId != null && presetName != null) {
              if (presetId == CUSTOM_PRESET_ID) {
                state { copy(currentStep = OnboardingStep.HabitSetup) }
              } else {
                state { copy(isCreatingHabit = true, habitName = presetName, creationError = null) }
                command(
                  OnboardingEffect.Command.CreateFirstHabit(
                    presetName,
                    presetId,
                    state.selectedColor,
                  ),
                )
              }
            }
          }
          OnboardingStep.HabitSetup -> {
            if (state.habitName.isNotBlank()) {
              state { copy(isCreatingHabit = true, creationError = null) }
              command(
                OnboardingEffect.Command.CreateFirstHabit(
                  state.habitName,
                  state.selectedPresetId,
                  state.selectedColor,
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
        when (state.currentStep) {
          OnboardingStep.ChoosePreset -> state { copy(currentStep = OnboardingStep.Welcome) }
          OnboardingStep.HabitSetup -> state { copy(currentStep = OnboardingStep.ChoosePreset) }
          else -> {}
        }
      }

      is OnboardingAction.Navigation.Skip -> {
        notify(OnboardingEffect.Notification.Skipped)
      }
    }
  }
