package space.be1ski.vibits.shared.feature.habits.domain.model

/**
 * Habit completion status for a single habit tag.
 */
data class HabitStatus(
  /** Habit tag, e.g. #habits/зарядка. */
  val tag: String,
  /** Habit label for display. */
  val label: String,
  /** True when the habit is marked completed in a daily memo. */
  val done: Boolean
)
