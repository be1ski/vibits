package space.be1ski.vibits.feature.auth.data.platform

import space.be1ski.vibits.core.platform.app.DesktopStoragePaths
import space.be1ski.vibits.feature.auth.data.LocalCredentials
import space.be1ski.vibits.feature.auth.data.internal.MacOsKeychainCredentials
import java.util.prefs.Preferences

private val isMacOs = System.getProperty("os.name", "").lowercase().contains("mac")

internal class DesktopCredentialsStore : CredentialsStore {
  private val service = DesktopStoragePaths.preferencesNode()
  private val prefs = Preferences.userRoot().node(service)

  override fun load(): LocalCredentials {
    val baseUrl = prefs.get("base_url", "").trim()
    val token = prefs.get("token", "").trim()
    return LocalCredentials(baseUrl = baseUrl, token = token)
  }

  override fun save(credentials: LocalCredentials) {
    prefs.put("base_url", credentials.baseUrl)
    prefs.put("token", credentials.token)
  }

  override fun loadFromSecureStorage(): LocalCredentials? = if (isMacOs) MacOsKeychainCredentials.load(service) else null

  override fun isSecureStorageAvailable(): Boolean = isMacOs
}

actual fun createCredentialsStore(): CredentialsStore = DesktopCredentialsStore()
