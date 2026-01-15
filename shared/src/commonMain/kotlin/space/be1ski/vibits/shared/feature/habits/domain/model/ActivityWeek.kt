package space.be1ski.vibits.shared.feature.habits.domain.model

import kotlinx.datetime.LocalDate

/**
 * Aggregated week data with daily breakdown.
 */
data class ActivityWeek(
  /** Start date of the week (Monday). */
  val startDate: LocalDate,
  /** Daily breakdown for the week. */
  val days: List<ContributionDay>,
  /** Sum of all posts in the week. */
  val weeklyCount: Int
)
