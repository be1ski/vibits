package space.be1ski.vibits.shared.app.domain.model

import space.be1ski.vibits.shared.feature.memos.domain.model.PostTags

/**
 * Activity visualization modes.
 */
enum class ActivityMode {
  /** Habit completion based on PostTags.HABITS_DAILY + PostTags.HABITS_CONFIG. */
  HABITS,

  /** Raw post count per day. */
  POSTS,
}
