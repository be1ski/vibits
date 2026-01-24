package space.be1ski.vibits.shared.core.platform.storage

import kotlinx.browser.localStorage

actual fun createKeyValueStore(): KeyValueStore = WasmKeyValueStore()

class WasmKeyValueStore : KeyValueStore {
  override fun getString(
    key: String,
    defaultValue: String?,
  ): String? = localStorage.getItem(key) ?: defaultValue

  override fun putString(
    key: String,
    value: String,
  ) {
    localStorage.setItem(key, value)
  }

  override fun remove(key: String) {
    localStorage.removeItem(key)
  }
}
