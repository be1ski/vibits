package space.be1ski.vibits.feature.memos.data.platform

import space.be1ski.vibits.feature.memos.data.offline.OfflineMemosFileDto

/**
 * Platform-specific offline memo storage.
 * Stored in Documents/memos.json (Android, Desktop, iOS) or localStorage (WASM).
 */
interface OfflineMemoStorage {
  fun load(): OfflineMemosFileDto

  fun save(data: OfflineMemosFileDto)
}

expect fun createOfflineMemoStorage(): OfflineMemoStorage
