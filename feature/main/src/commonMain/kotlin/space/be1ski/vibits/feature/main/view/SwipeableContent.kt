package space.be1ski.vibits.feature.main.view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import space.be1ski.vibits.core.platform.date.quarterIndex
import space.be1ski.vibits.core.platform.date.startOfWeek
import space.be1ski.vibits.core.platform.isDesktop
import space.be1ski.vibits.core.ui.Indent
import space.be1ski.vibits.core.ui.date.DateFormatter
import space.be1ski.vibits.feature.habits.domain.model.ActivityMode
import space.be1ski.vibits.feature.habits.domain.model.ActivityRange
import space.be1ski.vibits.feature.habits.domain.parseConfigFromContent
import space.be1ski.vibits.feature.habits.domain.usecase.CalculateActivityRangeDeltaUseCase
import space.be1ski.vibits.feature.habits.domain.usecase.ExtractHabitsConfigUseCase
import space.be1ski.vibits.feature.habits.domain.usecase.NavigateActivityRangeUseCase
import space.be1ski.vibits.feature.habits.presentation.action.HabitsAction
import space.be1ski.vibits.feature.habits.presentation.state.HabitsState
import space.be1ski.vibits.feature.habits.presentation.view.StatsScreen
import space.be1ski.vibits.feature.habits.presentation.view.StatsScreenState
import space.be1ski.vibits.feature.main.domain.model.AppState
import space.be1ski.vibits.feature.main.domain.model.Screen
import space.be1ski.vibits.feature.main.presentation.action.AppAction
import space.be1ski.vibits.feature.memos.domain.model.Memo
import space.be1ski.vibits.feature.memos.domain.model.PostFilter
import space.be1ski.vibits.feature.memos.domain.usecase.ClassifyPostTypeUseCase
import space.be1ski.vibits.feature.memos.presentation.action.MemosAction
import space.be1ski.vibits.feature.memos.presentation.state.MemosState
import space.be1ski.vibits.feature.memos.presentation.view.FeedScreen
import space.be1ski.vibits.feature.memos.presentation.view.PostsScreen
import space.be1ski.vibits.feature.settings.domain.model.TimeRangeTab

private const val PAGER_CENTER_PAGE = 500

@Composable
internal fun SwipeableTabContent(
  memosState: MemosState,
  appState: AppState,
  currentRange: ActivityRange,
  minRange: ActivityRange?,
  habitsState: HabitsState,
  onHabitsAction: (HabitsAction) -> Unit,
  onAppAction: (AppAction) -> Unit,
  dateFormatter: DateFormatter,
  dispatchMemos: (MemosAction) -> Unit = {},
  feedListState: LazyListState,
) {
  if (appState.selectedScreen == Screen.FEED) {
    FeedScreen(
      memos = memosState.memos,
      dateFormatter = dateFormatter,
      activeFilter = memosState.activePostFilter,
      onFilterChange = { filter -> dispatchMemos(MemosAction.Loading.ChangePostFilter(filter)) },
      isRefreshing = memosState.isLoading,
      onRefresh = {},
      enablePullRefresh = !isDesktop,
      demoMode = appState.isDemoMode,
      onMemoClick = { memo ->
        handleMemoClick(
          memo = memo,
          memos = memosState.memos,
          onMemosAction = dispatchMemos,
          onHabitsAction = onHabitsAction,
        )
      },
      onDeleteMemo = { memo -> dispatchMemos(MemosAction.Crud.DeleteMemo(memo.name)) },
      listState = feedListState,
    )
    return
  }

  val selectedTab =
    when (appState.selectedScreen) {
      Screen.HABITS -> appState.habitsTimeRangeTab
      Screen.STATS -> appState.postsTimeRangeTab
      Screen.FEED -> appState.habitsTimeRangeTab
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
      onAppAction = onAppAction,
      onMemosAction = dispatchMemos,
      dateFormatter = dateFormatter,
    )
  }
}

