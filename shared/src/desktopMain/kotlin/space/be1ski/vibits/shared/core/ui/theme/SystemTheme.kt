package space.be1ski.vibits.shared.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private const val THEME_POLL_INTERVAL_MS = 2000L

@Composable
actual fun rememberSystemDarkTheme(): Boolean {
  var isDark by remember { mutableStateOf(isSystemInDarkThemeDesktopCached()) }

  LaunchedEffect(Unit) {
    while (true) {
      delay(THEME_POLL_INTERVAL_MS)
      val currentIsDark =
        withContext(Dispatchers.IO) {
          isSystemInDarkThemeDesktop()
        }
      if (currentIsDark != isDark) {
        isDark = currentIsDark
      }
    }
  }

  return isDark
}

// Cached value for initial composition (avoid blocking UI)
private var cachedDarkTheme: Boolean? = null

private fun isSystemInDarkThemeDesktopCached(): Boolean = cachedDarkTheme ?: isSystemInDarkThemeDesktop().also { cachedDarkTheme = it }

private fun isSystemInDarkThemeDesktop(): Boolean {
  val osName = System.getProperty("os.name", "").lowercase()
  return when {
    osName.contains("mac") -> isMacOsDarkTheme()
    osName.contains("windows") -> isWindowsDarkTheme()
    else -> isLinuxDarkTheme()
  }
}

private fun isMacOsDarkTheme(): Boolean =
  try {
    val process =
      Runtime.getRuntime().exec(
        arrayOf("defaults", "read", "-g", "AppleInterfaceStyle"),
      )
    val result =
      process.inputStream
        .bufferedReader()
        .readText()
        .trim()
    process.waitFor()
    result.equals("Dark", ignoreCase = true)
  } catch (_: Exception) {
    false
  }

private fun isWindowsDarkTheme(): Boolean =
  try {
    val process =
      Runtime.getRuntime().exec(
        arrayOf(
          "reg",
          "query",
          "HKCU\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
          "/v",
          "AppsUseLightTheme",
        ),
      )
    val result = process.inputStream.bufferedReader().readText()
    process.waitFor()
    // AppsUseLightTheme = 0 means dark mode
    result.contains("0x0")
  } catch (_: Exception) {
    false
  }

private fun isLinuxDarkTheme(): Boolean {
  return try {
    // Try GTK theme detection
    val gtkTheme = System.getenv("GTK_THEME") ?: ""
    if (gtkTheme.lowercase().contains("dark")) {
      return true
    }

    // Try gsettings for GNOME
    val process =
      Runtime.getRuntime().exec(
        arrayOf("gsettings", "get", "org.gnome.desktop.interface", "color-scheme"),
      )
    val result =
      process.inputStream
        .bufferedReader()
        .readText()
        .trim()
    process.waitFor()
    result.contains("dark")
  } catch (_: Exception) {
    false
  }
}
