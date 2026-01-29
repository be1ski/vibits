package space.be1ski.vibits.shared.feature.habits.presentation.handler

import kotlinx.coroutines.flow.Flow
import space.be1ski.vibits.shared.core.elm.EffectHandler
import space.be1ski.vibits.shared.core.elm.actions
import space.be1ski.vibits.shared.core.logging.Log
import space.be1ski.vibits.shared.feature.habits.domain.usecase.CalculateActivityDataUseCase
import space.be1ski.vibits.shared.feature.habits.domain.usecase.PrewarmActivityDataUseCase
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsAction
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsEffect

private const val TAG = "HabitsActivityEffect"

class HabitsActivityEffectHandler(
  private val calculateActivityDataUseCase: CalculateActivityDataUseCase,
  private val prewarmActivityDataUseCase: PrewarmActivityDataUseCase,
) : EffectHandler<HabitsEffect.Activity, HabitsAction> {
  override fun invoke(effect: HabitsEffect.Activity): Flow<HabitsAction> =
    when (effect) {
      is HabitsEffect.RunPrewarmAllRanges -> handleRunPrewarmAllRanges(effect)
      is HabitsEffect.RecalculateActivityData -> handleRecalculateActivityData(effect)
    }

  private fun handleRunPrewarmAllRanges(effect: HabitsEffect.RunPrewarmAllRanges): Flow<HabitsAction> =
    actions {
      Log.d(TAG, "Prewarming all ranges for AppMode: ${effect.appMode}")
      val results = prewarmActivityDataUseCase(effect.memos, effect.appMode)
      // Emit AFTER withContext (TC-04: avoid Flow invariant violation)
      results.forEach { result ->
        emit(
          HabitsAction.UpdateActivityData(
            range = result.range,
            mode = result.mode,
            appMode = result.appMode,
            weekData = result.weekData,
            configTimeline = result.configTimeline,
            successRate = result.successRate,
          ),
        )
      }
      emit(HabitsAction.PrewarmCompleted)
    }

  private fun handleRecalculateActivityData(effect: HabitsEffect.RecalculateActivityData): Flow<HabitsAction> =
    actions {
      Log.d(TAG, "Recalculating for ${effect.range}")
      val result = calculateActivityDataUseCase(effect.range, effect.mode, effect.memos)
      emit(
        HabitsAction.UpdateActivityData(
          range = effect.range,
          mode = effect.mode,
          appMode = effect.appMode,
          weekData = result.weekData,
          configTimeline = result.configTimeline,
          successRate = result.successRate,
        ),
      )
    }
}
