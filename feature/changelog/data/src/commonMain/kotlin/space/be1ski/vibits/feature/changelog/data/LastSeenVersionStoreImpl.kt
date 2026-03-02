package space.be1ski.vibits.feature.changelog.data

import space.be1ski.vibits.core.platform.storage.KeyValueStore
import space.be1ski.vibits.feature.changelog.domain.repository.LastSeenVersionStore

class LastSeenVersionStoreImpl(
  private val store: KeyValueStore,
) : LastSeenVersionStore {
  override fun getLastSeenVersion(): String? = store.getString(KEY_LAST_SEEN_VERSION)

  override fun setLastSeenVersion(version: String) {
    store.putString(KEY_LAST_SEEN_VERSION, version)
  }

  private companion object {
    const val KEY_LAST_SEEN_VERSION = "last_seen_app_version"
  }
}
