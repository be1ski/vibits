package space.be1ski.vibits.feature.memos.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MemoDto(
  val name: String = "",
  val content: String = "",
  @SerialName("createTime") val createTime: String? = null,
  @SerialName("updateTime") val updateTime: String? = null,
)
