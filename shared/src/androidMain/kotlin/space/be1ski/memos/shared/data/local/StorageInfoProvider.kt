package space.be1ski.memos.shared.data.local

import android.os.Environment
import space.be1ski.memos.shared.domain.model.storage.StorageInfo
import java.io.File

/**
 * Android storage info implementation.
 */
actual class StorageInfoProvider {
  actual fun load(): StorageInfo {
    if (!AndroidContextHolder.isReady()) {
      return StorageInfo(
        environment = "unknown",
        credentialsStore = "SharedPreferences(memos_prefs)",
        memosDatabase = "memos.db",
        offlineStorage = "memos.json"
      )
    }
    val context = AndroidContextHolder.context
    val databasePath = context.getDatabasePath("memos.db").absolutePath
    val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
    val offlinePath = documentsDir?.let { File(it, "memos.json").absolutePath } ?: "memos.json"
    return StorageInfo(
      environment = "android",
      credentialsStore = "SharedPreferences(memos_prefs)",
      memosDatabase = databasePath,
      offlineStorage = offlinePath
    )
  }
}
