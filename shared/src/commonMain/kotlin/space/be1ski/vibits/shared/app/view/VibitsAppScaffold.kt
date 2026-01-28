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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.shared.app.di.AppDependencies
import space.be1ski.vibits.shared.app.domain.model.ActivityRange
import space.be1ski.vibits.shared.app.domain.model.AppState
import space.be1ski.vibits.shared.app.domain.model.Screen
import space.be1ski.vibits.shared.app.presentation.AppFeatures
import space.be1ski.vibits.shared.core.platform.date.currentLocalDate
import space.be1ski.vibits.shared.core.ui.Indent
import space.be1ski.vibits.shared.core.ui.date.DateFormatter
import space.be1ski.vibits.shared.core.ui.date.rememberDateFormatter
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitsConfigEntry
import space.be1ski.vibits.shared.feature.habits.domain.usecase.EarliestMemoDateUseCase
import space.be1ski.vibits.shared.feature.habits.domain.usecase.IsActivityRangeBeforeUseCase
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsAction
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsState
import space.be1ski.vibits.shared.feature.habits.view.components.rememberHabitsConfigTimeline
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.memos.presentation.MemosState
import space.be1ski.vibits.shared.feature.settings.domain.model.AppLanguage
import space.be1ski.vibits.shared.feature.settings.domain.model.AppTheme
import space.be1ski.vibits.shared.generated.Res
import space.be1ski.vibits.shared.generated.action_create_memo
import space.be1ski.vibits.shared.generated.action_track_today

@Suppress("LongMethod")
@Composable
internal fun VibitsAppScaffold(
  features: AppFeatures,
  dependencies: AppDependencies,
  appState: AppState,
  memosState: MemosState,
  habitsState: HabitsState,
  currentLanguage: AppLanguage,
  currentTheme: AppTheme,
) {
  val timeZone = remember { TimeZone.currentSystemDefault() }
  val today = currentLocalDate()
  val dateFormatter = rememberDateFormatter()
  val habitsTimeline = rememberHabitsConfigTimeline(memosState.memos)
  val todayData = rememberTodayData(habitsTimeline, memosState.memos, timeZone, today)

  val activityRange = activityRangeForAppState(appState)
  val feedListState = rememberLazyListState()
  val scope = rememberCoroutineScope()
  val callbacks =
    rememberScaffoldCallbacks(
      appState = appState,
      activityRange = activityRange,
      onAppAction = features.app::send,
      onHabitsAction = features.habits::send,
      onMemosAction = features.memos::send,
      feedListState = feedListState,
      scope = scope,
      todayData = todayData,
    )

  // Prewarm trigger: single source of truth for cache warming
  var prevAppMode by remember { mutableStateOf<space.be1ski.vibits.shared.feature.mode.domain.model.AppMode?>(null) }
  LaunchedEffect(appState.appMode) {
    if (prevAppMode != null && prevAppMode != appState.appMode) {
      features.habits.send(HabitsAction.InvalidateAllCache)
    }
    prevAppMode = appState.appMode
  }

  LaunchedEffect(
    appState.appMode,
    memosState.memosRevision,
    habitsState.needsCacheRefresh,
    habitsState.isInitialLoading,
  ) {
    val shouldPrewarm =
      !habitsState.isInitialLoading &&
        (habitsState.needsCacheRefresh || habitsState.activityDataCache.isEmpty())
    if (memosState.memos.isNotEmpty() && shouldPrewarm) {
      features.habits.send(
        HabitsAction.RequestPrewarmAllRanges(
          memos = memosState.memos,
          appMode = appState.appMode,
        ),
      )
    }
  }

  Scaffold(
    floatingActionButton = { AppFab(appState, todayData, callbacks) },
    bottomBar = { MemosBottomNavigation(appState, features.app::send, callbacks.onClearSelection, callbacks.onFeedScrollToTop) },
  ) { padding ->
    ScaffoldContent(
      padding = padding,
      features = features,
      dependencies = dependencies,
      appState = appState,
      memosState = memosState,
      habitsState = habitsState,
      habitsTimeline = habitsTimeline,
      activityRange = activityRange,
      timeZone = timeZone,
      today = today,
      callbacks = callbacks,
      currentLanguage = currentLanguage,
      currentTheme = currentTheme,
      feedListState = feedListState,
      dateFormatter = dateFormatter,
    )
  }
}

