package space.be1ski.vibits.shared.feature.habits.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import space.be1ski.vibits.shared.app.domain.model.ActivityMode
import space.be1ski.vibits.shared.app.domain.model.ActivityRange
import space.be1ski.vibits.shared.core.elm.EffectHandler
import space.be1ski.vibits.shared.core.logging.Log
import space.be1ski.vibits.shared.core.platform.date.currentLocalDate
import space.be1ski.vibits.shared.feature.habits.domain.model.ActivityWeekData
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitsConfigEntry
import space.be1ski.vibits.shared.feature.habits.domain.model.SuccessRateData
import space.be1ski.vibits.shared.feature.habits.domain.usecase.BuildActivityDataUseCase
import space.be1ski.vibits.shared.feature.habits.domain.usecase.CalculateSuccessRateUseCase
import space.be1ski.vibits.shared.feature.habits.domain.usecase.EarliestMemoDateUseCase
import space.be1ski.vibits.shared.feature.habits.domain.usecase.ExtractDailyMemosUseCase
import space.be1ski.vibits.shared.feature.habits.domain.usecase.ExtractHabitsConfigUseCase
import space.be1ski.vibits.shared.feature.habits.domain.usecase.quarterIndex
import space.be1ski.vibits.shared.feature.habits.domain.usecase.startOfWeek
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.memos.domain.repository.MemosRepository
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode

private const val TAG = "HabitsEffect"

/**
 * Effect handler for the Habits feature.
 * Converts side effects into actions.
 */
