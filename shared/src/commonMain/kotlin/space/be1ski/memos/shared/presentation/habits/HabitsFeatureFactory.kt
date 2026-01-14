package space.be1ski.memos.shared.presentation.habits

import space.be1ski.memos.shared.domain.repository.MemosRepository
import space.be1ski.memos.shared.elm.Feature
import space.be1ski.memos.shared.elm.FeatureImpl

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
