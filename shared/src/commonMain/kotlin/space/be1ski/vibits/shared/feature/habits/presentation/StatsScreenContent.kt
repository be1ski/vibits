@file:Suppress("TooManyFunctions")

package space.be1ski.vibits.shared.feature.habits.presentation

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.shared.Res
import space.be1ski.vibits.shared.action_configure_habits
import space.be1ski.vibits.shared.action_track
import space.be1ski.vibits.shared.format_habits_progress
import space.be1ski.vibits.shared.format_posts_count
import space.be1ski.vibits.shared.format_posts_today
import space.be1ski.vibits.shared.hint_add_habits_config
import space.be1ski.vibits.shared.label_activity
import space.be1ski.vibits.shared.label_habits_config
import space.be1ski.vibits.shared.msg_no_habits_yet
import space.be1ski.vibits.shared.action_show_posts
import space.be1ski.vibits.shared.action_hide_posts
import space.be1ski.vibits.shared.label_time_night
import space.be1ski.vibits.shared.label_time_morning
import space.be1ski.vibits.shared.label_time_day
import space.be1ski.vibits.shared.label_time_evening
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.core.platform.DateFormatter
import space.be1ski.vibits.shared.core.ui.ActivityMode
import space.be1ski.vibits.shared.core.ui.ActivityRange
import space.be1ski.vibits.shared.core.ui.theme.AppColors
import space.be1ski.vibits.shared.core.ui.theme.resolve
import space.be1ski.vibits.shared.feature.habits.domain.model.ContributionDay
import space.be1ski.vibits.shared.feature.habits.presentation.components.localizedLabel
import space.be1ski.vibits.shared.feature.habits.presentation.components.ContributionGrid
import space.be1ski.vibits.shared.feature.habits.presentation.components.ContributionGridState
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitConfig
import space.be1ski.vibits.shared.core.ui.Indent
import space.be1ski.vibits.shared.feature.habits.presentation.components.calculateLayout
import space.be1ski.vibits.shared.feature.habits.presentation.components.habitsConfigForDate
import space.be1ski.vibits.shared.feature.habits.presentation.components.WeeklyBarChart
import space.be1ski.vibits.shared.feature.habits.presentation.components.WeeklyBarChartState
import space.be1ski.vibits.shared.feature.habits.presentation.components.activityWeekDataForHabit
import space.be1ski.vibits.shared.feature.habits.presentation.components.lastSevenDays
import space.be1ski.vibits.shared.feature.habits.presentation.components.ChartDimens

@Composable
internal fun StatsInfoCard(
  derived: StatsScreenDerivedState,
  dispatch: (HabitsAction) -> Unit
) {
  val state = derived.state
  val isHabitsMode = state.activityMode == ActivityMode.Habits
  val hasTodayConfig = derived.todayConfig.isNotEmpty()

  if (!isHabitsMode || !hasTodayConfig) return

  val todayStatuses = derived.todayDay?.habitStatuses.orEmpty()
  val todayDone = todayStatuses.count { it.done }
  val todayTotal = todayStatuses.size

  if (todayTotal == 0) return

  OutlinedCard(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.padding(Indent.s).fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        stringResource(Res.string.format_habits_progress, todayDone, todayTotal),
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.weight(1f, fill = false)
      )
      Row(
        horizontalArrangement = Arrangement.spacedBy(Indent.xs),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = { dispatch(HabitsAction.OpenConfigDialog(derived.currentHabitsConfig)) },
          modifier = Modifier.size(36.dp)
        ) {
          Icon(
            imageVector = Icons.Filled.Settings,
            contentDescription = stringResource(Res.string.label_habits_config),
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Button(
          onClick = {
            val day = derived.todayDay ?: return@Button
            dispatch(HabitsAction.OpenEditor(day, derived.todayConfig))
          }
        ) {
          Text(stringResource(Res.string.action_track))
        }
      }
    }
  }
}

@Composable
internal fun StatsHeaderRow() {
  Text(stringResource(Res.string.label_activity), style = MaterialTheme.typography.titleMedium)
}

