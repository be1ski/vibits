package space.be1ski.vibits.feature.onboarding.presentation.reducer

import space.be1ski.vibits.core.elm.Reducer
import space.be1ski.vibits.feature.onboarding.presentation.action.OnboardingAction
import space.be1ski.vibits.feature.onboarding.presentation.effect.OnboardingEffect
import space.be1ski.vibits.feature.onboarding.presentation.state.OnboardingState

val onboardingReducer: Reducer<OnboardingAction, OnboardingState, OnboardingEffect.Command, OnboardingEffect.Notification> =
  { action, state ->
    when (action) {
      is OnboardingAction.Navigation -> navigationReducer(action, state)
      is OnboardingAction.Preset -> presetReducer(action, state)
      is OnboardingAction.Habit -> habitReducer(action, state)
      is OnboardingAction.Completion -> completionReducer(action, state)
    }
  }
