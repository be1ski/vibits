package space.be1ski.vibits.feature.habits.domain.model

/**
 * Daily memo metadata for editing.
 */
data class DailyMemo(
  /** Memo resource name. */
  val name: String,
  /** Memo content. */
  val content: String,
)
