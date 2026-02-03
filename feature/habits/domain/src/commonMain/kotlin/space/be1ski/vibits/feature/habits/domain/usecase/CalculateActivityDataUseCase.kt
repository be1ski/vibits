package space.be1ski.vibits.feature.habits.domain.usecase

import kotlinx.datetime.TimeZone
import space.be1ski.vibits.core.platform.date.currentLocalDate
import space.be1ski.vibits.feature.habits.domain.model.ActivityMode
import space.be1ski.vibits.feature.habits.domain.model.ActivityRange
import space.be1ski.vibits.feature.habits.domain.model.ActivityWeekData
import space.be1ski.vibits.feature.habits.domain.model.HabitsConfigEntry
import space.be1ski.vibits.feature.habits.domain.model.SuccessRateData
import space.be1ski.vibits.feature.memos.domain.model.Memo

class CalculateActivityDataUseCase(
  private val buildActivityDataUseCase: BuildActivityDataUseCase,
  private val calculateSuccessRateUseCase: CalculateSuccessRateUseCase,
) {
  operator fun invoke(
    range: ActivityRange,
    mode: ActivityMode,
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
}

data class CachedActivityData(
  val weekData: ActivityWeekData,
  val configTimeline: List<HabitsConfigEntry>,
  val successRate: SuccessRateData?,
)
