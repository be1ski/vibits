@file:Suppress("TooManyFunctions")

package space.be1ski.memos.shared.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.stringResource
import space.be1ski.memos.shared.Res
import space.be1ski.memos.shared.feature.preferences.domain.model.TimeRangeTab
import space.be1ski.memos.shared.time_months
import space.be1ski.memos.shared.action_next
import space.be1ski.memos.shared.core.ui.ActivityRange
import space.be1ski.memos.shared.core.ui.Indent
import space.be1ski.memos.shared.action_previous
import space.be1ski.memos.shared.time_quarters
import space.be1ski.memos.shared.time_weeks
import space.be1ski.memos.shared.time_years
import kotlinx.datetime.Month as CalendarMonth

@Suppress("LongParameterList")
@Composable
internal fun TimeRangeControls(
  selectedTab: TimeRangeTab,
  selectedRange: ActivityRange,
  currentRange: ActivityRange,
  minRange: ActivityRange?,
  successRate: Float? = null,
  onTabChange: (TimeRangeTab) -> Unit,
  onRangeChange: (ActivityRange) -> Unit
) {
  Column(verticalArrangement = Arrangement.spacedBy(Indent.xs)) {
    PrimaryTabRow(selectedTabIndex = selectedTab.ordinal) {
      Tab(
        selected = selectedTab == TimeRangeTab.Weeks,
        onClick = { onTabChange(TimeRangeTab.Weeks) },
        text = { Text(stringResource(Res.string.time_weeks)) }
      )
      Tab(
        selected = selectedTab == TimeRangeTab.Months,
        onClick = { onTabChange(TimeRangeTab.Months) },
        text = { Text(stringResource(Res.string.time_months)) }
      )
      Tab(
        selected = selectedTab == TimeRangeTab.Quarters,
        onClick = { onTabChange(TimeRangeTab.Quarters) },
        text = { Text(stringResource(Res.string.time_quarters)) }
      )
      Tab(
        selected = selectedTab == TimeRangeTab.Years,
        onClick = { onTabChange(TimeRangeTab.Years) },
        text = { Text(stringResource(Res.string.time_years)) }
      )
    }
    TimeRangeNavigator(
      selectedRange = selectedRange,
      currentRange = currentRange,
      minRange = minRange,
      successRate = successRate,
      onRangeChange = onRangeChange
    )
  }
}

@Composable
private fun TimeRangeNavigator(
  selectedRange: ActivityRange,
  currentRange: ActivityRange,
  minRange: ActivityRange?,
  successRate: Float?,
  onRangeChange: (ActivityRange) -> Unit
) {
  val label = rangeLabel(selectedRange)
  val canGoForward = isBeforeRange(selectedRange, currentRange)
  val canGoBack = minRange?.let { isBeforeRange(it, selectedRange) } ?: true
  Box(
    modifier = Modifier.fillMaxWidth(),
    contentAlignment = Alignment.Center
  ) {
    IconButton(
      onClick = { onRangeChange(shiftRange(selectedRange, -1)) },
      enabled = canGoBack,
      modifier = Modifier.align(Alignment.CenterStart)
    ) {
      Icon(imageVector = Icons.Filled.ChevronLeft, contentDescription = stringResource(Res.string.action_previous))
    }
    Row(
      horizontalArrangement = Arrangement.spacedBy(Indent.s),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(label, style = MaterialTheme.typography.titleSmall)
      successRate?.let { rate ->
        SuccessRateBadge(rate)
      }
    }
    IconButton(
      onClick = { onRangeChange(shiftRange(selectedRange, 1)) },
      enabled = canGoForward,
      modifier = Modifier.align(Alignment.CenterEnd)
    ) {
      Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = stringResource(Res.string.action_next))
    }
  }
}

private fun rangeLabel(range: ActivityRange): String {
  return when (range) {
    is ActivityRange.Week -> {
      val endDate = range.startDate.plus(DatePeriod(days = WEEK_END_OFFSET))
      "${formatMonthDay(range.startDate)} - ${formatMonthDay(endDate)}"
    }
    is ActivityRange.Month -> "${monthShort(range.month)} ${range.year}"
    is ActivityRange.Quarter -> "Q${range.index} ${range.year}"
    is ActivityRange.Year -> range.year.toString()
  }
}

private fun formatMonthDay(date: LocalDate): String {
  return "${monthShort(date.month)} ${date.day}"
}

