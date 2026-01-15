package space.be1ski.memos.shared.data.local

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Desktop paths for preferences and databases.
 */
object DesktopStoragePaths {
  private const val appName = "Memos"
  private const val appId = "space.be1ski.memos"
  private const val environmentProperty = "memos.env"
  private const val versionProperty = "memos.version"

  /**
   * Returns the application version.
   */
  fun appVersion(): String =
    System.getProperty(versionProperty)?.takeIf { it.isNotBlank() }
      ?: DesktopStoragePaths::class.java.`package`?.implementationVersion
      ?: "dev"

  /**
   * Returns the preferences node name with an optional environment suffix.
   */
  fun preferencesNode(): String {
    val env = environmentSuffix()
    return if (env.isBlank()) appId else "$appId.$env"
  }

  /**
   * Returns the full path to the memo database file.
   */
  fun databasePath(): String = appDataDir().resolve("memos.db").toString()

  /**
   * Returns the current environment label.
   */
  fun environmentLabel(): String = environmentSuffix().ifBlank { "prod" }

  private fun appDataDir(): Path {
    val osName = System.getProperty("os.name").lowercase()
    val home = System.getProperty("user.home")
    val baseDir = when {
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
    val target = baseDir.resolve("$appName-$env")
    runCatching { Files.createDirectories(target) }
    return target
  }

  private fun environmentSuffix(): String =
    System.getProperty(environmentProperty)
      ?.trim()
      ?.lowercase()
      ?.takeIf { it.isNotBlank() }
      .orEmpty()
}
