@file:Suppress("TooManyFunctions")

package space.be1ski.vibits.shared.feature.habits.presentation.components

import androidx.compose.foundation.ScrollState
import space.be1ski.vibits.shared.core.ui.Indent
import space.be1ski.vibits.shared.core.ui.hoverAware
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
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
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.shared.core.ui.theme.AppColors
import space.be1ski.vibits.shared.core.ui.theme.resolve
import space.be1ski.vibits.shared.feature.habits.domain.model.ContributionDay
import space.be1ski.vibits.shared.Res
import space.be1ski.vibits.shared.title_create_day
import space.be1ski.vibits.shared.title_edit_day
import space.be1ski.vibits.shared.day_fri
import space.be1ski.vibits.shared.day_mon
import space.be1ski.vibits.shared.day_sat
import space.be1ski.vibits.shared.day_sun
import space.be1ski.vibits.shared.day_thu
import space.be1ski.vibits.shared.day_tue
import space.be1ski.vibits.shared.day_wed
import space.be1ski.vibits.shared.format_tooltip_habits
import space.be1ski.vibits.shared.format_tooltip_posts
import space.be1ski.vibits.shared.core.platform.LocalDateFormatter

internal object ChartDimens {
  val legendWidth = LEGEND_WIDTH_DP.dp

  fun spacing(compact: Boolean): Dp = if (compact) COMPACT_SPACING_DP.dp else REGULAR_SPACING_DP.dp

  fun minCell(compact: Boolean): Dp = if (compact) COMPACT_CELL_DP.dp else REGULAR_CELL_DP.dp

  fun maxCell(compact: Boolean): Dp? = if (compact) MAX_COMPACT_CELL_DP.dp else null
}

private const val LEGEND_WIDTH_DP = 26
private const val COMPACT_SPACING_DP = 1
private const val REGULAR_SPACING_DP = 2
private const val COMPACT_CELL_DP = 7
private const val REGULAR_CELL_DP = 10
private const val MAX_COMPACT_CELL_DP = 40

private const val HABIT_COLOR_LIGHT_RATIO = 0.3f

/**
 * Renders GitHub-style daily activity grid for the provided [state].
 */
@Suppress("LongParameterList")
@Composable
fun ContributionGrid(
  state: ContributionGridState,
  onDaySelected: (ContributionDay) -> Unit,
  onClearSelection: () -> Unit,
  onEditRequested: (ContributionDay) -> Unit,
  onCreateRequested: (ContributionDay) -> Unit,
  modifier: Modifier = Modifier
) {
  val interaction = remember { ContributionGridInteractionState() }
  if (!state.isActiveSelection && interaction.tooltip != null) {
    interaction.tooltip = null
  }
  Column(modifier = modifier) {
    ContributionGridLayout(state, onDaySelected, onClearSelection, interaction)
    ContributionGridTooltip(state, interaction, onEditRequested, onCreateRequested)
  }
}

private class ContributionGridInteractionState {
  var tooltip by mutableStateOf<DayTooltip?>(null)
  var hoveredDate by mutableStateOf<LocalDate?>(null)
  var suppressClear by mutableStateOf(false)
}

private data class ContributionGridLayoutState(
  val layout: ChartLayout,
  val spacing: Dp,
  val legendWidth: Dp,
  val legendSpacing: Dp,
  val timelineLabels: List<String>,
  val headerLabels: List<String> = emptyList()
)

@Composable
private fun ContributionGridLayout(
  state: ContributionGridState,
  onDaySelected: (ContributionDay) -> Unit,
  onClearSelection: () -> Unit,
  interaction: ContributionGridInteractionState
) {
  BoxWithConstraints(
    modifier = Modifier
      .fillMaxWidth()
      .pointerInput(Unit) {
        detectTapGestures(
          onTap = {
            if (interaction.suppressClear) {
              interaction.suppressClear = false
              return@detectTapGestures
            }
            interaction.tooltip = null
            interaction.hoveredDate = null
            onClearSelection()
          }
        )
      }
  ) {
    if (state.calendarLayout) {
      CalendarGridLayout(state, onDaySelected, interaction, maxWidth)
    } else {
      ContributionGridContent(state, onDaySelected, interaction, maxWidth)
    }
  }
}

