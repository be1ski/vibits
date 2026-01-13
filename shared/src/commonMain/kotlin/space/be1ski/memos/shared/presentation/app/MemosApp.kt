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
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.datetime.TimeZone
import org.koin.compose.koinInject
import space.be1ski.memos.shared.presentation.components.ActivityMode
import space.be1ski.memos.shared.presentation.components.Indent
import space.be1ski.memos.shared.presentation.components.availableYears
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

/** Root shared UI for the app. */
@Composable
fun MemosApp() {
  val viewModel: MemosViewModel = koinInject()
  val appState = remember { MemosAppUiState() }
  val uiState = viewModel.uiState
  val timeZone = remember { TimeZone.currentSystemDefault() }
  val currentYear = remember { currentLocalDate().year }
  val years = remember(uiState.memos) { availableYears(uiState.memos, timeZone, currentYear) }

  SyncAutoLoad(uiState, appState, viewModel)
  SyncCredentialsDialog(uiState, appState)

  MemosAppContent(uiState, appState, viewModel, years)
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
  viewModel: MemosViewModel,
  years: List<Int>
) {
  MaterialTheme {
    Scaffold(
      floatingActionButton = {
        if (memosFabModeForTab(appState.selectedTab) == MemosFabMode.Memo) {
          FloatingActionButton(
            onClick = { appState.showCreateMemoDialog = true }
          ) {
            Icon(
              imageVector = Icons.Filled.Edit,
              contentDescription = "Create memo"
            )
          }
        }
      }
    ) { padding ->
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
        MemosTabs(appState)
        MemosTabContent(uiState, appState, viewModel, years)
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
private fun MemosTabs(appState: MemosAppUiState) {
  PrimaryTabRow(selectedTabIndex = appState.selectedTab) {
    Tab(
      selected = appState.selectedTab == 0,
      onClick = { appState.selectedTab = 0 },
      text = { Text("Habits") }
    )
    Tab(
      selected = appState.selectedTab == 1,
      onClick = { appState.selectedTab = 1 },
      text = { Text("Stats") }
    )
    Tab(
      selected = appState.selectedTab == 2,
      onClick = { appState.selectedTab = 2 },
      text = { Text("Feed") }
    )
  }
}
@Composable
private fun MemosTabContent(
  uiState: MemosUiState,
  appState: MemosAppUiState,
  viewModel: MemosViewModel,
  years: List<Int>
) {
  val memos = uiState.memos
  when (appState.selectedTab) {
    0 -> StatsScreen(
      state = StatsScreenState(
        memos = memos,
        years = years,
        range = appState.activityRange,
        activityMode = ActivityMode.Habits,
        useVerticalScroll = true,
        enablePullRefresh = false
      ),
      actions = StatsScreenActions(
        onRangeChange = { appState.activityRange = it },
        onEditDailyMemo = { memo, content -> viewModel.updateMemo(memo.name, content) },
        onDeleteDailyMemo = { memo -> viewModel.deleteDailyMemo(memo.name) },
        onCreateDailyMemo = { content -> viewModel.createMemo(content) }
      )
    )
    1 -> PostsScreen(
      memos = memos,
      years = years,
      range = appState.activityRange,
      onRangeChange = { appState.activityRange = it }
    )
    2 -> FeedScreen(
      memos = memos,
      isRefreshing = uiState.isLoading,
      onRefresh = { viewModel.loadMemos() },
      enablePullRefresh = !isDesktop,
      onMemoClick = { memo -> beginEditMemo(appState, memo) }
    )
  }
}

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
