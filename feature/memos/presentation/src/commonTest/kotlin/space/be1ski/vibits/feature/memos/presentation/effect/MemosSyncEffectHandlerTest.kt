package space.be1ski.vibits.feature.memos.presentation.effect

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import space.be1ski.vibits.feature.memos.domain.model.Memo
import space.be1ski.vibits.feature.memos.presentation.action.MemosAction
import space.be1ski.vibits.feature.settings.domain.model.DEFAULT_MEMOS_AUTO_SYNC_DEBOUNCE_DURATION
import space.be1ski.vibits.feature.settings.domain.model.TimeRangeTab
import space.be1ski.vibits.feature.settings.domain.model.UserPreferences
import space.be1ski.vibits.feature.settings.domain.test.FakePreferencesRepository
import space.be1ski.vibits.feature.settings.domain.usecase.LoadSyncDebounceDurationUseCase
import space.be1ski.vibits.feature.sync.domain.model.ConflictType
import space.be1ski.vibits.feature.sync.domain.model.SyncConflict
import space.be1ski.vibits.feature.sync.domain.model.SyncOperation
import space.be1ski.vibits.feature.sync.domain.model.SyncOperationType
import space.be1ski.vibits.feature.sync.domain.model.SyncResult
import space.be1ski.vibits.feature.sync.domain.model.SyncStatus
import space.be1ski.vibits.feature.sync.domain.test.FakeSyncEngine
import space.be1ski.vibits.feature.sync.domain.test.FakeSyncQueueRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

class MemosSyncEffectHandlerTest {
  private val fakeSyncEngine = FakeSyncEngine()
  private val fakeSyncQueue = FakeSyncQueueRepository()
  private val handler =
    MemosSyncEffectHandler(
      syncEngine = fakeSyncEngine,
      syncQueueRepository = fakeSyncQueue,
      loadSyncDebounceDuration = LoadSyncDebounceDurationUseCase(FakePreferencesRepository()),
    )

  private val testMemo =
    Memo(
      name = "memos/1",
      content = "test",
      createTime = Clock.System.now(),
      updateTime = Clock.System.now(),
    )

  @Test
  fun `when PerformSync succeeds then emits SyncStarted and SyncCompleted`() =
    runTest {
      fakeSyncEngine.performSyncResult = SyncResult.Success(listOf(testMemo))

      val actions = handler(MemosEffect.PerformSync).toList()

      assertEquals(2, actions.size)
      assertTrue(actions[0] is MemosAction.Sync.SyncStarted)
      val action = actions[1] as MemosAction.Sync.SyncCompleted
      assertEquals(1, action.memos.size)
    }

  @Test
  fun `when PerformSync has conflicts then emits SyncStarted and SyncConflictDetected`() =
    runTest {
      val conflict =
        SyncConflict(
          operation =
            SyncOperation(
              id = "op-1",
              type = SyncOperationType.UPDATE,
              memoName = "memos/1",
              content = "content",
              createdAt = Clock.System.now(),
            ),
          localMemo = testMemo,
          serverMemo = testMemo,
          conflictType = ConflictType.BOTH_MODIFIED,
        )
      fakeSyncEngine.performSyncResult = SyncResult.Conflict(listOf(conflict))

      val actions = handler(MemosEffect.PerformSync).toList()

      assertEquals(2, actions.size)
      assertTrue(actions[0] is MemosAction.Sync.SyncStarted)
      val action = actions[1] as MemosAction.Sync.SyncConflictDetected
      assertEquals(1, action.conflicts.size)
    }

  @Test
  fun `when PerformSync fails then emits SyncStarted and SyncFailed`() =
    runTest {
      fakeSyncEngine.performSyncResult = SyncResult.Error("Network error")

      val actions = handler(MemosEffect.PerformSync).toList()

      assertEquals(2, actions.size)
      assertTrue(actions[0] is MemosAction.Sync.SyncStarted)
      val action = actions[1] as MemosAction.Sync.SyncFailed
      assertEquals("Network error", action.error)
    }

  @Test
  fun `when PerformSync has no credentials then emits SyncStarted and SyncFailed`() =
    runTest {
      fakeSyncEngine.performSyncResult = SyncResult.NoCredentials

      val actions = handler(MemosEffect.PerformSync).toList()

      assertEquals(2, actions.size)
      assertTrue(actions[0] is MemosAction.Sync.SyncStarted)
      assertTrue(actions[1] is MemosAction.Sync.SyncFailed)
    }

  @Test
  fun `when ForceLocalSync succeeds then emits SyncCompleted`() =
    runTest {
      fakeSyncEngine.forceLocalSyncResult = SyncResult.Success(listOf(testMemo))

      val actions = handler(MemosEffect.ForceLocalSync).toList()

      assertEquals(1, actions.size)
      assertTrue(actions[0] is MemosAction.Sync.SyncCompleted)
    }