@Composable
internal fun StatsHabitsEmptyState(
  derived: StatsScreenDerivedState,
  dispatch: (HabitsAction) -> Unit
) {
  val state = derived.state
  if (state.activityMode != ActivityMode.Habits || derived.currentHabitsConfig.isNotEmpty()) {
    return
  }
  OutlinedCard(modifier = Modifier.fillMaxWidth()) {
    Column(
      modifier = Modifier.padding(Indent.s),
      verticalArrangement = Arrangement.spacedBy(Indent.xs)
    ) {
      Text(stringResource(Res.string.msg_no_habits_yet), style = MaterialTheme.typography.titleSmall)
      Text(
        stringResource(Res.string.hint_add_habits_config),
        style = MaterialTheme.typography.bodySmall
      )
      Button(onClick = { dispatch(HabitsAction.OpenConfigDialog(emptyList())) }) {
        Text(stringResource(Res.string.action_configure_habits))
      }
    }
  }
}

@Composable
internal fun StatsPostsInfoCard(
  derived: StatsScreenDerivedState
) {
  val state = derived.state
  if (state.activityMode != ActivityMode.Posts) return

  val totalPosts = derived.weekData.weeks.sumOf { it.weeklyCount }
  val todayPosts = derived.todayDay?.count ?: 0

  if (totalPosts == 0 && todayPosts == 0) return

  OutlinedCard(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.padding(Indent.s).fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(Indent.x2s)) {
        Text(
          stringResource(Res.string.format_posts_count, totalPosts),
          style = MaterialTheme.typography.titleSmall
        )
        if (todayPosts > 0) {
          Text(
            stringResource(Res.string.format_posts_today, todayPosts),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}

private const val DAYS_IN_WEEK = 7
private const val TIME_BLOCKS = 4
private const val HOURS_PER_BLOCK = 6
private const val INTENSITY_BASE = 0.3f
private const val INTENSITY_RANGE = 0.7f

@Composable
private fun WeeklyPostsHeatmap(
  memos: List<Memo>,
  weekStart: kotlinx.datetime.LocalDate,
  timeZone: TimeZone,
  compactHeight: Boolean
) {
  val weekEnd = weekStart.plus(DatePeriod(days = DAYS_IN_WEEK - 1))
  val countMatrix = rememberPostCountMatrix(memos, weekStart, weekEnd, timeZone)
  val maxCount = countMatrix.maxOfOrNull { row -> row.maxOrNull() ?: 0 } ?: 0

  val timeBlockLabels = listOf(
    stringResource(Res.string.label_time_night),
    stringResource(Res.string.label_time_morning),
    stringResource(Res.string.label_time_day),
    stringResource(Res.string.label_time_evening)
  )

  BoxWithConstraints {
    val labelWidth = HABIT_LABEL_WIDTH
    val spacing = ChartDimens.spacing(compactHeight)
    val availableWidth = (maxWidth - labelWidth - spacing).coerceAtLeast(0.dp)
    val layout = calculateLayout(
      maxWidth = availableWidth,
      columns = DAYS_IN_WEEK,
      minColumnSize = ChartDimens.minCell(compactHeight),
      spacing = spacing
    )
    val cellSize = layout.columnSize

    Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
      HeatmapDayHeaders(weekStart, cellSize, labelWidth, spacing)
      HeatmapTimeBlocks(countMatrix, maxCount, timeBlockLabels, cellSize, labelWidth, spacing)
      HeatmapDayNumbers(weekStart, cellSize, labelWidth, spacing)
    }
  }
}

@Composable
private fun rememberPostCountMatrix(
  memos: List<Memo>,
  weekStart: kotlinx.datetime.LocalDate,
  weekEnd: kotlinx.datetime.LocalDate,
  timeZone: TimeZone
): Array<IntArray> = remember(memos, weekStart, weekEnd, timeZone) {
  val matrix = Array(TIME_BLOCKS) { IntArray(DAYS_IN_WEEK) }
  memos.forEach { memo ->
    val instant = memo.createTime ?: memo.updateTime ?: return@forEach
    val dateTime = instant.toLocalDateTime(timeZone)
    val date = dateTime.date
    if (date !in weekStart..weekEnd) return@forEach

    val dayIndex = date.dayOfWeek.ordinal
    val blockIndex = dateTime.hour / HOURS_PER_BLOCK
    matrix[blockIndex][dayIndex]++
  }
  matrix
}

@Composable
private fun HeatmapDayHeaders(
  weekStart: kotlinx.datetime.LocalDate,
  cellSize: androidx.compose.ui.unit.Dp,
  labelWidth: androidx.compose.ui.unit.Dp,
  spacing: androidx.compose.ui.unit.Dp
) {
  Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
    Spacer(modifier = Modifier.width(labelWidth))
    for (dayOffset in 0 until DAYS_IN_WEEK) {
      val date = weekStart.plus(DatePeriod(days = dayOffset))
      Box(modifier = Modifier.size(cellSize), contentAlignment = Alignment.Center) {
        Text(DateFormatter.dayOfWeekShort(date.dayOfWeek), style = MaterialTheme.typography.labelSmall)
      }
    }
  }
}

@Suppress("LongParameterList")
@Composable
private fun HeatmapTimeBlocks(
  countMatrix: Array<IntArray>,
  maxCount: Int,
  timeBlockLabels: List<String>,
  cellSize: androidx.compose.ui.unit.Dp,
  labelWidth: androidx.compose.ui.unit.Dp,
  spacing: androidx.compose.ui.unit.Dp
) {
  val activeColor = AppColors.habitGradientEnd.resolve()
  val inactiveColor = AppColors.inactiveCell.resolve()

  timeBlockLabels.forEachIndexed { blockIndex, label ->
    Row(
      horizontalArrangement = Arrangement.spacedBy(spacing),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(text = label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(labelWidth))
      for (dayIndex in 0 until DAYS_IN_WEEK) {
        val count = countMatrix[blockIndex][dayIndex]
        val intensity = if (maxCount > 0) count.toFloat() / maxCount else 0f
        val cellColor = if (count > 0) {
          activeColor.copy(alpha = INTENSITY_BASE + intensity * INTENSITY_RANGE)
        } else {
          inactiveColor
        }
        Box(
          modifier = Modifier
            .size(cellSize)
            .background(cellColor, shape = MaterialTheme.shapes.extraSmall),
          contentAlignment = Alignment.Center
        ) {
          if (count > 0) {
            Text(
              count.toString(),
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }
    }
  }
}

@Composable
private fun HeatmapDayNumbers(
  weekStart: kotlinx.datetime.LocalDate,
  cellSize: androidx.compose.ui.unit.Dp,
  labelWidth: androidx.compose.ui.unit.Dp,
  spacing: androidx.compose.ui.unit.Dp
) {
  Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
    Spacer(modifier = Modifier.width(labelWidth))
    for (dayOffset in 0 until DAYS_IN_WEEK) {
      val date = weekStart.plus(DatePeriod(days = dayOffset))
      Box(modifier = Modifier.size(cellSize), contentAlignment = Alignment.Center) {
        Text(date.day.toString(), style = MaterialTheme.typography.labelSmall)
      }
    }
  }
}

@Composable
internal fun StatsCollapsiblePosts(
  derived: StatsScreenDerivedState,
  expanded: Boolean,
  onExpandedChange: (Boolean) -> Unit
) {
  val state = derived.state
  if (state.activityMode != ActivityMode.Posts) return

  val periodMemos = rememberPeriodMemos(state.memos, state.range, derived.timeZone)
  if (periodMemos.isEmpty()) return

  Column(verticalArrangement = Arrangement.spacedBy(Indent.xs)) {
    TextButton(onClick = { onExpandedChange(!expanded) }) {
      Text(
        if (expanded) {
          stringResource(Res.string.action_hide_posts)
        } else {
          stringResource(Res.string.action_show_posts)
        }
      )
    }
    if (expanded) {
      Column(
        modifier = Modifier
          .heightIn(max = POSTS_LIST_MAX_HEIGHT)
          .verticalScroll(rememberScrollState())
      ) {
        periodMemos.forEachIndexed { index, memo ->
          CompactPostRow(memo = memo, timeZone = derived.timeZone)
          if (index < periodMemos.lastIndex) {
            HorizontalDivider(
              modifier = Modifier.padding(vertical = Indent.x2s),
              color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = DIVIDER_ALPHA)
            )
          }
        }
      }
    }
  }
}

private val POSTS_LIST_MAX_HEIGHT = 200.dp
private const val DIVIDER_ALPHA = 0.5f

@Composable
private fun rememberPeriodMemos(
  memos: List<Memo>,
  range: ActivityRange,
  timeZone: TimeZone
): List<Memo> = remember(memos, range, timeZone) {
  val (start, end) = when (range) {
    is ActivityRange.Week -> range.startDate to range.startDate.plus(DatePeriod(days = DAYS_IN_WEEK - 1))
    is ActivityRange.Month -> {
      val start = kotlinx.datetime.LocalDate(range.year, range.month, 1)
      val nextMonth = start.plus(DatePeriod(months = 1))
      val end = nextMonth.plus(DatePeriod(days = -1))
      start to end
    }
    is ActivityRange.Quarter -> {
      val startMonth = (range.index - 1) * MONTHS_PER_QUARTER + 1
      val start = kotlinx.datetime.LocalDate(range.year, startMonth, 1)
      val end = start.plus(DatePeriod(months = MONTHS_PER_QUARTER)).plus(DatePeriod(days = -1))
      start to end
    }
    is ActivityRange.Year -> {
      val start = kotlinx.datetime.LocalDate(range.year, JANUARY, 1)
      val end = kotlinx.datetime.LocalDate(range.year, DECEMBER, LAST_DAY_OF_DECEMBER)
      start to end
    }
  }
  memos.filter { memo ->
    val instant = memo.createTime ?: memo.updateTime ?: return@filter false
    val date = instant.toLocalDateTime(timeZone).date
    date in start..end
  }.sortedByDescending { it.createTime ?: it.updateTime }
}

private const val MONTHS_PER_QUARTER = 3
private const val JANUARY = 1
private const val DECEMBER = 12
private const val LAST_DAY_OF_DECEMBER = 31

@Composable
private fun CompactPostRow(memo: Memo, timeZone: TimeZone) {
  val instant = memo.createTime ?: memo.updateTime
  val dateLabel = instant?.let {
    DateFormatter.compactDateTime(it.toLocalDateTime(timeZone))
  } ?: ""

  Row(
    modifier = Modifier.fillMaxWidth().padding(vertical = Indent.x2s),
    horizontalArrangement = Arrangement.spacedBy(Indent.s)
  ) {
    Text(
      dateLabel,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
      memo.content.lines().firstOrNull()?.take(COMPACT_POST_MAX_LENGTH) ?: "",
      style = MaterialTheme.typography.bodySmall,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.weight(1f)
    )
  }
}

private const val COMPACT_POST_MAX_LENGTH = 50

private const val SHIMMER_DURATION_MS = 1000
private const val SHIMMER_LABEL_HEIGHT = 20

@Composable
private fun ChartShimmer(compactHeight: Boolean) {
  val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
  val alpha = infiniteTransition.animateFloat(
    initialValue = 0.3f,
    targetValue = 0.7f,
    animationSpec = infiniteRepeatable(
      animation = tween(SHIMMER_DURATION_MS),
      repeatMode = RepeatMode.Reverse
    ),
    label = "shimmer_alpha"
  )
  val cellSize = ChartDimens.minCell(compactHeight)
  val spacing = ChartDimens.spacing(compactHeight)
  val chartHeight = (cellSize + spacing) * DAYS_IN_WEEK

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(chartHeight)
      .background(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha.value),
        shape = RoundedCornerShape(4.dp)
      )
  )
}

