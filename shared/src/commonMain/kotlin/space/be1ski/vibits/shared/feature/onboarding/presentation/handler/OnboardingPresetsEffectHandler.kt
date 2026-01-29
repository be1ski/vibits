package space.be1ski.vibits.shared.feature.onboarding.presentation.handler

import kotlinx.coroutines.flow.Flow
import space.be1ski.vibits.shared.core.elm.EffectHandler
import space.be1ski.vibits.shared.core.elm.actions
import space.be1ski.vibits.shared.core.logging.Log
import space.be1ski.vibits.shared.feature.onboarding.domain.usecase.GetHabitPresetsUseCase
import space.be1ski.vibits.shared.feature.onboarding.presentation.action.OnboardingAction
import space.be1ski.vibits.shared.feature.onboarding.presentation.effect.OnboardingEffect

private const val TAG = "OnboardingPresetsEffect"

class OnboardingPresetsEffectHandler(
  private val getHabitPresets: GetHabitPresetsUseCase,
) : EffectHandler<OnboardingEffect.Command.Presets, OnboardingAction> {
  override fun invoke(command: OnboardingEffect.Command.Presets): Flow<OnboardingAction> =
    when (command) {
      OnboardingEffect.Command.LoadPresets -> handleLoadPresets()
    }

  private fun handleLoadPresets(): Flow<OnboardingAction> =
    actions {
      Log.d(TAG, "Loading habit presets")
      val presets = getHabitPresets()
      emit(OnboardingAction.Preset.PresetsLoaded(presets))
    }
}
