package space.be1ski.vibits.feature.changelog.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubAssetDto(
  val name: String = "",
)

@Serializable
data class GitHubReleaseDto(
  @SerialName("tag_name") val tagName: String = "",
  val name: String = "",
  val body: String = "",
  @SerialName("published_at") val publishedAt: String = "",
  val assets: List<GitHubAssetDto> = emptyList(),
)
