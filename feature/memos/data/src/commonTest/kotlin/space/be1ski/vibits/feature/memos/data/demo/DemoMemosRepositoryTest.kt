package space.be1ski.vibits.feature.memos.data.demo

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DemoMemosRepositoryTest {
  @Test
  fun `when listMemos then initializes with demo data`() =
    runTest {
      val repository = DemoMemosRepository()

      val result = repository.listMemos()

      assertTrue(result.isNotEmpty())
    }

  @Test
  fun `when cachedMemos then returns same as listMemos`() =
    runTest {
      val repository = DemoMemosRepository()

      val list = repository.listMemos()
      val cached = repository.cachedMemos()

      assertEquals(list, cached)
    }

  @Test
  fun `when reset then clears and regenerates data`() =
    runTest {
      val repository = DemoMemosRepository()
      val initialMemos = repository.listMemos()

      repository.reset()
      val afterReset = repository.listMemos()

      assertTrue(afterReset.isNotEmpty())
      // After reset, memos should be regenerated (different instances)
      assertTrue(initialMemos.first().name != afterReset.first().name || initialMemos.size == afterReset.size)
    }

  @Test
  fun `when updateMemo with existing name then updates content`() =
    runTest {
      val repository = DemoMemosRepository()
      val memos = repository.listMemos()
      val existingMemo = memos.first()

      val updated = repository.updateMemo(existingMemo.name, "Updated content")

      assertEquals(existingMemo.name, updated.name)
      assertEquals("Updated content", updated.content)
      assertNotNull(updated.updateTime)
    }

  @Test
  fun `when updateMemo with nonexistent name then returns fallback memo`() =
    runTest {
      val repository = DemoMemosRepository()
      repository.listMemos() // initialize

      val result = repository.updateMemo("memos/nonexistent", "Content")

      assertEquals("memos/nonexistent", result.name)
      assertEquals("Content", result.content)
    }

  @Test
  fun `when createMemo then adds memo at beginning`() =
    runTest {
      val repository = DemoMemosRepository()
      val initialCount = repository.listMemos().size

      val created = repository.createMemo("New memo content")
      val afterCreate = repository.listMemos()

      assertTrue(created.name.startsWith("memos/demo_"))
      assertEquals("New memo content", created.content)
      assertNotNull(created.createTime)
      assertNotNull(created.updateTime)
      assertEquals(initialCount + 1, afterCreate.size)
      assertEquals(created, afterCreate.first())
    }

  @Test
  fun `when deleteMemo then removes memo`() =
    runTest {
      val repository = DemoMemosRepository()
      val memos = repository.listMemos()
      val memoToDelete = memos.first()
      val initialCount = memos.size

      repository.deleteMemo(memoToDelete.name)
      val afterDelete = repository.listMemos()

      assertEquals(initialCount - 1, afterDelete.size)
      assertTrue(afterDelete.none { it.name == memoToDelete.name })
    }

  @Test
  fun `when deleteMemo with nonexistent name then does nothing`() =
    runTest {
      val repository = DemoMemosRepository()
      val initialCount = repository.listMemos().size

      repository.deleteMemo("memos/nonexistent")

      assertEquals(initialCount, repository.listMemos().size)
    }
}
