package space.be1ski.vibits.feature.changelog.domain.model

data class UpdateAvailability(
  val latestVersion: String,
  val currentVersion: String,
  val isHomebrewInstallation: Boolean,
)
