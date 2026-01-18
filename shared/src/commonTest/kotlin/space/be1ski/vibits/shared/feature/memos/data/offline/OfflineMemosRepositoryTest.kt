package space.be1ski.vibits.shared.feature.memos.data.offline

import kotlinx.coroutines.test.runTest
import space.be1ski.vibits.shared.feature.memos.data.platform.OfflineMemoStorage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class OfflineMemosRepositoryTest {
  @Test
  fun `when listMemos then returns memos from storage`() =
    runTest {
      val storage =
        FakeOfflineMemoStorage(
          initial =
            OfflineMemosFileDto(
              memos =
                listOf(
                  OfflineMemoDto(name = "memos/1", content = "Hello"),
                  OfflineMemoDto(name = "memos/2", content = "World"),
                ),
            ),
        )
      val repository = OfflineMemosRepository(storage)

      val result = repository.listMemos()

      assertEquals(2, result.size)
      assertEquals("memos/1", result[0].name)
      assertEquals("Hello", result[0].content)
      assertEquals("memos/2", result[1].name)
      assertEquals("World", result[1].content)
    }

  @Test
  fun `when cachedMemos then returns same as listMemos`() =
    runTest {
      val storage =
        FakeOfflineMemoStorage(
          initial =
            OfflineMemosFileDto(
              memos = listOf(OfflineMemoDto(name = "memos/1", content = "Test")),
            ),
        )
      val repository = OfflineMemosRepository(storage)

      val cached = repository.cachedMemos()
      val list = repository.listMemos()

      assertEquals(list, cached)
    }

  @Test
  fun `when listMemos with timestamps then parses instants correctly`() =
    runTest {
      val storage =
        FakeOfflineMemoStorage(
          initial =
            OfflineMemosFileDto(
              memos =
                listOf(
                  OfflineMemoDto(
                    name = "memos/1",
                    content = "Test",
                    createTime = "2024-01-15T10:30:00Z",
                    updateTime = "2024-01-15T12:00:00Z",
                  ),
                ),
            ),
        )
      val repository = OfflineMemosRepository(storage)

      val result = repository.listMemos()

      assertEquals(1, result.size)
      assertNotNull(result[0].createTime)
      assertNotNull(result[0].updateTime)
      assertEquals(Instant.parse("2024-01-15T10:30:00Z"), result[0].createTime)
      assertEquals(Instant.parse("2024-01-15T12:00:00Z"), result[0].updateTime)
    }

  @Test
  fun `when listMemos with null timestamps then returns null instants`() =
    runTest {
      val storage =
        FakeOfflineMemoStorage(
          initial =
            OfflineMemosFileDto(
              memos =
                listOf(
                  OfflineMemoDto(name = "memos/1", content = "Test", createTime = null, updateTime = null),
                ),
            ),
        )
      val repository = OfflineMemosRepository(storage)

      val result = repository.listMemos()

      assertNull(result[0].createTime)
      assertNull(result[0].updateTime)
    }

  @Test
  fun `when listMemos with invalid timestamps then returns null instants`() =
    runTest {
      val storage =
        FakeOfflineMemoStorage(
          initial =
            OfflineMemosFileDto(
              memos =
                listOf(
                  OfflineMemoDto(name = "memos/1", content = "Test", createTime = "invalid", updateTime = ""),
                ),
            ),
        )
      val repository = OfflineMemosRepository(storage)

      val result = repository.listMemos()

      assertNull(result[0].createTime)
      assertNull(result[0].updateTime)
    }

  @Test
  fun `when updateMemo then updates content and saves`() =
    runTest {
      val storage =
        FakeOfflineMemoStorage(
          initial =
            OfflineMemosFileDto(
              memos =
                listOf(
                  OfflineMemoDto(name = "memos/1", content = "Old content"),
                ),
            ),
        )
      val repository = OfflineMemosRepository(storage)

      val result = repository.updateMemo("memos/1", "New content")

      assertEquals("memos/1", result.name)
      assertEquals("New content", result.content)
      assertNotNull(result.updateTime)
      assertEquals(
        "New content",
        storage.saved
          ?.memos
          ?.first()
          ?.content,
      )
    }

  @Test
  fun `when updateMemo with nonexistent name then returns fallback memo`() =
    runTest {
      val storage =
        FakeOfflineMemoStorage(
          initial = OfflineMemosFileDto(memos = emptyList()),
        )
      val repository = OfflineMemosRepository(storage)

      val result = repository.updateMemo("memos/nonexistent", "Content")

      assertEquals("memos/nonexistent", result.name)
      assertEquals("Content", result.content)
    }

  @Test
  fun `when createMemo then adds memo with generated name`() =
    runTest {
      val storage =
        FakeOfflineMemoStorage(
          initial = OfflineMemosFileDto(memos = emptyList()),
        )
      val repository = OfflineMemosRepository(storage)

      val result = repository.createMemo("New memo content")

      assertTrue(result.name.startsWith("memos/"))
      assertEquals("New memo content", result.content)
      assertNotNull(result.createTime)
      assertNotNull(result.updateTime)
      assertEquals(1, storage.saved?.memos?.size)
    }

  @Test
  fun `when createMemo then preserves existing memos`() =
    runTest {
      val storage =
        FakeOfflineMemoStorage(
          initial =
            OfflineMemosFileDto(
              memos = listOf(OfflineMemoDto(name = "memos/existing", content = "Existing")),
            ),
        )
      val repository = OfflineMemosRepository(storage)

      repository.createMemo("New memo")

      assertEquals(2, storage.saved?.memos?.size)
      assertEquals(
        "memos/existing",
        storage.saved
          ?.memos
          ?.first()
          ?.name,
      )
    }

  @Test
  fun `when deleteMemo then removes memo from storage`() =
    runTest {
      val storage =
        FakeOfflineMemoStorage(
          initial =
            OfflineMemosFileDto(
              memos =
                listOf(
                  OfflineMemoDto(name = "memos/1", content = "Keep"),
                  OfflineMemoDto(name = "memos/2", content = "Delete"),
                ),
            ),
        )
      val repository = OfflineMemosRepository(storage)

      repository.deleteMemo("memos/2")

      assertEquals(1, storage.saved?.memos?.size)
      assertEquals(
        "memos/1",
        storage.saved
          ?.memos
          ?.first()
          ?.name,
      )
    }

  @Test
  fun `when deleteMemo with nonexistent name then does nothing`() =
    runTest {
      val storage =
        FakeOfflineMemoStorage(
          initial =
            OfflineMemosFileDto(
              memos = listOf(OfflineMemoDto(name = "memos/1", content = "Keep")),
            ),
        )
      val repository = OfflineMemosRepository(storage)

      repository.deleteMemo("memos/nonexistent")

      assertEquals(1, storage.saved?.memos?.size)
    }
}

private class FakeOfflineMemoStorage(
  private val initial: OfflineMemosFileDto = OfflineMemosFileDto(),
) : OfflineMemoStorage {
  var saved: OfflineMemosFileDto? = null
    private set

  override fun load(): OfflineMemosFileDto = saved ?: initial

  override fun save(data: OfflineMemosFileDto) {
    saved = data
  }
}
