package space.be1ski.vibits.feature.memos.data.test

import space.be1ski.vibits.feature.memos.data.offline.OfflineMemosFileDto
import space.be1ski.vibits.feature.memos.data.platform.OfflineMemoStorage

class FakeOfflineMemoStorage(
  initial: OfflineMemosFileDto = OfflineMemosFileDto(memos = emptyList()),
) : OfflineMemoStorage {
  var stored: OfflineMemosFileDto = initial
    private set
  var saveCalls: Int = 0
    private set

  override fun load(): OfflineMemosFileDto = stored

  override fun save(data: OfflineMemosFileDto) {
    stored = data
    saveCalls += 1
  }
}
