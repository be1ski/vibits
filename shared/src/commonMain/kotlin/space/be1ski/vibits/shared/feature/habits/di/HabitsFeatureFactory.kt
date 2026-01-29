package space.be1ski.vibits.shared.feature.habits.di

import space.be1ski.vibits.shared.core.elm.Feature
import space.be1ski.vibits.shared.core.elm.FeatureImpl
import space.be1ski.vibits.shared.feature.habits.domain.usecase.CalculateActivityDataUseCase
import space.be1ski.vibits.shared.feature.habits.domain.usecase.PrewarmActivityDataUseCase
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsAction
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsActivityEffectHandler
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsEffect
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsEffectHandler
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsMemoEffectHandler
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsRefreshEffectHandler
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsState
import space.be1ski.vibits.shared.feature.habits.presentation.habitsReducer

/**
 * Creates a new HabitsFeature instance.
 */
fun createHabitsFeature(
  dependencies: HabitsDependencies,
  onRefresh: () -> Unit,
  initialState: HabitsState = HabitsState(),
): Feature<HabitsAction, HabitsState, HabitsEffect, Nothing> =
  FeatureImpl(
    initialState = initialState,
    reducer = habitsReducer,
    effectHandler =
      HabitsEffectHandler(
        memoHandler =
          HabitsMemoEffectHandler(
            memosRepository = dependencies.memosRepository,
          ),
        refreshHandler =
          HabitsRefreshEffectHandler(
            onRefresh = onRefresh,
          ),
        activityHandler =
          HabitsActivityEffectHandler(
            calculateActivityDataUseCase =
              CalculateActivityDataUseCase(
                buildActivityDataUseCase = dependencies.buildActivityDataUseCase,
                calculateSuccessRateUseCase = dependencies.calculateSuccessRateUseCase,
              ),
            prewarmActivityDataUseCase =
              PrewarmActivityDataUseCase(
                calculateActivityDataUseCase =
                  CalculateActivityDataUseCase(
                    buildActivityDataUseCase = dependencies.buildActivityDataUseCase,
                    calculateSuccessRateUseCase = dependencies.calculateSuccessRateUseCase,
                  ),
              ),
          ),
      ),
  )
