package space.be1ski.memos.shared.feature.memos.domain.model

/**
 * Domain model for a memo record in the app.
 */
data class Memo(
  val name: String = "",
  val content: String = "",
  val createTime: kotlin.time.Instant? = null,
  val updateTime: kotlin.time.Instant? = null
)
