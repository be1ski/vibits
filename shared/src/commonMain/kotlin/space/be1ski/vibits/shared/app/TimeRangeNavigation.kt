package space.be1ski.vibits.shared.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import space.be1ski.vibits.shared.core.platform.isDesktop
import space.be1ski.vibits.shared.core.ui.ActivityMode
import space.be1ski.vibits.shared.core.ui.ActivityRange
import space.be1ski.vibits.shared.core.ui.Indent
import space.be1ski.vibits.shared.feature.habits.domain.usecase.CalculateSuccessRateUseCase
import space.be1ski.vibits.shared.feature.habits.domain.usecase.NavigateActivityRangeUseCase
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsAction
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsState
import space.be1ski.vibits.shared.feature.habits.presentation.StatsScreen
import space.be1ski.vibits.shared.feature.habits.presentation.StatsScreenState
import space.be1ski.vibits.shared.feature.habits.presentation.components.quarterIndex
import space.be1ski.vibits.shared.feature.habits.presentation.components.startOfWeek
import space.be1ski.vibits.shared.feature.memos.presentation.FeedScreen
import space.be1ski.vibits.shared.feature.memos.presentation.MemosAction
import space.be1ski.vibits.shared.feature.memos.presentation.MemosState
import space.be1ski.vibits.shared.feature.memos.presentation.PostsScreen
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.settings.domain.model.TimeRangeTab

private val navigateRange = NavigateActivityRangeUseCase()
private const val PAGER_CENTER_PAGE = 500
private const val MONTHS_PER_QUARTER = 3

@Suppress("LongParameterList")
@Composable
internal fun SwipeableTabContent(
  memosState: MemosState,
  appState: VibitsAppUiState,
  currentRange: ActivityRange,
  minRange: ActivityRange?,
  habitsState: HabitsState,
  onHabitsAction: (HabitsAction) -> Unit,
  calculateSuccessRate: CalculateSuccessRateUseCase,
  dispatchMemos: (MemosAction) -> Unit = {},
) {
  if (appState.selectedScreen == MemosScreen.FEED) {
    FeedScreen(
      memos = memosState.memos,
      isRefreshing = memosState.isLoading,
      onRefresh = {},
      enablePullRefresh = !isDesktop,
      onMemoClick = { memo -> beginEditMemo(appState, memo) },
      onDeleteMemo = { memo -> dispatchMemos(MemosAction.DeleteMemo(memo.name)) },
    )
    return
  }

  val selectedTab =
    when (appState.selectedScreen) {
      MemosScreen.HABITS -> appState.habitsTimeRangeTab
      MemosScreen.STATS -> appState.postsTimeRangeTab
      MemosScreen.FEED -> appState.habitsTimeRangeTab
    }

  // Key the entire pager on selectedTab to force re-initialization when tab changes
  // This prevents flickering where old page content is briefly visible
  key(selectedTab) {
    SwipeablePagerContent(
      memosState = memosState,
      appState = appState,
      currentRange = currentRange,
      minRange = minRange,
      habitsState = habitsState,
      onHabitsAction = onHabitsAction,
      calculateSuccessRate = calculateSuccessRate,
    )
  }
}

