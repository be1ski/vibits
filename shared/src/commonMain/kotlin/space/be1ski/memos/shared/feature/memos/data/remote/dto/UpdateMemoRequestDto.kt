package space.be1ski.memos.shared.feature.memos.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Request payload for updating memo content.
 */
@Serializable
data class UpdateMemoRequestDto(
  /**
   * Updated memo content.
   */
  val content: String
)
