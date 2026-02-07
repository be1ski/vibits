@file:Suppress("MagicNumber")

package space.be1ski.vibits.feature.habits.domain.model

/** Material Green 500 — default color when none is specified in config. */
const val DEFAULT_HABIT_COLOR: Long = 0xFF4CAF50L

/**
 * Habit configuration entry.
 */
data class HabitConfig(
  /** Habit tag, e.g. #habits/зарядка. */
  val tag: String,
  /** User-friendly label. */
  val label: String,
  /** Habit color as ARGB Long. */
  val color: Long = DEFAULT_HABIT_COLOR,
)
