@file:Suppress("MagicNumber")

package space.be1ski.vibits.shared.feature.habits.domain.model

/** Default habit color (Material Green 500). */
const val DEFAULT_HABIT_COLOR = 0xFF4CAF50L

/** Predefined habit colors (Material Design palette). */
val HABIT_COLORS =
  listOf(
    0xFF4CAF50L, // Green
    0xFF2196F3L, // Blue
    0xFFF44336L, // Red
    0xFFFF9800L, // Orange
    0xFF9C27B0L, // Purple
    0xFF00BCD4L, // Cyan
    0xFFE91E63L, // Pink
    0xFF795548L, // Brown
    0xFF607D8BL, // Blue Grey
    0xFFFFEB3BL, // Yellow
  )

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
