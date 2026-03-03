package space.be1ski.vibits.core.platform.env

import space.be1ski.vibits.core.platform.app.DesktopStoragePaths
import space.be1ski.vibits.core.platform.logging.LogLevel
import space.be1ski.vibits.core.platform.logging.platformLog
import java.io.File
import java.util.Properties

private const val TAG = "LocalConfig"

actual fun createLocalConfigProvider(): LocalConfigProvider {
  val appDataConfigFile = File(DesktopStoragePaths.localConfigPath())
  val appDataProperties = Properties()
  if (appDataConfigFile.exists()) {
    appDataConfigFile.inputStream().use { appDataProperties.load(it) }
    platformLog(LogLevel.INFO, TAG, "Loaded ${appDataProperties.size} keys from $appDataConfigFile")
  } else {
    platformLog(LogLevel.DEBUG, TAG, "App data config not found: $appDataConfigFile")
  }

  val localPropertiesFile = File("../local.properties")
  val localProperties = Properties()
  if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
    platformLog(LogLevel.INFO, TAG, "Loaded ${localProperties.size} keys from $localPropertiesFile")
  } else {
    platformLog(LogLevel.DEBUG, TAG, "Local properties not found: $localPropertiesFile")
  }

  return LocalConfigProvider { key ->
    // 1. Try app data dir config (~/Library/Application Support/Memos-prod/local.properties)
    appDataProperties.getProperty(key)?.also {
      platformLog(LogLevel.DEBUG, TAG, "Resolved '$key' from app data config")
    }
      // 2. Try relative local.properties (../local.properties — dev/Gradle only)
      ?: localProperties.getProperty(key)?.also {
        platformLog(LogLevel.DEBUG, TAG, "Resolved '$key' from local.properties")
      }
      // 3. Try ENV with dot notation (memos.baseUrl)
      ?: System.getenv(key)?.also {
        platformLog(LogLevel.DEBUG, TAG, "Resolved '$key' from env")
      }
      // 4. Try ENV with uppercase snake_case (MEMOS_BASE_URL)
      ?: System.getenv(key.replace('.', '_').uppercase())?.also {
        platformLog(LogLevel.DEBUG, TAG, "Resolved '$key' from env (UPPER_SNAKE)")
      }
  }
}
