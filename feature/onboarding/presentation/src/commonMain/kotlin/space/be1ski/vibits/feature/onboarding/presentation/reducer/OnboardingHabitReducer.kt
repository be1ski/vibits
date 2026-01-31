package space.be1ski.vibits.feature.onboarding.presentation.reducer

import space.be1ski.vibits.core.elm.Reducer
import space.be1ski.vibits.core.elm.reducer
import space.be1ski.vibits.feature.onboarding.presentation.action.OnboardingAction
import space.be1ski.vibits.feature.onboarding.presentation.effect.OnboardingEffect
import space.be1ski.vibits.feature.onboarding.presentation.state.OnboardingState
import space.be1ski.vibits.feature.onboarding.presentation.state.OnboardingStep

internal val habitReducer: Reducer<OnboardingAction.Habit, OnboardingState, OnboardingEffect.Command, OnboardingEffect.Notification> =
  reducer { action, state ->
    when (action) {
      is OnboardingAction.Habit.UpdateHabitName -> {
        state { state.copy(habitName = action.name, creationError = null) }
      }

      is OnboardingAction.Habit.UpdateHabitColor -> {
        state { state.copy(selectedColor = action.color) }
      }

      is OnboardingAction.Habit.CreateHabit -> {
        if (state.habitName.isNotBlank()) {
          state { state.copy(isCreatingHabit = true, creationError = null) }
          command(
            OnboardingEffect.Command.CreateFirstHabit(
              state.habitName,
              state.selectedPresetId,
              state.selectedColor,
            ),
          )
        } else {
          state { state.copy(creationError = "habit_name_required") }
        }
      }

      is OnboardingAction.Habit.HabitCreated -> {
        state {
          state.copy(
            isCreatingHabit = false,
            habitCreated = true,
            currentStep = OnboardingStep.Success,
          )
        }
      }

      is OnboardingAction.Habit.HabitCreationFailed -> {
        state {
          state.copy(
            isCreatingHabit = false,
            creationError = action.error,
          )
        }
      }
    }
  }
