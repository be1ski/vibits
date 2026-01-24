package space.be1ski.vibits.shared.feature.habits.domain.model

import kotlinx.datetime.LocalDate

/**
 * Streak tracking result.
 */
data class StreakData(
  val current: Int,
  val best: Int,
  val currentStreakStart: LocalDate?,
)

/**
 * Per-habit streak tracking result.
 */
data class HabitStreakData(
  val habitTag: String,
  val current: Int,
  val best: Int,
  val currentStreakStart: LocalDate?,
)
