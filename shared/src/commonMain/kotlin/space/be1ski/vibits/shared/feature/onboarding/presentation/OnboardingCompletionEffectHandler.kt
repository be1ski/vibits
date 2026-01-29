package space.be1ski.vibits.shared.feature.onboarding.presentation

import kotlinx.coroutines.flow.Flow
import space.be1ski.vibits.shared.core.elm.EffectHandler
import space.be1ski.vibits.shared.core.elm.actions
import space.be1ski.vibits.shared.core.logging.Log
import space.be1ski.vibits.shared.feature.onboarding.domain.usecase.MarkOnboardingCompletedUseCase

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
