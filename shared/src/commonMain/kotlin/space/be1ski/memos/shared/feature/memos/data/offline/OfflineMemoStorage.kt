package space.be1ski.memos.shared.feature.memos.data.offline

/**
 * Platform-specific storage for offline memos JSON file.
 */
expect class OfflineMemoStorage() {
  fun load(): OfflineMemosFileDto
  fun save(data: OfflineMemosFileDto)
}
