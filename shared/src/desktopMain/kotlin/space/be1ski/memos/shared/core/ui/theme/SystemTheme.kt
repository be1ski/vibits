package space.be1ski.memos.shared.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import java.awt.Toolkit

private const val THEME_POLL_INTERVAL_MS = 1000L

@Composable
actual fun rememberSystemDarkTheme(): Boolean {
  var isDark by remember { mutableStateOf(isSystemInDarkThemeDesktop()) }

  LaunchedEffect(Unit) {
    while (true) {
      delay(THEME_POLL_INTERVAL_MS)
      val currentIsDark = isSystemInDarkThemeDesktop()
      if (currentIsDark != isDark) {
        isDark = currentIsDark
      }
    }
  }

  return isDark
}

private fun isSystemInDarkThemeDesktop(): Boolean {
  val osName = System.getProperty("os.name", "").lowercase()
  return when {
    osName.contains("mac") -> isMacOsDarkTheme()
    osName.contains("windows") -> isWindowsDarkTheme()
    else -> isLinuxDarkTheme()
  }
}

private fun isMacOsDarkTheme(): Boolean {
  return try {
    val process = Runtime.getRuntime().exec(
      arrayOf("defaults", "read", "-g", "AppleInterfaceStyle")
    )
    val result = process.inputStream.bufferedReader().readText().trim()
    process.waitFor()
    result.equals("Dark", ignoreCase = true)
  } catch (_: Exception) {
    false
  }
}

private fun isWindowsDarkTheme(): Boolean {
  return try {
    val process = Runtime.getRuntime().exec(
      arrayOf(
        "reg", "query",
        "HKCU\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
        "/v", "AppsUseLightTheme"
      )
    )
    val result = process.inputStream.bufferedReader().readText()
    process.waitFor()
    // AppsUseLightTheme = 0 means dark mode
    result.contains("0x0")
  } catch (_: Exception) {
    false
  }
}

private fun isLinuxDarkTheme(): Boolean {
  return try {
    // Try GTK theme detection
    val gtkTheme = System.getenv("GTK_THEME") ?: ""
    if (gtkTheme.lowercase().contains("dark")) {
      return true
    }

    // Try gsettings for GNOME
    val process = Runtime.getRuntime().exec(
      arrayOf("gsettings", "get", "org.gnome.desktop.interface", "color-scheme")
    )
    val result = process.inputStream.bufferedReader().readText().trim()
    process.waitFor()
    result.contains("dark")
  } catch (_: Exception) {
    false
  }
}