@Composable
private fun HabitSectionShimmer(label: String, compactHeight: Boolean) {
  val infiniteTransition = rememberInfiniteTransition(label = "shimmer_$label")
  val alpha = infiniteTransition.animateFloat(
    initialValue = 0.3f,
    targetValue = 0.7f,
    animationSpec = infiniteRepeatable(
      animation = tween(SHIMMER_DURATION_MS),
      repeatMode = RepeatMode.Reverse
    ),
    label = "shimmer_alpha_$label"
  )
  val cellSize = ChartDimens.minCell(compactHeight)
  val spacing = ChartDimens.spacing(compactHeight)
  val chartHeight = (cellSize + spacing) * DAYS_IN_WEEK

  Column(
    verticalArrangement = Arrangement.spacedBy(Indent.xs),
    modifier = Modifier.padding(top = Indent.s)
  ) {
    // Label shimmer
    Box(
      modifier = Modifier
        .width(80.dp)
        .height(SHIMMER_LABEL_HEIGHT.dp)
        .background(
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha.value),
          shape = RoundedCornerShape(4.dp)
        )
    )
    // Chart shimmer
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(chartHeight)
        .background(
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha.value),
          shape = RoundedCornerShape(4.dp)
        )
    )
  }
}

@Suppress("LongMethod")
@Composable
internal fun StatsMainChart(
  derived: StatsScreenDerivedState,
  dispatch: (HabitsAction) -> Unit
) {
  val state = derived.state

  // For Posts/Week show time-of-day heatmap instead of contribution grid
  if (state.activityMode == ActivityMode.Posts && state.range is ActivityRange.Week) {
    WeeklyPostsHeatmap(
      memos = state.memos,
      weekStart = state.range.startDate,
      timeZone = derived.timeZone,
      compactHeight = derived.useCompactHeight
    )
    return
  }

  // Show shimmer while loading and no data yet
  if (derived.isLoadingWeekData && derived.weekData.weeks.isEmpty()) {
    ChartShimmer(derived.useCompactHeight)
    return
  }

  val habitsState = derived.habitsState
  val chartScrollState = rememberScrollState()

  val onDaySelected = remember(dispatch, state.activityMode, derived.weekData.weeks) {
    { day: ContributionDay ->
      dispatch(HabitsAction.SelectDay(day, "main"))
      if (state.activityMode == ActivityMode.Posts) {
        val week = derived.weekData.weeks.firstOrNull { week ->
          week.days.any { it.date == day.date }
        }
        if (week != null) {
          dispatch(HabitsAction.SelectWeek(week))
        }
      }
    }
  }
  val onClearSelection = remember(dispatch) { { dispatch(HabitsAction.ClearSelection) } }
  val onEditRequested = remember(dispatch, derived.habitsConfigTimeline) {
    { day: ContributionDay ->
      val config = habitsConfigForDate(derived.habitsConfigTimeline, day.date)?.habits.orEmpty()
      dispatch(HabitsAction.OpenEditor(day, config))
    }
  }

  if (derived.showLast7DaysMatrix) {
    val onSingleHabitToggle = remember(dispatch, derived.currentHabitsConfig) {
      { day: ContributionDay, habitTag: String, habitLabel: String ->
        dispatch(HabitsAction.RequestSingleHabitToggle(day, habitTag, habitLabel, derived.currentHabitsConfig))
      }
    }
    LastSevenDaysMatrix(
      days = lastSevenDays(derived.weekData),
      habits = derived.currentHabitsConfig,
      compactHeight = derived.useCompactHeight,
      demoMode = state.demoMode,
      today = derived.today,
      onHabitClick = onSingleHabitToggle
    )
  } else {
    val showTimeline = state.range is ActivityRange.Quarter || state.range is ActivityRange.Year
    ContributionGrid(
      state = ContributionGridState(
        weekData = derived.weekData,
        range = state.range,
        selectedDay = if (habitsState.activeSelectionId == "main") derived.selectedDay else null,
        selectedWeekStart = if (state.activityMode == ActivityMode.Posts) habitsState.selectedWeek?.startDate else null,
        isActiveSelection = habitsState.activeSelectionId == "main",
        scrollState = chartScrollState,
        showWeekdayLegend = derived.showWeekdayLegend,
        showAllWeekdayLabels = true,
        compactHeight = derived.useCompactHeight,
        showTimeline = showTimeline,
        showDayNumbers = false,
        today = derived.today,
        demoMode = state.demoMode
      ),
      onDaySelected = onDaySelected,
      onClearSelection = onClearSelection,
      onEditRequested = onEditRequested,
      onCreateRequested = onEditRequested
    )
  }
}

