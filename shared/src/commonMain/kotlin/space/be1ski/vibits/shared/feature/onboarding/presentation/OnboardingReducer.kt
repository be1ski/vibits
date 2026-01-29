package space.be1ski.vibits.shared.feature.onboarding.presentation

import space.be1ski.vibits.shared.core.elm.Reducer
import space.be1ski.vibits.shared.core.elm.reducer

val onboardingReducer: Reducer<OnboardingAction, OnboardingState, OnboardingEffect.Command, OnboardingEffect.Notification> =
  reducer { action, state ->
    when (action) {
      // Navigation
      is OnboardingAction.StartOnboarding -> {
        state { copy(currentStep = OnboardingStep.Welcome) }
        command(OnboardingEffect.Command.LoadPresets)
      }

      is OnboardingAction.Continue -> {
        when (state.currentStep) {
          OnboardingStep.Welcome -> state { copy(currentStep = OnboardingStep.ChoosePreset) }
          OnboardingStep.ChoosePreset -> {
            if (state.selectedPresetId != null) {
              state { copy(currentStep = OnboardingStep.HabitSetup) }
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

      is OnboardingAction.Back -> {
        when (state.currentStep) {
          OnboardingStep.ChoosePreset -> state { copy(currentStep = OnboardingStep.Welcome) }
          OnboardingStep.HabitSetup -> state { copy(currentStep = OnboardingStep.ChoosePreset) }
          else -> {}
        }
      }

      is OnboardingAction.Skip -> {
        notify(OnboardingEffect.Notification.Skipped)
      }

      // Preset selection
      is OnboardingAction.PresetsLoaded -> {
        state { copy(presets = action.presets) }
      }

      is OnboardingAction.SelectPreset -> {
        state { copy(selectedPresetId = action.presetId) }
      }

      // Habit setup
      is OnboardingAction.UpdateHabitName -> {
        state { copy(habitName = action.name, creationError = null) }
      }

      is OnboardingAction.UpdateHabitColor -> {
        state { copy(selectedColor = action.color) }
      }

      is OnboardingAction.CreateHabit -> {
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

      // Responses
      is OnboardingAction.HabitCreated -> {
        state {
          copy(
            isCreatingHabit = false,
            habitCreated = true,
            currentStep = OnboardingStep.Success,
          )
        }
      }

      is OnboardingAction.HabitCreationFailed -> {
        state {
          copy(
            isCreatingHabit = false,
            creationError = action.error,
          )
        }
      }

      // Completion
      is OnboardingAction.MarkFirstCheckIn -> {
        command(OnboardingEffect.Command.MarkFirstCheckIn)
        command(OnboardingEffect.Command.MarkOnboardingCompleted)
      }

      is OnboardingAction.FirstCheckInCreated -> {
        notify(OnboardingEffect.Notification.FirstCheckInCreated)
        notify(OnboardingEffect.Notification.Completed)
      }

      is OnboardingAction.GoToDashboard -> {
        command(OnboardingEffect.Command.MarkOnboardingCompleted)
        notify(OnboardingEffect.Notification.Completed)
      }
    }
  }
