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
    if (isMacOs) {
      val keychain = MacOsKeychainCredentials.load(service)
      if (keychain != null) return keychain
    }
    val baseUrl = prefs.get("base_url", "").trim()
    val token = prefs.get("token", "").trim()
    return LocalCredentials(baseUrl = baseUrl, token = token)
  }

  override fun save(credentials: LocalCredentials) {
    if (isMacOs && MacOsKeychainCredentials.save(service, credentials)) {
      prefs.remove("base_url")
      prefs.remove("token")
      return
    }
    prefs.put("base_url", credentials.baseUrl)
    prefs.put("token", credentials.token)
  }
}

actual fun createCredentialsStore(): CredentialsStore = DesktopCredentialsStore()
