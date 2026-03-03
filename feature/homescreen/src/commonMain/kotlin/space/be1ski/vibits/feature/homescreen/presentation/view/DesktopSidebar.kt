package space.be1ski.vibits.feature.homescreen.presentation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.core.platform.date.currentLocalDate
import space.be1ski.vibits.core.strings.generated.Res
import space.be1ski.vibits.core.strings.generated.action_refresh
import space.be1ski.vibits.core.strings.generated.action_track_today
import space.be1ski.vibits.core.strings.generated.app_name
import space.be1ski.vibits.core.strings.generated.format_habits_progress
import space.be1ski.vibits.core.strings.generated.label_habits_config
import space.be1ski.vibits.core.strings.generated.nav_feed
import space.be1ski.vibits.core.strings.generated.nav_habits
import space.be1ski.vibits.core.strings.generated.nav_memos
import space.be1ski.vibits.core.strings.generated.nav_settings
import space.be1ski.vibits.core.strings.generated.title_new_memo
import space.be1ski.vibits.core.ui.Indent
import space.be1ski.vibits.core.ui.platform.hoverAware
import space.be1ski.vibits.feature.homescreen.domain.model.AppState
import space.be1ski.vibits.feature.homescreen.domain.model.Screen
import space.be1ski.vibits.feature.homescreen.presentation.action.AppAction
import space.be1ski.vibits.feature.memos.presentation.action.MemosAction
import space.be1ski.vibits.feature.memos.presentation.state.MemosState
import space.be1ski.vibits.feature.memos.presentation.view.SyncDot

private val SIDEBAR_WIDTH = 256.dp

@Suppress("LongParameterList")
@Composable
internal fun DesktopSidebar(
  appState: AppState,
  todayHabits: TodayHabits,
  memosState: MemosState,
  onAppAction: (AppAction) -> Unit,
  onClearSelection: () -> Unit,
  onFeedScrollToTop: () -> Unit,
  onOpenTodayEditor: () -> Unit,
  onOpenConfigDialog: () -> Unit,
  onShowCreateMemoDialog: () -> Unit,
  onSettingsClick: () -> Unit,
  dispatchMemos: (MemosAction) -> Unit,
) {
  Surface(
    modifier = Modifier.width(SIDEBAR_WIDTH).fillMaxHeight(),
    tonalElevation = 1.dp,
  ) {
    Column(modifier = Modifier.padding(Indent.s)) {
      SidebarHeader(memosState, dispatchMemos)
      SidebarNavigation(appState, onAppAction, onClearSelection, onFeedScrollToTop)
      Spacer(modifier = Modifier.weight(1f))
      SidebarActions(appState.selectedScreen, todayHabits, onOpenTodayEditor, onOpenConfigDialog, onShowCreateMemoDialog, onSettingsClick)
    }
  }
}

@Composable
private fun SidebarNavigation(
  appState: AppState,
  onAppAction: (AppAction) -> Unit,
  onClearSelection: () -> Unit,
  onFeedScrollToTop: () -> Unit,
) {
  Column(verticalArrangement = Arrangement.spacedBy(Indent.x3s)) {
    SidebarNavItem(
      icon = Icons.Filled.CheckCircle,
      label = stringResource(Res.string.nav_habits),
      selected = appState.selectedScreen == Screen.HABITS,
      testTag = AppShellTestTags.SIDEBAR_NAV_HABITS,
      onClick = {
        onClearSelection()
        if (appState.selectedScreen == Screen.HABITS) {
          onAppAction(AppAction.TimeRange.ResetToHome(currentLocalDate()))
        } else {
          onAppAction(AppAction.Navigation.SelectScreen(Screen.HABITS))
        }
      },
    )
    SidebarNavItem(
      icon = Icons.AutoMirrored.Filled.StickyNote2,
      label = stringResource(Res.string.nav_memos),
      selected = appState.selectedScreen == Screen.STATS,
      testTag = AppShellTestTags.SIDEBAR_NAV_STATS,
      onClick = {
        onClearSelection()
        if (appState.selectedScreen == Screen.STATS) {
          onAppAction(AppAction.TimeRange.ResetToHome(currentLocalDate()))
        } else {
          onAppAction(AppAction.Navigation.SelectScreen(Screen.STATS))
        }
      },
    )
    SidebarNavItem(
      icon = Icons.AutoMirrored.Filled.List,
      label = stringResource(Res.string.nav_feed),
      selected = appState.selectedScreen == Screen.FEED,
      testTag = AppShellTestTags.SIDEBAR_NAV_FEED,
      onClick = {
        onClearSelection()
        if (appState.selectedScreen == Screen.FEED) {
          onFeedScrollToTop()
        } else {
          onAppAction(AppAction.Navigation.SelectScreen(Screen.FEED))
        }
      },
    )
  }
}

