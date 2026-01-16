@file:Suppress("TooManyFunctions")

package space.be1ski.vibits.shared.feature.habits.presentation

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.shared.Res
import space.be1ski.vibits.shared.action_configure_habits
import space.be1ski.vibits.shared.action_track
import space.be1ski.vibits.shared.format_habits_progress
import space.be1ski.vibits.shared.hint_add_habits_config
import space.be1ski.vibits.shared.label_activity
import space.be1ski.vibits.shared.label_habits_config
import space.be1ski.vibits.shared.msg_no_habits_yet
import space.be1ski.vibits.shared.core.ui.ActivityMode
import space.be1ski.vibits.shared.core.ui.ActivityRange
import space.be1ski.vibits.shared.core.ui.theme.AppColors
import space.be1ski.vibits.shared.core.ui.theme.resolve
import space.be1ski.vibits.shared.feature.habits.domain.model.ContributionDay
import space.be1ski.vibits.shared.feature.habits.presentation.components.ContributionGrid
import space.be1ski.vibits.shared.feature.habits.presentation.components.ContributionGridState
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitConfig
import space.be1ski.vibits.shared.core.ui.Indent
import space.be1ski.vibits.shared.feature.habits.presentation.components.calculateLayout
import space.be1ski.vibits.shared.feature.habits.presentation.components.habitsConfigForDate
import space.be1ski.vibits.shared.feature.habits.presentation.components.WeeklyBarChart
import space.be1ski.vibits.shared.feature.habits.presentation.components.WeeklyBarChartState
import space.be1ski.vibits.shared.feature.habits.presentation.components.activityWeekDataForHabit
import space.be1ski.vibits.shared.feature.habits.presentation.components.lastSevenDays
import space.be1ski.vibits.shared.feature.habits.presentation.components.ChartDimens

@Composable
internal fun StatsInfoCard(
  derived: StatsScreenDerivedState,
  dispatch: (HabitsAction) -> Unit
) {
  val state = derived.state
  val isHabitsMode = state.activityMode == ActivityMode.Habits
  val hasTodayConfig = derived.todayConfig.isNotEmpty()

  if (!isHabitsMode || !hasTodayConfig) return

  val todayStatuses = derived.todayDay?.habitStatuses.orEmpty()
  val todayDone = todayStatuses.count { it.done }
  val todayTotal = todayStatuses.size

  if (todayTotal == 0) return

  OutlinedCard(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.padding(Indent.s).fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        stringResource(Res.string.format_habits_progress, todayDone, todayTotal),
        style = MaterialTheme.typography.titleSmall
      )
      Row(
        horizontalArrangement = Arrangement.spacedBy(Indent.xs),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = { dispatch(HabitsAction.OpenConfigDialog(derived.currentHabitsConfig)) },
          modifier = Modifier.size(36.dp)
        ) {
          Icon(
            imageVector = Icons.Filled.Settings,
            contentDescription = stringResource(Res.string.label_habits_config),
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Button(
          onClick = {
            val day = derived.todayDay ?: return@Button
            dispatch(HabitsAction.OpenEditor(day, derived.todayConfig))
          }
        ) {
          Text(stringResource(Res.string.action_track))
        }
      }
    }
  }
}

@Composable
internal fun StatsHeaderRow() {
  Text(stringResource(Res.string.label_activity), style = MaterialTheme.typography.titleMedium)
}

@Composable
internal fun StatsHabitsEmptyState(
  derived: StatsScreenDerivedState,
  dispatch: (HabitsAction) -> Unit
) {
  val state = derived.state
  if (state.activityMode != ActivityMode.Habits || derived.currentHabitsConfig.isNotEmpty()) {
    return
  }
  OutlinedCard(modifier = Modifier.fillMaxWidth()) {
    Column(
      modifier = Modifier.padding(Indent.s),
      verticalArrangement = Arrangement.spacedBy(Indent.xs)
    ) {
      Text(stringResource(Res.string.msg_no_habits_yet), style = MaterialTheme.typography.titleSmall)
      Text(
        stringResource(Res.string.hint_add_habits_config),
        style = MaterialTheme.typography.bodySmall
      )
      Button(onClick = { dispatch(HabitsAction.OpenConfigDialog(emptyList())) }) {
        Text(stringResource(Res.string.action_configure_habits))
      }
    }
  }
}