@Composable
private fun BoxWithConstraintsScope.ContributionGridContent(
  state: ContributionGridState,
  onDaySelected: (ContributionDay) -> Unit,
  interaction: ContributionGridInteractionState,
  maxWidth: Dp
) {
  val columns = state.weekData.weeks.size.coerceAtLeast(1)
  val spacing = ChartDimens.spacing(state.compactHeight)
  val minCell = ChartDimens.minCell(state.compactHeight)
  val maxCell = ChartDimens.maxCell(state.compactHeight)
  val legendWidth = if (state.showWeekdayLegend) ChartDimens.legendWidth else 0.dp
  val legendSpacing = if (state.showWeekdayLegend) spacing else 0.dp
  val availableWidth = (maxWidth - legendWidth - legendSpacing).coerceAtLeast(0.dp)
  val layout = calculateLayout(
    availableWidth, columns, minColumnSize = minCell, spacing = spacing, maxColumnSize = maxCell
  )
  val formatter = LocalDateFormatter.current
  val timelineLabels = remember(state.weekData.weeks, state.range, formatter) {
    if (state.showTimeline) buildTimelineLabels(state.weekData.weeks, state.range, formatter) else emptyList()
  }
  val headerLabels = remember(state.weekData.weeks, state.showWeekStartHeaders) {
    if (state.showWeekStartHeaders) {
      state.weekData.weeks.map { week ->
        val firstInRangeDay = week.days.firstOrNull { it.inRange }
        firstInRangeDay?.date?.day?.toString() ?: ""
      }
    } else emptyList()
  }
  val layoutState = ContributionGridLayoutState(
    layout = layout,
    spacing = spacing,
    legendWidth = legendWidth,
    legendSpacing = legendSpacing,
    timelineLabels = timelineLabels,
    headerLabels = headerLabels
  )

  Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
    ContributionGridHeaderRow(state, layoutState)
    ContributionGridWeeks(state, onDaySelected, interaction, layoutState)
    ContributionGridTimelineRow(state, layoutState)
  }
}

@Composable
private fun BoxWithConstraintsScope.CalendarGridLayout(
  state: ContributionGridState,
  onDaySelected: (ContributionDay) -> Unit,
  interaction: ContributionGridInteractionState,
  maxWidth: Dp
) {
  val spacing = ChartDimens.spacing(state.compactHeight)
  val minCell = ChartDimens.minCell(state.compactHeight)
  val maxCell = ChartDimens.maxCell(state.compactHeight)
  val layout = calculateLayout(
    maxWidth, DAYS_IN_WEEK, minColumnSize = minCell, spacing = spacing, maxColumnSize = maxCell
  )
  val cellSize = layout.columnSize

  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(spacing)
  ) {
    // Weekday header row
    CalendarWeekdayHeader(cellSize, spacing, state.weekendDays)

    // Week rows
    state.weekData.weeks.forEach { week ->
      Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
        week.days.forEach { day ->
          val isWeekend = day.date.dayOfWeek in state.weekendDays
          ContributionCell(
            state = ContributionCellState(
              day = day,
              maxCount = state.weekData.maxDaily,
              enabled = day.inRange,
              size = cellSize,
              isSelected = state.selectedDay?.date == day.date,
              isHovered = interaction.hoveredDate == day.date,
              isWeekSelected = false,
              showDayNumber = true,
              isToday = state.today == day.date,
              isWeekend = isWeekend,
              habitColor = state.habitColor
            ),
            onClick = { offset ->
              onDaySelected(day)
              interaction.tooltip = DayTooltip(day, offset)
              interaction.suppressClear = true
            },
            onHoverChange = { hovering ->
              interaction.hoveredDate = if (hovering) {
                day.date
              } else {
                interaction.hoveredDate?.takeIf { it != day.date }
              }
            }
          )
        }
      }
    }
  }
}

