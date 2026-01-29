package space.be1ski.vibits.shared.feature.onboarding.presentation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import space.be1ski.vibits.shared.core.elm.EffectHandler
import space.be1ski.vibits.shared.core.logging.Log
import space.be1ski.vibits.shared.core.platform.date.currentLocalDate
import space.be1ski.vibits.shared.feature.onboarding.domain.usecase.CreateFirstCheckInUseCase
import space.be1ski.vibits.shared.feature.onboarding.domain.usecase.CreateFirstHabitUseCase
import space.be1ski.vibits.shared.feature.onboarding.domain.usecase.MarkOnboardingCompletedUseCase

private const val TAG = "OnboardingEffect"

class OnboardingEffectHandler(
  private val getHabitPresets: space.be1ski.vibits.shared.feature.onboarding.domain.usecase.GetHabitPresetsUseCase,
  private val createFirstHabit: CreateFirstHabitUseCase,
  private val createFirstCheckIn: CreateFirstCheckInUseCase,
  private val markOnboardingCompleted: MarkOnboardingCompletedUseCase,
) : EffectHandler<OnboardingEffect.Command, OnboardingAction> {
  override fun invoke(command: OnboardingEffect.Command): Flow<OnboardingAction> =
    when (command) {
      is OnboardingEffect.Command.LoadPresets -> handleLoadPresets()
      is OnboardingEffect.Command.CreateFirstHabit -> handleCreateFirstHabit(command)
      is OnboardingEffect.Command.MarkOnboardingCompleted -> handleMarkOnboardingCompleted()
      is OnboardingEffect.Command.MarkFirstCheckIn -> handleMarkFirstCheckIn()
    }

  private fun handleLoadPresets(): Flow<OnboardingAction> =
    flow {
      Log.d(TAG, "Loading habit presets")
      val presets = getHabitPresets()
      emit(OnboardingAction.PresetsLoaded(presets))
    }

  private fun handleCreateFirstHabit(command: OnboardingEffect.Command.CreateFirstHabit): Flow<OnboardingAction> =
    flow {
      Log.d(TAG, "Creating first habit: ${command.name}")
      createFirstHabit(command.name, command.presetId, command.color)
        .onSuccess {
          Log.d(TAG, "Habit created successfully")
          emit(OnboardingAction.HabitCreated)
        }.onFailure { error ->
          Log.e(TAG, "Failed to create habit", error)
          emit(OnboardingAction.HabitCreationFailed(error.message ?: "Unknown error"))
        }
    }

  private fun handleMarkOnboardingCompleted(): Flow<OnboardingAction> =
    flow {
      Log.i(TAG, "Marking onboarding as completed")
      markOnboardingCompleted()
    }

  private fun handleMarkFirstCheckIn(): Flow<OnboardingAction> =
    flow {
      Log.d(TAG, "Creating first check-in")
      val today = currentLocalDate()
      createFirstCheckIn(today)
        .onSuccess {
          Log.d(TAG, "First check-in created successfully")
          emit(OnboardingAction.FirstCheckInCreated)
        }.onFailure { error ->
          Log.e(TAG, "Failed to create first check-in", error)
        }
    }
}
