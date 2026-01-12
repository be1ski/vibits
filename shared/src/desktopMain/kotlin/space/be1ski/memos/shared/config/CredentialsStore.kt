package space.be1ski.memos.shared.config

import java.util.prefs.Preferences

actual class CredentialsStore {
  private val prefs = Preferences.userRoot().node("space.be1ski.memos")

  actual fun load(): LocalCredentials {
    val baseUrl = prefs.get("base_url", "").trim()
    val token = prefs.get("token", "").trim()
    return LocalCredentials(baseUrl = baseUrl, token = token)
  }

  actual fun save(credentials: LocalCredentials) {
    prefs.put("base_url", credentials.baseUrl.trim())
    prefs.put("token", credentials.token.trim())
  }
}