@Suppress("LongMethod")
@Composable
private fun SwipeablePagerContent(
  memosState: MemosState,
  appState: AppState,
  currentRange: ActivityRange,
  minRange: ActivityRange?,
  habitsState: HabitsState,
  onHabitsAction: (HabitsAction) -> Unit,
  onAppAction: (AppAction) -> Unit,
  onMemosAction: (MemosAction) -> Unit,
  dateFormatter: DateFormatter,
) {
  val activityRange = activityRangeForAppState(appState)
  val currentActivityRange by rememberUpdatedState(activityRange)
  val currentDelta =
    remember(activityRange, currentRange) {
      CalculateActivityRangeDeltaUseCase(currentRange, activityRange)
    }
  val minDelta =
    remember(minRange, currentRange) {
      minRange?.let { CalculateActivityRangeDeltaUseCase(currentRange, it) } ?: -PAGER_CENTER_PAGE
    }
  val maxDelta = 0
  val pageCount = maxDelta - minDelta + 1
  val initialPage = (currentDelta - minDelta).coerceIn(0, pageCount - 1)

  val pagerState =
    rememberPagerState(
      initialPage = initialPage,
      pageCount = { pageCount },
    )

  LaunchedEffect(currentDelta, minDelta) {
    val targetPage = (currentDelta - minDelta).coerceIn(0, pageCount - 1)
    if (pagerState.currentPage != targetPage) {
      pagerState.scrollToPage(targetPage)
    }
  }

  LaunchedEffect(pagerState, minDelta) {
    snapshotFlow { pagerState.settledPage }.collect { page ->
      val delta = page + minDelta
      val newRange = NavigateActivityRangeUseCase(currentRange, delta)
      if (newRange != currentActivityRange) {
        onAppAction(AppAction.SetActivityRange(newRange))
        onHabitsAction(HabitsAction.Selection.ClearSelection)
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
    val pageRange = NavigateActivityRangeUseCase(currentRange, delta)
    MemosTabContent(
      memosState = memosState,
      appState = appState,
      activityRange = pageRange,
      habitsState = habitsState,
      onHabitsAction = onHabitsAction,
      onAppAction = onAppAction,
      onMemosAction = onMemosAction,
      dateFormatter = dateFormatter,
    )
  }
}

@Composable
private fun MemosTabContent(
  memosState: MemosState,
  appState: AppState,
  activityRange: ActivityRange,
  habitsState: HabitsState,
  onHabitsAction: (HabitsAction) -> Unit,
  onAppAction: (AppAction) -> Unit,
  onMemosAction: (MemosAction) -> Unit,
  dateFormatter: DateFormatter,
) {
  val memos = memosState.memos
  when (appState.selectedScreen) {
    Screen.HABITS ->
      StatsScreen(
        state =
          StatsScreenState(
            memos = memos,
            range = activityRange,
            activityMode = ActivityMode.HABITS,
            useVerticalScroll = true,
            enablePullRefresh = false,
            demoMode = appState.isDemoMode,
          ),
        appMode = appState.appMode,
        dateFormatter = dateFormatter,
        habitsState = habitsState,
        onHabitsAction = onHabitsAction,
      )
    Screen.STATS ->
      PostsScreen(
        memos = memos,
        range = activityRange,
        appMode = appState.appMode,
        demoMode = appState.isDemoMode,
        dateFormatter = dateFormatter,
        habitsState = habitsState,
        onHabitsAction = onHabitsAction,
        postsListExpanded = appState.postsListExpanded,
        onPostsListExpandedChange = { onAppAction(AppAction.SetPostsListExpanded(it)) },
      )
    Screen.FEED ->
      FeedScreen(
        memos = memos,
        dateFormatter = dateFormatter,
        activeFilter = memosState.activePostFilter,
        onFilterChange = { filter -> onMemosAction(MemosAction.Loading.ChangePostFilter(filter)) },
        isRefreshing = memosState.isLoading,
        onRefresh = {},
        enablePullRefresh = !isDesktop,
        demoMode = appState.isDemoMode,
        onMemoClick = { memo ->
          handleMemoClick(
            memo = memo,
            memos = memosState.memos,
            onMemosAction = onMemosAction,
            onHabitsAction = onHabitsAction,
          )
        },
      )
  }
}

internal fun activityRangeForAppState(appState: AppState): ActivityRange {
  val date = appState.periodStartDate
  return when (appState.currentTimeRangeTab) {
    TimeRangeTab.WEEKS -> ActivityRange.Week(startOfWeek(date))
    TimeRangeTab.MONTHS -> ActivityRange.Month(date.year, date.month)
    TimeRangeTab.QUARTERS -> ActivityRange.Quarter(date.year, quarterIndex(date))
    TimeRangeTab.YEARS -> ActivityRange.Year(date.year)
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

private fun handleMemoClick(
  memo: Memo,
  memos: List<Memo>,
  onMemosAction: (MemosAction) -> Unit,
  onHabitsAction: (HabitsAction) -> Unit,
) {
  val postType = ClassifyPostTypeUseCase(memo)
  val timeZone = TimeZone.currentSystemDefault()
  val configEntries = ExtractHabitsConfigUseCase(memos, timeZone)
  val currentConfig = configEntries.lastOrNull()?.habits ?: emptyList()

  when (postType) {
    PostFilter.CONFIG -> {
      // Parse config from the clicked memo using domain logic
      val clickedConfig = parseConfigFromContent(memo.content)
      onHabitsAction(HabitsAction.Config.OpenConfigDialog(clickedConfig, existingMemo = memo))
    }
    PostFilter.HABIT_TRACKING -> {
      onHabitsAction(HabitsAction.Editor.OpenEditor(config = currentConfig, memo = memo))
    }
    else -> {
      onMemosAction(MemosAction.EditDialog.ShowEditDialog(memo))
    }
  }
}
