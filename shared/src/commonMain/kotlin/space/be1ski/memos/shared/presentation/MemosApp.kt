package space.be1ski.memos.shared.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import org.koin.compose.koinInject
import space.be1ski.memos.shared.domain.model.Memo
import space.be1ski.memos.shared.presentation.components.ActivityRange
import space.be1ski.memos.shared.presentation.components.ActivityMode
import space.be1ski.memos.shared.presentation.components.ActivityWeek
import space.be1ski.memos.shared.presentation.components.ContributionDay
import space.be1ski.memos.shared.presentation.components.ContributionGrid
import space.be1ski.memos.shared.presentation.components.DailyMemoInfo
import space.be1ski.memos.shared.presentation.components.WeeklyBarChart
import space.be1ski.memos.shared.presentation.components.availableYears
import space.be1ski.memos.shared.presentation.components.lastSevenDays
import space.be1ski.memos.shared.presentation.components.rememberActivityWeekData
import space.be1ski.memos.shared.presentation.components.rememberHabitsConfig
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

/**
 * Stats tab with activity charts.
 */
@Composable
private fun StatsScreen(
  memos: List<Memo>,
  years: List<Int>,
  range: ActivityRange,
  onRangeChange: (ActivityRange) -> Unit,
  onEditDailyMemo: (DailyMemoInfo, String) -> Unit,
  onCreateDailyMemo: (String) -> Unit
) {
  var activityMode by remember { mutableStateOf(ActivityMode.Habits) }
  var editingMemo by remember { mutableStateOf<DailyMemoInfo?>(null) }
  var editingContent by remember { mutableStateOf("") }
  var creatingDay by remember { mutableStateOf<ContributionDay?>(null) }
  var creatingSelections by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
  val habitsConfig = rememberHabitsConfig(memos)

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
    if (activityMode == ActivityMode.Habits) {
      SectionCard(title = "Habits") {
        if (habitsConfig.isEmpty()) {
          Text("No #habits_config found", style = MaterialTheme.typography.bodySmall)
        } else {
          FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            habitsConfig.forEach { habit ->
              HabitChip(label = habit)
            }
          }
        }
      }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      val habitsSelected = activityMode == ActivityMode.Habits
      OutlinedButton(
        onClick = { activityMode = ActivityMode.Habits },
        enabled = !habitsSelected
      ) {
        Text("Habits")
      }
      OutlinedButton(
        onClick = { activityMode = ActivityMode.Posts },
        enabled = habitsSelected
      ) {
        Text("Posts")
      }
    }
    val weekData = rememberActivityWeekData(memos, range, activityMode)
    var selectedWeek by remember(weekData.weeks, activityMode) { mutableStateOf<ActivityWeek?>(null) }
    var selectedDay by remember(weekData.weeks, activityMode) { mutableStateOf(weekData.weeks.lastOrNull()?.days?.lastOrNull()) }
    val chartScrollState = rememberScrollState()
    SectionCard(title = "Last 7 days") {
      LastSevenDaysCard(days = lastSevenDays(weekData), mode = activityMode)
    }
    ContributionGrid(
      weekData = weekData,
      selectedDay = selectedDay,
      selectedWeekStart = selectedWeek?.startDate,
      onDaySelected = { day ->
        selectedDay = day
      },
      onEditRequested = { memo ->
        editingMemo = memo
        editingContent = memo.content
      },
      onCreateRequested = { day ->
        creatingDay = day
        creatingSelections = day.habitStatuses.associate { it.tag to it.done }
      },
      scrollState = chartScrollState
    )
    WeeklyBarChart(
      weekData = weekData,
      selectedWeek = selectedWeek,
      onWeekSelected = { week ->
        selectedWeek = if (selectedWeek?.startDate == week.startDate) null else week
        selectedDay = selectedDay?.takeIf { day ->
          week.days.any { it.date == day.date }
        }
      },
      scrollState = chartScrollState
    )
  }

  if (editingMemo != null) {
    AlertDialog(
      onDismissRequest = { editingMemo = null },
      title = { Text("Edit day") },
      text = {
        TextField(
          value = editingContent,
          onValueChange = { editingContent = it },
          modifier = Modifier.fillMaxWidth()
        )
      },
      confirmButton = {
        Button(
          onClick = {
            val memo = editingMemo
            if (memo != null) {
              onEditDailyMemo(memo, editingContent)
            }
            editingMemo = null
          }
        ) {
          Text("Save")
        }
      },
      dismissButton = {
        TextButton(onClick = { editingMemo = null }) {
          Text("Cancel")
        }
      }
    )
  }

  if (creatingDay != null) {
    AlertDialog(
      onDismissRequest = { creatingDay = null },
      title = { Text("Create day") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          creatingSelections.forEach { (tag, done) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
              Checkbox(
                checked = done,
                onCheckedChange = { checked ->
                  creatingSelections = creatingSelections.toMutableMap().also { it[tag] = checked }
                }
              )
              Text(tag, style = MaterialTheme.typography.bodySmall)
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val day = creatingDay
            if (day != null) {
              val content = buildString {
                append("#daily ${day.date}\n\n")
                creatingSelections.forEach { (tag, done) ->
                  val mark = if (done) "x" else " "
                  append("- [").append(mark).append("] ").append(tag).append('\n')
                }
              }
              onCreateDailyMemo(content)
            }
            creatingDay = null
          }
        ) {
          Text("Create")
        }
      },
      dismissButton = {
        TextButton(onClick = { creatingDay = null }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
private fun LastSevenDaysCard(
  days: List<ContributionDay>,
  mode: ActivityMode
) {
  if (days.isEmpty()) {
    return
  }
  val totalHabits = days.maxOfOrNull { it.totalHabits } ?: 0
  val fullDays = if (mode == ActivityMode.Habits && totalHabits > 0) {
    days.count { it.count >= totalHabits }
  } else {
    days.count { it.count > 0 }
  }
  val avgRatio = if (mode == ActivityMode.Habits && totalHabits > 0) {
    days.sumOf { it.completionRatio.toDouble() } / days.size.toDouble()
  } else {
    0.0
  }

  if (mode == ActivityMode.Habits && totalHabits > 0) {
    Text("Full days: $fullDays/7 · Avg: ${(avgRatio * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
  } else {
    Text("Active days: $fullDays/7", style = MaterialTheme.typography.bodySmall)
  }
  Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
    days.forEach { day ->
      val label = day.date.day.toString()
      val ratio = if (mode == ActivityMode.Habits && day.totalHabits > 0) {
        day.completionRatio
      } else {
        if (day.count > 0) 1f else 0f
      }
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
          modifier = Modifier
            .size(12.dp)
            .background(activityColorForRatio(ratio), shape = MaterialTheme.shapes.extraSmall)
        )
        Text(label, style = MaterialTheme.typography.labelSmall)
      }
    }
  }
}

@Composable
private fun SectionCard(
  title: String,
  content: @Composable ColumnScope.() -> Unit
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Text(title, style = MaterialTheme.typography.titleSmall)
      content()
    }
  }
}

@Composable
private fun HabitChip(label: String) {
  Surface(
    color = MaterialTheme.colorScheme.surface,
    contentColor = MaterialTheme.colorScheme.onSurface,
    shape = MaterialTheme.shapes.extraLarge
  ) {
    Text(
      label,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
      style = MaterialTheme.typography.labelSmall
    )
  }
}

private fun activityColorForRatio(ratio: Float): Color {
  return when {
    ratio <= 0f -> Color(0xFFE2E8F0)
    ratio <= 0.25f -> Color(0xFFBFE3C0)
    ratio <= 0.5f -> Color(0xFF7ACB8D)
    ratio <= 0.75f -> Color(0xFF34A853)
    else -> Color(0xFF0B7D3E)
  }
}

/**
 * Selects activity range (last 12 months or specific year).
 */
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

/**
 * Posts tab with memo list.
 */
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
          if (!memo.createTime.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              memo.createTime,
              style = MaterialTheme.typography.labelSmall
            )
          } else if (!memo.updateTime.isNullOrBlank()) {
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
