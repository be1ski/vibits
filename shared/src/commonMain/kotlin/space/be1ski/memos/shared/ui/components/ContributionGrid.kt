package space.be1ski.memos.shared.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import space.be1ski.memos.shared.config.currentLocalDate
import space.be1ski.memos.shared.model.Memo

@Composable
fun ContributionGrid(
  memos: List<Memo>,
  range: ActivityRange,
  modifier: Modifier = Modifier
) {
  val timeZone = remember { TimeZone.currentSystemDefault() }
  val today = remember { currentLocalDate() }
  val rangeStart = remember(range, today) {
    when (range) {
      is ActivityRange.LastYear -> today.minus(DatePeriod(days = 364))
      is ActivityRange.Year -> LocalDate(range.year, 1, 1)
    }
  }
  val rangeEnd = remember(range, today) {
    when (range) {
      is ActivityRange.LastYear -> today
      is ActivityRange.Year -> LocalDate(range.year + 1, 1, 1).minus(DatePeriod(days = 1))
    }
  }

  val weeks = remember(memos, range, today) {
    buildWeeks(memos, timeZone, rangeStart, rangeEnd)
  }
  val maxCount = remember(weeks) { weeks.maxOfOrNull { week -> week.maxOfOrNull { it.count } ?: 0 } ?: 0 }

  Column(modifier = modifier) {
    Text("Posting activity", style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(8.dp))
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      weeks.forEach { week ->
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          week.forEach { day ->
            ContributionCell(
              count = day.count,
              maxCount = maxCount,
              enabled = day.inRange
            )
          }
        }
      }
    }
    Spacer(modifier = Modifier.height(8.dp))
    LegendRow(maxCount = maxCount)
  }
}

private data class ContributionDay(
  val date: LocalDate,
  val count: Int,
  val inRange: Boolean
)

private fun buildWeeks(
  memos: List<Memo>,
  timeZone: TimeZone,
  rangeStart: LocalDate,
  rangeEnd: LocalDate
): List<List<ContributionDay>> {
  val counts = mutableMapOf<LocalDate, Int>()
  memos.forEach { memo ->
    val date = parseMemoDate(memo, timeZone)
    if (date != null && date >= rangeStart && date <= rangeEnd) {
      counts[date] = (counts[date] ?: 0) + 1
    }
  }

  var start = rangeStart
  while (start.dayOfWeek != DayOfWeek.MONDAY) {
    start = start.minus(DatePeriod(days = 1))
  }

  val weeks = mutableListOf<List<ContributionDay>>()
  var cursor = start
  while (cursor <= rangeEnd) {
    val week = (0..6).map { offset ->
      val date = cursor.plus(DatePeriod(days = offset))
      ContributionDay(
        date = date,
        count = counts[date] ?: 0,
        inRange = date >= rangeStart && date <= rangeEnd
      )
    }
    weeks.add(week)
    cursor = cursor.plus(DatePeriod(days = 7))
  }
  return weeks
}

@Composable
private fun ContributionCell(
  count: Int,
  maxCount: Int,
  enabled: Boolean
) {
  val color = when {
    !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    count == 0 -> Color(0xFFE2E8F0)
    maxCount <= 1 -> Color(0xFFBFE3C0)
    else -> {
      val ratio = count.toFloat() / maxCount.toFloat()
      when {
        ratio <= 0.25f -> Color(0xFFBFE3C0)
        ratio <= 0.5f -> Color(0xFF7ACB8D)
        ratio <= 0.75f -> Color(0xFF34A853)
        else -> Color(0xFF0B7D3E)
      }
    }
  }

  Spacer(
    modifier = Modifier
      .size(12.dp)
      .background(color = color, shape = MaterialTheme.shapes.extraSmall)
  )
}

@Composable
private fun LegendRow(maxCount: Int) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    Text("Less", style = MaterialTheme.typography.labelSmall)
    ContributionCell(count = 0, maxCount = maxCount, enabled = true)
    ContributionCell(count = 1, maxCount = maxCount, enabled = true)
    ContributionCell(count = (maxCount * 0.5f).toInt().coerceAtLeast(1), maxCount = maxCount, enabled = true)
    ContributionCell(count = maxCount.coerceAtLeast(1), maxCount = maxCount.coerceAtLeast(1), enabled = true)
    Text("More", style = MaterialTheme.typography.labelSmall)
  }
}

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

sealed class ActivityRange {
  data object LastYear : ActivityRange()
  data class Year(val year: Int) : ActivityRange()
}

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
