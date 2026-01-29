package space.be1ski.vibits.shared.feature.onboarding.presentation.reducer

import space.be1ski.vibits.shared.core.elm.ReducerResult
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.feature.onboarding.presentation.action.OnboardingAction
import space.be1ski.vibits.shared.feature.onboarding.presentation.effect.OnboardingEffect
import space.be1ski.vibits.shared.feature.onboarding.presentation.state.OnboardingState
import space.be1ski.vibits.shared.feature.onboarding.presentation.state.OnboardingStep

internal fun habitReducer(
  action: OnboardingAction.Habit,
  state: OnboardingState,
): ReducerResult<OnboardingState, OnboardingEffect.Command, OnboardingEffect.Notification> =
  reducer<OnboardingAction.Habit, OnboardingState, OnboardingEffect.Command, OnboardingEffect.Notification> { a, s ->
    when (a) {
      is OnboardingAction.Habit.UpdateHabitName -> {
        state { copy(habitName = a.name, creationError = null) }
      }

      is OnboardingAction.Habit.UpdateHabitColor -> {
        state { copy(selectedColor = a.color) }
      }

      is OnboardingAction.Habit.CreateHabit -> {
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
            creationError = a.error,
          )
        }
      }
    }
  }(action, state)