@Composable
internal fun StatsWeeklyChart(
  derived: StatsScreenDerivedState,
  dispatch: (HabitsAction) -> Unit
) {
  val state = derived.state

  // Skip for non-Posts mode or Week view (only 1 bar makes no sense)
  if (state.activityMode != ActivityMode.Posts || state.range is ActivityRange.Week) {
    return
  }

  // Show shimmer while loading
  if (derived.isLoadingWeekData && derived.weekData.weeks.isEmpty()) {
    ChartShimmer(derived.useCompactHeight)
    return
  }

  val habitsState = derived.habitsState
  val chartScrollState = rememberScrollState()
  WeeklyBarChart(
    state = WeeklyBarChartState(
      weekData = derived.weekData,
      selectedWeek = habitsState.selectedWeek,
      scrollState = chartScrollState,
      showWeekdayLegend = derived.showWeekdayLegend,
      compactHeight = derived.useCompactHeight
    ),
    onWeekSelected = { week ->
      if (habitsState.selectedWeek?.startDate == week.startDate) {
        dispatch(HabitsAction.ClearSelection)
      } else {
        dispatch(HabitsAction.SelectWeek(week))
      }
    }
  )
}

@Composable
internal fun StatsHabitSections(
  derived: StatsScreenDerivedState,
  dispatch: (HabitsAction) -> Unit
) {
  if (!derived.showHabitSections) {
    return
  }

  // Show shimmers while loading
  if (derived.isLoadingWeekData && derived.weekData.weeks.isEmpty()) {
    derived.currentHabitsConfig.forEach { habit ->
      HabitSectionShimmer(habit.tag, derived.useCompactHeight)
    }
    return
  }

  val habitsState = derived.habitsState
  val onClearSelection = remember(dispatch) { { dispatch(HabitsAction.ClearSelection) } }
  val onEditRequested = remember(dispatch, derived.habitsConfigTimeline) {
    { day: ContributionDay ->
      val config = habitsConfigForDate(derived.habitsConfigTimeline, day.date)?.habits.orEmpty()
      dispatch(HabitsAction.OpenEditor(day, config))
    }
  }

  derived.currentHabitsConfig.forEach { habit ->
    val selectionId = "habit:${habit.tag}"
    val onDaySelected = remember(dispatch, selectionId) {
      { day: ContributionDay -> dispatch(HabitsAction.SelectDay(day, selectionId)) }
    }
    HabitActivitySection(
      state = HabitActivitySectionState(
        habit = habit,
        baseWeekData = derived.weekData,
        selectedDate = if (habitsState.activeSelectionId == selectionId) habitsState.selectedDate else null,
        isActiveSelection = habitsState.activeSelectionId == selectionId,
        showWeekdayLegend = derived.showWeekdayLegend,
        compactHeight = derived.useCompactHeight,
        range = derived.state.range,
        demoMode = derived.state.demoMode,
        today = derived.today,
        habitColor = habit.color
      ),
      onDaySelected = onDaySelected,
      onClearSelection = onClearSelection,
      onEditRequested = onEditRequested,
      onCreateRequested = onEditRequested
    )
  }
}

