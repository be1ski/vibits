package space.be1ski.memos.shared.data.local

import android.content.Context

/**
 * Android implementation backed by SharedPreferences.
 */
actual class CredentialsStore {
  private val prefsName = "memos_prefs"

  /**
   * Loads credentials or returns empty values when not available.
   */
  actual fun load(): LocalCredentials {
    if (!AndroidContextHolder.isReady()) {
      return LocalCredentials(baseUrl = "", token = "")
    }
    val prefs = AndroidContextHolder.context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    val baseUrl = prefs.getString("base_url", "")?.trim().orEmpty()
    val token = prefs.getString("token", "")?.trim().orEmpty()
    return LocalCredentials(baseUrl = baseUrl, token = token)
  }

  /**
   * Saves credentials to SharedPreferences.
   */
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
