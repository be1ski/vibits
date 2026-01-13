package space.be1ski.memos.shared.presentation.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.koin.compose.koinInject
import space.be1ski.memos.shared.presentation.components.ActivityMode
import space.be1ski.memos.shared.presentation.components.ActivityRange
import space.be1ski.memos.shared.presentation.components.Indent
import space.be1ski.memos.shared.presentation.components.earliestMemoDate
import space.be1ski.memos.shared.domain.model.memo.Memo
import space.be1ski.memos.shared.presentation.screen.FeedScreen
import space.be1ski.memos.shared.presentation.screen.PostsScreen
import space.be1ski.memos.shared.presentation.screen.StatsScreen
import space.be1ski.memos.shared.presentation.screen.StatsScreenActions
import space.be1ski.memos.shared.presentation.screen.StatsScreenState
import space.be1ski.memos.shared.presentation.state.MemosUiState
import space.be1ski.memos.shared.presentation.time.currentLocalDate
import space.be1ski.memos.shared.presentation.util.isDesktop
import space.be1ski.memos.shared.presentation.viewmodel.MemosViewModel
import space.be1ski.memos.shared.presentation.components.startOfWeek

/** Root shared UI for the app. */
@Composable
fun MemosApp() {
  val viewModel: MemosViewModel = koinInject()
  val appState = remember { MemosAppUiState(currentLocalDate()) }
  val uiState = viewModel.uiState

  SyncAutoLoad(uiState, appState, viewModel)
  SyncCredentialsDialog(uiState, appState)

  MemosAppContent(uiState, appState, viewModel)
  CredentialsDialog(uiState, appState, viewModel)
  MemoCreateDialog(appState, viewModel)
  MemoEditDialog(appState, viewModel)
}
@Composable
private fun SyncAutoLoad(
  uiState: MemosUiState,
  appState: MemosAppUiState,
  viewModel: MemosViewModel
) {
  LaunchedEffect(uiState, appState.autoLoaded) {
    if (uiState is MemosUiState.Ready && !appState.autoLoaded && !uiState.isLoading) {
      appState.autoLoaded = true
      viewModel.loadMemos()
    }
  }
}
@Composable
private fun SyncCredentialsDialog(
  uiState: MemosUiState,
  appState: MemosAppUiState
) {
  LaunchedEffect(uiState, appState.showCredentialsDialog, appState.credentialsDismissed) {
    if (uiState is MemosUiState.CredentialsInput && !appState.showCredentialsDialog && !appState.credentialsDismissed) {
      appState.showCredentialsDialog = true
      appState.credentialsInitialized = false
    }
    if (uiState is MemosUiState.Ready) {
      appState.credentialsDismissed = false
    }
  }
}
@Composable
private fun MemosAppContent(
  uiState: MemosUiState,
  appState: MemosAppUiState,
  viewModel: MemosViewModel
) {
  MaterialTheme {
    Scaffold(
      floatingActionButton = {
        if (memosFabModeForScreen(appState.selectedScreen) == MemosFabMode.Memo) {
          FloatingActionButton(
            onClick = { appState.showCreateMemoDialog = true }
          ) {
            Icon(
              imageVector = Icons.Filled.Edit,
              contentDescription = "Create memo"
            )
          }
        }
      },
      bottomBar = {
        MemosBottomNavigation(appState)
      }
    ) { padding ->
      val currentRange = currentRangeForTab(appState.selectedTimeRangeTab, currentLocalDate())
      val activityRange = activityRangeForState(appState)
      val timeZone = remember { TimeZone.currentSystemDefault() }
      val earliestDate = remember(uiState.memos) { earliestMemoDate(uiState.memos, timeZone) }
      val minRange = minRangeForTab(appState.selectedTimeRangeTab, earliestDate)
      Column(
        modifier = Modifier
          .padding(padding)
          .padding(Indent.m)
          .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Indent.s)
      ) {
        MemosHeader(appState, viewModel)
        uiState.errorMessage?.let { message ->
          Text(message, color = MaterialTheme.colorScheme.error)
        }
        if (appState.selectedScreen != MemosScreen.Feed) {
          TimeRangeControls(
            selectedTab = appState.selectedTimeRangeTab,
            selectedRange = activityRange,
            currentRange = currentRange,
            minRange = minRange,
            onTabChange = { appState.selectedTimeRangeTab = it },
            onRangeChange = { range -> updateTimeRangeState(appState, range) }
          )
        }
        MemosTabContent(uiState, appState, viewModel, activityRange)
      }
    }
  }
}
@Composable
private fun MemosHeader(appState: MemosAppUiState, viewModel: MemosViewModel) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text("Memos", style = MaterialTheme.typography.headlineSmall)
    Row(horizontalArrangement = Arrangement.spacedBy(Indent.xs), verticalAlignment = Alignment.CenterVertically) {
      if (isDesktop) {
        IconButton(onClick = { viewModel.loadMemos() }) {
          Icon(imageVector = Icons.Filled.Refresh, contentDescription = "Refresh")
        }
      }
      TextButton(
        onClick = {
          viewModel.editCredentials()
          appState.showCredentialsDialog = true
          appState.credentialsInitialized = false
          appState.credentialsDismissed = false
        }
      ) {
        Text("Settings")
      }
    }
  }
}