@Composable
private fun SidebarActions(
  selectedScreen: Screen,
  todayHabits: TodayHabits,
  onOpenTodayEditor: () -> Unit,
  onOpenConfigDialog: () -> Unit,
  onShowCreateMemoDialog: () -> Unit,
  onSettingsClick: () -> Unit,
) {
  Column(verticalArrangement = Arrangement.spacedBy(Indent.x3s)) {
    if (todayHabits.config.isNotEmpty() && todayHabits.day != null) {
      val done = todayHabits.day.habitStatuses.count { it.done }
      val total = todayHabits.day.habitStatuses.size
      SidebarNavItem(
        icon = Icons.Filled.AddTask,
        label = stringResource(Res.string.action_track_today),
        subtitle = if (total > 0) stringResource(Res.string.format_habits_progress, done, total) else null,
        selected = false,
        accent = true,
        testTag = AppShellTestTags.SIDEBAR_TRACK_TODAY,
        onClick = onOpenTodayEditor,
        trailingIcon = {
          IconButton(
            onClick = onOpenConfigDialog,
            modifier = Modifier.size(32.dp),
          ) {
            Icon(
              imageVector = Icons.Filled.Tune,
              contentDescription = stringResource(Res.string.label_habits_config),
              modifier = Modifier.size(18.dp),
              tint = MaterialTheme.colorScheme.primary,
            )
          }
        },
      )
    }
    SidebarNavItem(
      icon = Icons.Filled.Edit,
      label = stringResource(Res.string.title_new_memo),
      selected = false,
      testTag = AppShellTestTags.SIDEBAR_NEW_MEMO,
      onClick = onShowCreateMemoDialog,
    )
    SidebarNavItem(
      icon = Icons.Filled.Settings,
      label = stringResource(Res.string.nav_settings),
      selected = selectedScreen == Screen.SETTINGS,
      testTag = AppShellTestTags.SIDEBAR_SETTINGS,
      onClick = onSettingsClick,
    )
  }
}

@Composable
private fun SidebarHeader(
  memosState: MemosState,
  dispatchMemos: (MemosAction) -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(start = Indent.xs, bottom = Indent.l),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = stringResource(Res.string.app_name),
      style = MaterialTheme.typography.titleLarge,
    )
    Spacer(modifier = Modifier.weight(1f))
    if (!memosState.isOfflineMode) {
      SyncDot(
        syncStatus = memosState.syncStatus,
        isSyncing = memosState.isSyncing,
        onClick = { dispatchMemos(MemosAction.Sync.ShowSyncLogDialog) },
      )
    }
    IconButton(onClick = { dispatchMemos(MemosAction.Loading.LoadMemos) }) {
      Icon(
        imageVector = Icons.Filled.Refresh,
        contentDescription = stringResource(Res.string.action_refresh),
        modifier = Modifier.size(20.dp),
      )
    }
  }
}

@Composable
private fun SidebarNavItem(
  icon: ImageVector,
  label: String,
  selected: Boolean,
  testTag: String,
  onClick: () -> Unit,
  accent: Boolean = false,
  subtitle: String? = null,
  trailingIcon: @Composable (() -> Unit)? = null,
) {
  var hovered by remember { mutableStateOf(false) }
  val shape = RoundedCornerShape(Indent.xs)
  val primaryColor = MaterialTheme.colorScheme.primary
  val backgroundColor =
    when {
      accent -> primaryColor.copy(alpha = 0.12f)
      selected -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
      hovered -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
      else -> Color.Transparent
    }
  val contentColor =
    when {
      accent -> primaryColor
      else -> MaterialTheme.colorScheme.onSurface
    }

  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .clip(shape)
        .background(backgroundColor, shape)
        .hoverAware { hovered = it }
        .clickable(onClick = onClick)
        .testTag(testTag)
        .padding(horizontal = Indent.s, vertical = Indent.xs),
    horizontalArrangement = Arrangement.spacedBy(Indent.s),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = contentColor)
    SidebarNavItemLabel(label, subtitle, accent, selected, contentColor)
    if (trailingIcon != null) {
      trailingIcon()
    }
  }
}

@Composable
private fun RowScope.SidebarNavItemLabel(
  label: String,
  subtitle: String?,
  accent: Boolean,
  selected: Boolean,
  contentColor: Color,
) {
  Column(modifier = Modifier.weight(1f)) {
    Text(
      label,
      style = if (accent) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
      fontWeight =
        when {
          selected -> FontWeight.SemiBold
          accent -> FontWeight.Medium
          else -> FontWeight.Normal
        },
      color = contentColor,
      maxLines = if (accent) 2 else 1,
      overflow = if (accent) TextOverflow.Clip else TextOverflow.Ellipsis,
    )
    if (subtitle != null) {
      Text(
        subtitle,
        style = MaterialTheme.typography.labelSmall,
        color = contentColor.copy(alpha = 0.7f),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}
