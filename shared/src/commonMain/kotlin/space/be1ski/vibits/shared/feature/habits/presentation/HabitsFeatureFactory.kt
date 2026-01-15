package space.be1ski.vibits.shared.feature.habits.presentation

import space.be1ski.vibits.shared.feature.memos.domain.repository.MemosRepository
import space.be1ski.vibits.shared.core.elm.Feature
import space.be1ski.vibits.shared.core.elm.FeatureImpl

/**
 * Creates a new HabitsFeature instance.
 */
fun createHabitsFeature(
  memosRepository: MemosRepository,
  onRefresh: () -> Unit,
  initialState: HabitsState = HabitsState()
): Feature<HabitsAction, HabitsState, HabitsEffect> {
  return FeatureImpl(
    initialState = initialState,
    reducer = habitsReducer,
    effectHandler = HabitsEffectHandler(memosRepository, onRefresh)
  )
}