class HabitsEffectHandler(
  private val memosRepository: MemosRepository,
  private val onRefresh: () -> Unit,
  private val buildActivityDataUseCase: BuildActivityDataUseCase,
  private val calculateSuccessRateUseCase: CalculateSuccessRateUseCase,
) : EffectHandler<HabitsEffect, HabitsAction> {
  @Suppress("LongMethod")
  override fun invoke(effect: HabitsEffect): Flow<HabitsAction> =
    flow {
      when (effect) {
        is HabitsEffect.CreateMemo -> {
          Log.d(TAG, "Creating habit memo")
          runCatching { memosRepository.createMemo(effect.content) }
            .onSuccess { memo -> emit(HabitsAction.MemoCreated(memo)) }
            .onFailure { error ->
              Log.e(TAG, "Failed to create habit memo", error)
              emit(HabitsAction.MemoOperationFailed(error.message ?: "Failed to create memo"))
            }
        }

        is HabitsEffect.UpdateMemo -> {
          Log.d(TAG, "Updating habit memo: ${effect.name}")
          runCatching { memosRepository.updateMemo(effect.name, effect.content) }
            .onSuccess { memo -> emit(HabitsAction.MemoUpdated(memo)) }
            .onFailure { error ->
              Log.e(TAG, "Failed to update habit memo", error)
              emit(HabitsAction.MemoOperationFailed(error.message ?: "Failed to update memo"))
            }
        }

        is HabitsEffect.DeleteMemo -> {
          Log.d(TAG, "Deleting habit memo: ${effect.name}")
          runCatching { memosRepository.deleteMemo(effect.name) }
            .onSuccess { emit(HabitsAction.MemoDeleted(effect.name)) }
            .onFailure { error ->
              Log.e(TAG, "Failed to delete habit memo", error)
              emit(HabitsAction.MemoOperationFailed(error.message ?: "Failed to delete memo"))
            }
        }

        is HabitsEffect.RefreshMemos -> {
          Log.d(TAG, "Refreshing memos")
          onRefresh()
        }

        is HabitsEffect.RunPrewarmAllRanges -> {
          Log.d(TAG, "Prewarming all ranges for AppMode: ${effect.appMode}")
          val results = prewarmAllRanges(effect.memos, effect.appMode)
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

        is HabitsEffect.RecalculateActivityData -> {
          Log.d(TAG, "Recalculating for ${effect.range}")
          val result = calculateActivityData(effect.range, effect.mode, effect.appMode, effect.memos)
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
    }

  private suspend fun prewarmAllRanges(
    memos: List<Memo>,
    appMode: AppMode,
  ): List<PrewarmResult> {
    val timeZone = TimeZone.currentSystemDefault()
    val earliestDate = EarliestMemoDateUseCase(memos, timeZone) ?: return emptyList()
    val today = currentLocalDate()

    val ranges =
      buildList {
        addAll(generateWeeks(earliestDate, today))
        addAll(generateMonths(earliestDate, today))
        addAll(generateQuarters(earliestDate, today))
        addAll(generateYears(earliestDate, today))
      }

    val modes = listOf(ActivityMode.HABITS, ActivityMode.POSTS)

    return withContext(Dispatchers.Default) {
      ranges
        .flatMap { range ->
          modes.map { mode ->
            async {
              val data = calculateActivityData(range, mode, appMode, memos)
              PrewarmResult(range, mode, appMode, data.weekData, data.configTimeline, data.successRate)
            }
          }
        }.awaitAll()
    }
  }

  private fun calculateActivityData(
    range: ActivityRange,
    mode: ActivityMode,
    @Suppress("UnusedParameter") appMode: AppMode,
    memos: List<Memo>,
  ): CachedActivityData {
    val timeZone = TimeZone.currentSystemDefault()
    val today = currentLocalDate()
    val configTimeline = ExtractHabitsConfigUseCase(memos, timeZone)
    val dailyMemos = ExtractDailyMemosUseCase(memos, timeZone)
    val weekData =
      buildActivityDataUseCase.buildWeekData(
        configTimeline = if (mode == ActivityMode.HABITS) configTimeline else emptyList(),
        dailyMemos = dailyMemos,
        timeZone = timeZone,
        memos = memos,
        range = range,
        mode = mode,
        today = today,
      )
    val configStartDate = configTimeline.firstOrNull()?.date
    val successRate =
      if (mode == ActivityMode.HABITS && configTimeline.isNotEmpty()) {
        calculateSuccessRateUseCase(weekData, range, today, configStartDate)
      } else {
        null
      }

    return CachedActivityData(
      weekData = weekData,
      configTimeline = configTimeline,
      successRate = successRate,
    )
  }

  private fun generateWeeks(
    startDate: LocalDate,
    endDate: LocalDate,
  ): List<ActivityRange.Week> {
    val weeks = mutableListOf<ActivityRange.Week>()
    var cursor = startOfWeek(startDate)
    while (cursor <= endDate) {
      weeks.add(ActivityRange.Week(cursor))
      cursor = cursor.plus(DatePeriod(days = 7))
    }
    return weeks
  }

  private fun generateMonths(
    startDate: LocalDate,
    endDate: LocalDate,
  ): List<ActivityRange.Month> {
    val months = mutableListOf<ActivityRange.Month>()
    var cursor = ActivityRange.Month(startDate.year, startDate.month)
    val end = ActivityRange.Month(endDate.year, endDate.month)
    while (cursor.year < end.year || (cursor.year == end.year && cursor.month <= end.month)) {
      months.add(cursor)
      val nextDate = LocalDate(cursor.year, cursor.month, 1).plus(DatePeriod(months = 1))
      cursor = ActivityRange.Month(nextDate.year, nextDate.month)
    }
    return months
  }

  @Suppress("MagicNumber")
  private fun generateQuarters(
    startDate: LocalDate,
    endDate: LocalDate,
  ): List<ActivityRange.Quarter> {
    val quarters = mutableListOf<ActivityRange.Quarter>()
    val startQuarter = quarterIndex(startDate)
    val endQuarter = quarterIndex(endDate)
    var yearCursor = startDate.year
    var quarterCursor = startQuarter
    while (yearCursor < endDate.year || (yearCursor == endDate.year && quarterCursor <= endQuarter)) {
      quarters.add(ActivityRange.Quarter(yearCursor, quarterCursor))
      quarterCursor++
      if (quarterCursor > 4) {
        quarterCursor = 1
        yearCursor++
      }
    }
    return quarters
  }

  private fun generateYears(
    startDate: LocalDate,
    endDate: LocalDate,
  ): List<ActivityRange.Year> = (startDate.year..endDate.year).map { ActivityRange.Year(it) }
}

private data class PrewarmResult(
  val range: ActivityRange,
  val mode: ActivityMode,
  val appMode: AppMode,
  val weekData: ActivityWeekData,
  val configTimeline: List<HabitsConfigEntry>,
  val successRate: SuccessRateData?,
)
