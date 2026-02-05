package space.be1ski.vibits.feature.memos.data.offline

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OfflineMemoDto(
  val name: String = "",
  val content: String = "",
  @SerialName("createTime") val createTime: String? = null,
  @SerialName("updateTime") val updateTime: String? = null,
)

@Serializable
data class OfflineMemosFileDto(
  val memos: List<OfflineMemoDto> = emptyList(),
)
