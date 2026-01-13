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
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddTask
import space.be1ski.memos.shared.presentation.components.ActivityMode
import space.be1ski.memos.shared.presentation.components.ContributionDay
import space.be1ski.memos.shared.presentation.components.ContributionGrid
import space.be1ski.memos.shared.presentation.components.ContributionGridCallbacks
import space.be1ski.memos.shared.presentation.components.ContributionGridState
import space.be1ski.memos.shared.presentation.components.HabitConfig
import space.be1ski.memos.shared.presentation.components.calculateLayout
import space.be1ski.memos.shared.presentation.components.habitsConfigForDate
import space.be1ski.memos.shared.presentation.components.WeeklyBarChart
import space.be1ski.memos.shared.presentation.components.WeeklyBarChartState
import space.be1ski.memos.shared.presentation.components.activityWeekDataForHabit
import space.be1ski.memos.shared.presentation.components.lastSevenDays
import space.be1ski.memos.shared.presentation.components.ChartDimens

@Composable
internal fun StatsHeaderRow(derived: StatsScreenDerivedState) {
  val state = derived.state
  val actions = derived.actions
  val uiState = derived.uiState
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text("Activity", style = MaterialTheme.typography.titleMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
      if (state.activityMode == ActivityMode.Habits) {
        TextButton(onClick = { uiState.showHabitsConfig = !uiState.showHabitsConfig }) {
          Text("Habits config")
        }
      }
      ActivityRangeSelector(
        years = state.years,
        selectedRange = state.range,
        onRangeChange = actions.onRangeChange
      )
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
internal fun StatsMainChart(derived: StatsScreenDerivedState) {
  val state = derived.state
  val uiState = derived.uiState
  val chartScrollState = rememberScrollState()
  if (derived.showLast7DaysMatrix) {
    LastSevenDaysMatrix(
      days = lastSevenDays(derived.weekData),
      habits = derived.currentHabitsConfig,
      compactHeight = derived.useCompactHeight
    )
  } else {
    ContributionGrid(
      state = ContributionGridState(
        weekData = derived.weekData,
        range = state.range,
        selectedDay = if (uiState.activeSelectionId == "main") derived.selectedDay else null,
        selectedWeekStart = if (state.activityMode == ActivityMode.Posts) uiState.selectedWeek?.startDate else null,
        isActiveSelection = uiState.activeSelectionId == "main",
        scrollState = chartScrollState,
        showWeekdayLegend = derived.showWeekdayLegend,
        compactHeight = derived.useCompactHeight,
        showTimeline = true
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
        }
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
    Text(if (uiState.showHabitDetails) "Hide habit details" else "Show habit details")
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
        habit = habit,
        baseWeekData = derived.weekData,
        selectedDate = if (uiState.activeSelectionId == "habit:${habit.tag}") uiState.selectedDate else null,
        isActiveSelection = uiState.activeSelectionId == "habit:${habit.tag}",
        showWeekdayLegend = derived.showWeekdayLegend,
        compactHeight = derived.useCompactHeight,
        range = derived.state.range
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
  val uiState = derived.uiState
  if (state.activityMode != ActivityMode.Habits || derived.todayConfig.isEmpty()) {
    return
  }
  FloatingActionButton(
    onClick = {
      val day = derived.todayDay ?: return@FloatingActionButton
      uiState.habitsEditorDay = day
      uiState.habitsEditorExisting = day.dailyMemo
      uiState.habitsEditorConfig = derived.todayConfig
      uiState.habitsEditorSelections = buildHabitsEditorSelections(day, uiState.habitsEditorConfig)
      uiState.habitsEditorError = null
    },
    modifier = Modifier
      .align(Alignment.BottomEnd)
      .padding(16.dp)
  ) {
    Icon(
      imageVector = Icons.Filled.AddTask,
      contentDescription = "Track today"
    )
  }
}

@Composable
private fun HabitsConfigCard(
  habitsConfigText: String,
  onConfigChange: (String) -> Unit,
  onSave: () -> Unit
) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(8.dp)) {
    Text("Habits config", style = MaterialTheme.typography.titleSmall)
    TextField(
      value = habitsConfigText,
      onValueChange = onConfigChange,
      modifier = Modifier.fillMaxWidth(),
      placeholder = { Text("Гимнастика | #habits/gym\nЧтение | #habits/reading") }
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      Button(onClick = onSave) {
        Text("Save")
      }
    }
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

  Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 12.dp)) {
    Text(state.habit.label, style = MaterialTheme.typography.titleSmall)
    ContributionGrid(
      state = ContributionGridState(
        weekData = habitWeekData,
        range = state.range,
        selectedDay = selectedDay,
        selectedWeekStart = null,
        isActiveSelection = state.isActiveSelection,
        scrollState = chartScrollState,
        showWeekdayLegend = state.showWeekdayLegend,
        compactHeight = state.compactHeight
      ),
      callbacks = ContributionGridCallbacks(
        onDaySelected = actions.onDaySelected,
        onClearSelection = actions.onClearSelection,
        onEditRequested = actions.onEditRequested,
        onCreateRequested = actions.onCreateRequested
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
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
          Text(habit.label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(labelWidth))
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
