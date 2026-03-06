package space.be1ski.vibits.feature.homescreen.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.core.platform.locale.AppLanguage
import space.be1ski.vibits.core.strings.generated.Res
import space.be1ski.vibits.core.strings.generated.msg_fill_all_fields
import space.be1ski.vibits.core.ui.Indent
import space.be1ski.vibits.core.utils.logging.LogEntry
import space.be1ski.vibits.feature.changelog.domain.model.UpdateAvailability
import space.be1ski.vibits.feature.habits.domain.usecase.EarliestMemoDateUseCase
import space.be1ski.vibits.feature.habits.domain.usecase.IsActivityRangeBeforeUseCase
import space.be1ski.vibits.feature.habits.presentation.state.HabitsState
import space.be1ski.vibits.feature.homescreen.domain.model.AppState
import space.be1ski.vibits.feature.homescreen.domain.model.Screen
import space.be1ski.vibits.feature.homescreen.presentation.AppFeatures
import space.be1ski.vibits.feature.homescreen.presentation.action.AppAction
import space.be1ski.vibits.feature.memos.domain.repository.ExportService
import space.be1ski.vibits.feature.memos.presentation.action.MemosAction
import space.be1ski.vibits.feature.memos.presentation.state.MemosState
import space.be1ski.vibits.feature.memos.presentation.view.SyncConflictDialog
import space.be1ski.vibits.feature.memos.presentation.view.SyncLogDialog
import space.be1ski.vibits.feature.settings.domain.model.AppTheme
import space.be1ski.vibits.feature.settings.presentation.action.SettingsAction
import space.be1ski.vibits.feature.settings.presentation.state.SettingsState
import space.be1ski.vibits.feature.settings.presentation.view.SettingsPage

private val DESKTOP_CONTENT_MAX_WIDTH = 900.dp

@Suppress("LongMethod")
@Composable
internal fun VibitsDesktopShell(
  features: AppFeatures,
  appState: AppState,
  memosState: MemosState,
  habitsState: HabitsState,
  settingsState: SettingsState,
  currentLanguage: AppLanguage,
  currentTheme: AppTheme,
  syncDebounceSeconds: Int,
  exportService: ExportService,
  testLogs: List<LogEntry>? = null,
  updateAvailability: UpdateAvailability? = null,
  upgradeState: UpgradeState = UpgradeState.IDLE,
  onUpgrade: () -> Unit = {},
  onRestart: () -> Unit = {},
) {
  val shell = rememberAppShellState(features, appState, memosState, habitsState)

  // Sync settings screen ↔ dialog state
  LaunchedEffect(settingsState.isOpen, appState.selectedScreen) {
    if (!settingsState.isOpen && appState.selectedScreen == Screen.SETTINGS) {
      features.app.send(AppAction.Navigation.SelectScreen(Screen.HABITS))
    }
    if (settingsState.isOpen && appState.selectedScreen != Screen.SETTINGS) {
      features.settings.send(SettingsAction.Dialog.Dismiss)
    }
  }

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
        onOpenConfigDialog = {
          features.habits.send(
            space.be1ski.vibits.feature.habits.presentation.action.HabitsAction.Config.OpenConfigDialog(
              shell.todayHabits.config,
            ),
          )
        },
        onShowCreateMemoDialog = shell.callbacks.onShowCreateMemoDialog,
        onSettingsClick = {
          features.app.send(AppAction.Navigation.SelectScreen(Screen.SETTINGS))
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
        updateAvailability = updateAvailability,
        upgradeState = upgradeState,
        onUpgrade = onUpgrade,
        onRestart = onRestart,
      )
      VerticalDivider()
      Box(
        modifier = Modifier.weight(1f).fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
      ) {
        DesktopContent(
          modifier = Modifier.widthIn(max = DESKTOP_CONTENT_MAX_WIDTH),
          features = features,
          appState = appState,
          memosState = memosState,
          habitsState = habitsState,
          settingsState = settingsState,
          exportService = exportService,
          testLogs = testLogs,
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

@Suppress("LongMethod", "LongParameterList")
@Composable
private fun DesktopContent(
  modifier: Modifier = Modifier,
  features: AppFeatures,
  appState: AppState,
  memosState: MemosState,
  habitsState: HabitsState,
  settingsState: SettingsState,
  exportService: ExportService,
  testLogs: List<LogEntry>?,
  shell: AppShellState,
) {
  if (appState.selectedScreen == Screen.SETTINGS) {
    SettingsPage(
      modifier = modifier.fillMaxSize(),
      state = settingsState,
      dispatch = features.settings::send,
      exportService = exportService,
      testLogs = testLogs,
    )
    return
  }

  val selectedTab = appState.currentTimeRangeTab
  val currentRange = currentRangeForTab(selectedTab, shell.today)
  val earliestDate = remember(memosState.memos) { EarliestMemoDateUseCase(memosState.memos, shell.timeZone) }
  val minRange = minRangeForTab(selectedTab, earliestDate)
  val canGoBack = minRange?.let { IsActivityRangeBeforeUseCase(it, shell.activityRange) } ?: true
  val canGoForward = IsActivityRangeBeforeUseCase(shell.activityRange, currentRange)

  Column(
    modifier = modifier.fillMaxSize().padding(horizontal = Indent.m, vertical = Indent.xs),
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
