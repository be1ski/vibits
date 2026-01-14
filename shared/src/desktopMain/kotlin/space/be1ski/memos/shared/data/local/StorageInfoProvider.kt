package space.be1ski.memos.shared.data.local

import space.be1ski.memos.shared.domain.model.storage.StorageInfo
import java.io.File
import java.nio.file.Paths

/**
 * Desktop storage info implementation.
 */
actual class StorageInfoProvider {
  actual fun load(): StorageInfo {
    val home = System.getProperty("user.home")
    val offlinePath = Paths.get(home, "Documents", "Memos", "memos.json").toString()
    return StorageInfo(
      environment = DesktopStoragePaths.environmentLabel(),
      credentialsStore = "Preferences(${DesktopStoragePaths.preferencesNode()})",
      memosDatabase = DesktopStoragePaths.databasePath(),
      offlineStorage = offlinePath
    )
  }
}