@Composable
private fun CalendarWeekdayHeader(
  cellSize: Dp,
  spacing: Dp,
  weekendDays: Set<DayOfWeek>
) {
  val weekdayLabels = listOf(
    stringResource(Res.string.day_mon),
    stringResource(Res.string.day_tue),
    stringResource(Res.string.day_wed),
    stringResource(Res.string.day_thu),
    stringResource(Res.string.day_fri),
    stringResource(Res.string.day_sat),
    stringResource(Res.string.day_sun)
  )
  val weekdays = listOf(
    DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
  )

  Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
    weekdays.forEachIndexed { index, dayOfWeek ->
      val isWeekend = dayOfWeek in weekendDays
      Box(
        modifier = Modifier.size(cellSize),
        contentAlignment = Alignment.Center
      ) {
        Text(
          weekdayLabels[index],
          style = MaterialTheme.typography.labelSmall,
          color = if (isWeekend) {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = WEEKEND_LABEL_ALPHA)
          } else {
            MaterialTheme.colorScheme.onSurfaceVariant
          }
        )
      }
    }
  }
}

private const val DAYS_IN_WEEK = 7
private const val WEEKEND_LABEL_ALPHA = 0.6f
private const val WEEKEND_CELL_ALPHA = 0.15f

@Composable
private fun ContributionGridWeeks(
  state: ContributionGridState,
  onDaySelected: (ContributionDay) -> Unit,
  interaction: ContributionGridInteractionState,
  layoutState: ContributionGridLayoutState
) {
  val spacing = layoutState.spacing
  val layout = layoutState.layout
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(spacing)
  ) {
    if (state.showWeekdayLegend) {
      DayOfWeekLegend(
        cellSize = layout.columnSize,
        spacing = spacing,
        showAllLabels = state.showAllWeekdayLabels
      )
    }
    Row(
      modifier = Modifier
        .width(layout.contentWidth)
        .then(if (layout.useScroll) Modifier.horizontalScroll(state.scrollState) else Modifier),
      horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
      state.weekData.weeks.forEach { week ->
        val isWeekSelected = state.selectedWeekStart == week.startDate
        Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
          week.days.forEach { day ->
            ContributionCell(
              state = ContributionCellState(
                day = day,
                maxCount = state.weekData.maxDaily,
                enabled = day.inRange,
                size = layout.columnSize,
                isSelected = state.selectedDay?.date == day.date,
                isHovered = interaction.hoveredDate == day.date,
                isWeekSelected = isWeekSelected,
                showDayNumber = state.showDayNumbers,
                isToday = state.today == day.date,
                habitColor = state.habitColor
              ),
              onClick = { offset ->
                onDaySelected(day)
                interaction.tooltip = DayTooltip(day, offset)
                interaction.suppressClear = true
              },
              onHoverChange = { hovering ->
                interaction.hoveredDate = if (hovering) {
                  day.date
                } else {
                  interaction.hoveredDate?.takeIf { it != day.date }
                }
              }
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ContributionGridHeaderRow(
  state: ContributionGridState,
  layoutState: ContributionGridLayoutState
) {
  if (!state.showWeekStartHeaders) {
    return
  }
  val layout = layoutState.layout
  TimelineRow(
    state = TimelineRowState(
      labels = layoutState.headerLabels,
      cellSize = layout.columnSize,
      contentWidth = layout.contentWidth,
      spacing = layoutState.spacing,
      legendWidth = layoutState.legendWidth,
      legendSpacing = layoutState.legendSpacing,
      useScroll = layout.useScroll,
      scrollState = state.scrollState
    )
  )
}

@Composable
private fun ContributionGridTimelineRow(
  state: ContributionGridState,
  layoutState: ContributionGridLayoutState
) {
  if (!state.showTimeline) {
    return
  }
  val layout = layoutState.layout
  TimelineRow(
    state = TimelineRowState(
      labels = layoutState.timelineLabels,
      cellSize = layout.columnSize,
      contentWidth = layout.contentWidth,
      spacing = layoutState.spacing,
      legendWidth = layoutState.legendWidth,
      legendSpacing = layoutState.legendSpacing,
      useScroll = layout.useScroll,
      scrollState = state.scrollState
    )
  )
}

@Suppress("CyclomaticComplexMethod")
@Composable
private fun ContributionGridTooltip(
  state: ContributionGridState,
  interaction: ContributionGridInteractionState,
  onEditRequested: (ContributionDay) -> Unit,
  onCreateRequested: (ContributionDay) -> Unit
) {
  val tooltip = interaction.tooltip ?: return
  val positionProvider = remember(tooltip.offset) {
    object : PopupPositionProvider {
      override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
      ): IntOffset {
        val maxX = (windowSize.width - popupContentSize.width).coerceAtLeast(0)
        val maxY = (windowSize.height - popupContentSize.height).coerceAtLeast(0)
        val x = tooltip.offset.x.coerceIn(0, maxX)
        val y = tooltip.offset.y.coerceIn(0, maxY)
        return IntOffset(x, y)
      }
    }
  }
  Popup(
    popupPositionProvider = positionProvider,
    onDismissRequest = { interaction.tooltip = null }
  ) {
    Column(
      modifier = Modifier
        .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.small)
        .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
      val tooltipText = if (tooltip.day.totalHabits > 0) {
        stringResource(Res.string.format_tooltip_habits, tooltip.day.date, tooltip.day.count, tooltip.day.totalHabits)
      } else {
        stringResource(Res.string.format_tooltip_posts, tooltip.day.date, tooltip.day.count)
      }
      Text(tooltipText, style = MaterialTheme.typography.labelMedium)
      if (tooltip.day.totalHabits > 0) {
        Column(modifier = Modifier.padding(top = 6.dp)) {
          tooltip.day.habitStatuses.forEach { status ->
            val color = if (status.done) {
              MaterialTheme.colorScheme.primary
            } else {
              MaterialTheme.colorScheme.onSurfaceVariant
            }
            val prefix = if (status.done) "\u2713 " else "\u2022 "
            Text("$prefix${status.label}", color = color, style = MaterialTheme.typography.labelSmall)
          }
        }
      }
      val isFuture = state.today != null && tooltip.day.date > state.today
      if (!isFuture) {
        tooltip.day.dailyMemo?.let {
          TextButton(onClick = { onEditRequested(tooltip.day) }) {
            Text(stringResource(Res.string.title_edit_day))
          }
        } ?: run {
          if (tooltip.day.totalHabits > 0 && tooltip.day.inRange && !state.demoMode) {
            TextButton(onClick = { onCreateRequested(tooltip.day) }) {
              Text(stringResource(Res.string.title_create_day))
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
  spacing: Dp,
  showAllLabels: Boolean
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
          DayOfWeek.MONDAY -> stringResource(Res.string.day_mon)
          DayOfWeek.TUESDAY -> if (showAllLabels) stringResource(Res.string.day_tue) else null
          DayOfWeek.WEDNESDAY -> stringResource(Res.string.day_wed)
          DayOfWeek.THURSDAY -> if (showAllLabels) stringResource(Res.string.day_thu) else null
          DayOfWeek.FRIDAY -> stringResource(Res.string.day_fri)
          DayOfWeek.SATURDAY -> if (showAllLabels) stringResource(Res.string.day_sat) else null
          DayOfWeek.SUNDAY -> if (showAllLabels) stringResource(Res.string.day_sun) else null
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

private data class TimelineRowState(
  val labels: List<String>,
  val cellSize: Dp,
  val contentWidth: Dp,
  val spacing: Dp,
  val legendWidth: Dp,
  val legendSpacing: Dp,
  val useScroll: Boolean,
  val scrollState: ScrollState
)

@Composable
private fun TimelineRow(state: TimelineRowState) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(state.spacing)
  ) {
    if (state.legendWidth > 0.dp) {
      Spacer(modifier = Modifier.width(state.legendWidth + state.legendSpacing))
    }
    Row(
      modifier = Modifier
        .width(state.contentWidth)
        .then(if (state.useScroll) Modifier.horizontalScroll(state.scrollState) else Modifier),
      horizontalArrangement = Arrangement.spacedBy(state.spacing),
      verticalAlignment = Alignment.CenterVertically
    ) {
      state.labels.forEach { label ->
        Box(
          modifier = Modifier
            .width(state.cellSize)
            .height(state.cellSize + 4.dp),
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

/**
 * Tooltip payload for a selected day.
 */
private data class DayTooltip(
  val day: ContributionDay,
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
 * Renders an individual activity cell.
 */
@Suppress("LongMethod", "CyclomaticComplexMethod")
@Composable
private fun ContributionCell(
  state: ContributionCellState,
  onClick: ((IntOffset) -> Unit)?,
  onHoverChange: ((Boolean) -> Unit)?
) {
  var coordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
  val defaultStartColor = AppColors.habitGradientStart.resolve()
  val defaultEndColor = AppColors.habitGradientEnd.resolve()
  val inactiveCellColor = AppColors.inactiveCell.resolve()
  val (startColor, endColor) = remember(state.habitColor, defaultStartColor, defaultEndColor) {
    if (state.habitColor != null) {
      val base = Color(state.habitColor)
      val light = lerp(Color.White, base, HABIT_COLOR_LIGHT_RATIO)
      light to base
    } else {
      defaultStartColor to defaultEndColor
    }
  }
  val color = when {
    !state.enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    else -> {
      val ratio = if (state.day.totalHabits > 0) {
        state.day.completionRatio
      } else if (state.maxCount > 0) {
        state.day.count.toFloat() / state.maxCount.toFloat()
      } else {
        0f
      }
      if (ratio <= 0f) {
        inactiveCellColor
      } else {
        lerp(startColor, endColor, ratio.coerceIn(0f, 1f))
      }
    }
  }

  // Apply subtle highlight for today and weekends
  val todayOverlay = AppColors.todayHighlight.resolve()
  val weekendOverlay = Color.Black.copy(alpha = WEEKEND_CELL_ALPHA)
  val baseColor = if (state.isWeekend && state.enabled) {
    color.compositeOver(weekendOverlay)
  } else {
    color
  }
  val cellColor = if (state.isToday && state.enabled) {
    baseColor.compositeOver(todayOverlay)
  } else {
    baseColor
  }

  val borderColor = when {
    state.isSelected -> MaterialTheme.colorScheme.primary
    state.isHovered -> MaterialTheme.colorScheme.outlineVariant
    state.isWeekSelected -> MaterialTheme.colorScheme.outlineVariant
    else -> Color.Transparent
  }
  val borderWidth = when {
    state.isSelected -> Indent.x4s
    state.isHovered || state.isWeekSelected -> Indent.x5s
    else -> 0.dp
  }

  Box(
    modifier = Modifier
      .size(state.size)
      .background(color = cellColor, shape = MaterialTheme.shapes.extraSmall)
      .border(width = borderWidth, color = borderColor, shape = MaterialTheme.shapes.extraSmall)
      .onGloballyPositioned { coordinates = it }
      .then(
        if (onHoverChange != null) {
          Modifier.hoverAware(onHoverChange)
        } else {
          Modifier
        }
      )
      .then(
        if (onClick != null && state.enabled) {
          Modifier.clickable {
            val coords = coordinates
            if (coords != null) {
              val position = coords.positionInWindow()
              val offset = IntOffset(position.x.toInt(), (position.y + coords.size.height).toInt())
              onClick.invoke(offset)
            }
          }
        } else {
          Modifier
        }
      ),
    contentAlignment = Alignment.Center
  ) {
    if (state.showDayNumber) {
      Text(
        state.day.date.day.toString(),
        style = MaterialTheme.typography.labelSmall,
        color = if (state.day.inRange) {
          MaterialTheme.colorScheme.onSurface
        } else {
          MaterialTheme.colorScheme.onSurfaceVariant
        }
      )
    }
  }
}

