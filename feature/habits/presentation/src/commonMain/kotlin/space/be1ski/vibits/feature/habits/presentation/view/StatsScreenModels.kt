package space.be1ski.vibits.feature.habits.presentation.view
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import space.be1ski.vibits.core.ui.date.DateFormatter
import space.be1ski.vibits.feature.habits.domain.model.ActivityMode
import space.be1ski.vibits.feature.habits.domain.model.ActivityRange
import space.be1ski.vibits.feature.habits.domain.model.ActivitySummary
import space.be1ski.vibits.feature.habits.domain.model.ContributionDay
import space.be1ski.vibits.feature.habits.domain.model.HabitColor
import space.be1ski.vibits.feature.habits.domain.model.HabitConfig
import space.be1ski.vibits.feature.habits.domain.model.HabitsConfigEntry
import space.be1ski.vibits.feature.habits.domain.model.SuccessRate
import space.be1ski.vibits.feature.habits.presentation.state.HabitsState
import space.be1ski.vibits.feature.memos.domain.model.Memo

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
  val demoMode: Boolean = false,
  val postsListExpanded: Boolean = false,
  val wideLayout: Boolean = false,
  val selectedHabitTag: String? = null,
)

internal data class HabitActivitySectionState(
  val habit: HabitConfig,
  val baseWeekData: ActivitySummary,
  val selectedDate: LocalDate?,
  val isActiveSelection: Boolean,
  val showWeekdayLegend: Boolean,
  val compactHeight: Boolean,
  val range: ActivityRange,
  val demoMode: Boolean,
  val today: LocalDate? = null,
  val habitColor: HabitColor? = null,
)

@Suppress("LongParameterList")
internal data class StatsScreenDerivedState(
  val state: StatsScreenState,
  val habitsState: HabitsState,
  val habitsConfigTimeline: List<HabitsConfigEntry>,
  val currentHabitsConfig: List<HabitConfig>,
  val weekData: ActivitySummary,
  val isLoadingWeekData: Boolean,
  val showWeekdayLegend: Boolean,
  val useCompactHeight: Boolean,
  val collapseHabits: Boolean,
  val showLast7DaysMatrix: Boolean,
  val showHabitSections: Boolean,
  val useHabitPicker: Boolean,
  val selectedDay: ContributionDay?,
  val todayConfig: List<HabitConfig>,
  val todayDay: ContributionDay?,
  val today: LocalDate,
  val timeZone: TimeZone,
  val successRate: SuccessRate?,
  val periodPosts: List<Memo>,
  val dateFormatter: DateFormatter,
  /** Date when habits were first configured (null if no config). */
  val configStartDate: LocalDate? = null,
)
