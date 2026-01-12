package space.be1ski.memos.shared.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import space.be1ski.memos.shared.domain.model.Memo
import space.be1ski.memos.shared.presentation.components.ActivityMode
import space.be1ski.memos.shared.presentation.components.ActivityRange
import space.be1ski.memos.shared.presentation.components.ActivityWeek
import space.be1ski.memos.shared.presentation.components.ContributionDay
import space.be1ski.memos.shared.presentation.components.ContributionGrid
import space.be1ski.memos.shared.presentation.components.DailyMemoInfo
import space.be1ski.memos.shared.presentation.components.buildHabitStatuses
import space.be1ski.memos.shared.presentation.components.activityWeekDataForHabit
import space.be1ski.memos.shared.presentation.components.findDailyMemoForDate
import space.be1ski.memos.shared.presentation.components.WeeklyBarChart
import space.be1ski.memos.shared.presentation.components.rememberActivityWeekData
import space.be1ski.memos.shared.presentation.components.rememberHabitsConfig
import space.be1ski.memos.shared.presentation.time.currentLocalDate

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
  var habitsEditorDay by remember { mutableStateOf<ContributionDay?>(null) }
  var habitsEditorSelections by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
  var habitsEditorExisting by remember { mutableStateOf<DailyMemoInfo?>(null) }
  val habitsConfig = rememberHabitsConfig(memos)
  var showHabitsConfig by remember { mutableStateOf(false) }
  val timeZone = remember { TimeZone.currentSystemDefault() }
  val today = remember { currentLocalDate() }
  val todayMemo = remember(memos, timeZone, today) { findDailyMemoForDate(memos, timeZone, today) }
  val todayDay = remember(habitsConfig, todayMemo, today) {
    buildHabitDay(
      date = today,
      habitsConfig = habitsConfig,
      dailyMemo = todayMemo
    )
  }

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
    if (activityMode == ActivityMode.Habits && habitsConfig.isNotEmpty()) {
      Button(
        onClick = {
          val day = todayDay ?: return@Button
          habitsEditorDay = day
          habitsEditorExisting = day.dailyMemo
          habitsEditorSelections = buildHabitsEditorSelections(day, habitsConfig)
        },
        modifier = Modifier.fillMaxWidth()
      ) {
        Text("Track today's habits")
      }
    }
    val weekData = rememberActivityWeekData(memos, range, activityMode)
    var selectedWeek by remember(weekData.weeks, activityMode) { mutableStateOf<ActivityWeek?>(null) }
    var selectedDate by remember(weekData.weeks, activityMode) { mutableStateOf(weekData.weeks.lastOrNull()?.days?.lastOrNull()?.date) }
    val selectedDay = remember(weekData.weeks, selectedDate) {
      selectedDate?.let { date -> findDayByDate(weekData, date) }
    }
    val chartScrollState = rememberScrollState()

    ContributionGrid(
      weekData = weekData,
      selectedDay = selectedDay,
      selectedWeekStart = if (activityMode == ActivityMode.Posts) selectedWeek?.startDate else null,
      onDaySelected = { day ->
        selectedDate = day.date
        if (activityMode == ActivityMode.Posts) {
          selectedWeek = weekData.weeks.firstOrNull { week ->
            week.days.any { it.date == day.date }
          }
        }
      },
      onEditRequested = { memo ->
        editingMemo = memo
        editingContent = memo.content
      },
      onCreateRequested = { day ->
        habitsEditorDay = day
        habitsEditorExisting = day.dailyMemo
        habitsEditorSelections = buildHabitsEditorSelections(day, habitsConfig)
      },
      scrollState = chartScrollState
    )

    if (activityMode == ActivityMode.Posts) {
      WeeklyBarChart(
        weekData = weekData,
        selectedWeek = selectedWeek,
        onWeekSelected = { week ->
          selectedWeek = if (selectedWeek?.startDate == week.startDate) null else week
        selectedDate = selectedDate?.takeIf { date ->
          week.days.any { it.date == date }
        }
      },
      scrollState = chartScrollState
    )
    }

    if (activityMode == ActivityMode.Habits && habitsConfig.isNotEmpty()) {
      habitsConfig.forEach { habit ->
        HabitActivitySection(
          label = habit,
          baseWeekData = weekData,
          selectedDate = selectedDate,
          onDaySelected = { day -> selectedDate = day.date },
          onEditRequested = { memo ->
            editingMemo = memo
            editingContent = memo.content
          },
          onCreateRequested = { day ->
            habitsEditorDay = day
            habitsEditorExisting = day.dailyMemo
            habitsEditorSelections = buildHabitsEditorSelections(day, habitsConfig)
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

  if (habitsEditorDay != null) {
    val isEditing = habitsEditorExisting != null
    AlertDialog(
      onDismissRequest = {
        habitsEditorDay = null
        habitsEditorExisting = null
      },
      title = { Text(if (isEditing) "Update day" else "Create day") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          val allChecked = habitsEditorSelections.values.all { it }
          TextButton(
            onClick = {
              val mark = !allChecked
              habitsEditorSelections = habitsEditorSelections.mapValues { mark }
            }
          ) {
            Text(if (allChecked) "Clear all" else "Check all")
          }
          habitsEditorSelections.forEach { (tag, done) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
              Checkbox(
                checked = done,
                onCheckedChange = { checked ->
                  habitsEditorSelections = habitsEditorSelections.toMutableMap().also { it[tag] = checked }
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
            val day = habitsEditorDay
            if (day != null) {
              val content = buildDailyContent(day.date, habitsConfig, habitsEditorSelections)
              val existing = habitsEditorExisting
              if (existing != null) {
                onEditDailyMemo(existing, content)
              } else {
                onCreateDailyMemo(content)
              }
            }
            habitsEditorDay = null
            habitsEditorExisting = null
          }
        ) {
          Text(if (isEditing) "Update" else "Create")
        }
      },
      dismissButton = {
        TextButton(onClick = {
          habitsEditorDay = null
          habitsEditorExisting = null
        }) {
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
  selectedDate: kotlinx.datetime.LocalDate?,
  onDaySelected: (ContributionDay) -> Unit,
  onEditRequested: (DailyMemoInfo) -> Unit,
  onCreateRequested: (ContributionDay) -> Unit
) {
  val habitWeekData = remember(baseWeekData, label) {
    activityWeekDataForHabit(baseWeekData, label)
  }
  val selectedDay = remember(habitWeekData.weeks, label, selectedDate) {
    selectedDate?.let { date -> findDayByDate(habitWeekData, date) }
  }
  val chartScrollState = rememberScrollState()

  Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 12.dp)) {
    Text(label, style = MaterialTheme.typography.titleSmall)
    ContributionGrid(
      weekData = habitWeekData,
      selectedDay = selectedDay,
      selectedWeekStart = null,
      onDaySelected = onDaySelected,
      onEditRequested = onEditRequested,
      onCreateRequested = onCreateRequested,
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
    is ActivityRange.Last30Days -> "Last 30 days"
    is ActivityRange.Last90Days -> "Last 90 days"
    is ActivityRange.Last6Months -> "Last 6 months"
    is ActivityRange.LastYear -> "Last year"
    is ActivityRange.Year -> selectedRange.year.toString()
  }
  Row(verticalAlignment = Alignment.CenterVertically) {
    TextButton(onClick = { expanded = true }) {
      Text(label)
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      DropdownMenuItem(
        text = { Text("Last 30 days") },
        onClick = {
          onRangeChange(ActivityRange.Last30Days)
          expanded = false
        }
      )
      DropdownMenuItem(
        text = { Text("Last 90 days") },
        onClick = {
          onRangeChange(ActivityRange.Last90Days)
          expanded = false
        }
      )
      DropdownMenuItem(
        text = { Text("Last 6 months") },
        onClick = {
          onRangeChange(ActivityRange.Last6Months)
          expanded = false
        }
      )
      DropdownMenuItem(
        text = { Text("Last year") },
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

private fun buildHabitsEditorSelections(
  day: ContributionDay,
  habitsConfig: List<String>
): Map<String, Boolean> {
  return if (habitsConfig.isNotEmpty()) {
    habitsConfig.associateWith { tag ->
      day.habitStatuses.firstOrNull { it.tag == tag }?.done == true
    }
  } else {
    day.habitStatuses.associate { it.tag to it.done }
  }
}

private fun findDayByDate(
  weekData: space.be1ski.memos.shared.presentation.components.ActivityWeekData,
  date: kotlinx.datetime.LocalDate
): ContributionDay? {
  return weekData.weeks.firstNotNullOfOrNull { week ->
    week.days.firstOrNull { it.date == date }
  }
}

private fun buildDailyContent(
  date: LocalDate,
  habitsConfig: List<String>,
  selections: Map<String, Boolean>
): String {
  return buildString {
    append("#daily ").append(date).append("\n\n")
    habitsConfig.forEach { tag ->
      val done = selections[tag] == true
      val mark = if (done) "x" else " "
      append("- [").append(mark).append("] ").append(tag).append('\n')
    }
  }
}

private fun buildHabitDay(
  date: LocalDate,
  habitsConfig: List<String>,
  dailyMemo: DailyMemoInfo?
): ContributionDay? {
  if (habitsConfig.isEmpty()) {
    return null
  }
  val statuses = buildHabitStatuses(dailyMemo?.content, habitsConfig)
  val completed = statuses.count { it.done }
  val total = habitsConfig.size
  val ratio = if (total > 0) completed.toFloat() / total.toFloat() else 0f
  return ContributionDay(
    date = date,
    count = completed,
    totalHabits = total,
    completionRatio = ratio.coerceIn(0f, 1f),
    habitStatuses = statuses,
    dailyMemo = dailyMemo,
    inRange = true
  )
}
