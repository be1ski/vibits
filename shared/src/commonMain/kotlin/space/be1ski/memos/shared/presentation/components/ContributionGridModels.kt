package space.be1ski.memos.shared.presentation.components

import androidx.compose.foundation.ScrollState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month as CalendarMonth
import space.be1ski.memos.shared.domain.model.memo.Memo

/**
 * UI state for rendering the contribution grid.
 */
data class ContributionGridState(
  val weekData: ActivityWeekData,
  val range: ActivityRange,
  val selectedDay: ContributionDay?,
  val selectedWeekStart: LocalDate?,
  val isActiveSelection: Boolean,
  val scrollState: ScrollState,
  val showWeekdayLegend: Boolean = false,
  val showAllWeekdayLabels: Boolean = false,
  val compactHeight: Boolean = false,
  val showTimeline: Boolean = false,
  val showDayNumbers: Boolean = false
)

/**
 * Event handlers for the contribution grid.
 */
data class ContributionGridCallbacks(
  val onDaySelected: (ContributionDay) -> Unit,
  val onEditRequested: (ContributionDay) -> Unit,
  val onCreateRequested: (ContributionDay) -> Unit,
  val onClearSelection: () -> Unit,
  val demoMode: Boolean = false
)

/**
 * UI state for rendering a weekly bar chart.
 */
data class WeeklyBarChartState(
  val weekData: ActivityWeekData,
  val selectedWeek: ActivityWeek?,
  val scrollState: ScrollState,
  val showWeekdayLegend: Boolean,
  val compactHeight: Boolean,
  val modifier: Modifier = Modifier
)

/**
 * Per-day activity entry.
 */
data class ContributionDay(
  /** Calendar date for the entry. */
  val date: LocalDate,
  /** Number of completed habits (or posts when habits are unavailable). */
  val count: Int,
  /** Total habits configured for the day. */
  val totalHabits: Int,
  /** Completion ratio in range [0..1]. */
  val completionRatio: Float,
  /** Parsed habit status list for the day. */
  val habitStatuses: List<HabitStatus>,
  /** Daily memo information when available. */
  val dailyMemo: DailyMemoInfo?,
  /** True if the day is within the requested range. */
  val inRange: Boolean
)

/**
 * Aggregated week data with daily breakdown.
 */
data class ActivityWeek(
  /** Start date of the week (Monday). */
  val startDate: LocalDate,
  /** Daily breakdown for the week. */
  val days: List<ContributionDay>,
  /** Sum of all posts in the week. */
  val weeklyCount: Int
)

/**
 * Fully prepared dataset for activity charts.
 */
data class ActivityWeekData(
  /** Ordered list of week entries. */
  val weeks: List<ActivityWeek>,
  /** Maximum posts for a single day in range. */
  val maxDaily: Int,
  /** Maximum posts in a week in range. */
  val maxWeekly: Int
)

/**
 * Habit completion status for a single habit tag.
 */
data class HabitStatus(
  /** Habit tag, e.g. #habits/зарядка. */
  val tag: String,
  /** Habit label for display. */
  val label: String,
  /** True when the habit is marked completed in a daily memo. */
  val done: Boolean
)

/**
 * Habit configuration entry.
 */
data class HabitConfig(
  /** Habit tag, e.g. #habits/зарядка. */
  val tag: String,
  /** User-friendly label. */
  val label: String
)

/**
 * Configuration entry with its effective date.
 */
data class HabitsConfigEntry(
  /** Date when config was created. */
  val date: LocalDate,
  /** Habits declared in the config. */
  val habits: List<HabitConfig>,
  /** Source memo. */
  val memo: Memo
)

/**
 * Daily memo metadata for editing.
 */
data class DailyMemoInfo(
  /** Memo resource name. */
  val name: String,
  /** Memo content. */
  val content: String
)

/**
 * Activity visualization modes.
 */
enum class ActivityMode {
  /** Habit completion based on #habits/daily + #habits/config. */
  Habits,
  /** Raw post count per day. */
  Posts
}

/**
 * Range selection for activity charts.
 */
sealed class ActivityRange {
  /** Fixed calendar week starting on Monday. */
  data class Week(val startDate: LocalDate) : ActivityRange()
  /** Fixed calendar month. */
  data class Month(val year: Int, val month: CalendarMonth) : ActivityRange()
  /** Fixed calendar quarter. */
  data class Quarter(val year: Int, val index: Int) : ActivityRange()
  /** Fixed calendar year. */
  data class Year(val year: Int) : ActivityRange()
}

internal data class ContributionCellState(
  val day: ContributionDay,
  val maxCount: Int,
  val enabled: Boolean,
  val size: Dp,
  val isSelected: Boolean,
  val isHovered: Boolean,
  val isWeekSelected: Boolean,
  val showDayNumber: Boolean
)

internal data class ContributionCellCallbacks(
  val onClick: ((IntOffset) -> Unit)?,
  val onHoverChange: ((Boolean) -> Unit)?
)
