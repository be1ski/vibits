package space.be1ski.vibits.shared.feature.habits.presentation.components

import androidx.compose.foundation.ScrollState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import kotlinx.datetime.LocalDate
import space.be1ski.vibits.shared.core.ui.ActivityRange
import space.be1ski.vibits.shared.feature.habits.domain.model.ActivityWeek
import space.be1ski.vibits.shared.feature.habits.domain.model.ActivityWeekData
import space.be1ski.vibits.shared.feature.habits.domain.model.ContributionDay

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
  val showDayNumbers: Boolean = false,
  val today: LocalDate? = null,
  val habitColor: Long? = null
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

internal data class ContributionCellState(
  val day: ContributionDay,
  val maxCount: Int,
  val enabled: Boolean,
  val size: Dp,
  val isSelected: Boolean,
  val isHovered: Boolean,
  val isWeekSelected: Boolean,
  val showDayNumber: Boolean,
  val isToday: Boolean = false,
  val habitColor: Long? = null
)

internal data class ContributionCellCallbacks(
  val onClick: ((IntOffset) -> Unit)?,
  val onHoverChange: ((Boolean) -> Unit)?
)
