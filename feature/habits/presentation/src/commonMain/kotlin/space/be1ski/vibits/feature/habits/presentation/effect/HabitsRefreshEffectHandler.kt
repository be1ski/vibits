package space.be1ski.vibits.feature.habits.presentation.effect

import kotlinx.coroutines.flow.Flow
import space.be1ski.vibits.core.elm.EffectHandler
import space.be1ski.vibits.core.elm.sideEffect
import space.be1ski.vibits.core.logging.Log
import space.be1ski.vibits.feature.habits.presentation.action.HabitsAction

private const val TAG = "HabitsRefreshEffect"

class HabitsRefreshEffectHandler(
  private val onRefresh: () -> Unit,
) : EffectHandler<HabitsEffect.Refresh, HabitsAction> {
  override fun invoke(effect: HabitsEffect.Refresh): Flow<HabitsAction> =
    when (effect) {
      HabitsEffect.RefreshMemos -> handleRefreshMemos()
    }

  private fun handleRefreshMemos(): Flow<HabitsAction> =
    sideEffect {
      Log.d(TAG, "Refreshing memos")
      onRefresh()
    }
}
