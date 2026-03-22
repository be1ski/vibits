package space.be1ski.vibits.feature.changelog.domain.model

data class ChangelogEntry(
  val version: String,
  val title: String,
  val body: String,
  val date: String,
  val hasDmgAsset: Boolean = false,
)
