@file:OptIn(androidx.compose.material.ExperimentalMaterialApi::class)

package space.be1ski.vibits.feature.habits.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import kotlinx.datetime.TimeZone
import space.be1ski.vibits.core.platform.date.currentLocalDate
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.core.ui.Indent
import space.be1ski.vibits.core.ui.date.DateFormatter
import space.be1ski.vibits.feature.habits.domain.model.ActivityMode
import space.be1ski.vibits.feature.habits.domain.model.ActivityRange
import space.be1ski.vibits.feature.habits.domain.model.ActivitySummary
import space.be1ski.vibits.feature.habits.domain.model.ContributionDay
import space.be1ski.vibits.feature.habits.domain.model.HabitConfig
import space.be1ski.vibits.feature.habits.domain.model.HabitsConfigEntry
import space.be1ski.vibits.feature.habits.domain.model.SuccessRate
import space.be1ski.vibits.feature.habits.domain.model.findDayByDate
import space.be1ski.vibits.feature.habits.domain.usecase.ExtractDailyMemosUseCase
import space.be1ski.vibits.feature.habits.domain.usecase.ExtractHabitsConfigUseCase
import space.be1ski.vibits.feature.habits.domain.usecase.GetPeriodPostsUseCase
import space.be1ski.vibits.feature.habits.presentation.action.HabitsAction
import space.be1ski.vibits.feature.habits.presentation.state.ActivityCacheKey
import space.be1ski.vibits.feature.habits.presentation.state.HabitsState
import space.be1ski.vibits.feature.habits.presentation.state.getActivityData
import space.be1ski.vibits.feature.habits.presentation.state.isDataLoading
import space.be1ski.vibits.feature.habits.presentation.view.components.HabitPicker
import space.be1ski.vibits.feature.memos.domain.model.Memo

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
  onSelectedHabitTagChange: (String?) -> Unit = {},
) {
  val derived = rememberStatsScreenDerived(state, appMode, habitsState, dateFormatter)
  StatsScreenContent(derived, onHabitsAction, onPostsListExpandedChange, onSelectedHabitTagChange)
  StatsScreenDialogs(derived, onHabitsAction)
}

@Composable
private fun rememberStatsScreenDerived(
  state: StatsScreenState,
  appMode: AppMode,
  habitsState: HabitsState,
  dateFormatter: DateFormatter,
): StatsScreenDerivedState {
  val range = state.range
  val activityMode = state.activityMode

  val cached = rememberCachedActivityData(habitsState, range, activityMode, appMode)
  val currentHabitsConfig =
    remember(cached.habitsConfigTimeline) {
      cached.habitsConfigTimeline.lastOrNull()?.habits ?: emptyList()
    }
  val timeZone = remember { TimeZone.currentSystemDefault() }
  val today = remember { currentLocalDate() }
  val todayData = rememberTodayData(state.memos, cached.habitsConfigTimeline, timeZone, today)

  val showLast7DaysMatrix =
    activityMode == ActivityMode.HABITS &&
      range is ActivityRange.Week &&
      currentHabitsConfig.isNotEmpty()

  return StatsScreenDerivedState(
    state = state,
    habitsState = habitsState,
    habitsConfigTimeline = cached.habitsConfigTimeline,
    currentHabitsConfig = currentHabitsConfig,
    weekData = cached.weekData,
    isLoadingWeekData = cached.isLoadingWeekData,
    showWeekdayLegend = range is ActivityRange.Week || range is ActivityRange.Month || range is ActivityRange.Quarter,
    useCompactHeight = range !is ActivityRange.Week,
    collapseHabits = activityMode == ActivityMode.HABITS && range is ActivityRange.Year,
    showLast7DaysMatrix = showLast7DaysMatrix,
    showHabitSections = !showLast7DaysMatrix && activityMode == ActivityMode.HABITS && currentHabitsConfig.isNotEmpty(),
    useHabitPicker = state.wideLayout && activityMode == ActivityMode.HABITS && currentHabitsConfig.isNotEmpty(),
    selectedDay =
      remember(cached.weekData.weeks, habitsState.selectedDate) {
        habitsState.selectedDate?.let { date -> cached.weekData.findDayByDate(date) }
      },
    todayConfig = todayData.todayConfig,
    todayDay = todayData.todayDay,
    today = today,
    timeZone = timeZone,
    successRate = cached.successRate,
    periodPosts = remember(state.memos, range, timeZone) { GetPeriodPostsUseCase(state.memos, range, timeZone) },
    dateFormatter = dateFormatter,
    configStartDate = remember(cached.habitsConfigTimeline) { cached.habitsConfigTimeline.firstOrNull()?.date },
  )
}

