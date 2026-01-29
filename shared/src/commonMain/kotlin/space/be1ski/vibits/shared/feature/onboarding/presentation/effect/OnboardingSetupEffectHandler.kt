package space.be1ski.vibits.shared.feature.onboarding.presentation.effect

import kotlinx.coroutines.flow.Flow
import space.be1ski.vibits.shared.core.elm.EffectHandler
import space.be1ski.vibits.shared.core.elm.actions
import space.be1ski.vibits.shared.core.logging.Log
import space.be1ski.vibits.shared.core.platform.date.currentLocalDate
import space.be1ski.vibits.shared.feature.onboarding.domain.usecase.CreateFirstCheckInUseCase
import space.be1ski.vibits.shared.feature.onboarding.domain.usecase.CreateFirstHabitUseCase
import space.be1ski.vibits.shared.feature.onboarding.presentation.action.OnboardingAction
import space.be1ski.vibits.shared.feature.onboarding.presentation.effect.OnboardingEffect

private const val TAG = "OnboardingSetupEffect"

class OnboardingSetupEffectHandler(
  private val createFirstHabit: CreateFirstHabitUseCase,
  private val createFirstCheckIn: CreateFirstCheckInUseCase,
) : EffectHandler<OnboardingEffect.Command.Setup, OnboardingAction> {
  override fun invoke(command: OnboardingEffect.Command.Setup): Flow<OnboardingAction> =
    when (command) {
      is OnboardingEffect.Command.CreateFirstHabit -> handleCreateFirstHabit(command)
      OnboardingEffect.Command.MarkFirstCheckIn -> handleMarkFirstCheckIn()
    }

  private fun handleCreateFirstHabit(command: OnboardingEffect.Command.CreateFirstHabit): Flow<OnboardingAction> =
    actions {
      Log.d(TAG, "Creating first habit: ${command.name}")
      createFirstHabit(command.name, command.presetId, command.color)
        .onSuccess {
          Log.d(TAG, "Habit created successfully")
          emit(OnboardingAction.Habit.HabitCreated)
        }.onFailure { error ->
          Log.e(TAG, "Failed to create habit", error)
          emit(OnboardingAction.Habit.HabitCreationFailed(error.message ?: "Unknown error"))
        }
    }

  private fun handleMarkFirstCheckIn(): Flow<OnboardingAction> =
    actions {
      Log.d(TAG, "Creating first check-in")
      val today = currentLocalDate()
      createFirstCheckIn(today)
        .onSuccess {
          Log.d(TAG, "First check-in created successfully")
          emit(OnboardingAction.Completion.FirstCheckInCreated)
        }.onFailure { error ->
          Log.e(TAG, "Failed to create first check-in", error)
        }
    }
}
