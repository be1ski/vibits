package space.be1ski.memos.shared.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import space.be1ski.memos.shared.domain.model.Memo
import space.be1ski.memos.shared.presentation.components.ActivityMode
import space.be1ski.memos.shared.presentation.components.ActivityRange
import space.be1ski.memos.shared.presentation.components.ActivityWeek
import space.be1ski.memos.shared.presentation.components.ContributionDay
import space.be1ski.memos.shared.presentation.components.ContributionGrid
import space.be1ski.memos.shared.presentation.components.DailyMemoInfo
import space.be1ski.memos.shared.presentation.components.activityWeekDataForHabit
import space.be1ski.memos.shared.presentation.components.WeeklyBarChart
import space.be1ski.memos.shared.presentation.components.rememberActivityWeekData
import space.be1ski.memos.shared.presentation.components.rememberHabitsConfig

/**
 * Stats tab with activity charts.
 */
@Composable
fun StatsScreen(
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
  var showHabitsConfig by remember { mutableStateOf(false) }

  Column(
    verticalArrangement = Arrangement.spacedBy(12.dp),
    modifier = Modifier.verticalScroll(rememberScrollState())
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text("Activity", style = MaterialTheme.typography.titleMedium)
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        if (activityMode == ActivityMode.Habits) {
          TextButton(onClick = { showHabitsConfig = !showHabitsConfig }) {
            Text("Settings")
          }
        }
        ActivityRangeSelector(
          years = years,
          selectedRange = range,
          onRangeChange = onRangeChange
        )
      }
    }
    if (activityMode == ActivityMode.Habits && showHabitsConfig) {
      HabitsConfigCard(habitsConfig = habitsConfig)
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

    if (activityMode == ActivityMode.Habits && habitsConfig.isNotEmpty()) {
      habitsConfig.forEach { habit ->
        HabitActivitySection(
          label = habit,
          baseWeekData = weekData,
          onEditRequested = { memo ->
            editingMemo = memo
            editingContent = memo.content
          },
          onCreateRequested = { day ->
            creatingDay = day
            creatingSelections = day.habitStatuses.associate { it.tag to it.done }
          }
        )
      }
    }
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
private fun HabitsConfigCard(habitsConfig: List<String>) {
  Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(8.dp)) {
    Text("Habits config", style = MaterialTheme.typography.titleSmall)
    if (habitsConfig.isEmpty()) {
      Text("No #habits_config found", style = MaterialTheme.typography.bodySmall)
    } else {
      habitsConfig.forEach { habit ->
        Text(habit, style = MaterialTheme.typography.bodySmall)
      }
    }
  }
}

@Composable
private fun HabitActivitySection(
  label: String,
  baseWeekData: space.be1ski.memos.shared.presentation.components.ActivityWeekData,
  onEditRequested: (DailyMemoInfo) -> Unit,
  onCreateRequested: (ContributionDay) -> Unit
) {
  val habitWeekData = remember(baseWeekData, label) {
    activityWeekDataForHabit(baseWeekData, label)
  }
  var selectedWeek by remember(habitWeekData.weeks, label) { mutableStateOf<ActivityWeek?>(null) }
  var selectedDay by remember(habitWeekData.weeks, label) { mutableStateOf(habitWeekData.weeks.lastOrNull()?.days?.lastOrNull()) }
  val chartScrollState = rememberScrollState()

  Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 12.dp)) {
    Text(label, style = MaterialTheme.typography.titleSmall)
    ContributionGrid(
      weekData = habitWeekData,
      selectedDay = selectedDay,
      selectedWeekStart = selectedWeek?.startDate,
      onDaySelected = { day -> selectedDay = day },
      onEditRequested = onEditRequested,
      onCreateRequested = onCreateRequested,
      scrollState = chartScrollState
    )
    WeeklyBarChart(
      weekData = habitWeekData,
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
  Row(verticalAlignment = Alignment.CenterVertically) {
    TextButton(onClick = { expanded = true }) {
      Text(label)
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      DropdownMenuItem(
        text = { Text("Last 12 months") },
        onClick = {
          onRangeChange(ActivityRange.LastYear)
          expanded = false
        }
      )
      years.forEach { year ->
        DropdownMenuItem(
          text = { Text(year.toString()) },
          onClick = {
            onRangeChange(ActivityRange.Year(year))
            expanded = false
          }
        )
      }
    }
  }
}
