package space.be1ski.vibits.shared.feature.onboarding.presentation

import space.be1ski.vibits.shared.core.elm.Reducer
import space.be1ski.vibits.shared.core.elm.reducer

val onboardingReducer: Reducer<OnboardingAction, OnboardingState, OnboardingEffect> =
  reducer { action, state ->
    when (action) {
      // Navigation
      is OnboardingAction.StartOnboarding -> {
        state { copy(currentStep = OnboardingStep.Welcome) }
        effect(OnboardingEffect.Command.LoadPresets)
      }

      is OnboardingAction.Continue -> {
        when (state.currentStep) {
          OnboardingStep.Welcome -> state { copy(currentStep = OnboardingStep.ChoosePreset) }
          OnboardingStep.ChoosePreset -> {
            if (state.selectedPresetId != null) {
              val selectedPreset = state.presets.find { it.id == state.selectedPresetId }
              val presetName = selectedPreset?.nameEn ?: ""
              state {
                copy(
                  currentStep = OnboardingStep.HabitSetup,
                  habitName = if (selectedPreset?.id == "custom") "" else presetName,
                )
              }
            }
          }
          OnboardingStep.HabitSetup -> {
            if (state.habitName.isNotBlank()) {
              state { copy(isCreatingHabit = true, creationError = null) }
              effect(OnboardingEffect.Command.CreateFirstHabit(state.habitName, state.selectedPresetId))
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
        effect(OnboardingEffect.Notification.Skipped)
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

      is OnboardingAction.CreateHabit -> {
        if (state.habitName.isNotBlank()) {
          state { copy(isCreatingHabit = true, creationError = null) }
          effect(OnboardingEffect.Command.CreateFirstHabit(state.habitName, state.selectedPresetId))
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
        effect(OnboardingEffect.Command.MarkFirstCheckIn)
        effect(OnboardingEffect.Command.MarkOnboardingCompleted)
      }

      is OnboardingAction.FirstCheckInCreated -> {
        effect(OnboardingEffect.Notification.FirstCheckInCreated)
        effect(OnboardingEffect.Notification.Completed)
      }

      is OnboardingAction.GoToDashboard -> {
        effect(OnboardingEffect.Command.MarkOnboardingCompleted)
        effect(OnboardingEffect.Notification.Completed)
      }

      is OnboardingAction.OnboardingCompleted -> {
        // Handled by coordinator
      }
    }
  }
