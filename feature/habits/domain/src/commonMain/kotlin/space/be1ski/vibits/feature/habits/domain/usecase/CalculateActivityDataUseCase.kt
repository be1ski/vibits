package space.be1ski.vibits.feature.habits.domain.usecase

import kotlinx.datetime.TimeZone
import space.be1ski.vibits.core.platform.date.currentLocalDate
import space.be1ski.vibits.feature.habits.domain.model.ActivityMode
import space.be1ski.vibits.feature.habits.domain.model.ActivityRange
import space.be1ski.vibits.feature.habits.domain.model.CachedActivityData
import space.be1ski.vibits.feature.memos.domain.model.Memo

class CalculateActivityDataUseCase(
  private val buildActivityDataUseCase: BuildActivityDataUseCase,
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
        CalculateSuccessRateUseCase(weekData, range, today, configStartDate)
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
