package space.be1ski.vibits.shared.feature.habits.presentation.components

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
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import space.be1ski.vibits.shared.feature.habits.domain.model.ActivityWeek

private val WEEKLY_BAR_MAX_HEIGHT = 72.dp
private val WEEKLY_BAR_MIN_HEIGHT = 4.dp

/**
 * Tooltip payload for a selected week.
 */
private data class WeekTooltip(
  val week: ActivityWeek,
  val offset: IntOffset,
)

/**
 * Renders weekly activity bars for the provided [state].
 */
@Composable
fun WeeklyBarChart(
  state: WeeklyBarChartState,
  onWeekSelected: (ActivityWeek) -> Unit,
) {
  var tooltip by remember { mutableStateOf<WeekTooltip?>(null) }
  Column(modifier = state.modifier) {
    WeeklyBarChartBars(
      state = state,
      onWeekSelected = onWeekSelected,
      onTooltipChange = { tooltip = it },
    )
    WeeklyBarChartTooltip(tooltip) { tooltip = null }
  }
}

@Composable
private fun WeeklyBarChartBars(
  state: WeeklyBarChartState,
  onWeekSelected: (ActivityWeek) -> Unit,
  onTooltipChange: (WeekTooltip?) -> Unit,
) {
  BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
    val columns =
      state.weekData.weeks.size
        .coerceAtLeast(1)
    val spacing = ChartDimens.spacing(state.compactHeight)
    val legendWidth = if (state.showWeekdayLegend) ChartDimens.legendWidth else 0.dp
    val legendSpacing = if (state.showWeekdayLegend) spacing else 0.dp
    val availableWidth = (maxWidth - legendWidth - legendSpacing).coerceAtLeast(0.dp)
    val layout =
      calculateLayout(
        availableWidth,
        columns,
        minColumnSize = ChartDimens.minCell(state.compactHeight),
        spacing = spacing,
      )

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(spacing),
      verticalAlignment = Alignment.Bottom,
    ) {
      if (state.showWeekdayLegend) {
        Spacer(modifier = Modifier.width(legendWidth + legendSpacing))
      }
      Row(
        modifier =
          Modifier
            .width(layout.contentWidth)
            .then(if (layout.useScroll) Modifier.horizontalScroll(state.scrollState) else Modifier),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.Bottom,
      ) {
        state.weekData.weeks.forEach { week ->
          WeeklyBar(
            count = week.weeklyCount,
            maxCount = state.weekData.maxWeekly,
            width = layout.columnSize,
            isSelected = state.selectedWeek?.startDate == week.startDate,
            onClick = { offset ->
              onWeekSelected(week)
              onTooltipChange(WeekTooltip(week, offset))
            },
          )
        }
      }
    }
  }
}

@Composable
private fun WeeklyBarChartTooltip(
  tooltip: WeekTooltip?,
  onDismiss: () -> Unit,
) {
  tooltip?.let { current ->
    Popup(
      alignment = Alignment.TopStart,
      offset = current.offset,
      onDismissRequest = onDismiss,
    ) {
      Column(
        modifier =
          Modifier
            .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.small)
            .padding(horizontal = 8.dp, vertical = 6.dp),
      ) {
        Text(
          "${current.week.startDate} - ${current.week.startDate.plus(DatePeriod(days = 6))}",
          style = MaterialTheme.typography.labelMedium,
        )
      }
    }
  }
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
  onClick: (IntOffset) -> Unit,
) {
  var coordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
  val maxHeight = WEEKLY_BAR_MAX_HEIGHT
  val height =
    if (maxCount <= 0) {
      WEEKLY_BAR_MIN_HEIGHT
    } else {
      (maxHeight.value * (count.toFloat() / maxCount.toFloat())).dp
    }
  val color = if (isSelected) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outlineVariant
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Bottom,
    modifier = Modifier.height(maxHeight),
  ) {
    Spacer(
      modifier =
        Modifier
          .width(width)
          .height(height)
          .background(color, shape = MaterialTheme.shapes.extraSmall)
          .onGloballyPositioned { coordinates = it }
          .clickable {
            val coords = coordinates ?: return@clickable
            val position = coords.positionInRoot()
            onClick(IntOffset(position.x.toInt(), (position.y + coords.size.height).toInt()))
          },
    )
  }
}
