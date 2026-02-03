package space.be1ski.vibits.core.platform.storage

import android.content.Context
import androidx.core.content.edit
import space.be1ski.vibits.core.platform.app.AndroidContextHolder

actual fun createKeyValueStore(): KeyValueStore = AndroidKeyValueStore()

class AndroidKeyValueStore : KeyValueStore {
  private val prefsName = "memos_prefs"

  override fun getString(
    key: String,
    defaultValue: String?,
  ): String? {
    if (!AndroidContextHolder.isReady()) return defaultValue
    val prefs = AndroidContextHolder.context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    return prefs.getString(key, defaultValue) ?: defaultValue
  }

  override fun putString(
    key: String,
    value: String,
  ) {
    if (!AndroidContextHolder.isReady()) return
    val prefs = AndroidContextHolder.context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    prefs.edit { putString(key, value) }
  }

  override fun remove(key: String) {
    if (!AndroidContextHolder.isReady()) return
    val prefs = AndroidContextHolder.context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    prefs.edit { remove(key) }
  }
}
