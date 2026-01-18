package space.be1ski.vibits.shared.feature.memos.data.platform

import space.be1ski.vibits.shared.feature.memos.data.offline.OfflineMemosFileDto

expect class OfflineMemoStorage() {
  fun load(): OfflineMemosFileDto

  fun save(data: OfflineMemosFileDto)
}
