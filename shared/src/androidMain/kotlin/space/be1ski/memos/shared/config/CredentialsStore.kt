package space.be1ski.memos.shared.config

import android.content.Context

actual class CredentialsStore {
  private val prefsName = "memos_prefs"

  actual fun load(): LocalCredentials {
    if (!AndroidContextHolder.isReady()) {
      return LocalCredentials(baseUrl = "", token = "")
    }
    val prefs = AndroidContextHolder.context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    val baseUrl = prefs.getString("base_url", "")?.trim().orEmpty()
    val token = prefs.getString("token", "")?.trim().orEmpty()
    return LocalCredentials(baseUrl = baseUrl, token = token)
  }

  actual fun save(credentials: LocalCredentials) {
    if (!AndroidContextHolder.isReady()) {
      return
    }
    val prefs = AndroidContextHolder.context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    prefs.edit()
      .putString("base_url", credentials.baseUrl.trim())
      .putString("token", credentials.token.trim())
      .apply()
  }
}
