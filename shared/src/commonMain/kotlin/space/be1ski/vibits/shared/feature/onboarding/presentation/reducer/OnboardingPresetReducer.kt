package space.be1ski.vibits.shared.feature.onboarding.presentation.reducer

import space.be1ski.vibits.shared.core.elm.Reducer
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.feature.onboarding.presentation.action.OnboardingAction
import space.be1ski.vibits.shared.feature.onboarding.presentation.effect.OnboardingEffect
import space.be1ski.vibits.shared.feature.onboarding.presentation.state.OnboardingState

internal val presetReducer: Reducer<OnboardingAction.Preset, OnboardingState, OnboardingEffect.Command, OnboardingEffect.Notification> =
  reducer { action, state ->
    when (action) {
      is OnboardingAction.Preset.PresetsLoaded -> {
        state { copy(presets = action.presets) }
      }

      is OnboardingAction.Preset.SelectPreset -> {
        state {
          copy(
            selectedPresetId = action.presetId,
            selectedPresetName = action.localizedName,
          )
        }
      }
    }
  }
