package space.be1ski.vibits.shared.feature.onboarding.presentation.reducer

import space.be1ski.vibits.shared.core.elm.Reducer
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.feature.onboarding.presentation.action.OnboardingAction
import space.be1ski.vibits.shared.feature.onboarding.presentation.effect.OnboardingEffect
import space.be1ski.vibits.shared.feature.onboarding.presentation.state.OnboardingState
import space.be1ski.vibits.shared.feature.onboarding.presentation.state.OnboardingStep

internal val habitReducer: Reducer<OnboardingAction.Habit, OnboardingState, OnboardingEffect.Command, OnboardingEffect.Notification> =
  reducer { action, state ->
    when (action) {
      is OnboardingAction.Habit.UpdateHabitName -> {
        state { copy(habitName = action.name, creationError = null) }
      }

      is OnboardingAction.Habit.UpdateHabitColor -> {
        state { copy(selectedColor = action.color) }
      }

      is OnboardingAction.Habit.CreateHabit -> {
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

      is OnboardingAction.Habit.HabitCreated -> {
        state {
          copy(
            isCreatingHabit = false,
            habitCreated = true,
            currentStep = OnboardingStep.Success,
          )
        }
      }

      is OnboardingAction.Habit.HabitCreationFailed -> {
        state {
          copy(
            isCreatingHabit = false,
            creationError = action.error,
          )
        }
      }
    }
  }
