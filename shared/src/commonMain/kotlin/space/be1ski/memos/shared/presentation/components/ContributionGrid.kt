package space.be1ski.memos.shared.presentation.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import space.be1ski.memos.shared.domain.model.Memo
import space.be1ski.memos.shared.presentation.time.currentLocalDate

@Composable
/**
 * Renders GitHub-style daily activity grid for the provided [weekData].
 */
fun ContributionGrid(
  weekData: ActivityWeekData,
  selectedDay: ContributionDay?,
  selectedWeekStart: LocalDate?,
  onDaySelected: (ContributionDay) -> Unit,
  onEditRequested: (DailyMemoInfo) -> Unit,
  scrollState: ScrollState,
  modifier: Modifier = Modifier
) {
  var tooltip by remember { mutableStateOf<DayTooltip?>(null) }
  Column(modifier = modifier) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
      val columns = weekData.weeks.size.coerceAtLeast(1)
      val spacing = 2.dp
      val layout = calculateLayout(maxWidth, columns, minColumnSize = 10.dp, spacing = spacing)

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .then(if (layout.useScroll) Modifier.horizontalScroll(scrollState) else Modifier),
        horizontalArrangement = Arrangement.spacedBy(spacing)
      ) {
        Row(
          modifier = Modifier.width(layout.contentWidth),
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
                  isWeekSelected = isWeekSelected,
                  onClick = { offset ->
                    onDaySelected(day)
                    tooltip = DayTooltip(day, offset)
                  }
                )
              }
            }
          }
        }
      }
    }
    LegendRow(maxCount = weekData.maxDaily)
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
                Text("$prefix${status.tag}", color = color, style = MaterialTheme.typography.labelSmall)
              }
            }
          }
          current.day.dailyMemo?.let { memo ->
            TextButton(onClick = { onEditRequested(memo) }) {
              Text("Edit day")
            }
          }
        }
      }
    }
  }
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
  modifier: Modifier = Modifier
) {
  var tooltip by remember { mutableStateOf<WeekTooltip?>(null) }
  Column(modifier = modifier) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
      val columns = weekData.weeks.size.coerceAtLeast(1)
      val spacing = 2.dp
      val layout = calculateLayout(maxWidth, columns, minColumnSize = 10.dp, spacing = spacing)

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .then(if (layout.useScroll) Modifier.horizontalScroll(scrollState) else Modifier),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.Bottom
      ) {
        Row(
          modifier = Modifier.width(layout.contentWidth),
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
  val today = remember { currentLocalDate() }
  return remember(memos, range, mode, today) {
    buildActivityWeekData(memos, timeZone, range, mode, today)
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
  /** Habit tag, e.g. #habit/зарядка. */
  val tag: String,
  /** True when the habit is marked completed in a daily memo. */
  val done: Boolean
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
  /** Habit completion based on #daily + #habits_config. */
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
private data class ChartLayout(
  val columnSize: Dp,
  val contentWidth: Dp,
  val useScroll: Boolean
)

/**
 * Calculates layout sizes for a fixed number of columns.
 */
private fun calculateLayout(
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
  val habits = if (mode == ActivityMode.Habits) extractHabitsConfig(memos, timeZone) else emptyList()
  val dailyMemos = extractDailyMemos(memos, timeZone)
  val useHabits = mode == ActivityMode.Habits && habits.isNotEmpty()
  val totalHabits = habits.size
  val counts = if (mode == ActivityMode.Posts || !useHabits) {
    extractDailyPostCounts(memos, timeZone, bounds)
  } else {
    emptyMap()
  }
  if (mode == ActivityMode.Habits) {
    println("Habits debug: habits=${habits.size}, dailyMemos=${dailyMemos.size}, useHabits=$useHabits")
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
      val habitStatuses = if (useHabits) {
        buildHabitStatuses(dailyMemo?.content, habits)
      } else {
        emptyList()
      }
      val completed = if (useHabits) {
        habitStatuses.count { it.done }
      } else {
        counts[date] ?: 0
      }
      val ratio = if (useHabits && totalHabits > 0) {
        completed.toFloat() / totalHabits.toFloat()
      } else {
        0f
      }
      ContributionDay(
        date = date,
        count = completed,
        totalHabits = if (useHabits) totalHabits else 0,
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
  isWeekSelected: Boolean,
  onClick: ((IntOffset) -> Unit)?
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
      when {
        ratio <= 0f -> Color(0xFFE2E8F0)
        ratio <= 0.25f -> Color(0xFFBFE3C0)
        ratio <= 0.5f -> Color(0xFF7ACB8D)
        ratio <= 0.75f -> Color(0xFF34A853)
        else -> Color(0xFF0B7D3E)
      }
    }
  }
  val selectedColor = if (isSelected) {
    MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
  } else {
    color
  }

  val borderColor = when {
    isSelected -> MaterialTheme.colorScheme.primary
    isWeekSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
    else -> Color.Transparent
  }

  Spacer(
    modifier = Modifier
      .size(size)
      .background(color = selectedColor, shape = MaterialTheme.shapes.extraSmall)
      .border(width = 1.dp, color = borderColor, shape = MaterialTheme.shapes.extraSmall)
      .onGloballyPositioned { coordinates = it }
      .then(
        if (onClick != null && enabled) {
          Modifier.clickable {
            val coords = coordinates
            if (coords != null) {
              val position = coords.positionInRoot()
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
 * Renders the legend row under the grid.
 */
@Composable
private fun LegendRow(maxCount: Int) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    Text("Less", style = MaterialTheme.typography.labelSmall)
    LegendCell(count = 0, maxCount = maxCount)
    LegendCell(count = 1, maxCount = maxCount)
    LegendCell(count = (maxCount * 0.5f).toInt().coerceAtLeast(1), maxCount = maxCount)
    LegendCell(count = maxCount.coerceAtLeast(1), maxCount = maxCount.coerceAtLeast(1))
    Text("More", style = MaterialTheme.typography.labelSmall)
  }
}

/**
 * Renders a fixed-size legend cell.
 */
@Composable
private fun LegendCell(count: Int, maxCount: Int) {
  ContributionCell(
    day = ContributionDay(LocalDate(1970, 1, 1), count, 0, 0f, emptyList(), null, true),
    maxCount = maxCount,
    enabled = true,
    size = 12.dp,
    isSelected = false,
    isWeekSelected = false,
    onClick = null
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

private fun extractHabitsConfig(memos: List<Memo>, timeZone: TimeZone): List<String> {
  val tagged = memos.filter { memo ->
    memo.content.contains("#habits_config")
  }
  val sorted = tagged.sortedByDescending { memo ->
    parseMemoDate(memo, timeZone) ?: LocalDate(1970, 1, 1)
  }
  val config = sorted.firstOrNull() ?: return emptyList()
  return Regex("#habit/[^\\s]+")
    .findAll(config.content)
    .map { it.value }
    .distinct()
    .toList()
}

private fun extractDailyMemos(
  memos: List<Memo>,
  timeZone: TimeZone
): Map<LocalDate, DailyMemoInfo> {
  val dailyMemos = memos.filter { memo -> memo.content.contains("#daily") }
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

private fun buildHabitStatuses(content: String?, habits: List<String>): List<HabitStatus> {
  if (habits.isEmpty()) {
    return emptyList()
  }
  if (content.isNullOrBlank()) {
    return habits.map { HabitStatus(tag = it, done = false) }
  }
  val done = extractCompletedHabits(content, habits.toSet())
  return habits.map { HabitStatus(tag = it, done = done.contains(it)) }
}

private fun extractCompletedHabits(content: String, habits: Set<String>): Set<String> {
  val lines = content.lineSequence()
  val checkboxRegex = Regex("^\\s*[-*]\\s*\\[(x|X)\\]\\s+(.+)$")
  val done = mutableSetOf<String>()
  lines.forEach { line ->
    val match = checkboxRegex.find(line) ?: return@forEach
    val trailing = match.groupValues[2]
    val habitTag = habits.firstOrNull { tag -> trailing.contains(tag) }
    if (habitTag != null) {
      done.add(habitTag)
    }
  }
  return done
}

private fun parseDailyDateFromContent(content: String): LocalDate? {
  if (!content.contains("#daily")) {
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
  /** Rolling 12-month range. */
  data object LastYear : ActivityRange()
  /** Fixed calendar year. */
  data class Year(val year: Int) : ActivityRange()
}

/**
 * Attempts to parse a memo timestamp into [LocalDate].
 */
private fun parseMemoDate(memo: Memo, timeZone: TimeZone): LocalDate? {
  return parseMemoInstant(memo)?.toLocalDateTime(timeZone)?.date
}

private fun parseMemoInstant(memo: Memo): Instant? {
  val candidates = listOfNotNull(memo.updateTime, memo.createTime)
  for (value in candidates) {
    val trimmed = value.trim()
    val instant = runCatching { Instant.parse(trimmed) }.getOrNull()
      ?: runCatching { Instant.parse("${trimmed}Z") }.getOrNull()
      ?: runCatching {
        val number = trimmed.toLong()
        val millis = if (trimmed.length > 10) number else number * 1000
        Instant.fromEpochMilliseconds(millis)
      }.getOrNull()
    if (instant != null) {
      return instant
    }
  }
  return null
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
  val color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
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
            val position = coords.positionInRoot()
            val offset = IntOffset(position.x.toInt(), (position.y + coords.size.height).toInt())
            onClick(offset)
          }
        }
    )
  }
}
