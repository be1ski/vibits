package space.be1ski.vibits.shared.feature.memos.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Network DTO describing a memo as returned by the Memos API.
 */
@Serializable
data class MemoDto(
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
  @SerialName("updateTime") val updateTime: String? = null,
)