private const val SHIMMER_DURATION_MS = 1000
private const val DAYS_IN_WEEK = 7
private const val SHIMMER_LABEL_HEIGHT = 20

@Composable
private fun ChartShimmer(compactHeight: Boolean) {
  val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
  val alpha = infiniteTransition.animateFloat(
    initialValue = 0.3f,
    targetValue = 0.7f,
    animationSpec = infiniteRepeatable(
      animation = tween(SHIMMER_DURATION_MS),
      repeatMode = RepeatMode.Reverse
    ),
    label = "shimmer_alpha"
  )
  val cellSize = ChartDimens.minCell(compactHeight)
  val spacing = ChartDimens.spacing(compactHeight)
  val chartHeight = (cellSize + spacing) * DAYS_IN_WEEK

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(chartHeight)
      .background(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha.value),
        shape = RoundedCornerShape(4.dp)
      )
  )
}

@Composable
private fun HabitSectionShimmer(label: String, compactHeight: Boolean) {
  val infiniteTransition = rememberInfiniteTransition(label = "shimmer_$label")
  val alpha = infiniteTransition.animateFloat(
    initialValue = 0.3f,
    targetValue = 0.7f,
    animationSpec = infiniteRepeatable(
      animation = tween(SHIMMER_DURATION_MS),
      repeatMode = RepeatMode.Reverse
    ),
    label = "shimmer_alpha_$label"
  )
  val cellSize = ChartDimens.minCell(compactHeight)
  val spacing = ChartDimens.spacing(compactHeight)
  val chartHeight = (cellSize + spacing) * DAYS_IN_WEEK

  Column(
    verticalArrangement = Arrangement.spacedBy(Indent.xs),
    modifier = Modifier.padding(top = Indent.s)
  ) {
    // Label shimmer
    Box(
      modifier = Modifier
        .width(80.dp)
        .height(SHIMMER_LABEL_HEIGHT.dp)
        .background(
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha.value),
          shape = RoundedCornerShape(4.dp)
        )
    )
    // Chart shimmer
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(chartHeight)
        .background(
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha.value),
          shape = RoundedCornerShape(4.dp)
        )
    )
  }
}

@Composable
internal fun StatsMainChart(
  derived: StatsScreenDerivedState,
  dispatch: (HabitsAction) -> Unit
) {
  // Show shimmer while loading and no data yet
  if (derived.isLoadingWeekData && derived.weekData.weeks.isEmpty()) {
    ChartShimmer(derived.useCompactHeight)
    return
  }

  val state = derived.state
  val habitsState = derived.habitsState
  val chartScrollState = rememberScrollState()

  val onDaySelected = remember(dispatch, state.activityMode, derived.weekData.weeks) {
    { day: ContributionDay ->
      dispatch(HabitsAction.SelectDay(day, "main"))
      if (state.activityMode == ActivityMode.Posts) {
        val week = derived.weekData.weeks.firstOrNull { week ->
          week.days.any { it.date == day.date }
        }
        if (week != null) {
          dispatch(HabitsAction.SelectWeek(week))
        }
      }
    }
  }
  val onClearSelection = remember(dispatch) { { dispatch(HabitsAction.ClearSelection) } }
  val onEditRequested = remember(dispatch, derived.habitsConfigTimeline) {
    { day: ContributionDay ->
      val config = habitsConfigForDate(derived.habitsConfigTimeline, day.date)?.habits.orEmpty()
      dispatch(HabitsAction.OpenEditor(day, config))
    }
  }

  if (derived.showLast7DaysMatrix) {
    LastSevenDaysMatrix(
      days = lastSevenDays(derived.weekData),
      habits = derived.currentHabitsConfig,
      compactHeight = derived.useCompactHeight
    )
  } else {
    val showTimeline = state.range is ActivityRange.Quarter || state.range is ActivityRange.Year
    ContributionGrid(
      state = ContributionGridState(
        weekData = derived.weekData,
        range = state.range,
        selectedDay = if (habitsState.activeSelectionId == "main") derived.selectedDay else null,
        selectedWeekStart = if (state.activityMode == ActivityMode.Posts) habitsState.selectedWeek?.startDate else null,
        isActiveSelection = habitsState.activeSelectionId == "main",
        scrollState = chartScrollState,
        showWeekdayLegend = derived.showWeekdayLegend,
        showAllWeekdayLabels = true,
        compactHeight = derived.useCompactHeight,
        showTimeline = showTimeline,
        showDayNumbers = false,
        today = derived.today,
        demoMode = state.demoMode
      ),
      onDaySelected = onDaySelected,
      onClearSelection = onClearSelection,
      onEditRequested = onEditRequested,
      onCreateRequested = onEditRequested
    )
  }
}

