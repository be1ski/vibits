package space.be1ski.vibits.shared.feature.habits.presentation

import kotlinx.coroutines.flow.Flow
import space.be1ski.vibits.shared.core.elm.EffectHandler

class HabitsEffectHandler(
  private val memoHandler: HabitsMemoEffectHandler,
  private val refreshHandler: HabitsRefreshEffectHandler,
  private val activityHandler: HabitsActivityEffectHandler,
) : EffectHandler<HabitsEffect, HabitsAction> {
  override fun invoke(effect: HabitsEffect): Flow<HabitsAction> =
    when (effect) {
      is HabitsEffect.Memo -> memoHandler(effect)
      is HabitsEffect.Refresh -> refreshHandler(effect)
      is HabitsEffect.Activity -> activityHandler(effect)
    }
}
