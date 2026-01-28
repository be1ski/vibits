@file:OptIn(androidx.compose.material.ExperimentalMaterialApi::class)

package space.be1ski.vibits.shared.feature.habits.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.datetime.TimeZone
import space.be1ski.vibits.shared.app.domain.model.ActivityMode
import space.be1ski.vibits.shared.app.domain.model.ActivityRange
import space.be1ski.vibits.shared.core.platform.date.currentLocalDate
import space.be1ski.vibits.shared.core.ui.Indent
import space.be1ski.vibits.shared.core.ui.date.DateFormatter
import space.be1ski.vibits.shared.feature.habits.domain.model.ActivityWeekData
import space.be1ski.vibits.shared.feature.habits.domain.model.findDayByDate
import space.be1ski.vibits.shared.feature.habits.domain.usecase.ExtractDailyMemosUseCase
import space.be1ski.vibits.shared.feature.habits.domain.usecase.ExtractHabitsConfigUseCase
import space.be1ski.vibits.shared.feature.habits.domain.usecase.GetPeriodPostsUseCase
import space.be1ski.vibits.shared.feature.habits.presentation.ActivityCacheKey
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsAction
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsState
import space.be1ski.vibits.shared.feature.habits.presentation.getActivityData
import space.be1ski.vibits.shared.feature.habits.presentation.isDataLoading
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode

/**
 * Stats tab with activity charts.
 */
@Composable
fun StatsScreen(
  state: StatsScreenState,
  appMode: AppMode,
  dateFormatter: DateFormatter,
  habitsState: HabitsState = HabitsState(),
  onHabitsAction: (HabitsAction) -> Unit = {},
  onPostsListExpandedChange: (Boolean) -> Unit = {},
) {
  val derived = rememberStatsScreenDerived(state, appMode, habitsState, dateFormatter)
  StatsScreenContent(derived, onHabitsAction, onPostsListExpandedChange)
  StatsScreenDialogs(derived, onHabitsAction)
}

@Suppress("LongMethod")
@Composable
private fun rememberStatsScreenDerived(
  state: StatsScreenState,
  appMode: AppMode,
  habitsState: HabitsState,
  dateFormatter: DateFormatter,
): StatsScreenDerivedState {
  val memos = state.memos
  val range = state.range
  val activityMode = state.activityMode

  // Read from TEA cache (new system)
  val cacheKey =
    remember(range, activityMode, appMode) {
      ActivityCacheKey(range, activityMode, appMode)
    }
  val cachedData = habitsState.getActivityData(range, activityMode, appMode)
  val isLoadingWeekData = habitsState.isDataLoading(cacheKey)

  // Read from TEA cache
  val emptyWeekData =
    remember {
      ActivityWeekData(
        weeks = emptyList(),
        maxDaily = 0,
        maxWeekly = 0,
      )
    }
  val weekData = cachedData?.weekData ?: emptyWeekData
  val habitsConfigTimeline = cachedData?.configTimeline.orEmpty()
  val successRateData = cachedData?.successRate

  val currentHabitsConfig =
    remember(habitsConfigTimeline) {
      habitsConfigTimeline.lastOrNull()?.habits ?: emptyList()
    }
  val timeZone = remember { TimeZone.currentSystemDefault() }
  val today = remember { currentLocalDate() }
  val todayMemo =
    remember(memos, timeZone, today) {
      ExtractDailyMemosUseCase.forDate(memos, timeZone, today)
    }
  val todayConfig =
    remember(habitsConfigTimeline, today) {
      ExtractHabitsConfigUseCase.forDate(habitsConfigTimeline, today)?.habits.orEmpty()
    }
  val todayDay =
    remember(todayConfig, todayMemo, today) {
      buildHabitDay(
        date = today,
        habitsConfig = todayConfig,
        dailyMemo = todayMemo,
      )
    }
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
      habitsState.selectedDate?.let { date -> weekData.findDayByDate(date) }
    }
  val configStartDate =
    remember(habitsConfigTimeline) {
      habitsConfigTimeline.firstOrNull()?.date
    }
  // Success rate comes from TEA cache
  val finalSuccessRateData = successRateData
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
    successRateData = finalSuccessRateData,
    periodPosts = periodPosts,
    dateFormatter = dateFormatter,
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
  EmptyDeleteDialog(derived, dispatch)
  SingleHabitToggleDialog(derived, dispatch)
}
