package space.be1ski.vibits.shared.feature.onboarding.presentation.reducer

import space.be1ski.vibits.shared.core.elm.ReducerResult
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.feature.onboarding.presentation.action.OnboardingAction
import space.be1ski.vibits.shared.feature.onboarding.presentation.effect.OnboardingEffect
import space.be1ski.vibits.shared.feature.onboarding.presentation.state.OnboardingState

internal fun completionReducer(
  action: OnboardingAction.Completion,
  state: OnboardingState,
): ReducerResult<OnboardingState, OnboardingEffect.Command, OnboardingEffect.Notification> =
  reducer<OnboardingAction.Completion, OnboardingState, OnboardingEffect.Command, OnboardingEffect.Notification> { a, s ->
    when (a) {
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
  }(action, state)
