package space.be1ski.memos.shared.feature.memos.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Request payload for creating a memo.
 */
@Serializable
data class CreateMemoRequestDto(
  /**
   * Memo content.
   */
  val content: String
)