  @Test
  fun `when ForceLocalSync fails then emits SyncFailed`() =
    runTest {
      fakeSyncEngine.forceLocalSyncResult = SyncResult.Error("Error")

      val actions = handler(MemosEffect.ForceLocalSync).toList()

      assertEquals(1, actions.size)
      assertTrue(actions[0] is MemosAction.Sync.SyncFailed)
    }

  @Test
  fun `when ForceLocalSync returns conflict then emits SyncFailed`() =
    runTest {
      fakeSyncEngine.forceLocalSyncResult = SyncResult.Conflict(emptyList())

      val actions = handler(MemosEffect.ForceLocalSync).toList()

      assertEquals(1, actions.size)
      val action = actions[0] as MemosAction.Sync.SyncFailed
      assertEquals("Unexpected conflict during force sync", action.error)
    }

  @Test
  fun `when ForceLocalSync has no credentials then emits SyncFailed`() =
    runTest {
      fakeSyncEngine.forceLocalSyncResult = SyncResult.NoCredentials

      val actions = handler(MemosEffect.ForceLocalSync).toList()

      assertEquals(1, actions.size)
      assertTrue(actions[0] is MemosAction.Sync.SyncFailed)
    }

  @Test
  fun `when ForceServerSync succeeds then emits SyncCompleted`() =
    runTest {
      fakeSyncEngine.forceServerSyncResult = SyncResult.Success(listOf(testMemo))

      val actions = handler(MemosEffect.ForceServerSync).toList()

      assertEquals(1, actions.size)
      assertTrue(actions[0] is MemosAction.Sync.SyncCompleted)
    }

  @Test
  fun `when ForceServerSync fails then emits SyncFailed`() =
    runTest {
      fakeSyncEngine.forceServerSyncResult = SyncResult.Error("Error")

      val actions = handler(MemosEffect.ForceServerSync).toList()

      assertEquals(1, actions.size)
      assertTrue(actions[0] is MemosAction.Sync.SyncFailed)
    }

  @Test
  fun `when ForceServerSync returns conflict then emits SyncFailed`() =
    runTest {
      fakeSyncEngine.forceServerSyncResult = SyncResult.Conflict(emptyList())

      val actions = handler(MemosEffect.ForceServerSync).toList()

      assertEquals(1, actions.size)
      val action = actions[0] as MemosAction.Sync.SyncFailed
      assertEquals("Unexpected conflict during force sync", action.error)
    }

  @Test
  fun `when ForceServerSync has no credentials then emits SyncFailed`() =
    runTest {
      fakeSyncEngine.forceServerSyncResult = SyncResult.NoCredentials

      val actions = handler(MemosEffect.ForceServerSync).toList()

      assertEquals(1, actions.size)
      assertTrue(actions[0] is MemosAction.Sync.SyncFailed)
    }

  @Test
  fun `when LoadSyncStatus then emits SyncStatusUpdated`() =
    runTest {
      fakeSyncQueue.syncStatus = SyncStatus(pendingCount = 5, failedCount = 2)

      val actions = handler(MemosEffect.LoadSyncStatus).toList()

      assertEquals(1, actions.size)
      val action = actions[0] as MemosAction.Sync.SyncStatusUpdated
      assertEquals(5, action.status.pendingCount)
      assertEquals(2, action.status.failedCount)
    }

  @Test
  fun `when ObserveSyncStatus then emits SyncStatusUpdated from flow`() =
    runTest {
      fakeSyncQueue.syncStatus = SyncStatus(pendingCount = 3, failedCount = 1)

      val actions = handler(MemosEffect.ObserveSyncStatus).toList()

      assertEquals(1, actions.size)
      val action = actions[0] as MemosAction.Sync.SyncStatusUpdated
      assertEquals(3, action.status.pendingCount)
    }

  @Test
  @OptIn(ExperimentalCoroutinesApi::class)
  fun `when PerformSync requested repeatedly within debounce window then only latest request performs sync`() =
    runTest {
      val debouncePrefs =
        FakePreferencesRepository(
          UserPreferences(TimeRangeTab.WEEKS, TimeRangeTab.WEEKS, memosAutoSyncDebounceDuration = 30.seconds),
        )
      val debounceHandler =
        MemosSyncEffectHandler(
          syncEngine = fakeSyncEngine,
          syncQueueRepository = fakeSyncQueue,
          loadSyncDebounceDuration = LoadSyncDebounceDurationUseCase(debouncePrefs),
        )
      fakeSyncEngine.performSyncResult = SyncResult.Success(listOf(testMemo))

      val firstRequest = async { debounceHandler(MemosEffect.PerformSync).toList() }
      advanceTimeBy(10_000)
      val secondRequest = async { debounceHandler(MemosEffect.PerformSync).toList() }
      advanceUntilIdle()

      val firstActions = firstRequest.await()
      val secondActions = secondRequest.await()

      assertTrue(firstActions.isEmpty())
      assertEquals(2, secondActions.size)
      assertTrue(secondActions[0] is MemosAction.Sync.SyncStarted)
      assertTrue(secondActions[1] is MemosAction.Sync.SyncCompleted)
      assertEquals(1, fakeSyncEngine.performSyncCallCount)
    }
}
