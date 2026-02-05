package space.be1ski.vibits.feature.memos.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateMemoRequestDto(
  val content: String,
)
