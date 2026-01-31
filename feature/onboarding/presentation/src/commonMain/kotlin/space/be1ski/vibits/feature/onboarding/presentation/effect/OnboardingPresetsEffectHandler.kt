package space.be1ski.vibits.feature.onboarding.presentation.effect

import kotlinx.coroutines.flow.Flow
import space.be1ski.vibits.core.elm.EffectHandler
import space.be1ski.vibits.core.elm.actions
import space.be1ski.vibits.core.logging.Log
import space.be1ski.vibits.feature.onboarding.domain.usecase.GetHabitPresetsUseCase
import space.be1ski.vibits.feature.onboarding.presentation.action.OnboardingAction

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
