package space.be1ski.memos.shared.presentation.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.math.max
import kotlin.time.Instant as KtInstant
import space.be1ski.memos.shared.domain.model.memo.Memo
import space.be1ski.memos.shared.presentation.time.currentLocalDate

internal object ChartDimens {
  val legendWidth = 26.dp

  fun spacing(compact: Boolean): Dp = if (compact) 1.dp else 2.dp

  fun minCell(compact: Boolean): Dp = if (compact) 7.dp else 10.dp
}

@Composable
/**
 * Renders GitHub-style daily activity grid for the provided [weekData].
 */
fun ContributionGrid(
  weekData: ActivityWeekData,
  range: ActivityRange,
  selectedDay: ContributionDay?,
  selectedWeekStart: LocalDate?,
  onDaySelected: (ContributionDay) -> Unit,
  onEditRequested: (ContributionDay) -> Unit,
  onCreateRequested: (ContributionDay) -> Unit,
  onClearSelection: () -> Unit,
  isActiveSelection: Boolean = true,
  scrollState: ScrollState,
  showWeekdayLegend: Boolean = false,
  compactHeight: Boolean = false,
  showTimeline: Boolean = false,
  modifier: Modifier = Modifier
) {
  var tooltip by remember { mutableStateOf<DayTooltip?>(null) }
  var hoveredDate by remember { mutableStateOf<LocalDate?>(null) }
  var suppressClear by remember { mutableStateOf(false) }
  if (!isActiveSelection && tooltip != null) {
    tooltip = null
  }
  Column(modifier = modifier) {
    BoxWithConstraints(
      modifier = Modifier
        .fillMaxWidth()
        .pointerInput(Unit) {
          detectTapGestures(
            onTap = {
              if (suppressClear) {
                suppressClear = false
                return@detectTapGestures
              }
              tooltip = null
              hoveredDate = null
              onClearSelection()
            }
          )
      }
    ) {
      val columns = weekData.weeks.size.coerceAtLeast(1)
      val spacing = ChartDimens.spacing(compactHeight)
      val minCell = ChartDimens.minCell(compactHeight)
      val legendWidth = if (showWeekdayLegend) ChartDimens.legendWidth else 0.dp
      val legendSpacing = if (showWeekdayLegend) spacing else 0.dp
      val availableWidth = (maxWidth - legendWidth - legendSpacing).coerceAtLeast(0.dp)
      val layout = calculateLayout(availableWidth, columns, minColumnSize = minCell, spacing = spacing)
      val timelineLabels = remember(weekData.weeks, range) {
        if (showTimeline) buildTimelineLabels(weekData.weeks, range) else emptyList()
      }

      Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
          if (showWeekdayLegend) {
            DayOfWeekLegend(
              cellSize = layout.columnSize,
              spacing = spacing
            )
          }
          Row(
            modifier = Modifier
              .width(layout.contentWidth)
              .then(if (layout.useScroll) Modifier.horizontalScroll(scrollState) else Modifier),
            horizontalArrangement = Arrangement.spacedBy(spacing)
          ) {
            weekData.weeks.forEach { week ->
              val isWeekSelected = selectedWeekStart == week.startDate
              Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
                week.days.forEach { day ->
                  ContributionCell(
                    day = day,
                    maxCount = weekData.maxDaily,
                    enabled = day.inRange,
                    size = layout.columnSize,
                    isSelected = selectedDay?.date == day.date,
                    isHovered = hoveredDate == day.date,
                    isWeekSelected = isWeekSelected,
                    onClick = { offset ->
                      onDaySelected(day)
                      tooltip = DayTooltip(day, offset)
                      suppressClear = true
                    },
                    onHoverChange = { hovering ->
                      hoveredDate = if (hovering) day.date else hoveredDate?.takeIf { it != day.date }
                    }
                  )
                }
              }
            }
          }
        }
        if (showTimeline) {
          TimelineRow(
            labels = timelineLabels,
            cellSize = layout.columnSize,
            contentWidth = layout.contentWidth,
            spacing = spacing,
            legendWidth = legendWidth,
            legendSpacing = legendSpacing,
            useScroll = layout.useScroll,
            scrollState = scrollState
          )
        }
      }
    }
    tooltip?.let { current ->
      val positionProvider = remember(current.offset) {
        object : PopupPositionProvider {
          override fun calculatePosition(
            anchorBounds: IntRect,
            windowSize: IntSize,
            layoutDirection: LayoutDirection,
            popupContentSize: IntSize
          ): IntOffset {
            val maxX = (windowSize.width - popupContentSize.width).coerceAtLeast(0)
            val maxY = (windowSize.height - popupContentSize.height).coerceAtLeast(0)
            val x = current.offset.x.coerceIn(0, maxX)
            val y = current.offset.y.coerceIn(0, maxY)
            return IntOffset(x, y)
          }
        }
      }
      Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = { tooltip = null }
      ) {
        Column(
          modifier = Modifier
            .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.small)
            .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
    val tooltipText = if (current.day.totalHabits > 0) {
      "${current.day.date}: ${current.day.count}/${current.day.totalHabits} habits"
    } else {
      "${current.day.date}: ${current.day.count} posts"
    }
          Text(tooltipText, style = MaterialTheme.typography.labelMedium)
          if (current.day.totalHabits > 0) {
            Column(modifier = Modifier.padding(top = 6.dp)) {
              current.day.habitStatuses.forEach { status ->
                val color = if (status.done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                val prefix = if (status.done) "\u2713 " else "\u2022 "
                Text("$prefix${status.label}", color = color, style = MaterialTheme.typography.labelSmall)
              }
            }
          }
          current.day.dailyMemo?.let {
            TextButton(onClick = { onEditRequested(current.day) }) {
              Text("Edit day")
            }
          } ?: run {
            if (current.day.totalHabits > 0 && current.day.inRange) {
              TextButton(onClick = { onCreateRequested(current.day) }) {
                Text("Create day")
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun DayOfWeekLegend(
  cellSize: Dp,
  spacing: Dp
) {
  val labelWidth = 26.dp
  val order = listOf(
    DayOfWeek.MONDAY,
    DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY,
    DayOfWeek.SATURDAY,
    DayOfWeek.SUNDAY
  )
  Column(
    verticalArrangement = Arrangement.spacedBy(spacing),
    modifier = Modifier.width(labelWidth)
  ) {
    order.forEach { day ->
      Box(
        modifier = Modifier.height(cellSize),
        contentAlignment = Alignment.CenterStart
      ) {
        val label = when (day) {
          DayOfWeek.MONDAY -> "Mon"
          DayOfWeek.WEDNESDAY -> "Wed"
          DayOfWeek.FRIDAY -> "Fri"
          else -> null
        }
        if (label != null) {
          Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}

@Composable
private fun TimelineRow(
  labels: List<String>,
  cellSize: Dp,
  contentWidth: Dp,
  spacing: Dp,
  legendWidth: Dp,
  legendSpacing: Dp,
  useScroll: Boolean,
  scrollState: ScrollState
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(spacing)
  ) {
    if (legendWidth > 0.dp) {
      Spacer(modifier = Modifier.width(legendWidth + legendSpacing))
    }
    Row(
      modifier = Modifier
        .width(contentWidth)
        .then(if (useScroll) Modifier.horizontalScroll(scrollState) else Modifier),
      horizontalArrangement = Arrangement.spacedBy(spacing),
      verticalAlignment = Alignment.CenterVertically
    ) {
      labels.forEach { label ->
        Box(
          modifier = Modifier.size(cellSize),
          contentAlignment = Alignment.Center
        ) {
          if (label.isNotBlank()) {
            Text(
              label,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }
  }
}

private fun buildTimelineLabels(weeks: List<ActivityWeek>, range: ActivityRange): List<String> {
  if (weeks.isEmpty()) {
    return emptyList()
  }
  return weeks.mapIndexed { index, week ->
    val start = week.startDate
    when (range) {
      is ActivityRange.Last7Days -> start.dayOfWeek.name.take(1)
      is ActivityRange.Last6Months,
      is ActivityRange.Last90Days -> {
        val prev = weeks.getOrNull(index - 1)?.startDate
        if (prev == null || prev.month != start.month || prev.year != start.year) {
          monthInitial(start.month)
        } else {
          ""
        }
      }
      is ActivityRange.LastYear,
      is ActivityRange.Year -> {
        if (isQuarterStart(start)) {
          quarterOf(start.month).toString()
        } else {
          ""
        }
      }
    }
  }
}

private fun monthInitial(month: Month): String {
  return month.name.take(1)
}

private fun isQuarterStart(date: LocalDate): Boolean {
  return date.day <= 7 && date.month in setOf(Month.JANUARY, Month.APRIL, Month.JULY, Month.OCTOBER)
}

private fun quarterOf(month: Month): Int {
  return month.ordinal / 3 + 1
}

@Composable
/**
 * Renders weekly activity bars for the provided [weekData].
 */
fun WeeklyBarChart(
  weekData: ActivityWeekData,
  selectedWeek: ActivityWeek?,
  onWeekSelected: (ActivityWeek) -> Unit,
  scrollState: ScrollState,
  showWeekdayLegend: Boolean = false,
  compactHeight: Boolean = false,
  modifier: Modifier = Modifier
) {
  var tooltip by remember { mutableStateOf<WeekTooltip?>(null) }
  Column(modifier = modifier) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
      val columns = weekData.weeks.size.coerceAtLeast(1)
      val spacing = ChartDimens.spacing(compactHeight)
      val legendWidth = if (showWeekdayLegend) ChartDimens.legendWidth else 0.dp
      val legendSpacing = if (showWeekdayLegend) spacing else 0.dp
      val availableWidth = (maxWidth - legendWidth - legendSpacing).coerceAtLeast(0.dp)
      val layout = calculateLayout(availableWidth, columns, minColumnSize = ChartDimens.minCell(compactHeight), spacing = spacing)

      Row(
        modifier = Modifier
          .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.Bottom
      ) {
        if (showWeekdayLegend) {
          Spacer(modifier = Modifier.width(legendWidth + legendSpacing))
        }
        Row(
          modifier = Modifier
            .width(layout.contentWidth)
            .then(if (layout.useScroll) Modifier.horizontalScroll(scrollState) else Modifier),
          horizontalArrangement = Arrangement.spacedBy(spacing),
          verticalAlignment = Alignment.Bottom
        ) {
          weekData.weeks.forEach { week ->
            WeeklyBar(
              count = week.weeklyCount,
              maxCount = weekData.maxWeekly,
              width = layout.columnSize,
              isSelected = selectedWeek?.startDate == week.startDate,
              onClick = { offset ->
                onWeekSelected(week)
                tooltip = WeekTooltip(week, offset)
              }
            )
          }
        }
      }
    }
    tooltip?.let { current ->
      Popup(
        alignment = Alignment.TopStart,
        offset = current.offset,
        onDismissRequest = { tooltip = null }
      ) {
        Column(
          modifier = Modifier
            .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.small)
            .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
          Text(
            "${current.week.startDate} - ${current.week.startDate.plus(DatePeriod(days = 6))}",
            style = MaterialTheme.typography.labelMedium
          )
        }
      }
    }
  }
}

@Composable
/**
 * Memoized builder for [ActivityWeekData].
 */
fun rememberActivityWeekData(
  memos: List<Memo>,
  range: ActivityRange,
  mode: ActivityMode
): ActivityWeekData {
  val timeZone = remember { TimeZone.currentSystemDefault() }
  val today = currentLocalDate()
  return remember(memos, range, mode, today) {
    buildActivityWeekData(memos, timeZone, range, mode, today)
  }
}

@Composable
/**
 * Memoized builder for habits config timeline.
 */
fun rememberHabitsConfigTimeline(memos: List<Memo>): List<HabitsConfigEntry> {
  val timeZone = remember { TimeZone.currentSystemDefault() }
  return remember(memos, timeZone) {
    extractHabitsConfigEntries(memos, timeZone)
  }
}

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
 * Inclusive bounds for an activity range.
 */
private data class RangeBounds(
  val start: LocalDate,
  val end: LocalDate
)

/**
 * Tooltip payload for a selected day.
 */
private data class DayTooltip(
  val day: ContributionDay,
  val offset: IntOffset
)

/**
 * Tooltip payload for a selected week.
 */
private data class WeekTooltip(
  val week: ActivityWeek,
  val offset: IntOffset
)

/**
 * Resolved sizing parameters for chart layout.
 */
internal data class ChartLayout(
  val columnSize: Dp,
  val contentWidth: Dp,
  val useScroll: Boolean
)

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
  mode: ActivityMode,
  today: LocalDate
): ActivityWeekData {
  val bounds = rangeBounds(range, today)
  val configTimeline = if (mode == ActivityMode.Habits) {
    extractHabitsConfigEntries(memos, timeZone)
  } else {
    emptyList()
  }
  val dailyMemos = extractDailyMemos(memos, timeZone)
  val counts = if (mode == ActivityMode.Posts) extractDailyPostCounts(memos, timeZone, bounds) else emptyMap()
  if (mode == ActivityMode.Habits) {
    println("Habits debug: configs=${configTimeline.size}, dailyMemos=${dailyMemos.size}")
  }

  var start = bounds.start
  while (start.dayOfWeek != DayOfWeek.MONDAY) {
    start = start.minus(DatePeriod(days = 1))
  }

  val weeks = mutableListOf<ActivityWeek>()
  var cursor = start
  while (cursor <= bounds.end) {
    val days = (0..6).map { offset ->
      val date = cursor.plus(DatePeriod(days = offset))
      val dailyMemo = dailyMemos[date]
      val configForDay = if (mode == ActivityMode.Habits) {
        habitsConfigForDate(configTimeline, date)
      } else {
        null
      }
      val habitsForDay = configForDay?.habits.orEmpty()
      val useHabitsForDay = mode == ActivityMode.Habits && habitsForDay.isNotEmpty()
      val habitStatuses = if (useHabitsForDay) {
        buildHabitStatuses(dailyMemo?.content, habitsForDay)
      } else {
        emptyList()
      }
      val memoHabitTags = if (useHabitsForDay) {
        extractHabitTagsFromContent(dailyMemo?.content)
      } else {
        emptySet()
      }
      val configTags = if (useHabitsForDay) habitsForDay.map { it.tag }.toSet() else emptySet()
      val memoRelevantTags = if (configTags.isNotEmpty()) memoHabitTags.intersect(configTags) else memoHabitTags
      val completed = if (useHabitsForDay) {
        if (memoHabitTags.isNotEmpty()) memoRelevantTags.size else habitStatuses.count { it.done }
      } else if (mode == ActivityMode.Posts) {
        counts[date] ?: 0
      } else {
        0
      }
      val totalHabitsForDay = if (useHabitsForDay) {
        if (configTags.isNotEmpty()) configTags.size else memoHabitTags.size
      } else {
        0
      }
      val ratio = if (mode == ActivityMode.Habits && totalHabitsForDay > 0) {
        completed.toFloat() / totalHabitsForDay.toFloat()
      } else {
        0f
      }
      ContributionDay(
        date = date,
        count = completed,
        totalHabits = if (mode == ActivityMode.Habits) totalHabitsForDay else 0,
        completionRatio = ratio.coerceIn(0f, 1f),
        habitStatuses = habitStatuses,
        dailyMemo = dailyMemo,
        inRange = date >= bounds.start && date <= bounds.end
      )
    }
    val weeklyCount = days.sumOf { it.count }
    weeks.add(ActivityWeek(startDate = cursor, days = days, weeklyCount = weeklyCount))
    cursor = cursor.plus(DatePeriod(days = 7))
  }

  val maxDaily = weeks.maxOfOrNull { week -> week.days.maxOfOrNull { it.count } ?: 0 } ?: 0
  val maxWeekly = weeks.maxOfOrNull { it.weeklyCount } ?: 0
  return ActivityWeekData(weeks = weeks, maxDaily = maxDaily, maxWeekly = maxWeekly)
}

/**
 * Builds the monthly dataset from weekly entries.
 */
/**
 * Resolves the calendar bounds for the given [range].
 */
private fun rangeBounds(range: ActivityRange, today: LocalDate): RangeBounds {
  return when (range) {
    is ActivityRange.Last7Days -> RangeBounds(
      start = today.minus(DatePeriod(days = 6)),
      end = today
    )
    is ActivityRange.Last90Days -> RangeBounds(
      start = today.minus(DatePeriod(days = 89)),
      end = today
    )
    is ActivityRange.Last6Months -> RangeBounds(
      start = today.minus(DatePeriod(months = 6)),
      end = today
    )
    is ActivityRange.LastYear -> RangeBounds(
      start = today.minus(DatePeriod(days = 364)),
      end = today
    )
    is ActivityRange.Year -> RangeBounds(
      start = LocalDate(range.year, 1, 1),
      end = LocalDate(range.year + 1, 1, 1).minus(DatePeriod(days = 1))
    )
  }
}

/**
 * Renders an individual activity cell.
 */
@Composable
private fun ContributionCell(
  day: ContributionDay,
  maxCount: Int,
  enabled: Boolean,
  size: Dp,
  isSelected: Boolean,
  isHovered: Boolean,
  isWeekSelected: Boolean,
  onClick: ((IntOffset) -> Unit)?,
  onHoverChange: ((Boolean) -> Unit)?
) {
  var coordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
  val color = when {
    !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    else -> {
      val ratio = if (day.totalHabits > 0) {
        day.completionRatio
      } else if (maxCount > 0) {
        day.count.toFloat() / maxCount.toFloat()
      } else {
        0f
      }
      if (ratio <= 0f) {
        Color(0xFFE2E8F0)
      } else {
        lerp(Color(0xFFCFEED6), Color(0xFF0B7D3E), ratio.coerceIn(0f, 1f))
      }
    }
  }
  val selectedColor = color

  val borderColor = when {
    isSelected -> MaterialTheme.colorScheme.outline
    isHovered -> MaterialTheme.colorScheme.outlineVariant
    isWeekSelected -> MaterialTheme.colorScheme.outlineVariant
    else -> Color.Transparent
  }

  Spacer(
    modifier = Modifier
      .size(size)
      .background(color = selectedColor, shape = MaterialTheme.shapes.extraSmall)
      .border(width = 1.dp, color = borderColor, shape = MaterialTheme.shapes.extraSmall)
      .onGloballyPositioned { coordinates = it }
      .then(
        if (onHoverChange != null) {
          Modifier.hoverAware(onHoverChange)
        } else {
          Modifier
        }
      )
      .then(
        if (onClick != null && enabled) {
          Modifier.clickable {
            val coords = coordinates
            if (coords != null) {
              val position = coords.positionInWindow()
              val offset = IntOffset(position.x.toInt(), (position.y + coords.size.height).toInt())
              onClick(offset)
            }
          }
        } else {
          Modifier
        }
      )
  )
}

/**
 * Computes available years from memo timestamps.
 */
fun availableYears(
  memos: List<Memo>,
  timeZone: TimeZone,
  fallbackYear: Int = currentLocalDate().year
): List<Int> {
  val years = memos.mapNotNull { memo ->
    parseDailyDateFromContent(memo.content)?.year ?: parseMemoDate(memo, timeZone)?.year
  }.toMutableSet()
  if (years.isEmpty()) {
    years.add(fallbackYear)
  } else {
    years.add(fallbackYear)
  }
  return years.toList().sortedDescending()
}

/**
 * Finds the latest config entry strictly before the provided [date].
 */
fun habitsConfigForDate(entries: List<HabitsConfigEntry>, date: LocalDate): HabitsConfigEntry? {
  return entries.lastOrNull { it.date <= date }
}

private fun extractHabitsConfigEntries(memos: List<Memo>, timeZone: TimeZone): List<HabitsConfigEntry> {
  val tagged = memos.filter { memo ->
    memo.content.contains("#habits/config") || memo.content.contains("#habits_config")
  }
  val entries = tagged.mapNotNull { memo ->
    val instant = memo.createTime ?: memo.updateTime ?: return@mapNotNull null
    val date = instant.toLocalDateTime(timeZone).date
    val lines = memo.content.lineSequence()
      .map { it.trim() }
      .filter { it.isNotBlank() }
      .filterNot { it.startsWith("#habits/config") || it.startsWith("#habits_config") }
    val habits = lines.mapNotNull { line -> parseHabitConfigLine(line) }
      .distinctBy { it.tag }
      .toList()
    HabitsConfigEntry(date = date, habits = habits, memo = memo) to instant
  }
  return entries
    .sortedBy { it.second.toEpochMilliseconds() }
    .map { it.first }
}

/**
 * Returns the last 7 in-range days, newest last.
 */
fun lastSevenDays(weekData: ActivityWeekData): List<ContributionDay> {
  val days = weekData.weeks.flatMap { it.days }.filter { it.inRange }
  return days.takeLast(7)
}

/**
 * Derives a week dataset for a single habit tag.
 */
fun activityWeekDataForHabit(
  weekData: ActivityWeekData,
  habit: HabitConfig
): ActivityWeekData {
  val weeks = weekData.weeks.map { week ->
    val days = week.days.map { day ->
      val hasConfig = day.totalHabits > 0
      val done = if (hasConfig) day.habitStatuses.firstOrNull { it.tag == habit.tag }?.done == true else false
      val count = if (hasConfig && done) 1 else 0
      day.copy(
        count = count,
        totalHabits = if (hasConfig) 1 else 0,
        completionRatio = if (hasConfig && done) 1f else 0f,
        habitStatuses = if (hasConfig) {
          listOf(HabitStatus(tag = habit.tag, label = habit.label, done = done))
        } else {
          emptyList()
        }
      )
    }
    week.copy(
      days = days,
      weeklyCount = days.sumOf { it.count }
    )
  }
  val maxDaily = weeks.maxOfOrNull { week -> week.days.maxOfOrNull { it.count } ?: 0 } ?: 0
  val maxWeekly = weeks.maxOfOrNull { it.weeklyCount } ?: 0
  return ActivityWeekData(weeks = weeks, maxDaily = maxDaily, maxWeekly = maxWeekly)
}

private fun extractDailyMemos(
  memos: List<Memo>,
  timeZone: TimeZone
): Map<LocalDate, DailyMemoInfo> {
  val dailyMemos = memos.filter { memo ->
    memo.content.contains("#habits/daily") || memo.content.contains("#daily")
  }
  val latestByDate = mutableMapOf<LocalDate, DailyMemoRecord>()
  dailyMemos.forEach { memo ->
    val date = parseDailyDateFromContent(memo.content) ?: parseMemoDate(memo, timeZone) ?: return@forEach
    val instant = parseMemoInstant(memo) ?: return@forEach
    val record = DailyMemoRecord(
      info = DailyMemoInfo(name = memo.name, content = memo.content),
      sortKey = instant.toEpochMilliseconds()
    )
    val current = latestByDate[date]
    if (current == null || record.sortKey > current.sortKey) {
      latestByDate[date] = record
    }
  }
  return latestByDate.mapValues { it.value.info }
}

internal fun findDailyMemoForDate(
  memos: List<Memo>,
  timeZone: TimeZone,
  date: LocalDate
): DailyMemoInfo? {
  return extractDailyMemos(memos, timeZone)[date]
}

private fun parseHabitConfigLine(line: String): HabitConfig? {
  val parts = line.split("|", limit = 2).map { it.trim() }.filter { it.isNotBlank() }
  if (parts.isEmpty()) {
    return null
  }
  val (label, tagRaw) = if (parts.size == 1) {
    val raw = parts.first()
    val tag = normalizeHabitTag(raw)
    val label = if (raw.startsWith("#habits/") || raw.startsWith("#habit/")) labelFromTag(tag) else raw
    label to tag
  } else {
    val label = parts[0]
    val tag = normalizeHabitTag(parts[1])
    label to tag
  }
  return HabitConfig(tag = tagRaw, label = label)
}

private fun normalizeHabitTag(raw: String): String {
  val trimmed = raw.trim()
  val withoutPrefix = trimmed.removePrefix("#habits/").removePrefix("#habit/")
  val sanitized = withoutPrefix.replace("\\s+".toRegex(), "_")
  return "#habits/$sanitized"
}

private fun labelFromTag(tag: String): String {
  return tag.removePrefix("#habits/").removePrefix("#habit/").replace('_', ' ')
}

internal fun buildHabitStatuses(content: String?, habits: List<HabitConfig>): List<HabitStatus> {
  if (habits.isEmpty()) {
    return emptyList()
  }
  if (content.isNullOrBlank()) {
    return habits.map { habit -> HabitStatus(tag = habit.tag, label = habit.label, done = false) }
  }
  val done = extractCompletedHabits(content, habits.map { it.tag }.toSet())
  return habits.map { habit ->
    HabitStatus(tag = habit.tag, label = habit.label, done = done.contains(habit.tag))
  }
}

private fun extractCompletedHabits(content: String, habits: Set<String>): Set<String> {
  val done = mutableSetOf<String>()
  val lines = content.lineSequence()
  val checkboxRegex = Regex("^\\s*[-*]\\s*\\[(x|X)\\]\\s+(.+)$")
  var sawCheckbox = false
  lines.forEach { line ->
    val match = checkboxRegex.find(line)
    if (match != null) {
      sawCheckbox = true
      val trailing = match.groupValues[2]
      val habitTag = habits.firstOrNull { tag -> trailing.contains(tag) }
      if (habitTag != null) {
        done.add(habitTag)
      }
      return@forEach
    }
  }
  if (!sawCheckbox) {
    val tags = extractHabitTagsFromContent(content)
    done.addAll(tags.intersect(habits))
  }
  return done
}

private fun extractHabitTagsFromContent(content: String?): Set<String> {
  if (content.isNullOrBlank()) {
    return emptySet()
  }
  return Regex("#habits/[^\\s]+")
    .findAll(content)
    .map { it.value }
    .filterNot { it.equals("#habits/daily", ignoreCase = true) || it.startsWith("#habits/daily") }
    .toSet()
}

private fun parseDailyDateFromContent(content: String): LocalDate? {
  if (!content.contains("#habits/daily") && !content.contains("#daily")) {
    return null
  }
  val match = Regex("\\b(\\d{4}-\\d{2}-\\d{2})\\b").find(content) ?: return null
  return runCatching { LocalDate.parse(match.groupValues[1]) }.getOrNull()
}

private fun extractDailyPostCounts(
  memos: List<Memo>,
  timeZone: TimeZone,
  bounds: RangeBounds
): Map<LocalDate, Int> {
  val counts = mutableMapOf<LocalDate, Int>()
  memos.forEach { memo ->
    val date = parseMemoDate(memo, timeZone) ?: return@forEach
    if (date < bounds.start || date > bounds.end) {
      return@forEach
    }
    counts[date] = (counts[date] ?: 0) + 1
  }
  return counts
}

/**
 * Range selection for activity charts.
 */
sealed class ActivityRange {
  /** Rolling 7-day range. */
  data object Last7Days : ActivityRange()
  /** Rolling 90-day range. */
  data object Last90Days : ActivityRange()
  /** Rolling 6-month range. */
  data object Last6Months : ActivityRange()
  /** Rolling 12-month range. */
  data object LastYear : ActivityRange()
  /** Fixed calendar year. */
  data class Year(val year: Int) : ActivityRange()
}

/**
 * Attempts to parse a memo timestamp into [LocalDate].
 */
private fun parseMemoDate(memo: Memo, timeZone: TimeZone): LocalDate? {
  val instant = parseMemoInstant(memo) ?: return null
  return instant.toLocalDateTime(timeZone).date
}

private fun parseMemoInstant(memo: Memo): KtInstant? {
  return memo.updateTime ?: memo.createTime
}

private data class DailyMemoRecord(
  val info: DailyMemoInfo,
  val sortKey: Long
)

/**
 * Renders a weekly activity bar.
 */
@Composable
private fun WeeklyBar(
  count: Int,
  maxCount: Int,
  width: Dp,
  isSelected: Boolean,
  onClick: (IntOffset) -> Unit
) {
  var coordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
  val maxHeight = 72.dp
  val height = if (maxCount <= 0) 4.dp else (maxHeight.value * (count.toFloat() / maxCount.toFloat())).dp
  val color = if (isSelected) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outlineVariant
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Bottom,
    modifier = Modifier.height(maxHeight)
  ) {
    Spacer(
      modifier = Modifier
        .width(width)
        .height(height.coerceAtLeast(4.dp))
        .background(color, shape = MaterialTheme.shapes.extraSmall)
        .onGloballyPositioned { coordinates = it }
        .clickable {
          val coords = coordinates
          if (coords != null) {
            val position = coords.positionInWindow()
            val offset = IntOffset(position.x.toInt(), (position.y + coords.size.height).toInt())
            onClick(offset)
          }
        }
    )
  }
}
