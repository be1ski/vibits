package space.be1ski.vibits.shared.data.local

import space.be1ski.vibits.shared.domain.model.app.AppDetails
import java.nio.file.Paths

/**
 * Desktop implementation.
 */
actual class AppDetailsProvider {
  actual fun load(): AppDetails {
    val home = System.getProperty("user.home")
    val offlinePath = Paths.get(home, "Documents", "Vibits", "memos.json").toString()
    return AppDetails(
      version = DesktopStoragePaths.appVersion(),
      environment = DesktopStoragePaths.environmentLabel(),
      credentialsStore = "Preferences(${DesktopStoragePaths.preferencesNode()})",
      memosDatabase = DesktopStoragePaths.databasePath(),
      offlineStorage = offlinePath,
    )
  }
}
