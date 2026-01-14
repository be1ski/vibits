package space.be1ski.memos.shared.presentation.screen

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
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import space.be1ski.memos.shared.Res
import space.be1ski.memos.shared.*
import space.be1ski.memos.shared.presentation.components.ActivityMode
import space.be1ski.memos.shared.presentation.components.ActivityRange
import space.be1ski.memos.shared.presentation.components.ContributionDay
import space.be1ski.memos.shared.presentation.components.ContributionGrid
import space.be1ski.memos.shared.presentation.components.ContributionGridCallbacks
import space.be1ski.memos.shared.presentation.components.ContributionGridState
import space.be1ski.memos.shared.presentation.components.HabitConfig
import space.be1ski.memos.shared.presentation.components.Indent
import space.be1ski.memos.shared.presentation.components.calculateLayout
import space.be1ski.memos.shared.presentation.components.habitsConfigForDate
import space.be1ski.memos.shared.presentation.components.obfuscateIfNeeded
import space.be1ski.memos.shared.presentation.components.WeeklyBarChart
import space.be1ski.memos.shared.presentation.components.WeeklyBarChartState
import space.be1ski.memos.shared.presentation.components.activityWeekDataForHabit
import space.be1ski.memos.shared.presentation.components.lastSevenDays
import space.be1ski.memos.shared.presentation.components.ChartDimens

@Composable
internal fun StatsHeaderRow(derived: StatsScreenDerivedState) {
  val state = derived.state
  val uiState = derived.uiState
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(stringResource(Res.string.label_activity), style = MaterialTheme.typography.titleMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(Indent.xs), verticalAlignment = Alignment.CenterVertically) {
      if (state.activityMode == ActivityMode.Habits) {
        TextButton(onClick = { uiState.showHabitsConfig = !uiState.showHabitsConfig }) {
          Text(stringResource(Res.string.label_habits_config))
        }
      }
    }
  }
}

@Composable
internal fun StatsHabitsConfigSection(derived: StatsScreenDerivedState) {
  val state = derived.state
  val actions = derived.actions
  val uiState = derived.uiState
  if (state.activityMode == ActivityMode.Habits && uiState.showHabitsConfig) {
    HabitsConfigCard(
      habitsConfigText = uiState.habitsConfigText,
      onConfigChange = { uiState.habitsConfigText = it },
      onSave = {
        val content = buildHabitsConfigContent(uiState.habitsConfigText)
        actions.onCreateDailyMemo(content)
      }
    )
  }
}

@Composable
internal fun StatsHabitsEmptyState(derived: StatsScreenDerivedState) {
  val state = derived.state
  val uiState = derived.uiState
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
      Button(onClick = { uiState.showHabitsConfig = true }) {
        Text(stringResource(Res.string.action_configure_habits))
      }
    }
  }
}

@Composable
internal fun StatsTodaySection(derived: StatsScreenDerivedState) {
  val state = derived.state
  if (state.activityMode != ActivityMode.Habits || derived.todayConfig.isEmpty()) {
    return
  }
  val todayStatuses = derived.todayDay?.habitStatuses.orEmpty()
  val doneCount = todayStatuses.count { it.done }
  val totalCount = todayStatuses.size
  OutlinedCard(modifier = Modifier.fillMaxWidth()) {
    Column(
      modifier = Modifier.padding(Indent.s),
      verticalArrangement = Arrangement.spacedBy(Indent.xs)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(Indent.x5s)) {
          Text(stringResource(Res.string.label_today), style = MaterialTheme.typography.titleSmall)
          Text(
            stringResource(Res.string.format_habits_progress, doneCount, totalCount),
            style = MaterialTheme.typography.bodySmall
          )
        }
        Button(onClick = { openTodayHabitEditor(derived) }) {
          Text(stringResource(Res.string.action_track))
        }
      }
    }
  }
}

