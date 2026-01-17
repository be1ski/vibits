package space.be1ski.vibits.shared.app

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
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.shared.Res
import space.be1ski.vibits.shared.action_next
import space.be1ski.vibits.shared.action_previous
import space.be1ski.vibits.shared.core.platform.LocalDateFormatter
import space.be1ski.vibits.shared.core.platform.currentLocalDate
import space.be1ski.vibits.shared.core.ui.ActivityRange
import space.be1ski.vibits.shared.core.ui.Indent
import space.be1ski.vibits.shared.core.ui.theme.AppColors
import space.be1ski.vibits.shared.core.ui.theme.resolve
import space.be1ski.vibits.shared.feature.habits.domain.usecase.NavigateActivityRangeUseCase
import space.be1ski.vibits.shared.feature.settings.domain.model.TimeRangeTab
import space.be1ski.vibits.shared.time_months
import space.be1ski.vibits.shared.time_quarters
import space.be1ski.vibits.shared.time_weeks
import space.be1ski.vibits.shared.time_years

private val navigateActivityRangeUseCase = NavigateActivityRangeUseCase()
private const val WEEK_END_OFFSET = 6

@Suppress("LongParameterList")
@Composable
internal fun TimeRangeControls(
  selectedTab: TimeRangeTab,
  selectedRange: ActivityRange,
  currentRange: ActivityRange,
  minRange: ActivityRange?,
  successRate: Float? = null,
  onTabChange: (TimeRangeTab) -> Unit,
  onRangeChange: (ActivityRange) -> Unit,
) {
  Column(verticalArrangement = Arrangement.spacedBy(Indent.xs)) {
    PrimaryScrollableTabRow(selectedTabIndex = selectedTab.ordinal, edgePadding = 0.dp) {
      Tab(
        selected = selectedTab == TimeRangeTab.WEEKS,
        onClick = { onTabChange(TimeRangeTab.WEEKS) },
        text = { Text(stringResource(Res.string.time_weeks)) },
      )
      Tab(
        selected = selectedTab == TimeRangeTab.MONTHS,
        onClick = { onTabChange(TimeRangeTab.MONTHS) },
        text = { Text(stringResource(Res.string.time_months)) },
      )
      Tab(
        selected = selectedTab == TimeRangeTab.QUARTERS,
        onClick = { onTabChange(TimeRangeTab.QUARTERS) },
        text = { Text(stringResource(Res.string.time_quarters)) },
      )
      Tab(
        selected = selectedTab == TimeRangeTab.YEARS,
        onClick = { onTabChange(TimeRangeTab.YEARS) },
        text = { Text(stringResource(Res.string.time_years)) },
      )
    }
    TimeRangeNavigator(
      selectedRange = selectedRange,
      currentRange = currentRange,
      minRange = minRange,
      successRate = successRate,
      onRangeChange = onRangeChange,
    )
  }
}

@Composable
private fun TimeRangeNavigator(
  selectedRange: ActivityRange,
  currentRange: ActivityRange,
  minRange: ActivityRange?,
  successRate: Float?,
  onRangeChange: (ActivityRange) -> Unit,
) {
  val label = rangeLabel(selectedRange)
  val canGoForward = isBeforeRange(selectedRange, currentRange)
  val canGoBack = minRange?.let { isBeforeRange(it, selectedRange) } ?: true
  Box(
    modifier = Modifier.fillMaxWidth(),
    contentAlignment = Alignment.Center,
  ) {
    IconButton(
      onClick = { onRangeChange(shiftRange(selectedRange, -1)) },
      enabled = canGoBack,
      modifier = Modifier.align(Alignment.CenterStart),
    ) {
      Icon(imageVector = Icons.Filled.ChevronLeft, contentDescription = stringResource(Res.string.action_previous))
    }
    Row(
      horizontalArrangement = Arrangement.spacedBy(Indent.s),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(label, style = MaterialTheme.typography.titleSmall)
      successRate?.let { rate ->
        SuccessRateBadge(rate)
      }
    }
    IconButton(
      onClick = { onRangeChange(shiftRange(selectedRange, 1)) },
      enabled = canGoForward,
      modifier = Modifier.align(Alignment.CenterEnd),
    ) {
      Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = stringResource(Res.string.action_next))
    }
  }
}

@Composable
private fun rangeLabel(range: ActivityRange): String {
  val formatter = LocalDateFormatter.current
  return when (range) {
    is ActivityRange.Week -> {
      val endDate = range.startDate.plus(DatePeriod(days = WEEK_END_OFFSET))
      val currentYear = currentLocalDate().year
      formatter.weekRange(range.startDate, endDate, currentYear)
    }
    is ActivityRange.Month -> "${formatter.monthShort(range.month)} ${range.year}"
    is ActivityRange.Quarter -> "Q${range.index} ${range.year}"
    is ActivityRange.Year -> range.year.toString()
  }
}

private fun isBeforeRange(
  selectedRange: ActivityRange,
  currentRange: ActivityRange,
): Boolean = navigateActivityRangeUseCase.isBefore(selectedRange, currentRange)

private fun shiftRange(
  range: ActivityRange,
  delta: Int,
): ActivityRange = navigateActivityRangeUseCase(range, delta)

@Composable
private fun SuccessRateBadge(rate: Float) {
  val percent = (rate * PERCENT_MULTIPLIER).toInt()
  val color =
    when {
      rate >= GREEN_THRESHOLD -> AppColors.statusGreen.resolve()
      rate >= YELLOW_THRESHOLD -> AppColors.statusYellow.resolve()
      else -> AppColors.statusRed.resolve()
    }
  Box(
    modifier =
      Modifier
        .background(color.copy(alpha = BADGE_ALPHA), RoundedCornerShape(BADGE_CORNER_RADIUS))
        .padding(horizontal = BADGE_PADDING_H, vertical = BADGE_PADDING_V),
  ) {
    Text(
      text = "$percent%",
      style = MaterialTheme.typography.labelSmall,
      color = color,
    )
  }
}

private const val PERCENT_MULTIPLIER = 100
private const val GREEN_THRESHOLD = 0.8f
private const val YELLOW_THRESHOLD = 0.5f
private const val BADGE_ALPHA = 0.2f
private val BADGE_CORNER_RADIUS = 4.dp
private val BADGE_PADDING_H = 6.dp
private val BADGE_PADDING_V = 2.dp
