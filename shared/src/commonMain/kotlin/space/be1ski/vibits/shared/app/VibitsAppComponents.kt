package space.be1ski.vibits.shared.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
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
import space.be1ski.vibits.shared.nav_feed
import space.be1ski.vibits.shared.nav_habits
import space.be1ski.vibits.shared.nav_posts
import space.be1ski.vibits.shared.nav_settings

@Composable
internal fun MemosHeader(appState: VibitsAppUiState, dispatch: (MemosAction) -> Unit) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(stringResource(Res.string.app_name), style = MaterialTheme.typography.headlineSmall)
    Row(horizontalArrangement = Arrangement.spacedBy(Indent.xs), verticalAlignment = Alignment.CenterVertically) {
      if (isDesktop) {
        IconButton(onClick = { dispatch(MemosAction.LoadMemos) }) {
          Icon(imageVector = Icons.Filled.Refresh, contentDescription = stringResource(Res.string.action_refresh))
        }
      }
      TextButton(
        onClick = {
          dispatch(MemosAction.EditCredentials)
          appState.showCredentialsDialog = true
          appState.credentialsInitialized = false
          appState.credentialsDismissed = false
        }
      ) {
        Text(stringResource(Res.string.nav_settings))
      }
    }
  }
}

@Composable
internal fun MemosBottomNavigation(
  appState: VibitsAppUiState,
  onClearSelection: () -> Unit
) {
  NavigationBar {
    NavigationBarItem(
      selected = appState.selectedScreen == MemosScreen.Habits,
      onClick = {
        onClearSelection()
        if (appState.selectedScreen == MemosScreen.Habits) {
          resetToHome(appState, currentLocalDate())
        } else {
          appState.selectedScreen = MemosScreen.Habits
        }
      },
      icon = {
        Icon(
          imageVector = Icons.Filled.CheckCircle,
          contentDescription = stringResource(Res.string.nav_habits)
        )
      },
      label = { Text(stringResource(Res.string.nav_habits)) }
    )
    NavigationBarItem(
      selected = appState.selectedScreen == MemosScreen.Stats,
      onClick = {
        onClearSelection()
        if (appState.selectedScreen == MemosScreen.Stats) {
          resetToHome(appState, currentLocalDate())
        } else {
          appState.selectedScreen = MemosScreen.Stats
        }
      },
      icon = {
        Icon(
          imageVector = Icons.Filled.Description,
          contentDescription = stringResource(Res.string.nav_posts)
        )
      },
      label = { Text(stringResource(Res.string.nav_posts)) }
    )
    NavigationBarItem(
      selected = appState.selectedScreen == MemosScreen.Feed,
      onClick = {
        onClearSelection()
        appState.selectedScreen = MemosScreen.Feed
      },
      icon = {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.List,
          contentDescription = stringResource(Res.string.nav_feed)
        )
      },
      label = { Text(stringResource(Res.string.nav_feed)) }
    )
  }
}

@Composable
internal fun rememberSuccessRate(
  memos: List<Memo>,
  activityRange: ActivityRange,
  calculateSuccessRate: CalculateSuccessRateUseCase
): Float? {
  val weekDataState = rememberActivityWeekData(memos, activityRange, ActivityMode.Habits)
  val weekData = weekDataState.data
  val habitsTimeline = rememberHabitsConfigTimeline(memos)
  val today = remember { currentLocalDate() }
  val configStartDate = remember(habitsTimeline) { habitsTimeline.firstOrNull()?.date }
  val data = remember(weekData, activityRange, today, configStartDate) {
    calculateSuccessRate(weekData, activityRange, today, configStartDate)
  }
  return if (data.total > 0) data.rate else null
}
