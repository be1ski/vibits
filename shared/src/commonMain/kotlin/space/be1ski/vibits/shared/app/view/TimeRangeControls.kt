package space.be1ski.vibits.shared.app.view

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
import space.be1ski.vibits.shared.app.domain.model.ActivityRange
import space.be1ski.vibits.shared.app.domain.model.SuccessRateLevel
import space.be1ski.vibits.shared.app.domain.usecase.GetSuccessRateLevelUseCase
import space.be1ski.vibits.shared.core.platform.date.currentLocalDate
import space.be1ski.vibits.shared.core.ui.Indent
import space.be1ski.vibits.shared.core.ui.date.DateFormatter
import space.be1ski.vibits.shared.core.ui.theme.AppColors
import space.be1ski.vibits.shared.core.ui.theme.resolve
import space.be1ski.vibits.shared.feature.settings.domain.model.TimeRangeTab
import space.be1ski.vibits.shared.generated.Res
import space.be1ski.vibits.shared.generated.action_next
import space.be1ski.vibits.shared.generated.action_previous
import space.be1ski.vibits.shared.generated.time_months
import space.be1ski.vibits.shared.generated.time_quarters
import space.be1ski.vibits.shared.generated.time_weeks
import space.be1ski.vibits.shared.generated.time_years

private const val WEEK_END_OFFSET = 6

@Composable
internal fun TimeRangeControls(
  selectedTab: TimeRangeTab,
  rangeLabel: String,
  successRate: Float? = null,
  canGoBack: Boolean,
  canGoForward: Boolean,
  onTabChange: (TimeRangeTab) -> Unit,
  onNavigateBack: () -> Unit,
  onNavigateForward: () -> Unit,
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
      rangeLabel = rangeLabel,
      successRate = successRate,
      canGoBack = canGoBack,
      canGoForward = canGoForward,
      onNavigateBack = onNavigateBack,
      onNavigateForward = onNavigateForward,
    )
  }
}

@Composable
private fun TimeRangeNavigator(
  rangeLabel: String,
  successRate: Float?,
  canGoBack: Boolean,
  canGoForward: Boolean,
  onNavigateBack: () -> Unit,
  onNavigateForward: () -> Unit,
) {
  Box(
    modifier = Modifier.fillMaxWidth(),
    contentAlignment = Alignment.Center,
  ) {
    IconButton(
      onClick = onNavigateBack,
      enabled = canGoBack,
      modifier = Modifier.align(Alignment.CenterStart),
    ) {
      Icon(imageVector = Icons.Filled.ChevronLeft, contentDescription = stringResource(Res.string.action_previous))
    }
    Row(
      horizontalArrangement = Arrangement.spacedBy(Indent.s),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(rangeLabel, style = MaterialTheme.typography.titleSmall)
      if (successRate != null) {
        SuccessRateBadge(successRate)
      } else {
        SuccessRatePlaceholder()
      }
    }
    IconButton(
      onClick = onNavigateForward,
      enabled = canGoForward,
      modifier = Modifier.align(Alignment.CenterEnd),
    ) {
      Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = stringResource(Res.string.action_next))
    }
  }
}

internal fun formatRangeLabel(
  range: ActivityRange,
  formatter: DateFormatter,
): String =
  when (range) {
    is ActivityRange.Week -> {
      val endDate = range.startDate.plus(DatePeriod(days = WEEK_END_OFFSET))
      val currentYear = currentLocalDate().year
      formatter.weekRange(range.startDate, endDate, currentYear)
    }
    is ActivityRange.Month -> "${formatter.monthShort(range.month)} ${range.year}"
    is ActivityRange.Quarter -> "Q${range.index} ${range.year}"
    is ActivityRange.Year -> range.year.toString()
  }

@Composable
private fun SuccessRateBadge(rate: Float) {
  val percent = (rate * PERCENT_MULTIPLIER).toInt()
  val level = GetSuccessRateLevelUseCase(rate)
  val color = colorForLevel(level)
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

@Composable
private fun SuccessRatePlaceholder() {
  Box(
    modifier =
      Modifier
        .padding(horizontal = BADGE_PADDING_H, vertical = BADGE_PADDING_V),
  ) {
    Text(
      text = "—",
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
    )
  }
}

@Composable
private fun colorForLevel(level: SuccessRateLevel) =
  when (level) {
    SuccessRateLevel.GOOD -> AppColors.statusGreen.resolve()
    SuccessRateLevel.MEDIUM -> AppColors.statusYellow.resolve()
    SuccessRateLevel.BAD -> AppColors.statusRed.resolve()
  }

private const val PERCENT_MULTIPLIER = 100
private const val BADGE_ALPHA = 0.2f
private val BADGE_CORNER_RADIUS = 4.dp
private val BADGE_PADDING_H = 6.dp
private val BADGE_PADDING_V = 2.dp
