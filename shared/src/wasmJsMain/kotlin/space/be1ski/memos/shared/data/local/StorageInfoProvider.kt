package space.be1ski.memos.shared.data.local

import space.be1ski.memos.shared.domain.model.storage.StorageInfo

/**
 * Web storage info implementation.
 */
actual class StorageInfoProvider {
  actual fun load(): StorageInfo = StorageInfo(
    environment = "web",
    credentialsStore = "in-memory",
    memosDatabase = "in-memory"
  )
}