private class CachedActivityResult(
  val weekData: ActivitySummary,
  val habitsConfigTimeline: List<HabitsConfigEntry>,
  val isLoadingWeekData: Boolean,
  val successRate: SuccessRate?,
)

@Composable
private fun rememberCachedActivityData(
  habitsState: HabitsState,
  range: ActivityRange,
  activityMode: ActivityMode,
  appMode: AppMode,
): CachedActivityResult {
  val cacheKey =
    remember(range, activityMode, appMode) {
      ActivityCacheKey(range, activityMode, appMode)
    }
  val cachedData = habitsState.getActivityData(range, activityMode, appMode)
  val isLoadingWeekData = habitsState.isDataLoading(cacheKey)
  val emptyWeekData =
    remember {
      ActivitySummary(weeks = emptyList(), maxDaily = 0, maxWeekly = 0)
    }
  return CachedActivityResult(
    weekData = cachedData?.weekData ?: emptyWeekData,
    habitsConfigTimeline = cachedData?.configTimeline.orEmpty(),
    isLoadingWeekData = isLoadingWeekData,
    successRate = cachedData?.successRate,
  )
}

private class TodayData(
  val todayConfig: List<HabitConfig>,
  val todayDay: ContributionDay?,
)

@Composable
private fun rememberTodayData(
  memos: List<Memo>,
  habitsConfigTimeline: List<HabitsConfigEntry>,
  timeZone: TimeZone,
  today: kotlinx.datetime.LocalDate,
): TodayData {
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
      buildHabitDay(date = today, habitsConfig = todayConfig, dailyMemo = todayMemo)
    }
  return TodayData(todayConfig = todayConfig, todayDay = todayDay)
}

@Composable
private fun StatsScreenContent(
  derived: StatsScreenDerivedState,
  dispatch: (HabitsAction) -> Unit,
  onPostsListExpandedChange: (Boolean) -> Unit,
  onSelectedHabitTagChange: (String?) -> Unit,
) {
  val state = derived.state
  val columnModifier =
    if (state.useVerticalScroll) {
      Modifier.verticalScroll(rememberScrollState())
    } else {
      Modifier
    }

  val selectedHabitTag = state.selectedHabitTag
  LaunchedEffect(derived.currentHabitsConfig, selectedHabitTag) {
    if (selectedHabitTag != null &&
      derived.currentHabitsConfig.none { it.tag == selectedHabitTag }
    ) {
      onSelectedHabitTagChange(null)
    }
  }

  val verticalSpacing = if (state.wideLayout) Indent.xs else Indent.s
  Column(
    verticalArrangement = Arrangement.spacedBy(verticalSpacing),
    modifier = Modifier.fillMaxSize().then(columnModifier).testTag(StatsTestTags.STATS_SCREEN),
  ) {
    StatsHabitsEmptyState(derived, dispatch)
    StatsInfoCard(derived, dispatch)
    StatsPostsInfoCard(derived)
    if (derived.useHabitPicker && !derived.showLast7DaysMatrix) {
      HabitPicker(
        habits = derived.currentHabitsConfig,
        selectedHabitTag = selectedHabitTag,
        demoMode = state.demoMode,
        onSelect = onSelectedHabitTagChange,
      )
      if (selectedHabitTag != null) {
        StatsSelectedHabitChart(derived, dispatch, selectedHabitTag)
      } else {
        StatsMainChart(derived, dispatch)
      }
    } else {
      StatsMainChart(derived, dispatch)
      StatsWeeklyChart(derived, dispatch)
      StatsCollapsiblePosts(derived, state.postsListExpanded, onPostsListExpandedChange)
      StatsHabitSections(derived, dispatch)
    }
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
