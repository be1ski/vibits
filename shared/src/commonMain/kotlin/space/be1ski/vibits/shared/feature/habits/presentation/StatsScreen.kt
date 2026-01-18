@file:OptIn(androidx.compose.material.ExperimentalMaterialApi::class)

package space.be1ski.vibits.shared.feature.habits.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.datetime.TimeZone
import space.be1ski.vibits.shared.core.platform.currentLocalDate
import space.be1ski.vibits.shared.core.ui.ActivityMode
import space.be1ski.vibits.shared.core.ui.ActivityRange
import space.be1ski.vibits.shared.core.ui.Indent
import space.be1ski.vibits.shared.feature.habits.domain.usecase.CalculateSuccessRateUseCase
import space.be1ski.vibits.shared.feature.habits.domain.usecase.GetPeriodPostsUseCase
import space.be1ski.vibits.shared.feature.habits.presentation.components.HabitsConfigDialog
import space.be1ski.vibits.shared.feature.habits.presentation.components.findDailyMemoForDate
import space.be1ski.vibits.shared.feature.habits.presentation.components.habitsConfigForDate
import space.be1ski.vibits.shared.feature.habits.presentation.components.rememberActivityWeekData
import space.be1ski.vibits.shared.feature.habits.presentation.components.rememberHabitsConfigTimeline

/**
 * Stats tab with activity charts.
 */
@Composable
fun StatsScreen(
  state: StatsScreenState,
  calculateSuccessRate: CalculateSuccessRateUseCase,
  habitsState: HabitsState = HabitsState(),
  onHabitsAction: (HabitsAction) -> Unit = {},
  onPostsListExpandedChange: (Boolean) -> Unit = {},
) {
  val derived = rememberStatsScreenDerived(state, habitsState, calculateSuccessRate)
  StatsScreenContent(derived, onHabitsAction, onPostsListExpandedChange)
  StatsScreenDialogs(derived, onHabitsAction)
}

@Suppress("LongMethod")
@Composable
private fun rememberStatsScreenDerived(
  state: StatsScreenState,
  habitsState: HabitsState,
  calculateSuccessRate: CalculateSuccessRateUseCase,
): StatsScreenDerivedState {
  val memos = state.memos
  val range = state.range
  val activityMode = state.activityMode
  val habitsConfigTimeline = rememberHabitsConfigTimeline(memos)
  val currentHabitsConfig =
    remember(habitsConfigTimeline) {
      habitsConfigTimeline.lastOrNull()?.habits ?: emptyList()
    }
  val timeZone = remember { TimeZone.currentSystemDefault() }
  val today = remember { currentLocalDate() }
  val todayMemo =
    remember(memos, timeZone, today) {
      findDailyMemoForDate(memos, timeZone, today)
    }
  val todayConfig =
    remember(habitsConfigTimeline, today) {
      habitsConfigForDate(habitsConfigTimeline, today)?.habits.orEmpty()
    }
  val todayDay =
    remember(todayConfig, todayMemo, today) {
      buildHabitDay(
        date = today,
        habitsConfig = todayConfig,
        dailyMemo = todayMemo,
      )
    }
  val weekDataState = rememberActivityWeekData(memos, range, activityMode)
  val weekData = weekDataState.data
  val isLoadingWeekData = weekDataState.isLoading
  val showWeekdayLegend =
    range is ActivityRange.Week ||
      range is ActivityRange.Month ||
      range is ActivityRange.Quarter
  val useCompactHeight = range is ActivityRange.Year || range is ActivityRange.Month
  val collapseHabits = activityMode == ActivityMode.HABITS && range is ActivityRange.Year
  val showLast7DaysMatrix =
    activityMode == ActivityMode.HABITS &&
      range is ActivityRange.Week &&
      currentHabitsConfig.isNotEmpty()
  val showHabitSections =
    !showLast7DaysMatrix &&
      activityMode == ActivityMode.HABITS &&
      currentHabitsConfig.isNotEmpty()
  val selectedDay =
    remember(weekData.weeks, habitsState.selectedDate) {
      habitsState.selectedDate?.let { date -> findDayByDate(weekData, date) }
    }
  val configStartDate =
    remember(habitsConfigTimeline) {
      habitsConfigTimeline.firstOrNull()?.date
    }
  val successRateData =
    remember(weekData, range, activityMode, currentHabitsConfig, configStartDate) {
      if (activityMode == ActivityMode.HABITS && currentHabitsConfig.isNotEmpty()) {
        calculateSuccessRate(weekData, range, today, configStartDate)
      } else {
        null
      }
    }
  val getPeriodPosts = remember { GetPeriodPostsUseCase() }
  val periodPosts =
    remember(memos, range, timeZone) {
      getPeriodPosts(memos, range, timeZone)
    }
  return StatsScreenDerivedState(
    state = state,
    habitsState = habitsState,
    habitsConfigTimeline = habitsConfigTimeline,
    currentHabitsConfig = currentHabitsConfig,
    weekData = weekData,
    isLoadingWeekData = isLoadingWeekData,
    showWeekdayLegend = showWeekdayLegend,
    useCompactHeight = useCompactHeight,
    collapseHabits = collapseHabits,
    showLast7DaysMatrix = showLast7DaysMatrix,
    showHabitSections = showHabitSections,
    selectedDay = selectedDay,
    todayConfig = todayConfig,
    todayDay = todayDay,
    today = today,
    timeZone = timeZone,
    successRateData = successRateData,
    periodPosts = periodPosts,
    configStartDate = configStartDate,
  )
}

@Composable
private fun StatsScreenContent(
  derived: StatsScreenDerivedState,
  dispatch: (HabitsAction) -> Unit,
  onPostsListExpandedChange: (Boolean) -> Unit,
) {
  val state = derived.state
  val columnModifier =
    if (state.useVerticalScroll) {
      Modifier.verticalScroll(rememberScrollState())
    } else {
      Modifier
    }

  Column(
    verticalArrangement = Arrangement.spacedBy(Indent.s),
    modifier = columnModifier,
  ) {
    StatsHeaderRow()
    StatsHabitsEmptyState(derived, dispatch)
    StatsInfoCard(derived, dispatch)
    StatsPostsInfoCard(derived)
    StatsMainChart(derived, dispatch)
    StatsWeeklyChart(derived, dispatch)
    StatsCollapsiblePosts(derived, state.postsListExpanded, onPostsListExpandedChange)
    StatsHabitSections(derived, dispatch)
  }
}

@Composable
private fun StatsScreenDialogs(
  derived: StatsScreenDerivedState,
  dispatch: (HabitsAction) -> Unit,
) {
  HabitEditorDialog(derived, dispatch)
  EmptyDeleteDialog(derived, dispatch)
  SingleHabitToggleDialog(derived, dispatch)
  HabitsConfigDialog(derived.habitsState, dispatch)
}
