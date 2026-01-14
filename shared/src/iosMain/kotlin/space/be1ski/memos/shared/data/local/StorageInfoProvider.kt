package space.be1ski.memos.shared.data.local

import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import space.be1ski.memos.shared.domain.model.storage.StorageInfo

/**
 * iOS storage info implementation.
 */
actual class StorageInfoProvider {
  actual fun load(): StorageInfo {
    val documentsPath = NSSearchPathForDirectoriesInDomains(
      NSDocumentDirectory,
      NSUserDomainMask,
      true
    ).firstOrNull() as? String ?: ""
    return StorageInfo(
      environment = "ios",
      credentialsStore = "NSUserDefaults(base_url, token)",
      memosDatabase = "$documentsPath/memos.db",
      offlineStorage = "$documentsPath/memos.json"
    )
  }
}
