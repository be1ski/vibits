package space.be1ski.memos.shared.data.local

import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import space.be1ski.memos.shared.domain.model.storage.StorageInfo

/**
 * iOS storage info implementation.
 */
actual class StorageInfoProvider {
  actual fun load(): StorageInfo {
    val basePath = NSSearchPathForDirectoriesInDomains(
      NSApplicationSupportDirectory,
      NSUserDomainMask,
      true
    ).firstOrNull()
    val databasePath = basePath?.let { "$it/memos.db" } ?: "memos.db"
    return StorageInfo(
      environment = "ios",
      credentialsStore = "NSUserDefaults(base_url, token)",
      memosDatabase = databasePath
    )
  }
}
