package space.be1ski.vibits.feature.homescreen.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.core.platform.date.currentLocalDate
import space.be1ski.vibits.core.platform.isDesktop
import space.be1ski.vibits.core.platform.locale.AppLanguage
import space.be1ski.vibits.core.strings.generated.Res
import space.be1ski.vibits.core.strings.generated.action_refresh
import space.be1ski.vibits.core.strings.generated.app_name
import space.be1ski.vibits.core.strings.generated.nav_feed
import space.be1ski.vibits.core.strings.generated.nav_habits
import space.be1ski.vibits.core.strings.generated.nav_memos
import space.be1ski.vibits.core.strings.generated.nav_settings
import space.be1ski.vibits.core.ui.Indent
import space.be1ski.vibits.feature.homescreen.domain.model.AppState
import space.be1ski.vibits.feature.homescreen.domain.model.Screen
import space.be1ski.vibits.feature.homescreen.presentation.action.AppAction
import space.be1ski.vibits.feature.memos.presentation.action.MemosAction
import space.be1ski.vibits.feature.memos.presentation.state.MemosState
import space.be1ski.vibits.feature.memos.presentation.view.SyncConflictDialog
import space.be1ski.vibits.feature.memos.presentation.view.SyncDot
import space.be1ski.vibits.feature.memos.presentation.view.SyncLogDialog
import space.be1ski.vibits.feature.settings.domain.model.AppTheme
import space.be1ski.vibits.feature.settings.presentation.action.SettingsAction

@Composable
internal fun MemosHeader(
  memosState: MemosState,
  appState: AppState,
  dispatchMemos: (MemosAction) -> Unit,
  dispatchSettings: (SettingsAction) -> Unit,
  language: AppLanguage,
  theme: AppTheme,
  syncDebounceSeconds: Int,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    // Left side: App name + sync dot (on desktop)
    Row(
      horizontalArrangement = Arrangement.spacedBy(Indent.s),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(stringResource(Res.string.app_name), style = MaterialTheme.typography.headlineSmall)
      // Show sync dot on desktop (left side) only in online mode
      if (isDesktop && !memosState.isOfflineMode) {
        SyncDot(
          syncStatus = memosState.syncStatus,
          isSyncing = memosState.isSyncing,
          onClick = { dispatchMemos(MemosAction.Sync.ShowSyncLogDialog) },
        )
      }
    }
    // Right side: refresh button + sync dot (on mobile) + settings
    Row(horizontalArrangement = Arrangement.spacedBy(Indent.xs), verticalAlignment = Alignment.CenterVertically) {
      if (isDesktop) {
        IconButton(onClick = { dispatchMemos(MemosAction.Loading.LoadMemos) }) {
          Icon(imageVector = Icons.Filled.Refresh, contentDescription = stringResource(Res.string.action_refresh))
        }
      } else if (!memosState.isOfflineMode) {
        // Show sync dot on mobile (right side) only in online mode
        SyncDot(
          syncStatus = memosState.syncStatus,
          isSyncing = memosState.isSyncing,
          onClick = { dispatchMemos(MemosAction.Sync.ShowSyncLogDialog) },
        )
      }
      TextButton(
        onClick = {
          dispatchSettings(
            SettingsAction.Dialog.Open(
              baseUrl = memosState.baseUrl,
              token = memosState.token,
              appMode = appState.appMode,
              language = language,
              theme = theme,
              syncDebounceSeconds = syncDebounceSeconds,
            ),
          )
        },
      ) {
        Text(stringResource(Res.string.nav_settings))
      }
    }
  }

  MemosDialogs(memosState = memosState, dispatchMemos = dispatchMemos)
}

@Composable
private fun MemosDialogs(
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
internal fun MemosBottomNavigation(
  appState: AppState,
  onAppAction: (AppAction) -> Unit,
  onClearSelection: () -> Unit,
  onFeedScrollToTop: () -> Unit,
) {
  val onTabClick = { screen: Screen ->
    onClearSelection()
    if (appState.selectedScreen == screen) {
      if (screen == Screen.FEED) {
        onFeedScrollToTop()
      } else {
        onAppAction(AppAction.TimeRange.ResetToHome(currentLocalDate()))
      }
    } else {
      onAppAction(AppAction.Navigation.SelectScreen(screen))
    }
  }

  NavigationBar {
    NavigationBarItem(
      selected = appState.selectedScreen == Screen.HABITS,
      onClick = { onTabClick(Screen.HABITS) },
      icon = { Icon(Icons.Filled.CheckCircle, contentDescription = stringResource(Res.string.nav_habits)) },
      label = { Text(stringResource(Res.string.nav_habits)) },
      modifier = Modifier.testTag(AppShellTestTags.BOTTOM_NAV_HABITS),
    )
    NavigationBarItem(
      selected = appState.selectedScreen == Screen.STATS,
      onClick = { onTabClick(Screen.STATS) },
      icon = { Icon(Icons.AutoMirrored.Filled.StickyNote2, contentDescription = stringResource(Res.string.nav_memos)) },
      label = { Text(stringResource(Res.string.nav_memos)) },
      modifier = Modifier.testTag(AppShellTestTags.BOTTOM_NAV_STATS),
    )
    NavigationBarItem(
      selected = appState.selectedScreen == Screen.FEED,
      onClick = { onTabClick(Screen.FEED) },
      icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = stringResource(Res.string.nav_feed)) },
      label = { Text(stringResource(Res.string.nav_feed)) },
      modifier = Modifier.testTag(AppShellTestTags.BOTTOM_NAV_FEED),
    )
  }
}
