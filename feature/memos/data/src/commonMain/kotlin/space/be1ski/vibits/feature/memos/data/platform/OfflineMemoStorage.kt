package space.be1ski.vibits.feature.memos.data.platform

import space.be1ski.vibits.feature.memos.data.offline.OfflineMemosFileDto

interface OfflineMemoStorage {
  fun load(): OfflineMemosFileDto

  fun save(data: OfflineMemosFileDto)
}

expect fun createOfflineMemoStorage(): OfflineMemoStorage
