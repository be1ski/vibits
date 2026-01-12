package space.be1ski.memos.shared.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** API memo model used across the app. */
@Serializable
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
  @SerialName("createTime") val createTime: String? = null,
  /**
   * ISO timestamp when memo was last updated.
   */
  @SerialName("updateTime") val updateTime: String? = null
)

/** Response payload for the memos list API. */
@Serializable
data class ListMemosResponse(
  /**
   * Returned memos.
   */
  val memos: List<Memo> = emptyList(),
  /**
   * Token for the next page, if available.
   */
  @SerialName("nextPageToken") val nextPageToken: String? = null
)
