package space.be1ski.vibits.shared.feature.auth.data

import java.util.prefs.Preferences
import space.be1ski.vibits.shared.data.local.DesktopStoragePaths

/**
 * Desktop implementation backed by Preferences API.
 */
actual class CredentialsStore {
  private val prefs = Preferences.userRoot().node(DesktopStoragePaths.preferencesNode())

  /**
   * Loads credentials or returns empty values when not available.
   */
  actual fun load(): LocalCredentials {
    val baseUrl = prefs.get("base_url", "").trim()
    val token = prefs.get("token", "").trim()
    return LocalCredentials(baseUrl = baseUrl, token = token)
  }

  /**
   * Saves credentials to Preferences.
   */
  actual fun save(credentials: LocalCredentials) {
    prefs.put("base_url", credentials.baseUrl.trim())
    prefs.put("token", credentials.token.trim())
  }
}
