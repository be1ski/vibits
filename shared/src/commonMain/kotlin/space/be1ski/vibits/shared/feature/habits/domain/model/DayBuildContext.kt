package space.be1ski.vibits.shared.feature.habits.domain.model

import kotlinx.datetime.LocalDate
import space.be1ski.vibits.shared.core.ui.ActivityMode

/**
 * Context for building a single ContributionDay.
 * Pure data class without UI dependencies.
 */
data class DayBuildContext(
  val date: LocalDate,
  val bounds: RangeBounds,
  val mode: ActivityMode,
  val configTimeline: List<HabitsConfigEntry>,
  val dailyMemos: Map<LocalDate, DailyMemoInfo>,
  val counts: Map<LocalDate, Int>,
  val today: LocalDate,
)
