@file:Suppress("TooManyFunctions")

package space.be1ski.vibits.shared.feature.habits.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.FloatingActionButton
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
import space.be1ski.vibits.shared.action_track_today
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
import space.be1ski.vibits.shared.feature.habits.presentation.components.ContributionGridCallbacks
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
internal fun StatsInfoCard(derived: StatsScreenDerivedState) {
  val state = derived.state
  val dispatch = derived.dispatch
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
internal fun StatsHabitsEmptyState(derived: StatsScreenDerivedState) {
  val state = derived.state
  val dispatch = derived.dispatch
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

@Composable
internal fun StatsMainChart(derived: StatsScreenDerivedState) {
  val state = derived.state
  val habitsState = derived.habitsState
  val dispatch = derived.dispatch
  val chartScrollState = rememberScrollState()
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
        today = derived.today
      ),
      callbacks = ContributionGridCallbacks(
        onDaySelected = { day ->
          dispatch(HabitsAction.SelectDay(day, "main"))
          if (state.activityMode == ActivityMode.Posts) {
            val week = derived.weekData.weeks.firstOrNull { week ->
              week.days.any { it.date == day.date }
            }
            if (week != null) {
              dispatch(HabitsAction.SelectWeek(week))
            }
          }
        },
        onClearSelection = { dispatch(HabitsAction.ClearSelection) },
        onEditRequested = { day ->
          val config = habitsConfigForDate(derived.habitsConfigTimeline, day.date)?.habits.orEmpty()
          dispatch(HabitsAction.OpenEditor(day, config))
        },
        onCreateRequested = { day ->
          val config = habitsConfigForDate(derived.habitsConfigTimeline, day.date)?.habits.orEmpty()
          dispatch(HabitsAction.OpenEditor(day, config))
        },
        demoMode = state.demoMode
      )
    )
  }
}

@Composable
internal fun StatsWeeklyChart(derived: StatsScreenDerivedState) {
  val state = derived.state
  if (state.activityMode != ActivityMode.Posts) {
    return
  }
  val habitsState = derived.habitsState
  val dispatch = derived.dispatch
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
internal fun StatsHabitSections(derived: StatsScreenDerivedState) {
  if (!derived.showHabitSections) {
    return
  }
  val habitsState = derived.habitsState
  val dispatch = derived.dispatch
  derived.currentHabitsConfig.forEach { habit ->
    HabitActivitySection(
      state = HabitActivitySectionState(
        habit = habit,
        baseWeekData = derived.weekData,
        selectedDate = if (habitsState.activeSelectionId == "habit:${habit.tag}") habitsState.selectedDate else null,
        isActiveSelection = habitsState.activeSelectionId == "habit:${habit.tag}",
        showWeekdayLegend = derived.showWeekdayLegend,
        compactHeight = derived.useCompactHeight,
        range = derived.state.range,
        demoMode = derived.state.demoMode,
        today = derived.today,
        habitColor = habit.color
      ),
      actions = HabitActivitySectionActions(
        onDaySelected = { day ->
          dispatch(HabitsAction.SelectDay(day, "habit:${habit.tag}"))
        },
        onClearSelection = {
          dispatch(HabitsAction.ClearSelection)
        },
        onEditRequested = { day ->
          val config = habitsConfigForDate(derived.habitsConfigTimeline, day.date)?.habits.orEmpty()
          dispatch(HabitsAction.OpenEditor(day, config))
        },
        onCreateRequested = { day ->
          val config = habitsConfigForDate(derived.habitsConfigTimeline, day.date)?.habits.orEmpty()
          dispatch(HabitsAction.OpenEditor(day, config))
        }
      )
    )
  }
}

@Composable
internal fun BoxScope.StatsFloatingAction(derived: StatsScreenDerivedState) {
  val state = derived.state
  val dispatch = derived.dispatch
  if (state.activityMode != ActivityMode.Habits || derived.todayConfig.isEmpty()) {
    return
  }
  FloatingActionButton(
    onClick = {
      val day = derived.todayDay ?: return@FloatingActionButton
      dispatch(HabitsAction.OpenEditor(day, derived.todayConfig))
    },
    modifier = Modifier.align(Alignment.BottomEnd)
  ) {
    Icon(
      imageVector = Icons.Filled.AddTask,
      contentDescription = stringResource(Res.string.action_track_today)
    )
  }
}

@Composable
private fun HabitActivitySection(
  state: HabitActivitySectionState,
  actions: HabitActivitySectionActions
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
        habitColor = state.habitColor
      ),
      callbacks = ContributionGridCallbacks(
        onDaySelected = actions.onDaySelected,
        onClearSelection = actions.onClearSelection,
        onEditRequested = actions.onEditRequested,
        onCreateRequested = actions.onCreateRequested,
        demoMode = state.demoMode
      )
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
