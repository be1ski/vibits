@file:Suppress("MagicNumber")

package space.be1ski.vibits.shared.feature.habits.domain.model

/** Default habit color (Material Green 500). */
const val DEFAULT_HABIT_COLOR = 0xFF4CAF50L

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
