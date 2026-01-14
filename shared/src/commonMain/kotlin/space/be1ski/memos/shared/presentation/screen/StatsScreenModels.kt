package space.be1ski.memos.shared.presentation.screen

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import space.be1ski.memos.shared.domain.model.memo.Memo
import space.be1ski.memos.shared.presentation.components.ActivityMode
import space.be1ski.memos.shared.presentation.components.ActivityRange
import space.be1ski.memos.shared.presentation.components.ActivityWeekData
import space.be1ski.memos.shared.presentation.components.ContributionDay
import space.be1ski.memos.shared.presentation.components.HabitConfig
import space.be1ski.memos.shared.presentation.components.HabitsConfigEntry
import space.be1ski.memos.shared.presentation.habits.HabitsAction
import space.be1ski.memos.shared.presentation.habits.HabitsState

/**
 * Public state holder for the stats screen.
 */
data class StatsScreenState(
  val memos: List<Memo>,
  val range: ActivityRange,
  val activityMode: ActivityMode,
  val useVerticalScroll: Boolean = true,
  val isRefreshing: Boolean = false,
  val enablePullRefresh: Boolean = true,
  val demoMode: Boolean = false
)

internal data class HabitActivitySectionState(
  val habit: HabitConfig,
  val baseWeekData: ActivityWeekData,
  val selectedDate: LocalDate?,
  val isActiveSelection: Boolean,
  val showWeekdayLegend: Boolean,
  val compactHeight: Boolean,
  val range: ActivityRange,
  val demoMode: Boolean
)

internal data class HabitActivitySectionActions(
  val onDaySelected: (ContributionDay) -> Unit,
  val onClearSelection: () -> Unit,
  val onEditRequested: (ContributionDay) -> Unit,
  val onCreateRequested: (ContributionDay) -> Unit
)

internal data class StatsScreenDerivedState(
  val state: StatsScreenState,
  val habitsState: HabitsState,
  val dispatch: (HabitsAction) -> Unit,
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
