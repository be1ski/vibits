package space.be1ski.vibits.shared.feature.onboarding.presentation.reducer

import space.be1ski.vibits.shared.core.elm.Reducer
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.feature.onboarding.presentation.action.OnboardingAction
import space.be1ski.vibits.shared.feature.onboarding.presentation.effect.OnboardingEffect
import space.be1ski.vibits.shared.feature.onboarding.presentation.state.OnboardingState

internal val completionReducer:
  Reducer<OnboardingAction.Completion, OnboardingState, OnboardingEffect.Command, OnboardingEffect.Notification> =
  reducer { action, state ->
    when (action) {
      is OnboardingAction.Completion.MarkFirstCheckIn -> {
        command(OnboardingEffect.Command.MarkFirstCheckIn)
        command(OnboardingEffect.Command.MarkOnboardingCompleted)
      }

      is OnboardingAction.Completion.FirstCheckInCreated -> {
        notify(OnboardingEffect.Notification.FirstCheckInCreated)
        notify(OnboardingEffect.Notification.Completed)
      }

      is OnboardingAction.Completion.GoToDashboard -> {
        command(OnboardingEffect.Command.MarkOnboardingCompleted)
        notify(OnboardingEffect.Notification.Completed)
      }
    }
  }
