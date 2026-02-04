package space.be1ski.vibits.feature.auth.data.platform

import android.content.Context
import androidx.core.content.edit
import space.be1ski.vibits.core.platform.app.AndroidContextHolder
import space.be1ski.vibits.feature.auth.data.LocalCredentials

internal class AndroidCredentialsStore : CredentialsStore {
  private val prefsName = "memos_prefs"

  override fun load(): LocalCredentials {
    if (!AndroidContextHolder.isReady()) {
      return LocalCredentials(baseUrl = "", token = "")
    }
    val prefs = AndroidContextHolder.context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    val baseUrl = prefs.getString("base_url", "")?.trim().orEmpty()
    val token = prefs.getString("token", "")?.trim().orEmpty()
    return LocalCredentials(baseUrl = baseUrl, token = token)
  }

  override fun save(credentials: LocalCredentials) {
    if (!AndroidContextHolder.isReady()) {
      return
    }
    val prefs = AndroidContextHolder.context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    prefs.edit {
      putString("base_url", credentials.baseUrl)
      putString("token", credentials.token)
    }
  }
}

actual fun createCredentialsStore(): CredentialsStore = AndroidCredentialsStore()
