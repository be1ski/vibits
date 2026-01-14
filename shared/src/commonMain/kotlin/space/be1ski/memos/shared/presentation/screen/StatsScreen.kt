@file:OptIn(androidx.compose.material.ExperimentalMaterialApi::class)

package space.be1ski.memos.shared.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.datetime.TimeZone
import space.be1ski.memos.shared.presentation.components.ActivityMode
import space.be1ski.memos.shared.presentation.components.ActivityRange
import space.be1ski.memos.shared.presentation.components.Indent
import space.be1ski.memos.shared.presentation.components.findDailyMemoForDate
import space.be1ski.memos.shared.presentation.components.habitsConfigForDate
import space.be1ski.memos.shared.presentation.components.rememberActivityWeekData
import space.be1ski.memos.shared.presentation.components.rememberHabitsConfigTimeline
import space.be1ski.memos.shared.presentation.habits.HabitsAction
import space.be1ski.memos.shared.presentation.habits.HabitsState
import space.be1ski.memos.shared.presentation.time.currentLocalDate
import space.be1ski.memos.shared.presentation.util.isDesktop

/**
 * Stats tab with activity charts.
 */
@Composable
fun StatsScreen(
  state: StatsScreenState,
  habitsState: HabitsState = HabitsState(),
  onHabitsAction: (HabitsAction) -> Unit = {}
) {
  val derived = rememberStatsScreenDerived(state, habitsState, onHabitsAction)
  SyncStatsScreenState(derived)
  StatsScreenContent(derived)
  StatsScreenDialogs(derived)
}

@Composable
private fun rememberStatsScreenDerived(
  state: StatsScreenState,
  habitsState: HabitsState,
  dispatch: (HabitsAction) -> Unit
): StatsScreenDerivedState {
  val memos = state.memos
  val range = state.range
  val activityMode = state.activityMode
  val habitsConfigTimeline = rememberHabitsConfigTimeline(memos)
  val currentHabitsConfig = remember(habitsConfigTimeline) {
    habitsConfigTimeline.lastOrNull()?.habits ?: emptyList()
  }
  val timeZone = remember { TimeZone.currentSystemDefault() }
  val today = remember { currentLocalDate() }
  val todayMemo = remember(memos, timeZone, today) {
    findDailyMemoForDate(memos, timeZone, today)
  }
  val todayConfig = remember(habitsConfigTimeline, today) {
    habitsConfigForDate(habitsConfigTimeline, today)?.habits.orEmpty()
  }
  val todayDay = remember(todayConfig, todayMemo, today) {
    buildHabitDay(
      date = today,
      habitsConfig = todayConfig,
      dailyMemo = todayMemo
    )
  }
  val weekData = rememberActivityWeekData(memos, range, activityMode)
  val showWeekdayLegend = range is ActivityRange.Week ||
    range is ActivityRange.Month ||
    range is ActivityRange.Quarter
  val useCompactHeight = range is ActivityRange.Year && !isDesktop
  val collapseHabits = activityMode == ActivityMode.Habits && range is ActivityRange.Year
  val showLast7DaysMatrix = activityMode == ActivityMode.Habits &&
    range is ActivityRange.Week &&
    currentHabitsConfig.isNotEmpty()
  val showHabitSections = !showLast7DaysMatrix &&
    activityMode == ActivityMode.Habits &&
    currentHabitsConfig.isNotEmpty()
  val selectedDay = remember(weekData.weeks, habitsState.selectedDate) {
    habitsState.selectedDate?.let { date -> findDayByDate(weekData, date) }
  }
  return StatsScreenDerivedState(
    state = state,
    habitsState = habitsState,
    dispatch = dispatch,
    habitsConfigTimeline = habitsConfigTimeline,
    currentHabitsConfig = currentHabitsConfig,
    weekData = weekData,
    showWeekdayLegend = showWeekdayLegend,
    useCompactHeight = useCompactHeight,
    collapseHabits = collapseHabits,
    showLast7DaysMatrix = showLast7DaysMatrix,
    showHabitSections = showHabitSections,
    selectedDay = selectedDay,
    todayConfig = todayConfig,
    todayDay = todayDay,
    timeZone = timeZone
  )
}

@Composable
private fun SyncStatsScreenState(derived: StatsScreenDerivedState) {
  val habitsState = derived.habitsState
  val dispatch = derived.dispatch
  val currentConfigText = remember(derived.currentHabitsConfig) {
    derived.currentHabitsConfig.joinToString("\n") { "${it.label} | ${it.tag}" }
  }
  LaunchedEffect(derived.weekData.weeks) {
    if (habitsState.selectedDate == null && habitsState.activeSelectionId == null) {
      val lastDay = derived.weekData.weeks.lastOrNull()?.days?.lastOrNull()
      if (lastDay != null) {
        dispatch(HabitsAction.SelectDay(lastDay, "main"))
      }
    }
  }
  LaunchedEffect(habitsState.showConfigEditor, currentConfigText) {
    if (habitsState.showConfigEditor && habitsState.configText.isBlank()) {
      dispatch(HabitsAction.UpdateConfigText(currentConfigText))
    }
  }
}

@Composable
private fun StatsScreenContent(derived: StatsScreenDerivedState) {
  val state = derived.state
  val columnModifier = if (state.useVerticalScroll) {
    Modifier.verticalScroll(rememberScrollState())
  } else {
    Modifier
  }

  Box(
    modifier = Modifier.fillMaxSize()
  ) {
    Column(
      verticalArrangement = Arrangement.spacedBy(Indent.s),
      modifier = columnModifier
    ) {
      StatsHeaderRow(derived)
      StatsHabitsEmptyState(derived)
      StatsTodaySection(derived)
      StatsHabitsConfigSection(derived)
      StatsMainChart(derived)
      StatsWeeklyChart(derived)
      StatsHabitSections(derived)
    }
    StatsFloatingAction(derived)
  }
}

@Composable
private fun StatsScreenDialogs(derived: StatsScreenDerivedState) {
  HabitEditorDialog(derived)
  EmptyDeleteDialog(derived)
}
