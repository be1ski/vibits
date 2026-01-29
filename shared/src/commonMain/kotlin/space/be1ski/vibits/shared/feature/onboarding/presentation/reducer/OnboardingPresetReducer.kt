package space.be1ski.vibits.shared.feature.onboarding.presentation.reducer

import space.be1ski.vibits.shared.core.elm.ReducerResult
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.feature.onboarding.presentation.OnboardingAction
import space.be1ski.vibits.shared.feature.onboarding.presentation.OnboardingEffect
import space.be1ski.vibits.shared.feature.onboarding.presentation.OnboardingState

internal fun presetReducer(
  action: OnboardingAction.Preset,
  state: OnboardingState,
): ReducerResult<OnboardingState, OnboardingEffect.Command, OnboardingEffect.Notification> =
  reducer<OnboardingAction.Preset, OnboardingState, OnboardingEffect.Command, OnboardingEffect.Notification> { a, s ->
    when (a) {
      is OnboardingAction.Preset.PresetsLoaded -> {
        state { copy(presets = a.presets) }
      }

      is OnboardingAction.Preset.SelectPreset -> {
        state { copy(selectedPresetId = a.presetId) }
      }
    }
  }(action, state)
