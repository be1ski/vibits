package space.be1ski.vibits.shared.feature.habits.di

import space.be1ski.vibits.shared.core.elm.Feature
import space.be1ski.vibits.shared.core.elm.FeatureImpl
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsAction
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsEffect
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsEffectHandler
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsState
import space.be1ski.vibits.shared.feature.habits.presentation.habitsReducer

/**
 * Creates a new HabitsFeature instance.
 */
fun createHabitsFeature(
  dependencies: HabitsDependencies,
  onRefresh: () -> Unit,
  initialState: HabitsState = HabitsState(),
): Feature<HabitsAction, HabitsState, HabitsEffect> =
  FeatureImpl(
    initialState = initialState,
    reducer = habitsReducer,
    effectHandler =
      HabitsEffectHandler(
        memosRepository = dependencies.memosRepository,
        buildActivityDataUseCase = dependencies.buildActivityDataUseCase,
        calculateSuccessRate = dependencies.calculateSuccessRate,
        onRefresh = onRefresh,
      ),
  )