@Composable
internal fun StatsMainChart(derived: StatsScreenDerivedState) {
  val state = derived.state
  val uiState = derived.uiState
  val chartScrollState = rememberScrollState()
  if (derived.showLast7DaysMatrix) {
    LastSevenDaysMatrix(
      days = lastSevenDays(derived.weekData),
      habits = derived.currentHabitsConfig,
      compactHeight = derived.useCompactHeight,
      demoMode = state.demoMode
    )
  } else {
    val showTimeline = state.range is ActivityRange.Quarter || state.range is ActivityRange.Year
    ContributionGrid(
      state = ContributionGridState(
        weekData = derived.weekData,
        range = state.range,
        selectedDay = if (uiState.activeSelectionId == "main") derived.selectedDay else null,
        selectedWeekStart = if (state.activityMode == ActivityMode.Posts) uiState.selectedWeek?.startDate else null,
        isActiveSelection = uiState.activeSelectionId == "main",
        scrollState = chartScrollState,
        showWeekdayLegend = derived.showWeekdayLegend,
        showAllWeekdayLabels = true,
        compactHeight = derived.useCompactHeight,
        showTimeline = showTimeline,
        showDayNumbers = false
      ),
      callbacks = ContributionGridCallbacks(
        onDaySelected = { day ->
          uiState.selectedDate = day.date
          uiState.activeSelectionId = "main"
          if (state.activityMode == ActivityMode.Posts) {
            uiState.selectedWeek = derived.weekData.weeks.firstOrNull { week ->
              week.days.any { it.date == day.date }
            }
          }
        },
        onClearSelection = {
          uiState.selectedDate = null
          uiState.selectedWeek = null
          uiState.activeSelectionId = null
        },
        onEditRequested = { day ->
          uiState.habitsEditorDay = day
          uiState.habitsEditorExisting = day.dailyMemo
          uiState.habitsEditorConfig = habitsConfigForDate(derived.habitsConfigTimeline, day.date)?.habits.orEmpty()
          uiState.habitsEditorSelections = buildHabitsEditorSelections(day, uiState.habitsEditorConfig)
        },
        onCreateRequested = { day ->
          uiState.habitsEditorDay = day
          uiState.habitsEditorExisting = day.dailyMemo
          uiState.habitsEditorConfig = habitsConfigForDate(derived.habitsConfigTimeline, day.date)?.habits.orEmpty()
          uiState.habitsEditorSelections = buildHabitsEditorSelections(day, uiState.habitsEditorConfig)
          uiState.habitsEditorError = null
        },
        demoMode = state.demoMode
      )
    )
  }
}

@Composable
internal fun StatsHabitDetailsToggle(derived: StatsScreenDerivedState) {
  if (!derived.collapseHabits || derived.currentHabitsConfig.isEmpty()) {
    return
  }
  val uiState = derived.uiState
  OutlinedButton(
    onClick = { uiState.showHabitDetails = !uiState.showHabitDetails },
    modifier = Modifier.fillMaxWidth()
  ) {
    Text(if (uiState.showHabitDetails) stringResource(Res.string.action_hide_details) else stringResource(Res.string.action_show_details))
  }
}

@Composable
internal fun StatsWeeklyChart(derived: StatsScreenDerivedState) {
  val state = derived.state
  if (state.activityMode != ActivityMode.Posts) {
    return
  }
  val uiState = derived.uiState
  val chartScrollState = rememberScrollState()
  WeeklyBarChart(
    state = WeeklyBarChartState(
      weekData = derived.weekData,
      selectedWeek = uiState.selectedWeek,
      scrollState = chartScrollState,
      showWeekdayLegend = derived.showWeekdayLegend,
      compactHeight = derived.useCompactHeight
    ),
    onWeekSelected = { week ->
      uiState.selectedWeek = if (uiState.selectedWeek?.startDate == week.startDate) null else week
      uiState.selectedDate = uiState.selectedDate?.takeIf { date ->
        week.days.any { it.date == date }
      }
    }
  )
}

