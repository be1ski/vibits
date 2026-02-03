package space.be1ski.vibits.core.platform.storage

/**
 * Platform-specific key-value storage.
 * Backed by SharedPreferences (Android), Preferences API (Desktop), NSUserDefaults (iOS), localStorage (Web).
 */
interface KeyValueStore {
  fun getString(
    key: String,
    defaultValue: String? = null,
  ): String?

  fun putString(
    key: String,
    value: String,
  )

  fun remove(key: String)
}

expect fun createKeyValueStore(): KeyValueStore
