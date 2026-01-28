package space.be1ski.vibits.shared.feature.habits.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import space.be1ski.vibits.shared.app.domain.model.ActivityMode
import space.be1ski.vibits.shared.core.elm.EffectHandler
import space.be1ski.vibits.shared.core.logging.Log
import space.be1ski.vibits.shared.core.platform.date.currentLocalDate
import space.be1ski.vibits.shared.feature.habits.domain.usecase.BuildActivityDataUseCase
import space.be1ski.vibits.shared.feature.habits.domain.usecase.CalculateSuccessRateUseCase
import space.be1ski.vibits.shared.feature.habits.domain.usecase.ExtractDailyMemosUseCase
import space.be1ski.vibits.shared.feature.habits.domain.usecase.ExtractHabitsConfigUseCase
import space.be1ski.vibits.shared.feature.memos.domain.repository.MemosRepository

private const val TAG = "HabitsEffect"

/**
 * Effect handler for the Habits feature.
 * Converts side effects into actions.
 */
class HabitsEffectHandler(
  private val memosRepository: MemosRepository,
  private val buildActivityDataUseCase: BuildActivityDataUseCase,
  private val calculateSuccessRate: CalculateSuccessRateUseCase,
  private val onRefresh: () -> Unit,
) : EffectHandler<HabitsEffect, HabitsAction> {
  override fun invoke(effect: HabitsEffect): Flow<HabitsAction> =
    flow {
      when (effect) {
        is HabitsEffect.CreateMemo -> handleCreateMemo(effect)
        is HabitsEffect.UpdateMemo -> handleUpdateMemo(effect)
        is HabitsEffect.DeleteMemo -> handleDeleteMemo(effect)
        is HabitsEffect.RefreshMemos -> handleRefreshMemos()
        is HabitsEffect.RecalculateActivityData -> emit(recalculateActivityData(effect))
      }
    }

  private suspend fun FlowCollector<HabitsAction>.handleCreateMemo(effect: HabitsEffect.CreateMemo) {
    Log.d(TAG, "Creating habit memo")
    runCatching { memosRepository.createMemo(effect.content) }
      .onSuccess { memo -> emit(HabitsAction.MemoCreated(memo)) }
      .onFailure { error ->
        Log.e(TAG, "Failed to create habit memo", error)
        emit(HabitsAction.MemoOperationFailed(error.message ?: "Failed to create memo"))
      }
  }

  private suspend fun FlowCollector<HabitsAction>.handleUpdateMemo(effect: HabitsEffect.UpdateMemo) {
    Log.d(TAG, "Updating habit memo: ${effect.name}")
    runCatching { memosRepository.updateMemo(effect.name, effect.content) }
      .onSuccess { memo -> emit(HabitsAction.MemoUpdated(memo)) }
      .onFailure { error ->
        Log.e(TAG, "Failed to update habit memo", error)
        emit(HabitsAction.MemoOperationFailed(error.message ?: "Failed to update memo"))
      }
  }

  private suspend fun FlowCollector<HabitsAction>.handleDeleteMemo(effect: HabitsEffect.DeleteMemo) {
    Log.d(TAG, "Deleting habit memo: ${effect.name}")
    runCatching { memosRepository.deleteMemo(effect.name) }
      .onSuccess { emit(HabitsAction.MemoDeleted(effect.name)) }
      .onFailure { error ->
        Log.e(TAG, "Failed to delete habit memo", error)
        emit(HabitsAction.MemoOperationFailed(error.message ?: "Failed to delete memo"))
      }
  }

  private fun handleRefreshMemos() {
    Log.d(TAG, "Refreshing memos")
    onRefresh()
  }

  private suspend fun recalculateActivityData(effect: HabitsEffect.RecalculateActivityData): HabitsAction {
    Log.d(TAG, "Recalculating activity data: range=${effect.range} mode=${effect.mode}")
    val result =
      withContext(Dispatchers.Default) {
        val timeZone = TimeZone.currentSystemDefault()
        val today = currentLocalDate()
        val memos = effect.memos

        val configTimeline = ExtractHabitsConfigUseCase(memos, timeZone)
        val dailyMemos = ExtractDailyMemosUseCase(memos, timeZone)

        val weekData =
          buildActivityDataUseCase.buildWeekData(
            configTimeline = configTimeline,
            dailyMemos = dailyMemos,
            timeZone = timeZone,
            memos = memos,
            range = effect.range,
            mode = effect.mode,
            today = today,
          )

        val configStartDate = configTimeline.firstOrNull()?.date
        val successRate =
          if (effect.mode == ActivityMode.HABITS && configTimeline.isNotEmpty()) {
            calculateSuccessRate(weekData, effect.range, today, configStartDate)
          } else {
            null
          }

        Log.d(
          TAG,
          "Activity data calculated: range=${effect.range} mode=${effect.mode} " +
            "weekDataMaxDaily=${weekData.maxDaily} successRate=${successRate?.rate}",
        )

        Triple(weekData, configTimeline, successRate)
      }

    return HabitsAction.UpdateActivityData(
      range = effect.range,
      mode = effect.mode,
      weekData = result.first,
      configTimeline = result.second,
      successRate = result.third,
    )
  }
}