@Composable
private fun HabitActivitySection(
  state: HabitActivitySectionState,
  onDaySelected: (ContributionDay) -> Unit,
  onClearSelection: () -> Unit,
  onEditRequested: (ContributionDay) -> Unit,
  onCreateRequested: (ContributionDay) -> Unit
) {
  val habitWeekData = remember(state.baseWeekData, state.habit) {
    activityWeekDataForHabit(state.baseWeekData, state.habit)
  }
  val selectedDay = remember(habitWeekData.weeks, state.habit, state.selectedDate) {
    state.selectedDate?.let { date -> findDayByDate(habitWeekData, date) }
  }
  val chartScrollState = rememberScrollState()

  Column(verticalArrangement = Arrangement.spacedBy(Indent.xs), modifier = Modifier.padding(top = Indent.s)) {
    Text(state.habit.localizedLabel(state.demoMode), style = MaterialTheme.typography.titleSmall)
    val showTimeline = state.range is ActivityRange.Quarter || state.range is ActivityRange.Year
    ContributionGrid(
      state = ContributionGridState(
        weekData = habitWeekData,
        range = state.range,
        selectedDay = selectedDay,
        selectedWeekStart = null,
        isActiveSelection = state.isActiveSelection,
        scrollState = chartScrollState,
        showWeekdayLegend = state.showWeekdayLegend,
        showAllWeekdayLabels = true,
        compactHeight = state.compactHeight,
        showTimeline = showTimeline,
        today = state.today,
        habitColor = state.habitColor,
        demoMode = state.demoMode
      ),
      onDaySelected = onDaySelected,
      onClearSelection = onClearSelection,
      onEditRequested = onEditRequested,
      onCreateRequested = onCreateRequested
    )
  }
}

