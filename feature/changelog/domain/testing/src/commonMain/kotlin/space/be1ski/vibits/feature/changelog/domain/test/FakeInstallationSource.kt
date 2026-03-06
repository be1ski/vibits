package space.be1ski.vibits.feature.changelog.domain.test

import space.be1ski.vibits.feature.changelog.domain.repository.InstallationSource

class FakeInstallationSource(
  private val homebrew: Boolean = false,
) : InstallationSource {
  override fun isHomebrew(): Boolean = homebrew
}