private fun monthShort(month: CalendarMonth): String {
  return month.name.take(MONTH_ABBREV_LENGTH).lowercase().replaceFirstChar { it.uppercase() }
}

private fun isBeforeRange(selectedRange: ActivityRange, currentRange: ActivityRange): Boolean {
  return when (selectedRange) {
    is ActivityRange.Week -> currentRange is ActivityRange.Week &&
      selectedRange.startDate < currentRange.startDate
    is ActivityRange.Month -> currentRange is ActivityRange.Month &&
      compareYearMonth(selectedRange.year, selectedRange.month, currentRange.year, currentRange.month) < 0
    is ActivityRange.Quarter -> currentRange is ActivityRange.Quarter &&
      compareYearQuarter(selectedRange.year, selectedRange.index, currentRange.year, currentRange.index) < 0
    is ActivityRange.Year -> currentRange is ActivityRange.Year && selectedRange.year < currentRange.year
  }
}

private fun compareYearMonth(
  year: Int,
  month: CalendarMonth,
  otherYear: Int,
  otherMonth: CalendarMonth
): Int {
  return if (year != otherYear) {
    year - otherYear
  } else {
    month.ordinal - otherMonth.ordinal
  }
}

private fun compareYearQuarter(
  year: Int,
  quarter: Int,
  otherYear: Int,
  otherQuarter: Int
): Int {
  return if (year != otherYear) {
    year - otherYear
  } else {
    quarter - otherQuarter
  }
}

private fun shiftRange(range: ActivityRange, delta: Int): ActivityRange {
  return when (range) {
    is ActivityRange.Week -> range.copy(
      startDate = range.startDate.plus(DatePeriod(days = delta * DAYS_IN_WEEK))
    )
    is ActivityRange.Month -> {
      val start = LocalDate(range.year, range.month, 1)
      val shifted = start.plus(DatePeriod(months = delta))
      ActivityRange.Month(shifted.year, shifted.month)
    }
    is ActivityRange.Quarter -> {
      val zeroBased = range.index - 1 + delta
      val yearShift = floorDiv(zeroBased, QUARTERS_IN_YEAR)
      val quarterIndex = floorMod(zeroBased, QUARTERS_IN_YEAR) + 1
      ActivityRange.Quarter(range.year + yearShift, quarterIndex)
    }
    is ActivityRange.Year -> ActivityRange.Year(range.year + delta)
  }
}

private fun floorDiv(value: Int, divisor: Int): Int {
  var result = value / divisor
  if (value xor divisor < 0 && value % divisor != 0) {
    result -= 1
  }
  return result
}

private fun floorMod(value: Int, divisor: Int): Int {
  val mod = value % divisor
  return if (mod < 0) mod + divisor else mod
}

@Composable
private fun SuccessRateBadge(rate: Float) {
  val percent = (rate * PERCENT_MULTIPLIER).toInt()
  val color = when {
    rate >= GREEN_THRESHOLD -> COLOR_GREEN
    rate >= YELLOW_THRESHOLD -> COLOR_YELLOW
    else -> COLOR_RED
  }
  Box(
    modifier = Modifier
      .background(color.copy(alpha = BADGE_ALPHA), RoundedCornerShape(BADGE_CORNER_RADIUS))
      .padding(horizontal = BADGE_PADDING_H, vertical = BADGE_PADDING_V)
  ) {
    Text(
      text = "$percent%",
      style = MaterialTheme.typography.labelSmall,
      color = color
    )
  }
}

private const val DAYS_IN_WEEK = 7
private const val WEEK_END_OFFSET = 6
private const val QUARTERS_IN_YEAR = 4
private const val MONTH_ABBREV_LENGTH = 3

private const val PERCENT_MULTIPLIER = 100
private const val GREEN_THRESHOLD = 0.8f
private const val YELLOW_THRESHOLD = 0.5f
private const val BADGE_ALPHA = 0.2f
private val BADGE_CORNER_RADIUS = 4.dp
private val BADGE_PADDING_H = 6.dp
private val BADGE_PADDING_V = 2.dp

private const val COLOR_GREEN_HEX = 0xFF4CAF50
private const val COLOR_YELLOW_HEX = 0xFFFFC107
private const val COLOR_RED_HEX = 0xFFE57373
private val COLOR_GREEN = Color(COLOR_GREEN_HEX)
private val COLOR_YELLOW = Color(COLOR_YELLOW_HEX)
private val COLOR_RED = Color(COLOR_RED_HEX)
