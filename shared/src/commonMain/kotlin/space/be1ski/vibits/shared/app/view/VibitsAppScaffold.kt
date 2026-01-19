package space.be1ski.vibits.shared.app.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.shared.Res
import space.be1ski.vibits.shared.action_create_memo
import space.be1ski.vibits.shared.action_track_today
import space.be1ski.vibits.shared.app.di.AppDependencies
import space.be1ski.vibits.shared.app.view.model.MemosFabMode
import space.be1ski.vibits.shared.app.view.model.MemosScreen
import space.be1ski.vibits.shared.app.view.model.VibitsAppUiState
import space.be1ski.vibits.shared.app.view.model.memosFabModeForScreen
import space.be1ski.vibits.shared.core.platform.date.currentLocalDate
import space.be1ski.vibits.shared.core.ui.ActivityRange
import space.be1ski.vibits.shared.core.ui.Indent
import space.be1ski.vibits.shared.feature.habits.domain.model.ContributionDay
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitConfig
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitsConfigEntry
import space.be1ski.vibits.shared.feature.habits.domain.usecase.EarliestMemoDateUseCase
import space.be1ski.vibits.shared.feature.habits.domain.usecase.ExtractDailyMemosUseCase
import space.be1ski.vibits.shared.feature.habits.domain.usecase.ExtractHabitsConfigUseCase
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsAction
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsState
import space.be1ski.vibits.shared.feature.habits.view.buildHabitDay
import space.be1ski.vibits.shared.feature.habits.view.components.rememberHabitsConfigTimeline
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.memos.presentation.MemosAction
import space.be1ski.vibits.shared.feature.memos.presentation.MemosState
import space.be1ski.vibits.shared.feature.settings.domain.model.AppLanguage
import space.be1ski.vibits.shared.feature.settings.domain.model.AppTheme
import space.be1ski.vibits.shared.feature.settings.domain.model.TimeRangeTab
import space.be1ski.vibits.shared.feature.settings.domain.usecase.TimeRangeScreen
import space.be1ski.vibits.shared.feature.settings.presentation.SettingsAction

@Composable
internal fun VibitsAppScaffold(
  features: AppFeatures,
  dependencies: AppDependencies,
  memosState: MemosState,
  habitsState: HabitsState,
  currentLanguage: AppLanguage,
  currentTheme: AppTheme,
) {
  val appState = features.appState
  val onHabitsAction = features.habits::send

  val timeZone = remember { TimeZone.currentSystemDefault() }
  val today = currentLocalDate()
  val habitsTimeline = rememberHabitsConfigTimeline(memosState.memos)
  val todayData = rememberTodayData(habitsTimeline, memosState.memos, timeZone, today)

  val feedListState = rememberLazyListState()
  val scope = rememberCoroutineScope()
  val callbacks = rememberScaffoldCallbacks(appState, onHabitsAction, feedListState, scope, todayData, dependencies)

  Scaffold(
    floatingActionButton = { AppFab(appState, todayData, callbacks) },
    bottomBar = { MemosBottomNavigation(appState, callbacks.onClearSelection, callbacks.onFeedScrollToTop) },
  ) { padding ->
    ScaffoldContent(
      padding = padding,
      features = features,
      dependencies = dependencies,
      memosState = memosState,
      habitsState = habitsState,
      habitsTimeline = habitsTimeline,
      timeZone = timeZone,
      today = today,
      callbacks = callbacks,
      currentLanguage = currentLanguage,
      currentTheme = currentTheme,
      feedListState = feedListState,
    )
  }
}

private class TodayData(
  val config: List<HabitConfig>,
  val day: ContributionDay?,
)

@Composable
private fun rememberTodayData(
  habitsTimeline: List<HabitsConfigEntry>,
  memos: List<Memo>,
  timeZone: TimeZone,
  today: LocalDate,
): TodayData {
  val todayConfig =
    remember(habitsTimeline, today) {
      ExtractHabitsConfigUseCase.forDate(habitsTimeline, today)?.habits.orEmpty()
    }
  val todayMemo =
    remember(memos, today) {
      ExtractDailyMemosUseCase.forDate(memos, timeZone, today)
    }
  val todayDay =
    remember(todayConfig, todayMemo, today) {
      buildHabitDay(date = today, habitsConfig = todayConfig, dailyMemo = todayMemo)
    }
  return TodayData(config = todayConfig, day = todayDay)
}

internal class ScaffoldCallbacks(
  val onClearSelection: () -> Unit,
  val onFeedScrollToTop: () -> Unit,
  val onShowCreateMemoDialog: () -> Unit,
  val onOpenTodayEditor: () -> Unit,
  val onRangeChange: (ActivityRange) -> Unit,
  val onTabChange: (TimeRangeTab) -> Unit,
)

@Composable
private fun rememberScaffoldCallbacks(
  appState: VibitsAppUiState,
  onHabitsAction: (HabitsAction) -> Unit,
  feedListState: LazyListState,
  scope: CoroutineScope,
  todayData: TodayData,
  dependencies: AppDependencies,
): ScaffoldCallbacks {
  val onClearSelection = remember(onHabitsAction) { { onHabitsAction(HabitsAction.ClearSelection) } }
  val onFeedScrollToTop: () -> Unit =
    remember(feedListState, scope) {
      {
        scope.launch { feedListState.animateScrollToItem(0) }
        Unit
      }
    }
  val onShowCreateMemoDialog = remember(appState) { { appState.showCreateMemoDialog = true } }
  val onOpenTodayEditor: () -> Unit =
    remember(onHabitsAction, todayData) {
      {
        todayData.day?.let { onHabitsAction(HabitsAction.OpenEditor(it, todayData.config)) }
        Unit
      }
    }
  val onRangeChange =
    remember(onHabitsAction, appState) {
      { range: ActivityRange ->
        onHabitsAction(HabitsAction.ClearSelection)
        updateTimeRangeState(appState, range)
      }
    }
  val onTabChange =
    remember(onHabitsAction, appState, dependencies.saveTimeRangeTab) {
      { newTab: TimeRangeTab ->
        onHabitsAction(HabitsAction.ClearSelection)
        handleTabChange(appState, newTab, dependencies)
      }
    }
  return ScaffoldCallbacks(
    onClearSelection = onClearSelection,
    onFeedScrollToTop = onFeedScrollToTop,
    onShowCreateMemoDialog = onShowCreateMemoDialog,
    onOpenTodayEditor = onOpenTodayEditor,
    onRangeChange = onRangeChange,
    onTabChange = onTabChange,
  )
}

