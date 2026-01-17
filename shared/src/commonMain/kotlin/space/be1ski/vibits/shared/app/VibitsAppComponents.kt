package space.be1ski.vibits.shared.app

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.shared.Res
import space.be1ski.vibits.shared.action_refresh
import space.be1ski.vibits.shared.app_name
import space.be1ski.vibits.shared.core.platform.currentLocalDate
import space.be1ski.vibits.shared.core.platform.isDesktop
import space.be1ski.vibits.shared.core.ui.ActivityMode
import space.be1ski.vibits.shared.core.ui.ActivityRange
import space.be1ski.vibits.shared.core.ui.Indent
import space.be1ski.vibits.shared.feature.habits.domain.usecase.CalculateSuccessRateUseCase
import space.be1ski.vibits.shared.feature.habits.presentation.components.rememberActivityWeekData
import space.be1ski.vibits.shared.feature.habits.presentation.components.rememberHabitsConfigTimeline
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.memos.presentation.MemosAction
import space.be1ski.vibits.shared.feature.memos.presentation.MemosState
import space.be1ski.vibits.shared.feature.preferences.domain.model.AppLanguage
import space.be1ski.vibits.shared.feature.preferences.domain.model.AppTheme
import space.be1ski.vibits.shared.feature.settings.presentation.SettingsAction
import space.be1ski.vibits.shared.nav_feed
import space.be1ski.vibits.shared.nav_habits
import space.be1ski.vibits.shared.nav_memos
import space.be1ski.vibits.shared.nav_settings

@Suppress("LongParameterList")
@Composable
internal fun MemosHeader(
  memosState: MemosState,
  appState: VibitsAppUiState,
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
        IconButton(onClick = { dispatchMemos(MemosAction.LoadMemos) }) {
          Icon(imageVector = Icons.Filled.Refresh, contentDescription = stringResource(Res.string.action_refresh))
        }
      }
      TextButton(
        onClick = {
          dispatchSettings(
            SettingsAction.Open(
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
  appState: VibitsAppUiState,
  onClearSelection: () -> Unit,
) {
  NavigationBar {
    NavigationBarItem(
      selected = appState.selectedScreen == MemosScreen.HABITS,
      onClick = {
        onClearSelection()
        if (appState.selectedScreen == MemosScreen.HABITS) {
          resetToHome(appState, currentLocalDate())
        } else {
          appState.selectedScreen = MemosScreen.HABITS
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
      selected = appState.selectedScreen == MemosScreen.STATS,
      onClick = {
        onClearSelection()
        if (appState.selectedScreen == MemosScreen.STATS) {
          resetToHome(appState, currentLocalDate())
        } else {
          appState.selectedScreen = MemosScreen.STATS
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
      selected = appState.selectedScreen == MemosScreen.FEED,
      onClick = {
        onClearSelection()
        appState.selectedScreen = MemosScreen.FEED
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

@Composable
internal fun rememberSuccessRate(
  memos: List<Memo>,
  activityRange: ActivityRange,
  calculateSuccessRate: CalculateSuccessRateUseCase,
): Float? {
  val weekDataState = rememberActivityWeekData(memos, activityRange, ActivityMode.HABITS)
  val weekData = weekDataState.data
  val habitsTimeline = rememberHabitsConfigTimeline(memos)
  val today = remember { currentLocalDate() }
  val configStartDate = remember(habitsTimeline) { habitsTimeline.firstOrNull()?.date }
  val data =
    remember(weekData, activityRange, today, configStartDate) {
      calculateSuccessRate(weekData, activityRange, today, configStartDate)
    }
  return if (data.total > 0) data.rate else null
}
