package space.be1ski.vibits.shared.core.platform.env

import java.io.File
import java.util.Properties

actual fun createLocalConfigProvider(): LocalConfigProvider {
  val properties = Properties()
  val localPropertiesFile = File("../local.properties")
  if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { properties.load(it) }
  }

  return LocalConfigProvider { key ->
    // 1. Try local.properties with dot notation (memos.baseUrl)
    properties.getProperty(key)
      // 2. Try ENV with dot notation (memos.baseUrl)
      ?: System.getenv(key)
      // 3. Try ENV with uppercase snake_case (MEMOS_BASE_URL)
      ?: System.getenv(key.replace('.', '_').uppercase())
  }
}
