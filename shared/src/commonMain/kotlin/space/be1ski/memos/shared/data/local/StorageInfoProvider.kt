package space.be1ski.memos.shared.data.local

import space.be1ski.memos.shared.domain.model.storage.StorageInfo

/**
 * Provides storage locations used by the app.
 */
expect class StorageInfoProvider() {
  /**
   * Returns human-readable storage details.
   */
  fun load(): StorageInfo
}
