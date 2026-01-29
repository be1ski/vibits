package space.be1ski.vibits.shared.feature.onboarding.presentation

import kotlinx.coroutines.flow.Flow
import space.be1ski.vibits.shared.core.elm.EffectHandler

class OnboardingEffectHandler(
  private val presetsHandler: OnboardingPresetsEffectHandler,
  private val setupHandler: OnboardingSetupEffectHandler,
  private val completionHandler: OnboardingCompletionEffectHandler,
) : EffectHandler<OnboardingEffect.Command, OnboardingAction> {
  override fun invoke(command: OnboardingEffect.Command): Flow<OnboardingAction> =
    when (command) {
      is OnboardingEffect.Command.Presets -> presetsHandler(command)
      is OnboardingEffect.Command.Setup -> setupHandler(command)
      is OnboardingEffect.Command.Completion -> completionHandler(command)
    }
}
