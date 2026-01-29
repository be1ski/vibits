package space.be1ski.vibits.shared.feature.habits.presentation.handler

import kotlinx.coroutines.flow.Flow
import space.be1ski.vibits.shared.core.elm.EffectHandler
import space.be1ski.vibits.shared.core.elm.sideEffect
import space.be1ski.vibits.shared.core.logging.Log
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsAction
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsEffect

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
