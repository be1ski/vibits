package space.be1ski.vibits.shared.feature.habits.domain.model

import kotlinx.datetime.LocalDate

/**
 * Per-day activity entry.
 */
data class ContributionDay(
  /** Calendar date for the entry. */
  val date: LocalDate,
  /** Number of completed habits (or posts when habits are unavailable). */
  val count: Int,
  /** Total habits configured for the day. */
  val totalHabits: Int,
  /** Completion ratio in range [0..1]. */
  val completionRatio: Float,
  /** Parsed habit status list for the day. */
  val habitStatuses: List<HabitStatus>,
  /** Daily memo information when available. */
  val dailyMemo: DailyMemoInfo?,
  /** True if the day is within the requested range. */
  val inRange: Boolean,
  /** True if the day can be clicked to toggle habits (not future, not before config). */
  val isClickable: Boolean = true,
)
