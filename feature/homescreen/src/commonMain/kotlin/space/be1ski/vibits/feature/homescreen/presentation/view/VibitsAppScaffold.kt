package space.be1ski.vibits.feature.homescreen.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.ui.Modifier
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.core.platform.locale.AppLanguage
import space.be1ski.vibits.core.strings.generated.Res
import space.be1ski.vibits.core.strings.generated.action_create_memo
import space.be1ski.vibits.core.strings.generated.action_track_today
import space.be1ski.vibits.core.strings.generated.msg_fill_all_fields
import space.be1ski.vibits.core.ui.Indent
import space.be1ski.vibits.core.ui.date.DateFormatter
import space.be1ski.vibits.feature.habits.domain.model.ActivityRange
import space.be1ski.vibits.feature.habits.domain.model.HabitsConfigEntry
import space.be1ski.vibits.feature.habits.domain.usecase.EarliestMemoDateUseCase
import space.be1ski.vibits.feature.habits.domain.usecase.IsActivityRangeBeforeUseCase
import space.be1ski.vibits.feature.habits.presentation.state.HabitsState
import space.be1ski.vibits.feature.homescreen.domain.model.AppState
import space.be1ski.vibits.feature.homescreen.domain.model.Screen
import space.be1ski.vibits.feature.homescreen.presentation.AppFeatures
import space.be1ski.vibits.feature.memos.presentation.state.MemosState
import space.be1ski.vibits.feature.settings.domain.model.AppTheme

@Composable
internal fun VibitsAppScaffold(
  features: AppFeatures,
  appState: AppState,
  memosState: MemosState,
  habitsState: HabitsState,
  currentLanguage: AppLanguage,
  currentTheme: AppTheme,
  syncDebounceSeconds: Int,
) {
  val shell = rememberAppShellState(features, appState, memosState, habitsState)

  Scaffold(
    floatingActionButton = { AppFab(appState, shell.todayHabits, shell.callbacks) },
    bottomBar = {
      MemosBottomNavigation(appState, features.app::send, shell.callbacks.onClearSelection, shell.callbacks.onFeedScrollToTop)
    },
  ) { padding ->
    ScaffoldContent(
      padding = padding,
      features = features,
      appState = appState,
      memosState = memosState,
      habitsState = habitsState,
      habitsTimeline = shell.habitsTimeline,
      activityRange = shell.activityRange,
      timeZone = shell.timeZone,
      today = shell.today,
      callbacks = shell.callbacks,
      currentLanguage = currentLanguage,
      currentTheme = currentTheme,
      syncDebounceSeconds = syncDebounceSeconds,
      feedListState = shell.feedListState,
      dateFormatter = shell.dateFormatter,
    )
  }
}

@Composable
private fun AppFab(
  appState: AppState,
  todayHabits: TodayHabits,
  callbacks: ScaffoldCallbacks,
) {
  when (appState.selectedScreen) {
    Screen.HABITS -> {
      if (todayHabits.config.isNotEmpty() && todayHabits.day != null) {
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
  syncDebounceSeconds: Int,
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
      syncDebounceSeconds,
    )
    val errorText =
      memosState.errorMessage
        ?: if (memosState.credentialsMode && memosState.needsCredentials) stringResource(Res.string.msg_fill_all_fields) else null
    errorText?.let { Text(it, color = MaterialTheme.colorScheme.error) }

    if (appState.selectedScreen != Screen.FEED) {
      val successRate = rememberSuccessRateIfNeeded(appState, habitsTimeline, activityRange, habitsState)
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
      dateFormatter = dateFormatter,
      dispatchMemos = features.memos::send,
      feedListState = feedListState,
    )
  }
}