@Composable
internal fun StatsWeeklyChart(
  derived: StatsScreenDerivedState,
  dispatch: (HabitsAction) -> Unit
) {
  val state = derived.state
  if (state.activityMode != ActivityMode.Posts) {
    return
  }

  // Show shimmer while loading
  if (derived.isLoadingWeekData && derived.weekData.weeks.isEmpty()) {
    ChartShimmer(derived.useCompactHeight)
    return
  }

  val habitsState = derived.habitsState
  val chartScrollState = rememberScrollState()
  WeeklyBarChart(
    state = WeeklyBarChartState(
      weekData = derived.weekData,
      selectedWeek = habitsState.selectedWeek,
      scrollState = chartScrollState,
      showWeekdayLegend = derived.showWeekdayLegend,
      compactHeight = derived.useCompactHeight
    ),
    onWeekSelected = { week ->
      if (habitsState.selectedWeek?.startDate == week.startDate) {
        dispatch(HabitsAction.ClearSelection)
      } else {
        dispatch(HabitsAction.SelectWeek(week))
      }
    }
  )
}

@Composable
internal fun StatsHabitSections(
  derived: StatsScreenDerivedState,
  dispatch: (HabitsAction) -> Unit
) {
  if (!derived.showHabitSections) {
    return
  }

  // Show shimmers while loading
  if (derived.isLoadingWeekData && derived.weekData.weeks.isEmpty()) {
    derived.currentHabitsConfig.forEach { habit ->
      HabitSectionShimmer(habit.tag, derived.useCompactHeight)
    }
    return
  }

  val habitsState = derived.habitsState
  val onClearSelection = remember(dispatch) { { dispatch(HabitsAction.ClearSelection) } }
  val onEditRequested = remember(dispatch, derived.habitsConfigTimeline) {
    { day: ContributionDay ->
      val config = habitsConfigForDate(derived.habitsConfigTimeline, day.date)?.habits.orEmpty()
      dispatch(HabitsAction.OpenEditor(day, config))
    }
  }

  derived.currentHabitsConfig.forEach { habit ->
    val selectionId = "habit:${habit.tag}"
    val onDaySelected = remember(dispatch, selectionId) {
      { day: ContributionDay -> dispatch(HabitsAction.SelectDay(day, selectionId)) }
    }
    HabitActivitySection(
      state = HabitActivitySectionState(
        habit = habit,
        baseWeekData = derived.weekData,
        selectedDate = if (habitsState.activeSelectionId == selectionId) habitsState.selectedDate else null,
        isActiveSelection = habitsState.activeSelectionId == selectionId,
        showWeekdayLegend = derived.showWeekdayLegend,
        compactHeight = derived.useCompactHeight,
        range = derived.state.range,
        demoMode = derived.state.demoMode,
        today = derived.today,
        habitColor = habit.color
      ),
      onDaySelected = onDaySelected,
      onClearSelection = onClearSelection,
      onEditRequested = onEditRequested,
      onCreateRequested = onEditRequested
    )
  }
}