@Composable
private fun AppFab(
  appState: AppState,
  todayData: TodayData,
  callbacks: ScaffoldCallbacks,
) {
  when (appState.selectedScreen) {
    Screen.HABITS -> {
      if (todayData.config.isNotEmpty() && todayData.day != null) {
        FloatingActionButton(onClick = callbacks.onOpenTodayEditor) {
          Icon(Icons.Filled.AddTask, contentDescription = stringResource(Res.string.action_track_today))
        }
      }
    }
    else -> {
      FloatingActionButton(onClick = callbacks.onShowCreateMemoDialog) {
        Icon(Icons.Filled.Edit, contentDescription = stringResource(Res.string.action_create_memo))
      }
    }
  }
}

@Composable
private fun ScaffoldContent(
  padding: PaddingValues,
  features: AppFeatures,
  dependencies: AppDependencies,
  appState: AppState,
  memosState: MemosState,
  habitsState: HabitsState,
  habitsTimeline: List<HabitsConfigEntry>,
  activityRange: ActivityRange,
  timeZone: TimeZone,
  today: LocalDate,
  callbacks: ScaffoldCallbacks,
  currentLanguage: AppLanguage,
  currentTheme: AppTheme,
  feedListState: LazyListState,
  dateFormatter: DateFormatter,
) {
  val selectedTab = appState.currentTimeRangeTab
  val currentRange = currentRangeForTab(selectedTab, today)
  val earliestDate = remember(memosState.memos) { EarliestMemoDateUseCase(memosState.memos, timeZone) }
  val minRange = minRangeForTab(selectedTab, earliestDate)
  val canGoBack = minRange?.let { IsActivityRangeBeforeUseCase(it, activityRange) } ?: true
  val canGoForward = IsActivityRangeBeforeUseCase(activityRange, currentRange)

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

    if (appState.selectedScreen != Screen.FEED) {
      val successRate = rememberSuccessRateIfNeeded(appState, habitsTimeline, memosState.memos, activityRange, dependencies)
      TimeRangeControls(
        selectedTab = selectedTab,
        rangeLabel = formatRangeLabel(activityRange, dateFormatter),
        successRate = successRate,
        canGoBack = canGoBack,
        canGoForward = canGoForward,
        onTabChange = callbacks.onTabChange,
        onNavigateBack = callbacks.onNavigateBack,
        onNavigateForward = callbacks.onNavigateForward,
      )
    }

    SwipeableTabContent(
      memosState = memosState,
      appState = appState,
      currentRange = currentRange,
      minRange = minRange,
      habitsState = habitsState,
      onHabitsAction = features.habits::send,
      onAppAction = features.app::send,
      calculateSuccessRate = dependencies.calculateSuccessRate,
      buildActivityDataUseCase = dependencies.buildActivityData,
      cache = dependencies.activityWeekDataCache,
      dateFormatter = dateFormatter,
      dispatchMemos = features.memos::send,
      feedListState = feedListState,
    )
  }
}

@Composable
private fun rememberSuccessRateIfNeeded(
  appState: AppState,
  habitsTimeline: List<HabitsConfigEntry>,
  memos: List<Memo>,
  activityRange: ActivityRange,
  dependencies: AppDependencies,
): Float? {
  val isHabitsScreen = appState.selectedScreen == Screen.HABITS
  val hasHabits = remember(habitsTimeline) { habitsTimeline.lastOrNull()?.habits?.isNotEmpty() == true }
  val shouldCalculate = isHabitsScreen && hasHabits
  return if (shouldCalculate) {
    rememberSuccessRate(
      memos,
      activityRange,
      dependencies.calculateSuccessRate,
      dependencies.buildActivityData,
      dependencies.activityWeekDataCache,
    )
  } else {
    null
  }
}
