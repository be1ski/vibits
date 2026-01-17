package space.be1ski.vibits.shared.feature.memos.data.offline

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for a single memo stored offline.
 * Format matches Memos API for future export/import compatibility.
 */
@Serializable
data class OfflineMemoDto(
  val name: String = "",
  val content: String = "",
  @SerialName("createTime") val createTime: String? = null,
  @SerialName("updateTime") val updateTime: String? = null,
)

/**
 * Root DTO for the offline memos JSON file.
 */
@Serializable
data class OfflineMemosFileDto(
  val memos: List<OfflineMemoDto> = emptyList(),
)
