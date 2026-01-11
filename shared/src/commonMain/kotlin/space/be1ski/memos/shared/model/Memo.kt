package space.be1ski.memos.shared.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Memo(
  val name: String = "",
  val content: String = "",
  @SerialName("createTime") val createTime: String? = null,
  @SerialName("updateTime") val updateTime: String? = null
)

@Serializable
data class ListMemosResponse(
  val memos: List<Memo> = emptyList()
)
