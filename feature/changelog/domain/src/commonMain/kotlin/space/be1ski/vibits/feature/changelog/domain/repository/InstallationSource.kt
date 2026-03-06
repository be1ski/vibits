package space.be1ski.vibits.feature.changelog.domain.repository

fun interface InstallationSource {
  fun isHomebrew(): Boolean
}
