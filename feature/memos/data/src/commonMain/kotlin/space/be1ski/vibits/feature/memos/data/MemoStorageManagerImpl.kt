package space.be1ski.vibits.feature.memos.data

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import space.be1ski.vibits.core.platform.di.AppScope
import space.be1ski.vibits.feature.memos.data.offline.OfflineMemosFileDto
import space.be1ski.vibits.feature.memos.data.platform.OfflineMemoStorage
import space.be1ski.vibits.feature.memos.domain.repository.MemoStorageManager

@Inject
@SingleIn(AppScope::class)
class MemoStorageManagerImpl(
  private val offlineMemoStorage: OfflineMemoStorage,
) : MemoStorageManager {
  override fun clearAll() {
    offlineMemoStorage.save(OfflineMemosFileDto(memos = emptyList()))
  }
}
