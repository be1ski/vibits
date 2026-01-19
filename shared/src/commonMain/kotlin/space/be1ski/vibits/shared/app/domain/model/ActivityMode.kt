package space.be1ski.vibits.shared.app.domain.model

/**
 * Activity visualization modes.
 */
enum class ActivityMode {
  /** Habit completion based on #habits/daily + #habits/config. */
  HABITS,

  /** Raw post count per day. */
  POSTS,
}