@Composable
internal fun StatsHabitSections(derived: StatsScreenDerivedState) {
  if (!derived.showHabitSections) {
    return
  }
  val uiState = derived.uiState
  derived.currentHabitsConfig.forEach { habit ->
    HabitActivitySection(
      state = HabitActivitySectionState(
        habit = habit.copy(
          label = obfuscateIfNeeded(habit.label, derived.state.demoMode, "Hidden habit")
        ),
        baseWeekData = derived.weekData,
        selectedDate = if (uiState.activeSelectionId == "habit:${habit.tag}") uiState.selectedDate else null,
        isActiveSelection = uiState.activeSelectionId == "habit:${habit.tag}",
        showWeekdayLegend = derived.showWeekdayLegend,
        compactHeight = derived.useCompactHeight,
        range = derived.state.range,
        demoMode = derived.state.demoMode
      ),
      actions = HabitActivitySectionActions(
        onDaySelected = { day ->
          uiState.selectedDate = day.date
          uiState.activeSelectionId = "habit:${habit.tag}"
        },
        onClearSelection = {
          uiState.selectedDate = null
          uiState.activeSelectionId = null
        },
        onEditRequested = { day ->
          uiState.habitsEditorDay = day
          uiState.habitsEditorExisting = day.dailyMemo
          uiState.habitsEditorConfig = habitsConfigForDate(derived.habitsConfigTimeline, day.date)?.habits.orEmpty()
          uiState.habitsEditorSelections = buildHabitsEditorSelections(day, uiState.habitsEditorConfig)
          uiState.habitsEditorError = null
        },
        onCreateRequested = { day ->
          uiState.habitsEditorDay = day
          uiState.habitsEditorExisting = day.dailyMemo
          uiState.habitsEditorConfig = habitsConfigForDate(derived.habitsConfigTimeline, day.date)?.habits.orEmpty()
          uiState.habitsEditorSelections = buildHabitsEditorSelections(day, uiState.habitsEditorConfig)
        }
      )
    )
  }
}

@Composable
internal fun BoxScope.StatsFloatingAction(derived: StatsScreenDerivedState) {
  val state = derived.state
  if (state.activityMode != ActivityMode.Habits || derived.todayConfig.isEmpty()) {
    return
  }
  FloatingActionButton(
    onClick = { openTodayHabitEditor(derived) },
    modifier = Modifier.align(Alignment.BottomEnd)
  ) {
    Icon(
      imageVector = Icons.Filled.AddTask,
      contentDescription = stringResource(Res.string.action_track_today)
    )
  }
}

@Composable
private fun HabitsConfigCard(
  habitsConfigText: String,
  onConfigChange: (String) -> Unit,
  onSave: () -> Unit
) {
  Column(verticalArrangement = Arrangement.spacedBy(Indent.xs), modifier = Modifier.padding(Indent.xs)) {
    Text(stringResource(Res.string.label_habits_config), style = MaterialTheme.typography.titleSmall)
    TextField(
      value = habitsConfigText,
      onValueChange = onConfigChange,
      modifier = Modifier.fillMaxWidth(),
      placeholder = { Text(stringResource(Res.string.hint_habits_config)) }
    )
    Row(horizontalArrangement = Arrangement.spacedBy(Indent.xs)) {
      Button(onClick = onSave) {
        Text(stringResource(Res.string.action_save))
      }
    }
  }
}

private fun openTodayHabitEditor(derived: StatsScreenDerivedState) {
  val uiState = derived.uiState
  val day = derived.todayDay ?: return
  uiState.habitsEditorDay = day
  uiState.habitsEditorExisting = day.dailyMemo
  uiState.habitsEditorConfig = derived.todayConfig
  uiState.habitsEditorSelections = buildHabitsEditorSelections(day, uiState.habitsEditorConfig)
  uiState.habitsEditorError = null
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
        showTimeline = showTimeline
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
  compactHeight: Boolean,
  demoMode: Boolean
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
      habits.forEach { habit ->
        Row(
          horizontalArrangement = Arrangement.spacedBy(spacing),
          verticalAlignment = Alignment.CenterVertically
        ) {
          val label = obfuscateIfNeeded(habit.label, demoMode, "Hidden habit")
          Text(label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(labelWidth))
          days.forEach { day ->
            val done = day.habitStatuses.firstOrNull { status -> status.tag == habit.tag }?.done == true
            val cellColor = if (done) HABIT_DONE_COLOR else HABIT_PENDING_COLOR
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
