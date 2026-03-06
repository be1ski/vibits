package space.be1ski.vibits.feature.changelog.data

import space.be1ski.vibits.feature.changelog.domain.repository.InstallationSource

class DefaultInstallationSource : InstallationSource {
  override fun isHomebrew(): Boolean = false
}
