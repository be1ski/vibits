package space.be1ski.vibits.shared.feature.onboarding.presentation

import space.be1ski.vibits.shared.core.elm.Reducer
import space.be1ski.vibits.shared.feature.onboarding.presentation.reducer.completionReducer
import space.be1ski.vibits.shared.feature.onboarding.presentation.reducer.habitReducer
import space.be1ski.vibits.shared.feature.onboarding.presentation.reducer.navigationReducer
import space.be1ski.vibits.shared.feature.onboarding.presentation.reducer.presetReducer

val onboardingReducer: Reducer<OnboardingAction, OnboardingState, OnboardingEffect.Command, OnboardingEffect.Notification> =
  { action, state ->
    when (action) {
      is OnboardingAction.Navigation -> navigationReducer(action, state)
      is OnboardingAction.Preset -> presetReducer(action, state)
      is OnboardingAction.Habit -> habitReducer(action, state)
      is OnboardingAction.Completion -> completionReducer(action, state)
    }
  }
