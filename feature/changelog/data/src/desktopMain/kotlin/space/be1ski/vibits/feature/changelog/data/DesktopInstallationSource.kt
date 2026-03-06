package space.be1ski.vibits.feature.changelog.data

import space.be1ski.vibits.feature.changelog.domain.repository.InstallationSource
import java.nio.file.Files
import java.nio.file.Path

class DesktopInstallationSource : InstallationSource {
  override fun isHomebrew(): Boolean =
    try {
      Files.exists(Path.of("/opt/homebrew/Caskroom/vibits")) ||
        Files.exists(Path.of("/usr/local/Caskroom/vibits"))
    } catch (_: Exception) {
      false
    }
}
