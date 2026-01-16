package space.be1ski.vibits.shared.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import space.be1ski.vibits.shared.core.platform.isDesktop
import space.be1ski.vibits.shared.core.ui.ActivityMode
import space.be1ski.vibits.shared.core.ui.ActivityRange
import space.be1ski.vibits.shared.feature.habits.domain.usecase.NavigateActivityRangeUseCase
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsAction
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsState
import space.be1ski.vibits.shared.feature.habits.presentation.StatsScreen
import space.be1ski.vibits.shared.feature.habits.presentation.StatsScreenState
import space.be1ski.vibits.shared.feature.habits.presentation.components.quarterIndex
import space.be1ski.vibits.shared.feature.habits.presentation.components.startOfWeek
import space.be1ski.vibits.shared.feature.memos.presentation.FeedScreen
import space.be1ski.vibits.shared.feature.memos.presentation.MemosState
import space.be1ski.vibits.shared.feature.memos.presentation.PostsScreen
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.preferences.domain.model.TimeRangeTab

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
  onHabitsAction: (HabitsAction) -> Unit
) {
  if (appState.selectedScreen == MemosScreen.Feed) {
    FeedScreen(
      memos = memosState.memos,
      isRefreshing = memosState.isLoading,
      onRefresh = {},
      enablePullRefresh = !isDesktop,
      onMemoClick = { memo -> beginEditMemo(appState, memo) }
    )
    return
  }

  val activityRange = activityRangeForState(appState)
  val currentDelta = remember(activityRange, currentRange) {
    navigateRange.calculateDelta(currentRange, activityRange)
  }
  val minDelta = remember(minRange, currentRange) {
    minRange?.let { navigateRange.calculateDelta(currentRange, it) } ?: -PAGER_CENTER_PAGE
  }
  val maxDelta = 0
  val pageCount = maxDelta - minDelta + 1
  val initialPage = (currentDelta - minDelta).coerceIn(0, pageCount - 1)

  val pagerState = rememberPagerState(
    initialPage = initialPage,
    pageCount = { pageCount }
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
      }
    }
  }

  HorizontalPager(
    state = pagerState,
    modifier = Modifier.fillMaxSize(),
    beyondViewportPageCount = 0,
    key = { it }
  ) { page ->
    val delta = page + minDelta
    val pageRange = navigateRange(currentRange, delta)
    MemosTabContent(
      memosState = memosState,
      appState = appState,
      activityRange = pageRange,
      habitsState = habitsState,
      onHabitsAction = onHabitsAction
    )
  }
}

@Composable
private fun MemosTabContent(
  memosState: MemosState,
  appState: VibitsAppUiState,
  activityRange: ActivityRange,
  habitsState: HabitsState,
  onHabitsAction: (HabitsAction) -> Unit
) {
  val memos = memosState.memos
  when (appState.selectedScreen) {
    MemosScreen.Habits -> StatsScreen(
      state = StatsScreenState(
        memos = memos,
        range = activityRange,
        activityMode = ActivityMode.Habits,
        useVerticalScroll = true,
        enablePullRefresh = false,
        demoMode = appState.appMode == AppMode.Demo
      ),
      habitsState = habitsState,
      onHabitsAction = onHabitsAction
    )
    MemosScreen.Stats -> PostsScreen(
      memos = memos,
      range = activityRange,
      demoMode = appState.appMode == AppMode.Demo
    )
    MemosScreen.Feed -> FeedScreen(
      memos = memos,
      isRefreshing = memosState.isLoading,
      onRefresh = {},
      enablePullRefresh = !isDesktop,
      onMemoClick = { memo -> beginEditMemo(appState, memo) }
    )
  }
}

internal fun activityRangeForState(appState: VibitsAppUiState): ActivityRange {
  val selectedTab = when (appState.selectedScreen) {
    MemosScreen.Habits -> appState.habitsTimeRangeTab
    MemosScreen.Stats -> appState.postsTimeRangeTab
    MemosScreen.Feed -> appState.habitsTimeRangeTab
  }
  val date = appState.periodStartDate
  return when (selectedTab) {
    TimeRangeTab.Weeks -> ActivityRange.Week(startOfWeek(date))
    TimeRangeTab.Months -> ActivityRange.Month(date.year, date.month)
    TimeRangeTab.Quarters -> ActivityRange.Quarter(date.year, quarterIndex(date))
    TimeRangeTab.Years -> ActivityRange.Year(date.year)
  }
}

internal fun updateTimeRangeState(appState: VibitsAppUiState, range: ActivityRange) {
  appState.periodStartDate = when (range) {
    is ActivityRange.Week -> range.startDate
    is ActivityRange.Month -> LocalDate(range.year, range.month, 1)
    is ActivityRange.Quarter -> {
      val month = Month((range.index - 1) * MONTHS_PER_QUARTER + 1)
      LocalDate(range.year, month, 1)
    }
    is ActivityRange.Year -> LocalDate(range.year, Month.JANUARY, 1)
  }
}

internal fun resetToHome(appState: VibitsAppUiState, today: LocalDate) {
  appState.periodStartDate = today
  when (appState.selectedScreen) {
    MemosScreen.Habits -> appState.habitsTimeRangeTab = TimeRangeTab.Weeks
    MemosScreen.Stats -> appState.postsTimeRangeTab = TimeRangeTab.Weeks
    MemosScreen.Feed -> {}
  }
}

internal fun currentRangeForTab(tab: TimeRangeTab, today: LocalDate): ActivityRange {
  return when (tab) {
    TimeRangeTab.Weeks -> ActivityRange.Week(startOfWeek(today))
    TimeRangeTab.Months -> ActivityRange.Month(today.year, today.month)
    TimeRangeTab.Quarters -> ActivityRange.Quarter(today.year, quarterIndex(today))
    TimeRangeTab.Years -> ActivityRange.Year(today.year)
  }
}

internal fun minRangeForTab(tab: TimeRangeTab, earliestDate: LocalDate?): ActivityRange? {
  if (earliestDate == null) {
    return null
  }
  return when (tab) {
    TimeRangeTab.Weeks -> ActivityRange.Week(startOfWeek(earliestDate))
    TimeRangeTab.Months -> ActivityRange.Month(earliestDate.year, earliestDate.month)
    TimeRangeTab.Quarters -> ActivityRange.Quarter(earliestDate.year, quarterIndex(earliestDate))
    TimeRangeTab.Years -> ActivityRange.Year(earliestDate.year)
  }
}
