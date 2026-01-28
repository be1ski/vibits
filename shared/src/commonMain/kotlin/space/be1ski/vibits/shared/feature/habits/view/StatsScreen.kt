@file:OptIn(androidx.compose.material.ExperimentalMaterialApi::class)

package space.be1ski.vibits.shared.feature.habits.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import space.be1ski.vibits.shared.app.domain.model.ActivityMode
import space.be1ski.vibits.shared.app.domain.model.ActivityRange
import space.be1ski.vibits.shared.core.platform.date.currentLocalDate
import space.be1ski.vibits.shared.core.ui.Indent
import space.be1ski.vibits.shared.core.ui.date.DateFormatter
import space.be1ski.vibits.shared.feature.habits.domain.model.ActivityWeekData
import space.be1ski.vibits.shared.feature.habits.domain.model.ContributionDay
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitConfig
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitsConfigEntry
import space.be1ski.vibits.shared.feature.habits.domain.model.findDayByDate
import space.be1ski.vibits.shared.feature.habits.domain.usecase.ExtractDailyMemosUseCase
import space.be1ski.vibits.shared.feature.habits.domain.usecase.ExtractHabitsConfigUseCase
import space.be1ski.vibits.shared.feature.habits.domain.usecase.GetPeriodPostsUseCase
import space.be1ski.vibits.shared.feature.habits.domain.usecase.parseMemoDate
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsAction
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsState
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo

/**
 * Stats tab with activity charts.
 */
@Composable
fun StatsScreen(
  state: StatsScreenState,
  dateFormatter: DateFormatter,
  habitsState: HabitsState = HabitsState(),
  onHabitsAction: (HabitsAction) -> Unit = {},
  onPostsListExpandedChange: (Boolean) -> Unit = {},
) {
  // Trigger recalculation when memos, range, or mode change
  LaunchedEffect(state.memos, state.range, state.activityMode) {
    onHabitsAction(HabitsAction.InvalidateCache(state.memos, state.range, state.activityMode))
  }

  val derived = rememberStatsScreenDerived(state, habitsState, dateFormatter)
  StatsScreenContent(derived, onHabitsAction, onPostsListExpandedChange)
  StatsScreenDialogs(derived, onHabitsAction)
}

@Composable
private fun rememberStatsScreenDerived(
  state: StatsScreenState,
  habitsState: HabitsState,
  dateFormatter: DateFormatter,
): StatsScreenDerivedState {
  val memos = state.memos
  val range = state.range
  val activityMode = state.activityMode
  val timeZone = remember { TimeZone.currentSystemDefault() }
  val today = remember { currentLocalDate() }

  val activityData = habitsState.getActivityData(range, activityMode)
  val weekData = activityData?.weekData ?: ActivityWeekData(emptyList(), 0, 0)
  val habitsConfigTimeline = activityData?.configTimeline ?: emptyList()
  val currentHabitsConfig = habitsConfigTimeline.lastOrNull()?.habits ?: emptyList()
  val successRateData = activityData?.successRate
  val isLoadingWeekData = habitsState.isDataLoading(range, activityMode)

  val uiFlags = rememberUiFlags(range, activityMode, currentHabitsConfig)
  val todayData = rememberTodayHabitsData(memos, habitsConfigTimeline, timeZone, today)
  val postsData = rememberPostsData(memos, range, timeZone, today)

  val selectedDay =
    remember(weekData.weeks, habitsState.selectedDate) {
      habitsState.selectedDate?.let { date -> weekData.findDayByDate(date) }
    }

  val configStartDate =
    remember(habitsConfigTimeline) {
      habitsConfigTimeline.firstOrNull()?.date
    }

  return StatsScreenDerivedState(
    state = state,
    habitsState = habitsState,
    habitsConfigTimeline = habitsConfigTimeline,
    currentHabitsConfig = currentHabitsConfig,
    weekData = weekData,
    isLoadingWeekData = isLoadingWeekData,
    showWeekdayLegend = uiFlags.showWeekdayLegend,
    useCompactHeight = uiFlags.useCompactHeight,
    collapseHabits = uiFlags.collapseHabits,
    showLast7DaysMatrix = uiFlags.showLast7DaysMatrix,
    showHabitSections = uiFlags.showHabitSections,
    selectedDay = selectedDay,
    todayConfig = todayData.config,
    todayDay = todayData.day,
    today = today,
    timeZone = timeZone,
    successRateData = successRateData,
    periodPosts = postsData.periodPosts,
    todayPostsCount = postsData.todayCount,
    dateFormatter = dateFormatter,
    configStartDate = configStartDate,
  )
}

private data class UiFlags(
  val showWeekdayLegend: Boolean,
  val useCompactHeight: Boolean,
  val collapseHabits: Boolean,
  val showLast7DaysMatrix: Boolean,
  val showHabitSections: Boolean,
)

@Composable
private fun rememberUiFlags(
  range: ActivityRange,
  activityMode: ActivityMode,
  currentHabitsConfig: List<HabitConfig>,
): UiFlags {
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

  return UiFlags(
    showWeekdayLegend = showWeekdayLegend,
    useCompactHeight = useCompactHeight,
    collapseHabits = collapseHabits,
    showLast7DaysMatrix = showLast7DaysMatrix,
    showHabitSections = showHabitSections,
  )
}

private data class TodayHabitsData(
  val config: List<HabitConfig>,
  val day: ContributionDay?,
)

@Composable
private fun rememberTodayHabitsData(
  memos: List<Memo>,
  habitsConfigTimeline: List<HabitsConfigEntry>,
  timeZone: TimeZone,
  today: LocalDate,
): TodayHabitsData {
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
  return TodayHabitsData(config = todayConfig, day = todayDay)
}

private data class PostsData(
  val periodPosts: List<Memo>,
  val todayCount: Int,
)

@Composable
private fun rememberPostsData(
  memos: List<Memo>,
  range: ActivityRange,
  timeZone: TimeZone,
  today: LocalDate,
): PostsData {
  val getPeriodPosts = remember { GetPeriodPostsUseCase() }
  val periodPosts =
    remember(memos, range, timeZone) {
      getPeriodPosts(memos, range, timeZone)
    }
  val todayPostsCount =
    remember(memos, today, timeZone) {
      memos.count { memo ->
        val memoDate = parseMemoDate(memo, timeZone)
        memoDate == today && !memo.content.contains("#habits")
      }
    }
  return PostsData(periodPosts = periodPosts, todayCount = todayPostsCount)
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
