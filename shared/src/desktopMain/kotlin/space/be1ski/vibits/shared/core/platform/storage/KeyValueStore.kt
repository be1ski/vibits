package space.be1ski.vibits.shared.core.platform.storage

import space.be1ski.vibits.shared.app.data.DesktopStoragePaths
import java.util.prefs.Preferences

actual fun createKeyValueStore(): KeyValueStore = DesktopKeyValueStore()

class DesktopKeyValueStore : KeyValueStore {
  private val prefs = Preferences.userRoot().node(DesktopStoragePaths.preferencesNode())

  override fun getString(
    key: String,
    defaultValue: String?,
  ): String? = prefs.get(key, defaultValue)

  override fun putString(
    key: String,
    value: String,
  ) {
    prefs.put(key, value)
  }

  override fun remove(key: String) {
    prefs.remove(key)
  }
}
