package space.be1ski.memos.shared.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
@OptIn(androidx.compose.material.ExperimentalMaterialApi::class)
@Composable
fun StatsScreen(
  memos: List<Memo>,
  years: List<Int>,
  range: ActivityRange,
  activityMode: ActivityMode,
  onRangeChange: (ActivityRange) -> Unit,
  onEditDailyMemo: (DailyMemoInfo, String) -> Unit,
  onDeleteDailyMemo: (DailyMemoInfo) -> Unit,
  onCreateDailyMemo: (String) -> Unit,
  useVerticalScroll: Boolean = true,
  isRefreshing: Boolean = false,
  onRefresh: () -> Unit = {},
  enablePullRefresh: Boolean = true
) {
  var habitsEditorDay by remember { mutableStateOf<ContributionDay?>(null) }
  var habitsEditorSelections by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
  var habitsEditorExisting by remember { mutableStateOf<DailyMemoInfo?>(null) }
  var habitsEditorError by remember { mutableStateOf<String?>(null) }
  var showEmptyDeleteConfirm by remember { mutableStateOf(false) }
  val habitsConfig = rememberHabitsConfig(memos)
  var showHabitsConfig by remember { mutableStateOf(false) }
  var habitsConfigText by remember { mutableStateOf("") }
  var showHabitDetails by remember { mutableStateOf(false) }
  val timeZone = remember { TimeZone.currentSystemDefault() }
  val habitsConfigMemo = remember(memos, timeZone) { findHabitsConfigMemo(memos) }
  val today = remember { currentLocalDate() }
  val todayMemo = remember(memos, timeZone, today) { findDailyMemoForDate(memos, timeZone, today) }
  val todayDay = remember(habitsConfig, todayMemo, today) {
    buildHabitDay(
      date = today,
      habitsConfig = habitsConfig,
      dailyMemo = todayMemo
    )
  }
  val weekData = rememberActivityWeekData(memos, range, activityMode)
  val showWeekdayLegend = range is ActivityRange.Last90Days
  val collapseHabits = activityMode == ActivityMode.Habits && range is ActivityRange.Last90Days
  var selectedWeek by remember { mutableStateOf<ActivityWeek?>(null) }
  var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
  var activeSelectionId by remember { mutableStateOf<String?>(null) }
  val selectedDay = remember(weekData.weeks, selectedDate) {
    selectedDate?.let { date -> findDayByDate(weekData, date) }
  }

  val columnModifier = if (useVerticalScroll) {
    Modifier.verticalScroll(rememberScrollState())
  } else {
    Modifier
  }
  val pullRefreshState = rememberPullRefreshState(isRefreshing, onRefresh)
  val containerModifier = if (enablePullRefresh) {
    Modifier.pullRefresh(pullRefreshState)
  } else {
    Modifier
  }
  val currentConfigText = remember(habitsConfig) { habitsConfig.joinToString("\n") { "${it.label} | ${it.tag}" } }

  LaunchedEffect(weekData.weeks) {
    if (selectedDate == null && activeSelectionId == null) {
      selectedDate = weekData.weeks.lastOrNull()?.days?.lastOrNull()?.date
    }
  }

  if (showHabitsConfig && habitsConfigText.isBlank()) {
    habitsConfigText = currentConfigText
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .then(containerModifier)
  ) {
    Column(
      verticalArrangement = Arrangement.spacedBy(12.dp),
      modifier = columnModifier
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
              Text("Habits config")
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
        HabitsConfigCard(
          habitsConfigText = habitsConfigText,
          onConfigChange = { habitsConfigText = it },
          onSave = {
            val content = buildHabitsConfigContent(habitsConfigText)
            val existing = habitsConfigMemo?.let { memo ->
              DailyMemoInfo(name = memo.name, content = memo.content)
            }
            if (existing != null) {
              onEditDailyMemo(existing, content)
            } else {
              onCreateDailyMemo(content)
            }
          }
        )
      }
      val chartScrollState = rememberScrollState()

      ContributionGrid(
        weekData = weekData,
        selectedDay = if (activeSelectionId == "main") selectedDay else null,
        selectedWeekStart = if (activityMode == ActivityMode.Posts) selectedWeek?.startDate else null,
        onDaySelected = { day ->
          selectedDate = day.date
          activeSelectionId = "main"
          if (activityMode == ActivityMode.Posts) {
            selectedWeek = weekData.weeks.firstOrNull { week ->
              week.days.any { it.date == day.date }
            }
          }
        },
        onClearSelection = {
          selectedDate = null
          selectedWeek = null
          activeSelectionId = null
        },
        onEditRequested = { day ->
          habitsEditorDay = day
          habitsEditorExisting = day.dailyMemo
          habitsEditorSelections = buildHabitsEditorSelections(day, habitsConfig)
        },
          onCreateRequested = { day ->
            habitsEditorDay = day
            habitsEditorExisting = day.dailyMemo
            habitsEditorSelections = buildHabitsEditorSelections(day, habitsConfig)
            habitsEditorError = null
          },
        isActiveSelection = activeSelectionId == "main",
        scrollState = chartScrollState,
        showWeekdayLegend = showWeekdayLegend,
        compactHeight = range is ActivityRange.Last90Days
      )

      if (collapseHabits && habitsConfig.isNotEmpty()) {
        OutlinedButton(
          onClick = { showHabitDetails = !showHabitDetails },
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(if (showHabitDetails) "Hide habit details" else "Show habit details")
        }
      }

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

      if (activityMode == ActivityMode.Habits && habitsConfig.isNotEmpty() && (!collapseHabits || showHabitDetails)) {
        habitsConfig.forEach { habit ->
          HabitActivitySection(
            habit = habit,
            baseWeekData = weekData,
            selectedDate = if (activeSelectionId == "habit:${habit.tag}") selectedDate else null,
            onDaySelected = { day ->
              selectedDate = day.date
              activeSelectionId = "habit:${habit.tag}"
            },
            onClearSelection = {
              selectedDate = null
              activeSelectionId = null
            },
            onEditRequested = { day ->
            habitsEditorDay = day
            habitsEditorExisting = day.dailyMemo
            habitsEditorSelections = buildHabitsEditorSelections(day, habitsConfig)
            habitsEditorError = null
          },
            onCreateRequested = { day ->
              habitsEditorDay = day
              habitsEditorExisting = day.dailyMemo
              habitsEditorSelections = buildHabitsEditorSelections(day, habitsConfig)
            },
            isActiveSelection = activeSelectionId == "habit:${habit.tag}",
            showWeekdayLegend = showWeekdayLegend,
            compactHeight = range is ActivityRange.Last90Days
          )
        }
      }
    }

    if (activityMode == ActivityMode.Habits && habitsConfig.isNotEmpty()) {
      FloatingActionButton(
        onClick = {
          val day = todayDay ?: return@FloatingActionButton
          habitsEditorDay = day
          habitsEditorExisting = day.dailyMemo
          habitsEditorSelections = buildHabitsEditorSelections(day, habitsConfig)
          habitsEditorError = null
        },
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(16.dp)
      ) {
        Icon(
          imageVector = Icons.Filled.AddTask,
          contentDescription = "Track today"
        )
      }
    }
    if (enablePullRefresh) {
      PullRefreshIndicator(
        refreshing = isRefreshing,
        state = pullRefreshState,
        modifier = Modifier.align(Alignment.TopCenter)
      )
    }
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
      if (habitsConfig.isNotEmpty()) {
        habitsConfig.forEach { habit ->
          val tag = habit.tag
          val done = habitsEditorSelections[tag] == true
              Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                  checked = done,
                  onCheckedChange = { checked ->
                    habitsEditorSelections = habitsEditorSelections.toMutableMap().also { it[tag] = checked }
                  }
                )
            Text(habit.label, style = MaterialTheme.typography.bodySmall)
          }
        }
      } else {
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
      habitsEditorError?.let { message ->
        Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
      }
    }
  },
  confirmButton = {
    Button(
      onClick = {
        val hasSelection = habitsEditorSelections.values.any { it }
        if (!hasSelection) {
          if (habitsEditorExisting != null) {
            showEmptyDeleteConfirm = true
          } else {
            habitsEditorError = "Select at least one habit."
          }
          return@Button
        }
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
        selectedDate = null
        selectedWeek = null
        activeSelectionId = null
        habitsEditorDay = null
        habitsEditorExisting = null
        habitsEditorError = null
      }
    ) {
      Text(if (isEditing) "Update" else "Create")
    }
  },
      dismissButton = {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          if (isEditing) {
            TextButton(onClick = {
              val existing = habitsEditorExisting
              if (existing != null) {
                onDeleteDailyMemo(existing)
              }
              selectedDate = null
              selectedWeek = null
              activeSelectionId = null
              habitsEditorDay = null
              habitsEditorExisting = null
              habitsEditorError = null
            }) {
              Text("Delete")
            }
          }
          TextButton(onClick = {
            habitsEditorDay = null
            habitsEditorExisting = null
            habitsEditorError = null
          }) {
            Text("Cancel")
          }
        }
      }
    )
  }

  if (showEmptyDeleteConfirm) {
    AlertDialog(
      onDismissRequest = { showEmptyDeleteConfirm = false },
      title = { Text("Delete day?") },
      text = { Text("No habits selected. The daily entry will be deleted.") },
      confirmButton = {
        Button(
          onClick = {
            val existing = habitsEditorExisting
            if (existing != null) {
              onDeleteDailyMemo(existing)
            }
            selectedDate = null
            selectedWeek = null
            activeSelectionId = null
            habitsEditorDay = null
            habitsEditorExisting = null
            habitsEditorError = null
            showEmptyDeleteConfirm = false
          }
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        TextButton(onClick = { showEmptyDeleteConfirm = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
private fun HabitsConfigCard(
  habitsConfigText: String,
  onConfigChange: (String) -> Unit,
  onSave: () -> Unit
) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(8.dp)) {
    Text("Habits config", style = MaterialTheme.typography.titleSmall)
    TextField(
      value = habitsConfigText,
      onValueChange = onConfigChange,
      modifier = Modifier.fillMaxWidth(),
      placeholder = { Text("Гимнастика | #habits/gym\nЧтение | #habits/reading") }
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      Button(onClick = onSave) {
        Text("Save")
      }
    }
  }
}

@Composable
private fun HabitActivitySection(
  habit: space.be1ski.memos.shared.presentation.components.HabitConfig,
  baseWeekData: space.be1ski.memos.shared.presentation.components.ActivityWeekData,
  selectedDate: kotlinx.datetime.LocalDate?,
  onDaySelected: (ContributionDay) -> Unit,
  onClearSelection: () -> Unit,
  onEditRequested: (ContributionDay) -> Unit,
  onCreateRequested: (ContributionDay) -> Unit,
  isActiveSelection: Boolean,
  showWeekdayLegend: Boolean,
  compactHeight: Boolean
) {
  val habitWeekData = remember(baseWeekData, habit) {
    activityWeekDataForHabit(baseWeekData, habit)
  }
  val selectedDay = remember(habitWeekData.weeks, habit, selectedDate) {
    selectedDate?.let { date -> findDayByDate(habitWeekData, date) }
  }
  val chartScrollState = rememberScrollState()

  Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 12.dp)) {
    Text(habit.label, style = MaterialTheme.typography.titleSmall)
    ContributionGrid(
      weekData = habitWeekData,
      selectedDay = selectedDay,
      selectedWeekStart = null,
      onDaySelected = onDaySelected,
      onClearSelection = onClearSelection,
      onEditRequested = onEditRequested,
      onCreateRequested = onCreateRequested,
      isActiveSelection = isActiveSelection,
      scrollState = chartScrollState,
      showWeekdayLegend = showWeekdayLegend,
      compactHeight = compactHeight
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
  habitsConfig: List<space.be1ski.memos.shared.presentation.components.HabitConfig>
): Map<String, Boolean> {
  return if (habitsConfig.isNotEmpty()) {
    habitsConfig.associate { habit ->
      habit.tag to (day.habitStatuses.firstOrNull { it.tag == habit.tag }?.done == true)
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
  habitsConfig: List<space.be1ski.memos.shared.presentation.components.HabitConfig>,
  selections: Map<String, Boolean>
): String {
  return buildString {
    append("#habits/daily ").append(date).append("\n\n")
    habitsConfig.forEach { habit ->
      val done = selections[habit.tag] == true
      if (done) {
        append("- [x] ").append(habit.tag).append('\n')
      }
    }
  }
}

private fun findHabitsConfigMemo(memos: List<Memo>): Memo? {
  return memos.filter { memo ->
    memo.content.contains("#habits/config") || memo.content.contains("#habits_config")
  }
    .maxByOrNull { memo ->
      memo.updateTime?.toEpochMilliseconds() ?: memo.createTime?.toEpochMilliseconds() ?: 0L
    }
}

private fun buildHabitsConfigContent(rawText: String): String {
  val entries = rawText.lineSequence()
    .map { it.trim() }
    .filter { it.isNotBlank() }
    .mapNotNull { parseHabitConfigLine(it) }
    .toList()
  return buildString {
    append("#habits/config\n\n")
    entries.forEach { entry ->
      append(entry.label).append(" | ").append(entry.tag).append('\n')
    }
  }
}

private fun parseHabitConfigLine(line: String): space.be1ski.memos.shared.presentation.components.HabitConfig? {
  val parts = line.split("|", limit = 2).map { it.trim() }.filter { it.isNotBlank() }
  if (parts.isEmpty()) {
    return null
  }
  val (label, tagRaw) = if (parts.size == 1) {
    val raw = parts.first()
    val tag = normalizeHabitTag(raw)
    val label = if (raw.startsWith("#habits/") || raw.startsWith("#habit/")) labelFromTag(tag) else raw
    label to tag
  } else {
    val label = parts[0]
    val tag = normalizeHabitTag(parts[1])
    label to tag
  }
  return space.be1ski.memos.shared.presentation.components.HabitConfig(tag = tagRaw, label = label)
}

private fun normalizeHabitTag(raw: String): String {
  val trimmed = raw.trim().removePrefix("#habits/").removePrefix("#habit/")
  val sanitized = trimmed.replace("\\s+".toRegex(), "_")
  return "#habits/$sanitized"
}

private fun labelFromTag(tag: String): String {
  return tag.removePrefix("#habits/").removePrefix("#habit/").replace('_', ' ')
}

private fun buildHabitDay(
  date: LocalDate,
  habitsConfig: List<space.be1ski.memos.shared.presentation.components.HabitConfig>,
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
