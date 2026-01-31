package space.be1ski.vibits.shared.feature.sync.domain.usecase

import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.sync.domain.model.ConflictType
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncOperation
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncOperationType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

class DetectSyncConflictsUseCaseTest {
  private val now = Clock.System.now()
  private val twoHoursAgo = now - 2.hours

  @Test
  fun `when no pending operations then returns empty list`() {
    val result =
      DetectSyncConflictsUseCase(
        pendingOperations = emptyList(),
        localMemos = listOf(createMemo("memos/1")),
        serverMemos = listOf(createMemo("memos/1")),
      )

    assertTrue(result.isEmpty())
  }

  @Test
  fun `when CREATE operation for temp memo then no conflict`() {
    val operation =
      createOperation(
        type = SyncOperationType.CREATE,
        memoName = "local_temp_123",
      )

    val result =
      DetectSyncConflictsUseCase(
        pendingOperations = listOf(operation),
        localMemos = emptyList(),
        serverMemos = emptyList(),
      )

    assertTrue(result.isEmpty())
  }

  @Test
  fun `when CREATE operation and memo exists on server then BOTH_MODIFIED conflict`() {
    val operation =
      createOperation(
        type = SyncOperationType.CREATE,
        memoName = "memos/1",
      )
    val serverMemo = createMemo("memos/1", content = "server content")
    val localMemo = createMemo("memos/1", content = "local content")

    val result =
      DetectSyncConflictsUseCase(
        pendingOperations = listOf(operation),
        localMemos = listOf(localMemo),
        serverMemos = listOf(serverMemo),
      )

    assertEquals(1, result.size)
    assertEquals(ConflictType.BOTH_MODIFIED, result[0].conflictType)
    assertEquals(localMemo, result[0].localMemo)
    assertEquals(serverMemo, result[0].serverMemo)
  }

  @Test
  fun `when UPDATE operation and memo deleted on server then DELETED_ON_SERVER conflict`() {
    val operation =
      createOperation(
        type = SyncOperationType.UPDATE,
        memoName = "memos/1",
      )
    val localMemo = createMemo("memos/1")

    val result =
      DetectSyncConflictsUseCase(
        pendingOperations = listOf(operation),
        localMemos = listOf(localMemo),
        serverMemos = emptyList(),
      )

    assertEquals(1, result.size)
    assertEquals(ConflictType.DELETED_ON_SERVER, result[0].conflictType)
    assertEquals(localMemo, result[0].localMemo)
    assertEquals(null, result[0].serverMemo)
  }

  @Test
  fun `when UPDATE operation and server is newer then SERVER_NEWER conflict`() {
    val operation =
      createOperation(
        type = SyncOperationType.UPDATE,
        memoName = "memos/1",
        createdAt = twoHoursAgo,
      )
    val localMemo = createMemo("memos/1", updateTime = twoHoursAgo)
    val serverMemo = createMemo("memos/1", updateTime = now)

    val result =
      DetectSyncConflictsUseCase(
        pendingOperations = listOf(operation),
        localMemos = listOf(localMemo),
        serverMemos = listOf(serverMemo),
      )

    assertEquals(1, result.size)
    assertEquals(ConflictType.SERVER_NEWER, result[0].conflictType)
  }

  @Test
  fun `when UPDATE operation and server is older then no conflict`() {
    val operation =
      createOperation(
        type = SyncOperationType.UPDATE,
        memoName = "memos/1",
        createdAt = now,
      )
    val localMemo = createMemo("memos/1", updateTime = now)
    val serverMemo = createMemo("memos/1", updateTime = twoHoursAgo)

    val result =
      DetectSyncConflictsUseCase(
        pendingOperations = listOf(operation),
        localMemos = listOf(localMemo),
        serverMemos = listOf(serverMemo),
      )

    assertTrue(result.isEmpty())
  }

  @Test
  fun `when DELETE operation and server modified after then SERVER_NEWER conflict`() {
    val operation =
      createOperation(
        type = SyncOperationType.DELETE,
        memoName = "memos/1",
        createdAt = twoHoursAgo,
      )
    val serverMemo = createMemo("memos/1", updateTime = now)

    val result =
      DetectSyncConflictsUseCase(
        pendingOperations = listOf(operation),
        localMemos = emptyList(),
        serverMemos = listOf(serverMemo),
      )

    assertEquals(1, result.size)
    assertEquals(ConflictType.SERVER_NEWER, result[0].conflictType)
    assertEquals(null, result[0].localMemo)
    assertEquals(serverMemo, result[0].serverMemo)
  }

  @Test
  fun `when DELETE operation and server not modified then no conflict`() {
    val operation =
      createOperation(
        type = SyncOperationType.DELETE,
        memoName = "memos/1",
        createdAt = now,
      )
    val serverMemo = createMemo("memos/1", updateTime = twoHoursAgo)

    val result =
      DetectSyncConflictsUseCase(
        pendingOperations = listOf(operation),
        localMemos = emptyList(),
        serverMemos = listOf(serverMemo),
      )

    assertTrue(result.isEmpty())
  }

  @Test
  fun `when DELETE operation and memo not on server then no conflict`() {
    val operation =
      createOperation(
        type = SyncOperationType.DELETE,
        memoName = "memos/1",
      )

    val result =
      DetectSyncConflictsUseCase(
        pendingOperations = listOf(operation),
        localMemos = emptyList(),
        serverMemos = emptyList(),
      )

    assertTrue(result.isEmpty())
  }

  @Test
  fun `when operation has null memoName then skipped`() {
    val operation =
      createOperation(
        type = SyncOperationType.UPDATE,
        memoName = null,
      )

    val result =
      DetectSyncConflictsUseCase(
        pendingOperations = listOf(operation),
        localMemos = emptyList(),
        serverMemos = emptyList(),
      )

    assertTrue(result.isEmpty())
  }

  private fun createMemo(
    name: String,
    content: String = "content",
    updateTime: kotlin.time.Instant = now,
  ) = Memo(
    name = name,
    content = content,
    createTime = twoHoursAgo,
    updateTime = updateTime,
  )

  private fun createOperation(
    type: SyncOperationType,
    memoName: String? = "memos/1",
    content: String? = "content",
    createdAt: kotlin.time.Instant = now,
  ) = SyncOperation(
    id = "op-1",
    type = type,
    memoName = memoName,
    content = content,
    createdAt = createdAt,
  )
}