@Composable
private fun MemosBottomNavigation(appState: MemosAppUiState) {
  NavigationBar {
    NavigationBarItem(
      selected = appState.selectedScreen == MemosScreen.Habits,
      onClick = { appState.selectedScreen = MemosScreen.Habits },
      icon = { Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = "Habits") },
      label = { Text("Habits") }
    )
    NavigationBarItem(
      selected = appState.selectedScreen == MemosScreen.Stats,
      onClick = { appState.selectedScreen = MemosScreen.Stats },
      icon = { Icon(imageVector = Icons.Filled.Description, contentDescription = "Posts") },
      label = { Text("Posts") }
    )
    NavigationBarItem(
      selected = appState.selectedScreen == MemosScreen.Feed,
      onClick = { appState.selectedScreen = MemosScreen.Feed },
      icon = { Icon(imageVector = Icons.AutoMirrored.Filled.List, contentDescription = "Feed") },
      label = { Text("Feed") }
    )
  }
}
@Composable
private fun MemosTabContent(
  uiState: MemosUiState,
  appState: MemosAppUiState,
  viewModel: MemosViewModel,
  activityRange: ActivityRange
) {
  val memos = uiState.memos
  when (appState.selectedScreen) {
    MemosScreen.Habits -> StatsScreen(
      state = StatsScreenState(
        memos = memos,
        range = activityRange,
        activityMode = ActivityMode.Habits,
        useVerticalScroll = true,
        enablePullRefresh = false,
        demoMode = appState.demoMode
      ),
      actions = StatsScreenActions(
        onEditDailyMemo = { memo, content -> viewModel.updateMemo(memo.name, content) },
        onDeleteDailyMemo = { memo -> viewModel.deleteDailyMemo(memo.name) },
        onCreateDailyMemo = { content -> viewModel.createMemo(content) }
      )
    )
    MemosScreen.Stats -> PostsScreen(
      memos = memos,
      range = activityRange,
      demoMode = appState.demoMode
    )
    MemosScreen.Feed -> FeedScreen(
      memos = memos,
      isRefreshing = uiState.isLoading,
      onRefresh = { viewModel.loadMemos() },
      enablePullRefresh = !isDesktop,
      demoMode = appState.demoMode,
      onMemoClick = { memo -> beginEditMemo(appState, memo) }
    )
  }
}

private fun activityRangeForState(appState: MemosAppUiState): ActivityRange {
  return when (appState.selectedTimeRangeTab) {
    TimeRangeTab.Weeks -> ActivityRange.Week(appState.weekStart)
    TimeRangeTab.Months -> ActivityRange.Month(appState.monthYear, appState.month)
    TimeRangeTab.Quarters -> ActivityRange.Quarter(appState.quarterYear, appState.quarterIndex)
    TimeRangeTab.Years -> ActivityRange.Year(appState.year)
  }
}

