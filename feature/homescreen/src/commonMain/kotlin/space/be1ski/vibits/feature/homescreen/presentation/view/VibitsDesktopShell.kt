package space.be1ski.vibits.feature.homescreen.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.core.platform.locale.AppLanguage
import space.be1ski.vibits.core.strings.generated.Res
import space.be1ski.vibits.core.strings.generated.msg_fill_all_fields
import space.be1ski.vibits.core.ui.Indent
import space.be1ski.vibits.feature.habits.domain.usecase.EarliestMemoDateUseCase
import space.be1ski.vibits.feature.habits.domain.usecase.IsActivityRangeBeforeUseCase
import space.be1ski.vibits.feature.habits.presentation.state.HabitsState
import space.be1ski.vibits.feature.homescreen.domain.model.AppState
import space.be1ski.vibits.feature.homescreen.domain.model.Screen
import space.be1ski.vibits.feature.homescreen.presentation.AppFeatures
import space.be1ski.vibits.feature.memos.presentation.action.MemosAction
import space.be1ski.vibits.feature.memos.presentation.state.MemosState
import space.be1ski.vibits.feature.memos.presentation.view.SyncConflictDialog
import space.be1ski.vibits.feature.memos.presentation.view.SyncLogDialog
import space.be1ski.vibits.feature.settings.domain.model.AppTheme
import space.be1ski.vibits.feature.settings.presentation.action.SettingsAction

@Composable
internal fun VibitsDesktopShell(
  features: AppFeatures,
  appState: AppState,
  memosState: MemosState,
  habitsState: HabitsState,
  currentLanguage: AppLanguage,
  currentTheme: AppTheme,
  syncDebounceSeconds: Int,
) {
  val shell = rememberAppShellState(features, appState, memosState, habitsState)

  Surface(modifier = Modifier.fillMaxSize()) {
    Row(modifier = Modifier.fillMaxSize()) {
      DesktopSidebar(
        appState = appState,
        todayHabits = shell.todayHabits,
        memosState = memosState,
        onAppAction = features.app::send,
        onClearSelection = shell.callbacks.onClearSelection,
        onFeedScrollToTop = shell.callbacks.onFeedScrollToTop,
        onOpenTodayEditor = shell.callbacks.onOpenTodayEditor,
        onShowCreateMemoDialog = shell.callbacks.onShowCreateMemoDialog,
        onSettingsClick = {
          features.settings.send(
            SettingsAction.Dialog.Open(
              baseUrl = memosState.baseUrl,
              token = memosState.token,
              appMode = appState.appMode,
              language = currentLanguage,
              theme = currentTheme,
              syncDebounceSeconds = syncDebounceSeconds,
            ),
          )
        },
        dispatchMemos = features.memos::send,
      )
      VerticalDivider()
      Column(modifier = Modifier.weight(1f).fillMaxSize()) {
        DesktopContent(
          features = features,
          appState = appState,
          memosState = memosState,
          habitsState = habitsState,
          shell = shell,
        )
      }
    }
  }

  SyncDialogs(memosState = memosState, dispatchMemos = features.memos::send)
}

@Composable
private fun SyncDialogs(
  memosState: MemosState,
  dispatchMemos: (MemosAction) -> Unit,
) {
  if (memosState.showSyncLogDialog) {
    SyncLogDialog(onDismiss = { dispatchMemos(MemosAction.Sync.DismissSyncLogDialog) })
  }
  if (memosState.showConflictDialog) {
    SyncConflictDialog(
      conflictCount = memosState.syncConflicts.size,
      onKeepLocal = { dispatchMemos(MemosAction.Sync.ResolveKeepLocal) },
      onKeepServer = { dispatchMemos(MemosAction.Sync.ResolveKeepServer) },
      onDismiss = { dispatchMemos(MemosAction.Sync.DismissConflictDialog) },
    )
  }
}

@Composable
private fun DesktopContent(
  features: AppFeatures,
  appState: AppState,
  memosState: MemosState,
  habitsState: HabitsState,
  shell: AppShellState,
) {
  val selectedTab = appState.currentTimeRangeTab
  val currentRange = currentRangeForTab(selectedTab, shell.today)
  val earliestDate = remember(memosState.memos) { EarliestMemoDateUseCase(memosState.memos, shell.timeZone) }
  val minRange = minRangeForTab(selectedTab, earliestDate)
  val canGoBack = minRange?.let { IsActivityRangeBeforeUseCase(it, shell.activityRange) } ?: true
  val canGoForward = IsActivityRangeBeforeUseCase(shell.activityRange, currentRange)

  Column(
    modifier = Modifier.padding(horizontal = Indent.m, vertical = Indent.xs).fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(Indent.xs),
  ) {
    val errorText =
      memosState.errorMessage
        ?: if (memosState.credentialsMode && memosState.needsCredentials) stringResource(Res.string.msg_fill_all_fields) else null
    errorText?.let { Text(it, color = MaterialTheme.colorScheme.error) }

    if (appState.selectedScreen != Screen.FEED) {
      val successRate = rememberSuccessRateIfNeeded(appState, shell.habitsTimeline, shell.activityRange, habitsState)
      TimeRangeControls(
        selectedTab = selectedTab,
        rangeLabel = formatRangeLabel(shell.activityRange, shell.dateFormatter),
        successRate = successRate,
        canGoBack = canGoBack,
        canGoForward = canGoForward,
        onTabChange = shell.callbacks.onTabChange,
        onNavigateBack = shell.callbacks.onNavigateBack,
        onNavigateForward = shell.callbacks.onNavigateForward,
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
      dateFormatter = shell.dateFormatter,
      dispatchMemos = features.memos::send,
      feedListState = shell.feedListState,
    )
  }
}