@Suppress("LongMethod", "LongParameterList")
@Composable
private fun LastSevenDaysMatrix(
  days: List<ContributionDay>,
  habits: List<HabitConfig>,
  compactHeight: Boolean,
  demoMode: Boolean,
  today: kotlinx.datetime.LocalDate,
  onHabitClick: (day: ContributionDay, habitTag: String, habitLabel: String) -> Unit = { _, _, _ -> }
) {
  if (days.isEmpty() || habits.isEmpty()) {
    return
  }
  BoxWithConstraints {
    val labelWidth = HABIT_LABEL_WIDTH
    val spacing = ChartDimens.spacing(compactHeight)
    val availableWidth = (maxWidth - labelWidth - spacing).coerceAtLeast(0.dp)
    val layout = calculateLayout(
      maxWidth = availableWidth,
      columns = days.size.coerceAtLeast(1),
      minColumnSize = ChartDimens.minCell(compactHeight),
      spacing = spacing
    )
    val cellSize = layout.columnSize
    Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
      Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
        Spacer(modifier = Modifier.width(labelWidth))
        days.forEach { day ->
          Box(modifier = Modifier.size(cellSize), contentAlignment = Alignment.Center) {
            Text(
              DateFormatter.dayOfWeekShort(day.date.dayOfWeek),
              style = MaterialTheme.typography.labelSmall
            )
          }
        }
      }
      val pendingColor = AppColors.inactiveCell.resolve()
      habits.forEach { habit ->
        Row(
          horizontalArrangement = Arrangement.spacedBy(spacing),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = habit.localizedLabel(demoMode),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(labelWidth)
          )
          days.forEach { day ->
            val done = day.habitStatuses.firstOrNull { status -> status.tag == habit.tag }?.done == true
            val isFuture = day.date > today
            val baseColor = if (done) androidx.compose.ui.graphics.Color(habit.color) else pendingColor
            val cellColor = if (isFuture) baseColor.copy(alpha = 0.3f) else baseColor
            Box(
              modifier = Modifier
                .size(cellSize)
                .background(cellColor, shape = MaterialTheme.shapes.extraSmall)
                .then(
                  if (!isFuture) {
                    Modifier.clickable { onHabitClick(day, habit.tag, habit.label) }
                  } else {
                    Modifier
                  }
                )
            )
          }
        }
      }
      Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
        Spacer(modifier = Modifier.width(labelWidth))
        days.forEach { day ->
          Box(modifier = Modifier.size(cellSize), contentAlignment = Alignment.Center) {
            Text(day.date.day.toString(), style = MaterialTheme.typography.labelSmall)
          }
        }
      }
    }
  }
}