private fun updateTimeRangeState(appState: MemosAppUiState, range: ActivityRange) {
  when (range) {
    is ActivityRange.Week -> appState.weekStart = range.startDate
    is ActivityRange.Month -> {
      appState.monthYear = range.year
      appState.month = range.month
    }
    is ActivityRange.Quarter -> {
      appState.quarterYear = range.year
      appState.quarterIndex = range.index
    }
    is ActivityRange.Year -> appState.year = range.year
  }
}

private fun currentRangeForTab(tab: TimeRangeTab, today: LocalDate): ActivityRange {
  return when (tab) {
    TimeRangeTab.Weeks -> ActivityRange.Week(startOfWeek(today))
    TimeRangeTab.Months -> ActivityRange.Month(today.year, today.month)
    TimeRangeTab.Quarters -> ActivityRange.Quarter(today.year, currentQuarterIndex(today))
    TimeRangeTab.Years -> ActivityRange.Year(today.year)
  }
}

private fun minRangeForTab(tab: TimeRangeTab, earliestDate: LocalDate?): ActivityRange? {
  if (earliestDate == null) {
    return null
  }
  return when (tab) {
    TimeRangeTab.Weeks -> ActivityRange.Week(startOfWeek(earliestDate))
    TimeRangeTab.Months -> ActivityRange.Month(earliestDate.year, earliestDate.month)
    TimeRangeTab.Quarters -> ActivityRange.Quarter(earliestDate.year, currentQuarterIndex(earliestDate))
    TimeRangeTab.Years -> ActivityRange.Year(earliestDate.year)
  }
}

private fun currentQuarterIndex(date: LocalDate): Int {
  return date.month.ordinal / MONTHS_IN_QUARTER + FIRST_QUARTER_INDEX
}

private const val MONTHS_IN_QUARTER = 3
private const val FIRST_QUARTER_INDEX = 1

@Composable
private fun MemoCreateDialog(appState: MemosAppUiState, viewModel: MemosViewModel) {
  if (!appState.showCreateMemoDialog) {
    return
  }
  AlertDialog(
    onDismissRequest = {
      appState.showCreateMemoDialog = false
      appState.createMemoContent = ""
    },
    title = { Text("New memo") },
    text = {
      TextField(
        value = appState.createMemoContent,
        onValueChange = { appState.createMemoContent = it },
        placeholder = { Text("Write a memo...") },
        modifier = Modifier.fillMaxWidth()
      )
    },
    confirmButton = {
      val content = appState.createMemoContent.trim()
      Button(
        onClick = {
          viewModel.createMemo(content)
          appState.showCreateMemoDialog = false
          appState.createMemoContent = ""
        },
        enabled = content.isNotBlank()
      ) {
        Text("Create")
      }
    },
    dismissButton = {
      TextButton(
        onClick = {
          appState.showCreateMemoDialog = false
          appState.createMemoContent = ""
        }
      ) {
        Text("Cancel")
      }
    }
  )
}

@Composable
private fun MemoEditDialog(appState: MemosAppUiState, viewModel: MemosViewModel) {
  if (!appState.showEditMemoDialog) {
    return
  }
  val memo = appState.editMemoTarget ?: return
  AlertDialog(
    onDismissRequest = { clearMemoEdit(appState) },
    title = { Text("Edit memo") },
    text = {
      TextField(
        value = appState.editMemoContent,
        onValueChange = { appState.editMemoContent = it },
        modifier = Modifier.fillMaxWidth()
      )
    },
    confirmButton = {
      val content = appState.editMemoContent.trim()
      Button(
        onClick = {
          viewModel.updateMemo(memo.name, content)
          clearMemoEdit(appState)
        },
        enabled = content.isNotBlank()
      ) {
        Text("Save")
      }
    },
    dismissButton = {
      TextButton(onClick = { clearMemoEdit(appState) }) {
        Text("Cancel")
      }
    }
  )
}

private fun beginEditMemo(appState: MemosAppUiState, memo: Memo) {
  appState.editMemoTarget = memo
  appState.editMemoContent = memo.content
  appState.showEditMemoDialog = true
}

private fun clearMemoEdit(appState: MemosAppUiState) {
  appState.showEditMemoDialog = false
  appState.editMemoTarget = null
  appState.editMemoContent = ""
}
