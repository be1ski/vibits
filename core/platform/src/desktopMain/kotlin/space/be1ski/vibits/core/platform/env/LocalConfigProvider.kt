package space.be1ski.vibits.core.platform.env

import space.be1ski.vibits.core.platform.app.DesktopStoragePaths
import java.io.File
import java.util.Properties

actual fun createLocalConfigProvider(): LocalConfigProvider {
  val appDataProperties = Properties()
  val appDataConfigFile = File(DesktopStoragePaths.localConfigPath())
  if (appDataConfigFile.exists()) {
    appDataConfigFile.inputStream().use { appDataProperties.load(it) }
  }

  val localProperties = Properties()
  val localPropertiesFile = File("../local.properties")
  if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
  }

  return LocalConfigProvider { key ->
    // 1. Try app data dir config (~/Library/Application Support/Memos-prod/local.properties)
    appDataProperties.getProperty(key)
      // 2. Try relative local.properties (../local.properties — dev/Gradle only)
      ?: localProperties.getProperty(key)
      // 3. Try ENV with dot notation (memos.baseUrl)
      ?: System.getenv(key)
      // 4. Try ENV with uppercase snake_case (MEMOS_BASE_URL)
      ?: System.getenv(key.replace('.', '_').uppercase())
  }
}
