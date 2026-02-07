package space.be1ski.vibits.feature.habits.domain.model

/**
 * Habit configuration entry.
 */
data class HabitConfig(
  /** Habit tag, e.g. #habits/зарядка. */
  val tag: String,
  /** User-friendly label. */
  val label: String,
  /** Habit color as ARGB Long. */
  val color: Long = DefaultHabitColor,
)
