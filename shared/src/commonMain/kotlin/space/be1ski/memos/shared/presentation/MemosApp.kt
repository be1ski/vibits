package space.be1ski.memos.shared.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import space.be1ski.memos.shared.presentation.components.ActivityRange
import space.be1ski.memos.shared.presentation.components.availableYears
import space.be1ski.memos.shared.presentation.state.MemosUiState
import space.be1ski.memos.shared.presentation.time.currentLocalDate

/** Root shared UI for the app. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemosApp() {
  val viewModel: MemosViewModel = koinInject()
  val uiState = viewModel.uiState
  val timeZone = remember { TimeZone.currentSystemDefault() }
  val currentYear = remember { currentLocalDate().year }
  val years = remember(uiState.memos) { availableYears(uiState.memos, timeZone, currentYear) }
  var selectedTab by remember { mutableStateOf(0) }
  var activityRange by remember { mutableStateOf<ActivityRange>(ActivityRange.LastYear) }
  val memos = uiState.memos
  val isLoading = uiState.isLoading
  val errorMessage = uiState.errorMessage

  MaterialTheme {
    Scaffold(
      topBar = {
        TopAppBar(title = { Text("Memos") })
      }
    ) { padding ->
      Column(
        modifier = Modifier
          .padding(padding)
          .padding(16.dp)
          .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        when (val state = uiState) {
          is MemosUiState.CredentialsInput -> {
            TextField(
              value = state.baseUrl,
              onValueChange = viewModel::updateBaseUrl,
              label = { Text("Base URL") },
              placeholder = { Text("https://memos.example.com") },
              modifier = Modifier.fillMaxWidth()
            )
            TextField(
              value = state.token,
              onValueChange = viewModel::updateToken,
              label = { Text("Access token") },
              visualTransformation = PasswordVisualTransformation(),
              modifier = Modifier.fillMaxWidth()
            )
          }
          is MemosUiState.Ready -> {
            TextButton(onClick = viewModel::editCredentials) {
              Text("Edit credentials")
            }
          }
        }
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Button(onClick = viewModel::loadMemos) {
            Text("Load memos")
          }
          if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp))
          }
          if (errorMessage != null) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
          }
        }
        PrimaryTabRow(selectedTabIndex = selectedTab) {
          Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            text = { Text("Stats") }
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
            onRangeChange = { activityRange = it },
            onEditDailyMemo = { memo, content ->
              viewModel.updateDailyMemo(memo.name, content)
            },
            onCreateDailyMemo = { content ->
              viewModel.createDailyMemo(content)
            }
          )
          1 -> PostsScreen(memos = memos)
        }
      }
    }
  }
}
