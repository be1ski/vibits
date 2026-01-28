package space.be1ski.vibits.shared.feature.onboarding.di

import space.be1ski.vibits.shared.core.elm.Feature
import space.be1ski.vibits.shared.core.elm.FeatureImpl
import space.be1ski.vibits.shared.feature.onboarding.presentation.OnboardingAction
import space.be1ski.vibits.shared.feature.onboarding.presentation.OnboardingEffect
import space.be1ski.vibits.shared.feature.onboarding.presentation.OnboardingEffectHandler
import space.be1ski.vibits.shared.feature.onboarding.presentation.OnboardingState
import space.be1ski.vibits.shared.feature.onboarding.presentation.onboardingReducer

fun createOnboardingFeature(
  dependencies: OnboardingDependencies,
  initialState: OnboardingState = OnboardingState(),
): Feature<OnboardingAction, OnboardingState, OnboardingEffect> =
  FeatureImpl(
    initialState = initialState,
    reducer = onboardingReducer,
    effectHandler =
      OnboardingEffectHandler(
        getHabitPresets = dependencies.getHabitPresets,
        createFirstHabit = dependencies.createFirstHabit,
        markOnboardingCompleted = dependencies.markOnboardingCompleted,
      ),
  )
