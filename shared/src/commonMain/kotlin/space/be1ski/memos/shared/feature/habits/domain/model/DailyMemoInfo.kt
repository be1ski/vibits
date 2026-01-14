package space.be1ski.memos.shared.feature.habits.domain.model

/**
 * Daily memo metadata for editing.
 */
data class DailyMemoInfo(
  /** Memo resource name. */
  val name: String,
  /** Memo content. */
  val content: String
)
