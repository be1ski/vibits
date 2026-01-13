package space.be1ski.memos.shared.presentation.screen

import kotlinx.datetime.LocalDate
import space.be1ski.memos.shared.domain.model.memo.Memo
import space.be1ski.memos.shared.presentation.components.ActivityMode
import space.be1ski.memos.shared.presentation.components.ActivityRange
import space.be1ski.memos.shared.presentation.components.ActivityWeek
import space.be1ski.memos.shared.presentation.components.ActivityWeekData
import space.be1ski.memos.shared.presentation.components.ContributionDay
import space.be1ski.memos.shared.presentation.components.DailyMemoInfo
import space.be1ski.memos.shared.presentation.components.HabitConfig
import space.be1ski.memos.shared.presentation.components.HabitsConfigEntry
import kotlinx.datetime.TimeZone
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Public state holder for the stats screen.
 */
data class StatsScreenState(
  val memos: List<Memo>,
  val range: ActivityRange,
  val activityMode: ActivityMode,
  val useVerticalScroll: Boolean = true,
  val isRefreshing: Boolean = false,
  val enablePullRefresh: Boolean = true
)

/**
 * Public callbacks for stats screen events.
 */
data class StatsScreenActions(
  val onEditDailyMemo: (DailyMemoInfo, String) -> Unit = { _, _ -> },
  val onDeleteDailyMemo: (DailyMemoInfo) -> Unit = {},
  val onCreateDailyMemo: (String) -> Unit = {},
  val onRefresh: () -> Unit = {}
)

internal data class HabitActivitySectionState(
  val habit: HabitConfig,
  val baseWeekData: ActivityWeekData,
  val selectedDate: LocalDate?,
  val isActiveSelection: Boolean,
  val showWeekdayLegend: Boolean,
  val compactHeight: Boolean,
  val range: ActivityRange
)

internal data class HabitActivitySectionActions(
  val onDaySelected: (ContributionDay) -> Unit,
  val onClearSelection: () -> Unit,
  val onEditRequested: (ContributionDay) -> Unit,
  val onCreateRequested: (ContributionDay) -> Unit
)

internal class StatsScreenUiState {
  var habitsEditorDay by mutableStateOf<ContributionDay?>(null)
  var habitsEditorConfig by mutableStateOf<List<HabitConfig>>(emptyList())
  var habitsEditorSelections by mutableStateOf<Map<String, Boolean>>(emptyMap())
  var habitsEditorExisting by mutableStateOf<DailyMemoInfo?>(null)
  var habitsEditorError by mutableStateOf<String?>(null)
  var showEmptyDeleteConfirm by mutableStateOf(false)
  var showHabitsConfig by mutableStateOf(false)
  var habitsConfigText by mutableStateOf("")
  var showHabitDetails by mutableStateOf(false)
  var selectedWeek by mutableStateOf<ActivityWeek?>(null)
  var selectedDate by mutableStateOf<LocalDate?>(null)
  var activeSelectionId by mutableStateOf<String?>(null)
}

internal data class StatsScreenDerivedState(
  val state: StatsScreenState,
  val actions: StatsScreenActions,
  val uiState: StatsScreenUiState,
  val habitsConfigTimeline: List<HabitsConfigEntry>,
  val currentHabitsConfig: List<HabitConfig>,
  val weekData: ActivityWeekData,
  val showWeekdayLegend: Boolean,
  val useCompactHeight: Boolean,
  val collapseHabits: Boolean,
  val showLast7DaysMatrix: Boolean,
  val showHabitSections: Boolean,
  val selectedDay: ContributionDay?,
  val todayConfig: List<HabitConfig>,
  val todayDay: ContributionDay?,
  val timeZone: TimeZone
)
