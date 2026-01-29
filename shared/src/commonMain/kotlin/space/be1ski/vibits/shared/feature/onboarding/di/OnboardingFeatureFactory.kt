package space.be1ski.vibits.shared.feature.onboarding.di

import space.be1ski.vibits.shared.core.elm.Feature
import space.be1ski.vibits.shared.core.elm.FeatureImpl
import space.be1ski.vibits.shared.feature.onboarding.presentation.OnboardingAction
import space.be1ski.vibits.shared.feature.onboarding.presentation.OnboardingEffect
import space.be1ski.vibits.shared.feature.onboarding.presentation.OnboardingState
import space.be1ski.vibits.shared.feature.onboarding.presentation.handler.OnboardingCompletionEffectHandler
import space.be1ski.vibits.shared.feature.onboarding.presentation.handler.OnboardingEffectHandler
import space.be1ski.vibits.shared.feature.onboarding.presentation.handler.OnboardingPresetsEffectHandler
import space.be1ski.vibits.shared.feature.onboarding.presentation.handler.OnboardingSetupEffectHandler
import space.be1ski.vibits.shared.feature.onboarding.presentation.onboardingReducer

fun createOnboardingFeature(
  dependencies: OnboardingDependencies,
  initialState: OnboardingState = OnboardingState(),
): Feature<OnboardingAction, OnboardingState, OnboardingEffect.Command, OnboardingEffect.Notification> =
  FeatureImpl(
    initialState = initialState,
    reducer = onboardingReducer,
    effectHandler =
      OnboardingEffectHandler(
        presetsHandler =
          OnboardingPresetsEffectHandler(
            getHabitPresets = dependencies.getHabitPresets,
          ),
        setupHandler =
          OnboardingSetupEffectHandler(
            createFirstHabit = dependencies.createFirstHabit,
            createFirstCheckIn = dependencies.createFirstCheckIn,
          ),
        completionHandler =
          OnboardingCompletionEffectHandler(
            markOnboardingCompleted = dependencies.markOnboardingCompleted,
          ),
      ),
  )
