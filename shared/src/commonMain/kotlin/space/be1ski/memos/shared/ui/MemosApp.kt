package space.be1ski.memos.shared.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import space.be1ski.memos.shared.config.currentLocalDate
import space.be1ski.memos.shared.ui.components.ContributionGrid
import space.be1ski.memos.shared.ui.components.availableYears

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemosApp() {
  val viewModel: MemosViewModel = koinInject()
  val uiState = viewModel.uiState
  val timeZone = remember { TimeZone.currentSystemDefault() }
  val currentYear = remember { currentLocalDate().year }
  val years = remember(uiState.memos) { availableYears(uiState.memos, timeZone, currentYear) }
  var selectedYear by remember { mutableStateOf(currentYear) }

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
        TextField(
          value = uiState.baseUrl,
          onValueChange = viewModel::updateBaseUrl,
          label = { Text("Base URL") },
          placeholder = { Text("https://memos.example.com") },
          modifier = Modifier.fillMaxWidth()
        )
        TextField(
          value = uiState.token,
          onValueChange = viewModel::updateToken,
          label = { Text("Access token") },
          visualTransformation = PasswordVisualTransformation(),
          modifier = Modifier.fillMaxWidth()
        )
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Button(onClick = viewModel::loadMemos) {
            Text("Load memos")
          }
          if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.width(20.dp).height(20.dp))
          }
          if (uiState.errorMessage != null) {
            Text(uiState.errorMessage, color = MaterialTheme.colorScheme.error)
          }
        }
        if (uiState.memos.isNotEmpty()) {
          ContributionGrid(
            memos = uiState.memos,
            selectedYear = selectedYear,
            availableYears = years,
            onYearSelected = { selectedYear = it }
          )
        }
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          items(uiState.memos) { memo ->
            Card(modifier = Modifier.fillMaxWidth()) {
              Column(modifier = Modifier.padding(12.dp)) {
                Text(memo.content)
                if (!memo.updateTime.isNullOrBlank()) {
                  Spacer(modifier = Modifier.height(6.dp))
                  Text(
                    memo.updateTime,
                    style = MaterialTheme.typography.labelSmall
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
