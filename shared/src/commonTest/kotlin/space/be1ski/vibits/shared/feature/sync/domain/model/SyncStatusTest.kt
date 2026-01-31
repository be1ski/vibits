package space.be1ski.vibits.shared.feature.sync.domain.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SyncStatusTest {
  @Test
  fun `when pendingCount is 0 then hasPendingOperations is false`() {
    val status = SyncStatus(pendingCount = 0)
    assertFalse(status.hasPendingOperations)
  }

  @Test
  fun `when pendingCount is greater than 0 then hasPendingOperations is true`() {
    val status = SyncStatus(pendingCount = 1)
    assertTrue(status.hasPendingOperations)
  }

  @Test
  fun `when failedCount is 0 then hasFailedOperations is false`() {
    val status = SyncStatus(failedCount = 0)
    assertFalse(status.hasFailedOperations)
  }

  @Test
  fun `when failedCount is greater than 0 then hasFailedOperations is true`() {
    val status = SyncStatus(failedCount = 1)
    assertTrue(status.hasFailedOperations)
  }

  @Test
  fun `when no pending and no failed then needsSync is false`() {
    val status = SyncStatus(pendingCount = 0, failedCount = 0)
    assertFalse(status.needsSync)
  }

  @Test
  fun `when has pending then needsSync is true`() {
    val status = SyncStatus(pendingCount = 1, failedCount = 0)
    assertTrue(status.needsSync)
  }

  @Test
  fun `when has failed then needsSync is true`() {
    val status = SyncStatus(pendingCount = 0, failedCount = 1)
    assertTrue(status.needsSync)
  }

  @Test
  fun `when has both pending and failed then needsSync is true`() {
    val status = SyncStatus(pendingCount = 2, failedCount = 3)
    assertTrue(status.needsSync)
  }
}