@Suppress("LongParameterList")
@Composable
private fun SwipeablePagerContent(
  memosState: MemosState,
  appState: VibitsAppUiState,
  currentRange: ActivityRange,
  minRange: ActivityRange?,
  habitsState: HabitsState,
  onHabitsAction: (HabitsAction) -> Unit,
  calculateSuccessRate: CalculateSuccessRateUseCase,
) {
  val activityRange = activityRangeForState(appState)
  val currentDelta =
    remember(activityRange, currentRange) {
      navigateRange.calculateDelta(currentRange, activityRange)
    }
  val minDelta =
    remember(minRange, currentRange) {
      minRange?.let { navigateRange.calculateDelta(currentRange, it) } ?: -PAGER_CENTER_PAGE
    }
  val maxDelta = 0
  val pageCount = maxDelta - minDelta + 1
  val initialPage = (currentDelta - minDelta).coerceIn(0, pageCount - 1)

  val pagerState =
    rememberPagerState(
      initialPage = initialPage,
      pageCount = { pageCount },
    )

  @Suppress("UNUSED_VARIABLE")
  val scope = rememberCoroutineScope()

  LaunchedEffect(currentDelta, minDelta) {
    val targetPage = (currentDelta - minDelta).coerceIn(0, pageCount - 1)
    if (pagerState.currentPage != targetPage) {
      pagerState.scrollToPage(targetPage)
    }
  }

  LaunchedEffect(pagerState, minDelta) {
    snapshotFlow { pagerState.settledPage }.collect { page ->
      val delta = page + minDelta
      val newRange = navigateRange(currentRange, delta)
      if (newRange != activityRangeForState(appState)) {
        updateTimeRangeState(appState, newRange)
        onHabitsAction(HabitsAction.ClearSelection)
      }
    }
  }

  HorizontalPager(
    state = pagerState,
    modifier = Modifier.fillMaxSize(),
    beyondViewportPageCount = 0,
    pageSpacing = Indent.xl,
    key = { it },
  ) { page ->
    val delta = page + minDelta
    val pageRange = navigateRange(currentRange, delta)
    MemosTabContent(
      memosState = memosState,
      appState = appState,
      activityRange = pageRange,
      habitsState = habitsState,
      onHabitsAction = onHabitsAction,
      calculateSuccessRate = calculateSuccessRate,
    )
  }
}

@Suppress("LongParameterList")
@Composable
private fun MemosTabContent(
  memosState: MemosState,
  appState: VibitsAppUiState,
  activityRange: ActivityRange,
  habitsState: HabitsState,
  onHabitsAction: (HabitsAction) -> Unit,
  calculateSuccessRate: CalculateSuccessRateUseCase,
) {
  val memos = memosState.memos
  when (appState.selectedScreen) {
    MemosScreen.HABITS ->
      StatsScreen(
        state =
          StatsScreenState(
            memos = memos,
            range = activityRange,
            activityMode = ActivityMode.HABITS,
            useVerticalScroll = true,
            enablePullRefresh = false,
            demoMode = appState.appMode == AppMode.DEMO,
          ),
        calculateSuccessRate = calculateSuccessRate,
        habitsState = habitsState,
        onHabitsAction = onHabitsAction,
      )
    MemosScreen.STATS ->
      PostsScreen(
        memos = memos,
        range = activityRange,
        demoMode = appState.appMode == AppMode.DEMO,
        calculateSuccessRate = calculateSuccessRate,
        postsListExpanded = appState.postsListExpanded,
        onPostsListExpandedChange = { appState.postsListExpanded = it },
      )
    MemosScreen.FEED ->
      FeedScreen(
        memos = memos,
        isRefreshing = memosState.isLoading,
        onRefresh = {},
        enablePullRefresh = !isDesktop,
        onMemoClick = { memo -> beginEditMemo(appState, memo) },
      )
  }
}

internal fun activityRangeForState(appState: VibitsAppUiState): ActivityRange {
  val selectedTab =
    when (appState.selectedScreen) {
      MemosScreen.HABITS -> appState.habitsTimeRangeTab
      MemosScreen.STATS -> appState.postsTimeRangeTab
      MemosScreen.FEED -> appState.habitsTimeRangeTab
    }
  val date = appState.periodStartDate
  return when (selectedTab) {
    TimeRangeTab.WEEKS -> ActivityRange.Week(startOfWeek(date))
    TimeRangeTab.MONTHS -> ActivityRange.Month(date.year, date.month)
    TimeRangeTab.QUARTERS -> ActivityRange.Quarter(date.year, quarterIndex(date))
    TimeRangeTab.YEARS -> ActivityRange.Year(date.year)
  }
}

