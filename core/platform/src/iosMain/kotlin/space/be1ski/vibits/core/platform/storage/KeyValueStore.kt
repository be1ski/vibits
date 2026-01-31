package space.be1ski.vibits.core.platform.storage

import platform.Foundation.NSUserDefaults

actual fun createKeyValueStore(): KeyValueStore = IosKeyValueStore()

class IosKeyValueStore : KeyValueStore {
  private val defaults = NSUserDefaults.standardUserDefaults

  override fun getString(
    key: String,
    defaultValue: String?,
  ): String? = defaults.stringForKey(key) ?: defaultValue

  override fun putString(
    key: String,
    value: String,
  ) {
    defaults.setObject(value, forKey = key)
  }

  override fun remove(key: String) {
    defaults.removeObjectForKey(key)
  }
}
