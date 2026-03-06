package space.be1ski.vibits.feature.changelog.data.platform

import space.be1ski.vibits.feature.changelog.data.DesktopInstallationSource
import space.be1ski.vibits.feature.changelog.domain.repository.InstallationSource

actual fun createInstallationSource(): InstallationSource = DesktopInstallationSource()
