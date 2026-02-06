package space.be1ski.vibits.feature.memos.data

import space.be1ski.vibits.feature.memos.data.offline.OfflineMemoDto
import space.be1ski.vibits.feature.memos.data.offline.OfflineMemosFileDto
import space.be1ski.vibits.feature.memos.data.platform.OfflineMemoStorage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MemoStorageManagerImplTest {
  @Test
  fun `when clearAll then saves empty memo list to storage`() {
    val storage =
      RecordingOfflineMemoStorage(
        initial =
          OfflineMemosFileDto(
            memos =
              listOf(
                OfflineMemoDto(name = "memos/1", content = "test"),
                OfflineMemoDto(name = "memos/2", content = "test2"),
              ),
          ),
      )
    val manager = MemoStorageManagerImpl(storage)

    manager.clearAll()

    assertEquals(1, storage.saveCalls)
    assertTrue(storage.stored.memos.isEmpty())
  }
}

private class RecordingOfflineMemoStorage(
  initial: OfflineMemosFileDto = OfflineMemosFileDto(),
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
