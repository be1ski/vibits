package space.be1ski.memos.shared.data.local

import space.be1ski.memos.shared.domain.model.storage.StorageInfo

/**
 * Desktop storage info implementation.
 */
actual class StorageInfoProvider {
  actual fun load(): StorageInfo = StorageInfo(
    environment = DesktopStoragePaths.environmentLabel(),
    credentialsStore = "Preferences(${DesktopStoragePaths.preferencesNode()})",
    memosDatabase = DesktopStoragePaths.databasePath()
  )
}
