package space.be1ski.vibits.shared.feature.habits.domain.usecase

import dev.zacsweers.metro.Inject
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import space.be1ski.vibits.shared.core.ui.ActivityMode
import space.be1ski.vibits.shared.core.ui.ActivityRange
import space.be1ski.vibits.shared.feature.habits.domain.model.ActivityWeek
import space.be1ski.vibits.shared.feature.habits.domain.model.ActivityWeekData
import space.be1ski.vibits.shared.feature.habits.domain.model.DailyMemoInfo
import space.be1ski.vibits.shared.feature.habits.domain.model.DayBuildContext
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitsConfigEntry
import space.be1ski.vibits.shared.feature.habits.domain.model.rangeBounds
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo

private const val DAYS_IN_WEEK = 7

/**
 * Use case for building activity data.
 */
@Inject
class BuildActivityDataUseCase(
  private val buildDayDataUseCase: BuildDayDataUseCase,
) {
  /**
   * Builds ActivityWeekData for a given range.
   */
  @Suppress("LongParameterList")
  fun buildWeekData(
    configTimeline: List<HabitsConfigEntry>,
    dailyMemos: Map<LocalDate, DailyMemoInfo>,
    timeZone: TimeZone,
    memos: List<Memo>,
    range: ActivityRange,
    mode: ActivityMode,
    today: LocalDate,
  ): ActivityWeekData {
    val bounds = rangeBounds(range)
    val effectiveConfigTimeline = if (mode == ActivityMode.HABITS) configTimeline else emptyList()
    val counts =
      if (mode == ActivityMode.POSTS) CountDailyPostsUseCase(memos, timeZone, bounds) else emptyMap()

    val start = startOfWeek(bounds.start)
    val weeks = mutableListOf<ActivityWeek>()
    var cursor = start
    while (cursor <= bounds.end) {
      val days =
        (0 until DAYS_IN_WEEK).map { offset ->
          buildDayDataUseCase(
            DayBuildContext(
              date = cursor.plus(DatePeriod(days = offset)),
              bounds = bounds,
              mode = mode,
              configTimeline = effectiveConfigTimeline,
              dailyMemos = dailyMemos,
              counts = counts,
              today = today,
            ),
          )
        }
      weeks.add(
        ActivityWeek(
          startDate = cursor,
          days = days,
          weeklyCount = days.sumOf { it.count },
        ),
      )
      cursor = cursor.plus(DatePeriod(days = DAYS_IN_WEEK))
    }
    val maxDaily = weeks.maxOfOrNull { week -> week.days.maxOfOrNull { it.count } ?: 0 } ?: 0
    val maxWeekly = weeks.maxOfOrNull { it.weeklyCount } ?: 0
    return ActivityWeekData(weeks = weeks, maxDaily = maxDaily, maxWeekly = maxWeekly)
  }
}
