package space.be1ski.vibits.feature.habits.domain.model

import kotlinx.datetime.LocalDate

/**
 * Context for building a single ContributionDay.
 * Pure data class without UI dependencies.
 */
data class DayBuildContext(
  val date: LocalDate,
  val bounds: RangeBounds,
  val mode: ActivityMode,
  val configTimeline: List<HabitsConfigEntry>,
  val dailyMemos: Map<LocalDate, DailyMemo>,
  val counts: Map<LocalDate, Int>,
  val today: LocalDate,
)