private fun handleTabChange(
  appState: VibitsAppUiState,
  newTab: TimeRangeTab,
  dependencies: AppDependencies,
) {
  when (appState.selectedScreen) {
    MemosScreen.HABITS -> {
      adjustDateForTabChange(appState, appState.habitsTimeRangeTab, newTab)
      appState.habitsTimeRangeTab = newTab
      dependencies.saveTimeRangeTab(TimeRangeScreen.HABITS, newTab)
    }
    MemosScreen.STATS -> {
      adjustDateForTabChange(appState, appState.postsTimeRangeTab, newTab)
      appState.postsTimeRangeTab = newTab
      dependencies.saveTimeRangeTab(TimeRangeScreen.POSTS, newTab)
    }
    MemosScreen.FEED -> {}
  }
}

@Composable
private fun AppFab(
  appState: VibitsAppUiState,
  todayData: TodayData,
  callbacks: ScaffoldCallbacks,
) {
  when (memosFabModeForScreen(appState.selectedScreen)) {
    MemosFabMode.MEMO -> {
      FloatingActionButton(onClick = callbacks.onShowCreateMemoDialog) {
        Icon(Icons.Filled.Edit, contentDescription = stringResource(Res.string.action_create_memo))
      }
    }
    MemosFabMode.HABITS -> {
      if (todayData.config.isNotEmpty() && todayData.day != null) {
        FloatingActionButton(onClick = callbacks.onOpenTodayEditor) {
          Icon(Icons.Filled.AddTask, contentDescription = stringResource(Res.string.action_track_today))
        }
      }
    }
  }
}

@Suppress("LongMethod")
@Composable
private fun ScaffoldContent(
  padding: PaddingValues,
  features: AppFeatures,
  dependencies: AppDependencies,
  memosState: MemosState,
  habitsState: HabitsState,
  habitsTimeline: List<HabitsConfigEntry>,
  timeZone: TimeZone,
  today: LocalDate,
  callbacks: ScaffoldCallbacks,
  currentLanguage: AppLanguage,
  currentTheme: AppTheme,
  feedListState: LazyListState,
) {
  val appState = features.appState
  val selectedTab =
    when (appState.selectedScreen) {
      MemosScreen.HABITS -> appState.habitsTimeRangeTab
      MemosScreen.STATS -> appState.postsTimeRangeTab
      MemosScreen.FEED -> appState.habitsTimeRangeTab
    }
  val currentRange = currentRangeForTab(selectedTab, today)
  val activityRange = activityRangeForState(appState)
  val earliestDate = remember(memosState.memos) { EarliestMemoDateUseCase(memosState.memos, timeZone) }
  val minRange = minRangeForTab(selectedTab, earliestDate)

  Column(
    modifier = Modifier.padding(padding).padding(Indent.m).fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(Indent.s),
  ) {
    MemosHeader(
      memosState,
      appState,
      features.memos::send,
      features.settings::send,
      currentLanguage,
      currentTheme,
    )
    memosState.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }

    if (appState.selectedScreen != MemosScreen.FEED) {
      val successRate =
        rememberSuccessRateIfNeeded(
          appState,
          habitsTimeline,
          memosState.memos,
          activityRange,
          dependencies,
          features.cache,
        )
      TimeRangeControls(
        selectedTab = selectedTab,
        selectedRange = activityRange,
        currentRange = currentRange,
        minRange = minRange,
        successRate = successRate,
        onTabChange = callbacks.onTabChange,
        onRangeChange = callbacks.onRangeChange,
      )
    }

    SwipeableTabContent(
      memosState = memosState,
      appState = appState,
      currentRange = currentRange,
      minRange = minRange,
      habitsState = habitsState,
      onHabitsAction = features.habits::send,
      calculateSuccessRate = dependencies.calculateSuccessRate,
      buildActivityDataUseCase = dependencies.buildActivityData,
      cache = features.cache,
      dispatchMemos = features.memos::send,
      feedListState = feedListState,
    )
  }
}

@Composable
private fun rememberSuccessRateIfNeeded(
  appState: VibitsAppUiState,
  habitsTimeline: List<HabitsConfigEntry>,
  memos: List<Memo>,
  activityRange: ActivityRange,
  dependencies: AppDependencies,
  cache: space.be1ski.vibits.shared.feature.habits.view.components.ActivityWeekDataCache,
): Float? {
  val isHabitsScreen = appState.selectedScreen == MemosScreen.HABITS
  val hasHabits = remember(habitsTimeline) { habitsTimeline.lastOrNull()?.habits?.isNotEmpty() == true }
  val shouldCalculate = isHabitsScreen && hasHabits
  return if (shouldCalculate) {
    rememberSuccessRate(memos, activityRange, dependencies.calculateSuccessRate, dependencies.buildActivityData, cache)
  } else {
    null
  }
}
