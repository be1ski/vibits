package space.be1ski.vibits.shared.data.local

import platform.Foundation.NSBundle
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import space.be1ski.vibits.shared.domain.model.app.AppDetails

/**
 * iOS implementation.
 */
actual class AppDetailsProvider {
  actual fun load(): AppDetails {
    val documentsPath = NSSearchPathForDirectoriesInDomains(
      NSDocumentDirectory,
      NSUserDomainMask,
      true
    ).firstOrNull() as? String ?: ""
    val version = NSBundle.mainBundle.infoDictionary
      ?.get("CFBundleShortVersionString") as? String ?: "unknown"
    return AppDetails(
      version = version,
      environment = "ios",
      credentialsStore = "NSUserDefaults(base_url, token)",
      memosDatabase = "$documentsPath/memos.db",
      offlineStorage = "$documentsPath/memos.json"
    )
  }
}
