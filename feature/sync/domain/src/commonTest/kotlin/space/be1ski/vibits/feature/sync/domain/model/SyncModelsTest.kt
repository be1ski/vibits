package space.be1ski.vibits.feature.sync.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

class SyncModelsTest {
  // ========== SyncStatus Tests ==========

  @Test
  fun `SyncStatus hasPendingOperations returns true when pendingCount greater than zero`() {
    val status = SyncStatus(pendingCount = 5, failedCount = 0)
    assertTrue(status.hasPendingOperations)
  }

  @Test
  fun `SyncStatus hasPendingOperations returns false when pendingCount is zero`() {
    val status = SyncStatus(pendingCount = 0, failedCount = 2)
    assertFalse(status.hasPendingOperations)
  }

  @Test
  fun `SyncStatus hasFailedOperations returns true when failedCount greater than zero`() {
    val status = SyncStatus(pendingCount = 0, failedCount = 3)
    assertTrue(status.hasFailedOperations)
  }

  @Test
  fun `SyncStatus hasFailedOperations returns false when failedCount is zero`() {
    val status = SyncStatus(pendingCount = 5, failedCount = 0)
    assertFalse(status.hasFailedOperations)
  }

  @Test
  fun `SyncStatus default values are zero`() {
    val status = SyncStatus()
    assertEquals(0, status.pendingCount)
    assertEquals(0, status.failedCount)
  }

  // ========== SyncOperation Tests ==========

  @Test
  fun `SyncOperation default status is PENDING`() {
    val operation =
      SyncOperation(
        id = "op1",
        type = SyncOperationType.CREATE,
        memoName = "memos/1",
        content = "content",
      )
    assertEquals(SyncOperationStatus.PENDING, operation.status)
  }

  @Test
  fun `SyncOperation createdAt defaults to current time`() {
    val before =
      kotlin.time.Clock.System
        .now()
    val operation =
      SyncOperation(
        id = "op1",
        type = SyncOperationType.UPDATE,
        memoName = "memos/1",
        content = "content",
      )
    val after =
      kotlin.time.Clock.System
        .now()
    assertTrue(operation.createdAt >= before && operation.createdAt <= after)
  }

  @Test
  fun `SyncOperation with custom createdAt preserves time`() {
    val customTime = Instant.fromEpochMilliseconds(1704067200000) // 2024-01-01
    val operation =
      SyncOperation(
        id = "op1",
        type = SyncOperationType.DELETE,
        memoName = "memos/1",
        createdAt = customTime,
      )
    assertEquals(customTime, operation.createdAt)
  }
}
