package space.be1ski.memos.shared.data.local

import android.os.Environment
import space.be1ski.memos.shared.domain.model.app.AppDetails
import java.io.File

/**
 * Android implementation.
 */
actual class AppDetailsProvider {
  actual fun load(): AppDetails {
    if (!AndroidContextHolder.isReady()) {
      return AppDetails(
        version = "unknown",
        environment = "unknown",
        credentialsStore = "SharedPreferences(memos_prefs)",
        memosDatabase = "memos.db",
        offlineStorage = "memos.json"
      )
    }
    val context = AndroidContextHolder.context
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val versionName = packageInfo.versionName ?: "unknown"
    val databasePath = context.getDatabasePath("memos.db").absolutePath
    val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
    val offlinePath = documentsDir?.let { File(it, "memos.json").absolutePath } ?: "memos.json"
    return AppDetails(
      version = versionName,
      environment = "android",
      credentialsStore = "SharedPreferences(memos_prefs)",
      memosDatabase = databasePath,
      offlineStorage = offlinePath
    )
  }
}
