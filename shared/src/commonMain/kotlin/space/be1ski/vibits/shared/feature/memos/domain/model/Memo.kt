package space.be1ski.vibits.shared.feature.memos.domain.model

data class Memo(
  val name: String = "",
  val content: String = "",
  val createTime: kotlin.time.Instant? = null,
  val updateTime: kotlin.time.Instant? = null,
)
