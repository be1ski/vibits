package space.be1ski.vibits.feature.memos.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ListMemosResponseDto(
  val memos: List<MemoDto> = emptyList(),
  @SerialName("nextPageToken") val nextPageToken: String? = null,
)
