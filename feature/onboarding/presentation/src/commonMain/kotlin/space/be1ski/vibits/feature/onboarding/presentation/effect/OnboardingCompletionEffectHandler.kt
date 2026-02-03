package space.be1ski.vibits.feature.onboarding.presentation.effect

import kotlinx.coroutines.flow.Flow
import space.be1ski.vibits.core.elm.EffectHandler
import space.be1ski.vibits.core.elm.actions
import space.be1ski.vibits.core.logging.Log
import space.be1ski.vibits.feature.onboarding.domain.usecase.MarkOnboardingCompletedUseCase
import space.be1ski.vibits.feature.onboarding.presentation.action.OnboardingAction

private const val TAG = "OnboardingCompletionEffect"

class OnboardingCompletionEffectHandler(
  private val markOnboardingCompleted: MarkOnboardingCompletedUseCase,
) : EffectHandler<OnboardingEffect.Command.Completion, OnboardingAction> {
  override fun invoke(command: OnboardingEffect.Command.Completion): Flow<OnboardingAction> =
    when (command) {
      OnboardingEffect.Command.MarkOnboardingCompleted -> handleMarkOnboardingCompleted()
    }

  private fun handleMarkOnboardingCompleted(): Flow<OnboardingAction> =
    actions {
      Log.i(TAG, "Marking onboarding as completed")
      markOnboardingCompleted()
    }
}
