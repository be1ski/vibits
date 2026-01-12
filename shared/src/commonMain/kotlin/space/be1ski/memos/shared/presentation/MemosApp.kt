package space.be1ski.memos.shared.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import org.koin.compose.koinInject
import space.be1ski.memos.shared.presentation.components.ActivityMode
import space.be1ski.memos.shared.presentation.components.ActivityRange
import space.be1ski.memos.shared.presentation.components.availableYears
import space.be1ski.memos.shared.presentation.state.MemosUiState
import space.be1ski.memos.shared.presentation.time.currentLocalDate

/** Root shared UI for the app. */
@Composable
fun MemosApp() {
  val viewModel: MemosViewModel = koinInject()
  val uiState = viewModel.uiState
  val timeZone = remember { TimeZone.currentSystemDefault() }
  val currentYear = remember { currentLocalDate().year }
  val years = remember(uiState.memos) { availableYears(uiState.memos, timeZone, currentYear) }
  var selectedTab by remember { mutableStateOf(0) }
  var activityRange by remember { mutableStateOf<ActivityRange>(ActivityRange.Last90Days) }
  val memos = uiState.memos
  val isLoading = uiState.isLoading
  val errorMessage = uiState.errorMessage
  var autoLoaded by remember { mutableStateOf(false) }
  var showCredentialsDialog by remember { mutableStateOf(false) }
  var credentialsInitialized by remember { mutableStateOf(false) }
  var credentialsDismissed by remember { mutableStateOf(false) }
  var editBaseUrl by remember { mutableStateOf("") }
  var editToken by remember { mutableStateOf("") }

  LaunchedEffect(uiState, autoLoaded) {
    if (uiState is MemosUiState.Ready && !autoLoaded && !isLoading) {
      autoLoaded = true
      viewModel.loadMemos()
    }
  }

  LaunchedEffect(uiState, showCredentialsDialog, credentialsDismissed) {
    if (uiState is MemosUiState.CredentialsInput && !showCredentialsDialog && !credentialsDismissed) {
      showCredentialsDialog = true
      credentialsInitialized = false
    }
    if (uiState is MemosUiState.Ready) {
      credentialsDismissed = false
    }
  }

  MaterialTheme {
    Scaffold { padding ->
      Column(
        modifier = Modifier
          .padding(padding)
          .padding(16.dp)
          .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Memos", style = MaterialTheme.typography.headlineSmall)
          TextButton(
            onClick = {
              viewModel.editCredentials()
              showCredentialsDialog = true
              credentialsInitialized = false
              credentialsDismissed = false
            }
          ) {
            Text("Settings")
          }
        }
        if (errorMessage != null) {
          Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }
        PrimaryTabRow(selectedTabIndex = selectedTab) {
          Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            text = { Text("Habits") }
          )
          Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            text = { Text("Posts") }
          )
        }
        when (selectedTab) {
          0 -> StatsScreen(
            memos = memos,
            years = years,
            range = activityRange,
            activityMode = ActivityMode.Habits,
            onRangeChange = { activityRange = it },
            onEditDailyMemo = { memo, content ->
              viewModel.updateDailyMemo(memo.name, content)
            },
            onCreateDailyMemo = { content ->
              viewModel.createDailyMemo(content)
            },
            useVerticalScroll = true,
            isRefreshing = isLoading,
            onRefresh = { viewModel.loadMemos() }
          )
          1 -> PostsScreen(
            memos = memos,
            years = years,
            range = activityRange,
            onRangeChange = { activityRange = it },
            isRefreshing = isLoading,
            onRefresh = { viewModel.loadMemos() }
          )
        }
      }
    }
  }

  if (showCredentialsDialog) {
    val state = uiState
    if (!credentialsInitialized && state is MemosUiState.CredentialsInput) {
      editBaseUrl = state.baseUrl
      editToken = state.token
      credentialsInitialized = true
    }
    androidx.compose.material3.AlertDialog(
      onDismissRequest = {
        showCredentialsDialog = false
        credentialsInitialized = false
        credentialsDismissed = true
      },
      title = { Text("Settings") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          TextField(
            value = editBaseUrl,
            onValueChange = { editBaseUrl = it },
            label = { Text("Base URL") },
            placeholder = { Text("https://memos.example.com") },
            modifier = Modifier.fillMaxWidth()
          )
          TextField(
            value = editToken,
            onValueChange = { editToken = it },
            label = { Text("Access token") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.updateBaseUrl(editBaseUrl)
            viewModel.updateToken(editToken)
            viewModel.loadMemos()
            autoLoaded = true
            showCredentialsDialog = false
            credentialsInitialized = false
            credentialsDismissed = false
          }
        ) {
          Text("Save")
        }
      },
      dismissButton = {
        TextButton(
          onClick = {
            showCredentialsDialog = false
            credentialsInitialized = false
            credentialsDismissed = true
          }
        ) {
          Text("Cancel")
        }
      }
    )
  }
}
