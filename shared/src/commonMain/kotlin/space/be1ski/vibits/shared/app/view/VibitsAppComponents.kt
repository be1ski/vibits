package space.be1ski.vibits.shared.app.view

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
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.shared.app.domain.model.AppState
import space.be1ski.vibits.shared.app.domain.model.Screen
import space.be1ski.vibits.shared.app.presentation.action.AppAction
import space.be1ski.vibits.shared.core.platform.date.currentLocalDate
import space.be1ski.vibits.shared.core.platform.isDesktop
import space.be1ski.vibits.shared.core.ui.Indent
import space.be1ski.vibits.shared.feature.memos.presentation.action.MemosAction
import space.be1ski.vibits.shared.feature.memos.presentation.state.MemosState
import space.be1ski.vibits.shared.feature.settings.domain.model.AppLanguage
import space.be1ski.vibits.shared.feature.settings.domain.model.AppTheme
import space.be1ski.vibits.shared.feature.settings.presentation.action.SettingsAction
import space.be1ski.vibits.shared.generated.Res
import space.be1ski.vibits.shared.generated.action_refresh
import space.be1ski.vibits.shared.generated.app_name
import space.be1ski.vibits.shared.generated.nav_feed
import space.be1ski.vibits.shared.generated.nav_habits
import space.be1ski.vibits.shared.generated.nav_memos
import space.be1ski.vibits.shared.generated.nav_settings

@Composable
internal fun MemosHeader(
  memosState: MemosState,
  appState: AppState,
  dispatchMemos: (MemosAction) -> Unit,
  dispatchSettings: (SettingsAction) -> Unit,
  language: AppLanguage,
  theme: AppTheme,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(stringResource(Res.string.app_name), style = MaterialTheme.typography.headlineSmall)
    Row(horizontalArrangement = Arrangement.spacedBy(Indent.xs), verticalAlignment = Alignment.CenterVertically) {
      if (isDesktop) {
        IconButton(onClick = { dispatchMemos(MemosAction.Loading.LoadMemos) }) {
          Icon(imageVector = Icons.Filled.Refresh, contentDescription = stringResource(Res.string.action_refresh))
        }
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
            ),
          )
        },
      ) {
        Text(stringResource(Res.string.nav_settings))
      }
    }
  }
}

@Composable
internal fun MemosBottomNavigation(
  appState: AppState,
  onAppAction: (AppAction) -> Unit,
  onClearSelection: () -> Unit,
  onFeedScrollToTop: () -> Unit,
) {
  NavigationBar {
    NavigationBarItem(
      selected = appState.selectedScreen == Screen.HABITS,
      onClick = {
        onClearSelection()
        if (appState.selectedScreen == Screen.HABITS) {
          onAppAction(AppAction.ResetToHome(currentLocalDate()))
        } else {
          onAppAction(AppAction.SelectScreen(Screen.HABITS))
        }
      },
      icon = {
        Icon(
          imageVector = Icons.Filled.CheckCircle,
          contentDescription = stringResource(Res.string.nav_habits),
        )
      },
      label = { Text(stringResource(Res.string.nav_habits)) },
    )
    NavigationBarItem(
      selected = appState.selectedScreen == Screen.STATS,
      onClick = {
        onClearSelection()
        if (appState.selectedScreen == Screen.STATS) {
          onAppAction(AppAction.ResetToHome(currentLocalDate()))
        } else {
          onAppAction(AppAction.SelectScreen(Screen.STATS))
        }
      },
      icon = {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.StickyNote2,
          contentDescription = stringResource(Res.string.nav_memos),
        )
      },
      label = { Text(stringResource(Res.string.nav_memos)) },
    )
    NavigationBarItem(
      selected = appState.selectedScreen == Screen.FEED,
      onClick = {
        onClearSelection()
        if (appState.selectedScreen == Screen.FEED) {
          onFeedScrollToTop()
        } else {
          onAppAction(AppAction.SelectScreen(Screen.FEED))
        }
      },
      icon = {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.List,
          contentDescription = stringResource(Res.string.nav_feed),
        )
      },
      label = { Text(stringResource(Res.string.nav_feed)) },
    )
  }
}
