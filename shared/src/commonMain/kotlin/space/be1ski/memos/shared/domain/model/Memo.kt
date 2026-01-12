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
   * ISO timestamp when memo was created.
   */
  val createTime: String? = null,
  /**
   * ISO timestamp when memo was last updated.
   */
  val updateTime: String? = null
)
