package space.be1ski.memos.shared.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import space.be1ski.memos.shared.domain.model.memo.Memo
import space.be1ski.memos.shared.presentation.time.currentLocalDate

private const val DAYS_IN_WEEK = 7

internal data class RangeBounds(
  val start: LocalDate,
  val end: LocalDate
)

internal data class DayDataContext(
  val date: LocalDate,
  val bounds: RangeBounds,
  val mode: ActivityMode,
  val configTimeline: List<HabitsConfigEntry>,
  val dailyMemos: Map<LocalDate, DailyMemoInfo>,
  val counts: Map<LocalDate, Int>
)

internal data class HabitSelectionState(
  val useHabits: Boolean,
  val habitStatuses: List<HabitStatus>,
  val memoHabitTags: Set<String>,
  val configTags: Set<String>
)

/**
 * Memoized builder for [ActivityWeekData].
 */
@Composable
fun rememberActivityWeekData(
  memos: List<Memo>,
  range: ActivityRange,
  mode: ActivityMode
): ActivityWeekData {
  val timeZone = remember { TimeZone.currentSystemDefault() }
  val today = currentLocalDate()
  return remember(memos, range, mode, today) {
    buildActivityWeekData(memos, timeZone, range, mode)
  }
}

/**
 * Memoized builder for habits config timeline.
 */
@Composable
fun rememberHabitsConfigTimeline(memos: List<Memo>): List<HabitsConfigEntry> {
  val timeZone = remember { TimeZone.currentSystemDefault() }
  return remember(memos, timeZone) {
    extractHabitsConfigEntries(memos, timeZone)
  }
}

/**
 * Calculates layout sizes for a fixed number of columns.
 */
internal fun calculateLayout(
  maxWidth: Dp,
  columns: Int,
  minColumnSize: Dp,
  spacing: Dp
): ChartLayout {
  val safeColumns = columns.coerceAtLeast(1)
  val totalSpacing = spacing * (safeColumns - 1)
  val calculated = (maxWidth - totalSpacing) / safeColumns
  val useScroll = calculated < minColumnSize
  val columnSize = if (useScroll) minColumnSize else calculated
  val contentWidth = if (useScroll) columnSize * safeColumns + totalSpacing else maxWidth
  return ChartLayout(columnSize = columnSize, contentWidth = contentWidth, useScroll = useScroll)
}

/**
 * Builds the chart dataset for a given [range].
 */
private fun buildActivityWeekData(
  memos: List<Memo>,
  timeZone: TimeZone,
  range: ActivityRange,
  mode: ActivityMode
): ActivityWeekData {
  val bounds = rangeBounds(range)
  val configTimeline = if (mode == ActivityMode.Habits) {
    extractHabitsConfigEntries(memos, timeZone)
  } else {
    emptyList()
  }
  val dailyMemos = extractDailyMemos(memos, timeZone)
  val counts = if (mode == ActivityMode.Posts) extractDailyPostCounts(memos, timeZone, bounds) else emptyMap()

  val start = startOfWeek(bounds.start)
  val weeks = mutableListOf<ActivityWeek>()
  var cursor = start
  while (cursor <= bounds.end) {
    val days = (0 until DAYS_IN_WEEK).map { offset ->
      buildDayData(
        DayDataContext(
          date = cursor.plus(DatePeriod(days = offset)),
          bounds = bounds,
          mode = mode,
          configTimeline = configTimeline,
          dailyMemos = dailyMemos,
          counts = counts
        )
      )
    }
    weeks.add(
      ActivityWeek(
        startDate = cursor,
        days = days,
        weeklyCount = days.sumOf { it.count }
      )
    )
    cursor = cursor.plus(DatePeriod(days = DAYS_IN_WEEK))
  }
  val maxDaily = weeks.maxOfOrNull { week -> week.days.maxOfOrNull { it.count } ?: 0 } ?: 0
  val maxWeekly = weeks.maxOfOrNull { it.weeklyCount } ?: 0
  return ActivityWeekData(weeks = weeks, maxDaily = maxDaily, maxWeekly = maxWeekly)
}
