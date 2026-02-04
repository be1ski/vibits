package space.be1ski.vibits.feature.auth.data.platform

import space.be1ski.vibits.core.platform.app.DesktopStoragePaths
import space.be1ski.vibits.feature.auth.data.LocalCredentials
import java.util.prefs.Preferences

internal class DesktopCredentialsStore : CredentialsStore {
  private val prefs = Preferences.userRoot().node(DesktopStoragePaths.preferencesNode())

  override fun load(): LocalCredentials {
    val baseUrl = prefs.get("base_url", "").trim()
    val token = prefs.get("token", "").trim()
    return LocalCredentials(baseUrl = baseUrl, token = token)
  }

  override fun save(credentials: LocalCredentials) {
    prefs.put("base_url", credentials.baseUrl)
    prefs.put("token", credentials.token)
  }
}

actual fun createCredentialsStore(): CredentialsStore = DesktopCredentialsStore()