@Composable
private fun HabitActivitySection(
  state: HabitActivitySectionState,
  onDaySelected: (ContributionDay) -> Unit,
  onClearSelection: () -> Unit,
  onEditRequested: (ContributionDay) -> Unit,
  onCreateRequested: (ContributionDay) -> Unit
) {
  val habitWeekData = remember(state.baseWeekData, state.habit) {
    activityWeekDataForHabit(state.baseWeekData, state.habit)
  }
  val selectedDay = remember(habitWeekData.weeks, state.habit, state.selectedDate) {
    state.selectedDate?.let { date -> findDayByDate(habitWeekData, date) }
  }
  val chartScrollState = rememberScrollState()

  Column(verticalArrangement = Arrangement.spacedBy(Indent.xs), modifier = Modifier.padding(top = Indent.s)) {
    Text(state.habit.label, style = MaterialTheme.typography.titleSmall)
    val showTimeline = state.range is ActivityRange.Quarter || state.range is ActivityRange.Year
    ContributionGrid(
      state = ContributionGridState(
        weekData = habitWeekData,
        range = state.range,
        selectedDay = selectedDay,
        selectedWeekStart = null,
        isActiveSelection = state.isActiveSelection,
        scrollState = chartScrollState,
        showWeekdayLegend = state.showWeekdayLegend,
        showAllWeekdayLabels = true,
        compactHeight = state.compactHeight,
        showTimeline = showTimeline,
        today = state.today,
        habitColor = state.habitColor,
        demoMode = state.demoMode
      ),
      onDaySelected = onDaySelected,
      onClearSelection = onClearSelection,
      onEditRequested = onEditRequested,
      onCreateRequested = onCreateRequested
    )
  }
}

@Composable
private fun LastSevenDaysMatrix(
  days: List<ContributionDay>,
  habits: List<HabitConfig>,
  compactHeight: Boolean
) {
  if (days.isEmpty() || habits.isEmpty()) {
    return
  }
  BoxWithConstraints {
    val labelWidth = HABIT_LABEL_WIDTH
    val spacing = ChartDimens.spacing(compactHeight)
    val availableWidth = (maxWidth - labelWidth - spacing).coerceAtLeast(0.dp)
    val layout = calculateLayout(
      maxWidth = availableWidth,
      columns = days.size.coerceAtLeast(1),
      minColumnSize = ChartDimens.minCell(compactHeight),
      spacing = spacing
    )
    val cellSize = layout.columnSize
    Column(verticalArrangement = Arrangement.spacedBy(Indent.xs)) {
      Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
        Spacer(modifier = Modifier.width(labelWidth))
        days.forEach { day ->
          Box(modifier = Modifier.size(cellSize), contentAlignment = Alignment.Center) {
            Text(
              day.date.dayOfWeek.name.take(2),
              style = MaterialTheme.typography.labelSmall
            )
          }
        }
      }
      val pendingColor = AppColors.inactiveCell.resolve()
      habits.forEach { habit ->
        Row(
          horizontalArrangement = Arrangement.spacedBy(spacing),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(habit.label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(labelWidth))
          days.forEach { day ->
            val done = day.habitStatuses.firstOrNull { status -> status.tag == habit.tag }?.done == true
            val cellColor = if (done) androidx.compose.ui.graphics.Color(habit.color) else pendingColor
            Box(
              modifier = Modifier
                .size(cellSize)
                .background(cellColor, shape = MaterialTheme.shapes.extraSmall)
            )
          }
        }
      }
      Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
        Spacer(modifier = Modifier.width(labelWidth))
        days.forEach { day ->
          Box(modifier = Modifier.size(cellSize), contentAlignment = Alignment.Center) {
            Text(day.date.day.toString(), style = MaterialTheme.typography.labelSmall)
          }
        }
      }
    }
  }
}