internal fun updateTimeRangeState(
  appState: VibitsAppUiState,
  range: ActivityRange,
) {
  appState.periodStartDate =
    when (range) {
      is ActivityRange.Week -> range.startDate
      is ActivityRange.Month -> LocalDate(range.year, range.month, 1)
      is ActivityRange.Quarter -> {
        val month = Month((range.index - 1) * MONTHS_PER_QUARTER + 1)
        LocalDate(range.year, month, 1)
      }
      is ActivityRange.Year -> LocalDate(range.year, Month.JANUARY, 1)
    }
}

internal fun resetToHome(
  appState: VibitsAppUiState,
  today: LocalDate,
) {
  appState.periodStartDate = today
  when (appState.selectedScreen) {
    MemosScreen.HABITS -> appState.habitsTimeRangeTab = TimeRangeTab.WEEKS
    MemosScreen.STATS -> appState.postsTimeRangeTab = TimeRangeTab.WEEKS
    MemosScreen.FEED -> {}
  }
}

internal fun currentRangeForTab(
  tab: TimeRangeTab,
  today: LocalDate,
): ActivityRange =
  when (tab) {
    TimeRangeTab.WEEKS -> ActivityRange.Week(startOfWeek(today))
    TimeRangeTab.MONTHS -> ActivityRange.Month(today.year, today.month)
    TimeRangeTab.QUARTERS -> ActivityRange.Quarter(today.year, quarterIndex(today))
    TimeRangeTab.YEARS -> ActivityRange.Year(today.year)
  }

internal fun minRangeForTab(
  tab: TimeRangeTab,
  earliestDate: LocalDate?,
): ActivityRange? {
  if (earliestDate == null) {
    return null
  }
  return when (tab) {
    TimeRangeTab.WEEKS -> ActivityRange.Week(startOfWeek(earliestDate))
    TimeRangeTab.MONTHS -> ActivityRange.Month(earliestDate.year, earliestDate.month)
    TimeRangeTab.QUARTERS -> ActivityRange.Quarter(earliestDate.year, quarterIndex(earliestDate))
    TimeRangeTab.YEARS -> ActivityRange.Year(earliestDate.year)
  }
}

/**
 * When switching from larger to smaller granularity, move periodStartDate to the end
 * of the current period so we show the LAST sub-period instead of the first.
 */
internal fun adjustDateForTabChange(
  appState: VibitsAppUiState,
  oldTab: TimeRangeTab,
  newTab: TimeRangeTab,
) {
  if (oldTab.ordinal > newTab.ordinal) {
    // Going from larger to smaller granularity - move to end of current period
    val date = appState.periodStartDate
    val currentRange =
      when (oldTab) {
        TimeRangeTab.WEEKS -> return // Can't go smaller than weeks
        TimeRangeTab.MONTHS -> ActivityRange.Month(date.year, date.month)
        TimeRangeTab.QUARTERS -> ActivityRange.Quarter(date.year, quarterIndex(date))
        TimeRangeTab.YEARS -> ActivityRange.Year(date.year)
      }
    appState.periodStartDate = endDateOfRange(currentRange)
  }
}

private fun endDateOfRange(range: ActivityRange): LocalDate =
  when (range) {
    is ActivityRange.Week -> range.startDate.plus(DatePeriod(days = DAYS_IN_WEEK - 1))
    is ActivityRange.Month -> {
      val nextMonth = LocalDate(range.year, range.month, 1).plus(DatePeriod(months = 1))
      nextMonth.minus(DatePeriod(days = 1))
    }
    is ActivityRange.Quarter -> {
      val lastMonthOfQuarter = range.index * MONTHS_PER_QUARTER
      val firstOfNextQuarter =
        if (lastMonthOfQuarter == MONTHS_IN_YEAR) {
          LocalDate(range.year + 1, Month.JANUARY, 1)
        } else {
          LocalDate(range.year, Month(lastMonthOfQuarter + 1), 1)
        }
      firstOfNextQuarter.minus(DatePeriod(days = 1))
    }
    is ActivityRange.Year -> LocalDate(range.year, Month.DECEMBER, LAST_DAY_OF_DECEMBER)
  }

private const val DAYS_IN_WEEK = 7
private const val MONTHS_IN_YEAR = 12
private const val LAST_DAY_OF_DECEMBER = 31
