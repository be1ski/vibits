package space.be1ski.memos.shared.data.local

import space.be1ski.memos.shared.domain.model.storage.StorageInfo

/**
 * Android storage info implementation.
 */
actual class StorageInfoProvider {
  actual fun load(): StorageInfo {
    if (!AndroidContextHolder.isReady()) {
      return StorageInfo(
        environment = "unknown",
        credentialsStore = "SharedPreferences(memos_prefs)",
        memosDatabase = "memos.db"
      )
    }
    val context = AndroidContextHolder.context
    val databasePath = context.getDatabasePath("memos.db").absolutePath
    return StorageInfo(
      environment = "android",
      credentialsStore = "SharedPreferences(memos_prefs)",
      memosDatabase = databasePath
    )
  }
}
