package space.be1ski.vibits.core.platform.app

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

object DesktopStoragePaths {
  private const val APP_NAME = "Memos"
  private const val APP_ID = "space.be1ski.vibits"
  private const val ENVIRONMENT_PROPERTY = "memos.env"
  private const val VERSION_PROPERTY = "memos.version"

  fun appVersion(): String =
    System.getProperty(VERSION_PROPERTY)?.takeIf { it.isNotBlank() }
      ?: DesktopStoragePaths::class.java.`package`?.implementationVersion
      ?: "dev"

  fun preferencesNode(): String {
    val env = environmentSuffix()
    return if (env.isBlank()) APP_ID else "$APP_ID.$env"
  }

  fun databasePath(): String = appDataDir().resolve("memos.db").toString()

  fun environmentLabel(): String = environmentSuffix().ifBlank { "prod" }

  private fun appDataDir(): Path {
    val osName = System.getProperty("os.name").lowercase()
    val home = System.getProperty("user.home")
    val baseDir =
      when {
        osName.contains("mac") -> Paths.get(home, "Library", "Application Support")
        osName.contains("win") -> {
          val appData = System.getenv("APPDATA")?.takeIf { it.isNotBlank() }
          if (appData != null) Paths.get(appData) else Paths.get(home, "AppData", "Roaming")
        }
        else -> {
          val xdg = System.getenv("XDG_DATA_HOME")?.takeIf { it.isNotBlank() }
          if (xdg != null) Paths.get(xdg) else Paths.get(home, ".local", "share")
        }
      }
    val env = environmentSuffix().ifBlank { "prod" }
    val target = baseDir.resolve("$APP_NAME-$env")
    runCatching { Files.createDirectories(target) }
    return target
  }

  private fun environmentSuffix(): String =
    System
      .getProperty(ENVIRONMENT_PROPERTY)
      ?.trim()
      ?.lowercase()
      ?.takeIf { it.isNotBlank() }
      .orEmpty()
}
