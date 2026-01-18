package space.be1ski.vibits.shared.feature.habits.domain.usecase

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import space.be1ski.vibits.shared.core.ui.ActivityMode
import space.be1ski.vibits.shared.core.ui.ActivityRange
import space.be1ski.vibits.shared.feature.habits.domain.model.ActivityWeek
import space.be1ski.vibits.shared.feature.habits.domain.model.ActivityWeekData
import space.be1ski.vibits.shared.feature.habits.domain.model.ContributionDay
import space.be1ski.vibits.shared.feature.habits.domain.model.DailyMemoInfo
import space.be1ski.vibits.shared.feature.habits.domain.model.DayBuildContext
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitConfig
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitStatus
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitsConfigEntry
import space.be1ski.vibits.shared.feature.habits.domain.model.RangeBounds
import space.be1ski.vibits.shared.feature.habits.domain.model.rangeBounds
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo

private const val LAST_SEVEN_DAYS = 7
private const val DAYS_IN_WEEK = 7

/**
 * Use case for building and filtering activity data.
 */
class BuildActivityDataUseCase {
  private val buildDayDataUseCase = BuildDayDataUseCase()
  private val dateCalculationsUseCase = DateCalculationsUseCase()
  private val extractDailyMemosUseCase = ExtractDailyMemosUseCase()
  private val countDailyPostsUseCase = CountDailyPostsUseCase()
  private val extractHabitsConfigUseCase = ExtractHabitsConfigUseCase()

  /**
   * Returns the last 7 in-range days from activity data.
   */
  fun lastSevenDays(weekData: ActivityWeekData): List<ContributionDay> {
    val days = weekData.weeks.flatMap { it.days }.filter { it.inRange }
    return days.takeLast(LAST_SEVEN_DAYS)
  }

  /**
   * Finds a contribution day by date.
   */
  fun findDayByDate(
    weekData: ActivityWeekData,
    date: LocalDate,
  ): ContributionDay? =
    weekData.weeks.firstNotNullOfOrNull { week ->
      week.days.firstOrNull { it.date == date }
    }

  /**
   * Filters activity data for a single habit.
   */
  fun activityWeekDataForHabit(
    weekData: ActivityWeekData,
    habit: HabitConfig,
  ): ActivityWeekData {
    val weeks =
      weekData.weeks.map { week ->
        val days =
          week.days.map { day ->
            val hasConfig = day.totalHabits > 0
            val done =
              if (hasConfig) {
                day.habitStatuses.firstOrNull { it.tag == habit.tag }?.done == true
              } else {
                false
              }
            val count = if (hasConfig && done) 1 else 0
            day.copy(
              count = count,
              totalHabits = if (hasConfig) 1 else 0,
              completionRatio = if (hasConfig && done) 1f else 0f,
              habitStatuses =
                if (hasConfig) {
                  listOf(HabitStatus(tag = habit.tag, label = habit.label, done = done))
                } else {
                  emptyList()
                },
            )
          }
        week.copy(
          days = days,
          weeklyCount = days.sumOf { it.count },
        )
      }
    val maxDaily = weeks.maxOfOrNull { week -> week.days.maxOfOrNull { it.count } ?: 0 } ?: 0
    val maxWeekly = weeks.maxOfOrNull { it.weeklyCount } ?: 0
    return ActivityWeekData(weeks = weeks, maxDaily = maxDaily, maxWeekly = maxWeekly)
  }

  /**
   * Builds ActivityWeekData for a given range.
   * Uses pre-extracted configTimeline and dailyMemos to avoid redundant work on range change.
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
    val counts = if (mode == ActivityMode.POSTS) countDailyPostsUseCase(memos, timeZone, bounds) else emptyMap()

    val start = dateCalculationsUseCase.startOfWeek(bounds.start)
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

  /**
   * Extracts habits config timeline from memos.
   */
  fun extractConfigTimeline(
    memos: List<Memo>,
    timeZone: TimeZone,
  ): List<HabitsConfigEntry> = extractHabitsConfigUseCase(memos, timeZone)

  /**
   * Extracts daily memos map from memos.
   */
  fun extractDailyMemos(
    memos: List<Memo>,
    timeZone: TimeZone,
  ): Map<LocalDate, DailyMemoInfo> = extractDailyMemosUseCase(memos, timeZone)
}
