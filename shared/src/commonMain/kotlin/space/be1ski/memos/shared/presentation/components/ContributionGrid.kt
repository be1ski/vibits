package space.be1ski.memos.shared.presentation.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
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
import kotlinx.datetime.Instant
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
  onDaySelected: (ContributionDay) -> Unit,
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
            Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
              week.days.forEach { day ->
                ContributionCell(
                  day = day,
                  maxCount = weekData.maxDaily,
                  enabled = day.inRange,
                  size = layout.columnSize,
                  isSelected = selectedDay?.date == day.date,
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
          Text(
            "${current.day.date}: ${current.day.count} posts",
            style = MaterialTheme.typography.labelMedium
          )
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
  range: ActivityRange
): ActivityWeekData {
  val timeZone = remember { TimeZone.currentSystemDefault() }
  val today = remember { currentLocalDate() }
  return remember(memos, range, today) {
    buildActivityWeekData(memos, timeZone, range, today)
  }
}

/**
 * Per-day activity entry.
 */
data class ContributionDay(
  /** Calendar date for the entry. */
  val date: LocalDate,
  /** Number of memos for the day. */
  val count: Int,
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
  today: LocalDate
): ActivityWeekData {
  val bounds = rangeBounds(range, today)
  val counts = mutableMapOf<LocalDate, Int>()
  memos.forEach { memo ->
    val date = parseMemoDate(memo, timeZone) ?: return@forEach
    if (date < bounds.start || date > bounds.end) {
      return@forEach
    }
    counts[date] = (counts[date] ?: 0) + 1
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
      ContributionDay(
        date = date,
        count = counts[date] ?: 0,
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
  onClick: ((IntOffset) -> Unit)?
) {
  var coordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
  val color = when {
    !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    day.count == 0 -> Color(0xFFE2E8F0)
    maxCount <= 1 -> Color(0xFFBFE3C0)
    else -> {
      val ratio = day.count.toFloat() / maxCount.toFloat()
      when {
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

  Spacer(
    modifier = Modifier
      .size(size)
      .background(color = selectedColor, shape = MaterialTheme.shapes.extraSmall)
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
    day = ContributionDay(LocalDate(1970, 1, 1), count, true),
    maxCount = maxCount,
    enabled = true,
    size = 12.dp,
    isSelected = false,
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
    parseMemoDate(memo, timeZone)?.year
  }.toMutableSet()
  if (years.isEmpty()) {
    years.add(fallbackYear)
  } else {
    years.add(fallbackYear)
  }
  return years.toList().sortedDescending()
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
  val candidates = listOfNotNull(memo.createTime, memo.updateTime)
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
      return instant.toLocalDateTime(timeZone).date
    }
  }
  return null
}

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
