package space.be1ski.vibits.feature.onboarding.presentation.reducer

import space.be1ski.vibits.core.elm.Reducer
import space.be1ski.vibits.core.elm.reducer
import space.be1ski.vibits.feature.onboarding.presentation.action.OnboardingAction
import space.be1ski.vibits.feature.onboarding.presentation.effect.OnboardingEffect
import space.be1ski.vibits.feature.onboarding.presentation.state.OnboardingState

internal val presetReducer: Reducer<OnboardingAction.Preset, OnboardingState, OnboardingEffect.Command, OnboardingEffect.Notification> =
  reducer { action, state ->
    when (action) {
      is OnboardingAction.Preset.PresetsLoaded -> {
        state { state.copy(presets = action.presets) }
      }

      is OnboardingAction.Preset.SelectPreset -> {
        state {
          state.copy(
            selectedPresetId = action.presetId,
            selectedPresetName = action.localizedName,
          )
        }
      }
    }
  }
