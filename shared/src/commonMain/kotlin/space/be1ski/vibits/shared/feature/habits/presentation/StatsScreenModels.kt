package space.be1ski.vibits.shared.feature.habits.presentation

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import space.be1ski.vibits.shared.core.ui.ActivityMode
import space.be1ski.vibits.shared.core.ui.ActivityRange
import space.be1ski.vibits.shared.feature.habits.domain.model.ActivityWeekData
import space.be1ski.vibits.shared.feature.habits.domain.model.ContributionDay
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitConfig
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitsConfigEntry
import space.be1ski.vibits.shared.feature.habits.domain.model.SuccessRateData
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo

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
  val demoMode: Boolean,
  val today: LocalDate? = null,
  val habitColor: Long? = null
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
  val today: LocalDate,
  val timeZone: TimeZone,
  val successRateData: SuccessRateData?
)
