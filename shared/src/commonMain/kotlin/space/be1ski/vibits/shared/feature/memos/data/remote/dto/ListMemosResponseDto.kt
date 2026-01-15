package space.be1ski.vibits.shared.feature.memos.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response payload for the memos list API.
 */
@Serializable
data class ListMemosResponseDto(
  /**
   * Returned memos.
   */
  val memos: List<MemoDto> = emptyList(),
  /**
   * Token for the next page, if available.
   */
  @SerialName("nextPageToken") val nextPageToken: String? = null
)
