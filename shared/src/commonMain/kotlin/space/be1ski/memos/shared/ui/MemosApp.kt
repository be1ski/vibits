package space.be1ski.memos.shared.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import space.be1ski.memos.shared.model.Memo
import space.be1ski.memos.shared.ui.components.ActivityRange
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
  var selectedTab by remember { mutableStateOf(0) }
  var activityRange by remember { mutableStateOf<ActivityRange>(ActivityRange.LastYear) }

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
            CircularProgressIndicator(modifier = Modifier.size(20.dp))
          }
          if (uiState.errorMessage != null) {
            Text(uiState.errorMessage, color = MaterialTheme.colorScheme.error)
          }
        }
        TabRow(selectedTabIndex = selectedTab) {
          Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            text = { Text("Profile") }
          )
          Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            text = { Text("Posts") }
          )
        }
        when (selectedTab) {
          0 -> ProfileScreen(
            memos = uiState.memos,
            years = years,
            range = activityRange,
            onRangeChange = { activityRange = it }
          )
          1 -> PostsScreen(memos = uiState.memos)
        }
      }
    }
  }
}

@Composable
private fun ProfileScreen(
  memos: List<Memo>,
  years: List<Int>,
  range: ActivityRange,
  onRangeChange: (ActivityRange) -> Unit
) {
  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text("Activity", style = MaterialTheme.typography.titleMedium)
      ActivityRangeSelector(
        years = years,
        selectedRange = range,
        onRangeChange = onRangeChange
      )
    }
    ContributionGrid(memos = memos, range = range)
  }
}

@Composable
private fun ActivityRangeSelector(
  years: List<Int>,
  selectedRange: ActivityRange,
  onRangeChange: (ActivityRange) -> Unit
) {
  var expanded by remember { mutableStateOf(false) }
  val label = when (selectedRange) {
    is ActivityRange.LastYear -> "Last 12 months"
    is ActivityRange.Year -> selectedRange.year.toString()
  }
  Box {
    OutlinedButton(onClick = { expanded = true }) {
      Text(label)
    }
    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false }
    ) {
      DropdownMenuItem(
        text = { Text("Last 12 months") },
        onClick = {
          expanded = false
          onRangeChange(ActivityRange.LastYear)
        }
      )
      years.forEach { year ->
        DropdownMenuItem(
          text = { Text(year.toString()) },
          onClick = {
            expanded = false
            onRangeChange(ActivityRange.Year(year))
          }
        )
      }
    }
  }
}

@Composable
private fun PostsScreen(
  memos: List<Memo>
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    items(memos) { memo ->
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
