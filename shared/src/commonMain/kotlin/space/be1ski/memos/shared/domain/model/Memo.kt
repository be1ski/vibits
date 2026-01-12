package space.be1ski.memos.shared.domain.model

/**
 * Domain model representing a memo.
 */
data class Memo(
  /**
   * API resource name.
   */
  val name: String = "",
  /**
   * Memo content.
   */
  val content: String = "",
  /**
   * Timestamp when memo was created.
   */
  val createTime: kotlin.time.Instant? = null,
  /**
   * Timestamp when memo was last updated.
   */
  val updateTime: kotlin.time.Instant? = null
)
