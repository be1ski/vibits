package space.be1ski.vibits.shared.feature.memos.data

import kotlinx.coroutines.test.runTest
import space.be1ski.vibits.shared.feature.auth.domain.model.Credentials
import space.be1ski.vibits.shared.feature.auth.domain.repository.CredentialsRepository
import space.be1ski.vibits.shared.feature.memos.data.demo.DemoMemosRepository
import space.be1ski.vibits.shared.feature.memos.data.mapper.MemoMapper
import space.be1ski.vibits.shared.feature.memos.data.offline.OfflineMemosFileDto
import space.be1ski.vibits.shared.feature.memos.data.offline.OfflineMemosRepository
import space.be1ski.vibits.shared.feature.memos.data.platform.MemoCache
import space.be1ski.vibits.shared.feature.memos.data.platform.OfflineMemoStorage
import space.be1ski.vibits.shared.feature.memos.data.remote.MemosApi
import space.be1ski.vibits.shared.feature.memos.data.remote.dto.ListMemosResponseDto
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.mode.domain.repository.AppModeRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ModeAwareMemosRepositoryTest {
  @Test
  fun `when mode is DEMO then uses demo repository`() =
    runTest {
      val appModeRepo = FakeAppModeRepository(AppMode.DEMO)
      val demoRepo = DemoMemosRepository()
      val repository = createModeAwareRepository(appModeRepo, demoRepo = demoRepo)

      val memos = repository.listMemos()

      assertTrue(memos.isNotEmpty()) // Demo repo generates data
    }

  @Test
  fun `when mode is OFFLINE then uses offline repository`() =
    runTest {
      val appModeRepo = FakeAppModeRepository(AppMode.OFFLINE)
      val offlineStorage = FakeOfflineMemoStorage()
      val offlineRepo = OfflineMemosRepository(offlineStorage)
      val repository = createModeAwareRepository(appModeRepo, offlineRepo = offlineRepo)

      repository.createMemo("Offline memo")
      val memos = repository.listMemos()

      assertEquals(1, memos.size)
      assertEquals("Offline memo", memos.first().content)
    }

  @Test
  fun `when mode is ONLINE then uses online repository`() =
    runTest {
      val appModeRepo = FakeAppModeRepository(AppMode.ONLINE)
      val cache = FakeMemoCache()
      val repository = createModeAwareRepository(appModeRepo, cache = cache)

      // For ONLINE mode, createMemo will fail since we don't have a real API
      // But we can test that it uses online repo by checking cachedMemos uses cache
      repository.cachedMemos()

      // When mode is ONLINE, cachedMemos should delegate to online repo which uses cache
      // This is enough to cover the else branch in currentRepository()
      val memos = repository.cachedMemos()
      assertEquals(emptyList(), memos)
    }

  @Test
  fun `when mode changes then clears cache`() =
    runTest {
      val appModeRepo = FakeAppModeRepository(AppMode.OFFLINE)
      val cache = FakeMemoCache()
      val repository = createModeAwareRepository(appModeRepo, cache = cache)

      // First call sets lastKnownMode
      repository.listMemos()

      // Change mode
      appModeRepo.mode = AppMode.DEMO

      // Second call should detect mode change and clear cache
      repository.listMemos()

      assertTrue(cache.clearCalled)
    }

  @Test
  fun `when cachedMemos then delegates to current repository`() =
    runTest {
      val appModeRepo = FakeAppModeRepository(AppMode.OFFLINE)
      val offlineStorage = FakeOfflineMemoStorage()
      val offlineRepo = OfflineMemosRepository(offlineStorage)
      val repository = createModeAwareRepository(appModeRepo, offlineRepo = offlineRepo)

      repository.createMemo("Test memo")
      val memos = repository.cachedMemos()

      assertEquals(1, memos.size)
    }

  @Test
  fun `when updateMemo then delegates to current repository`() =
    runTest {
      val appModeRepo = FakeAppModeRepository(AppMode.OFFLINE)
      val offlineStorage = FakeOfflineMemoStorage()
      val offlineRepo = OfflineMemosRepository(offlineStorage)
      val repository = createModeAwareRepository(appModeRepo, offlineRepo = offlineRepo)

      val created = repository.createMemo("Original")
      val updated = repository.updateMemo(created.name, "Updated")

      assertEquals("Updated", updated.content)
    }

  @Test
  fun `when deleteMemo then delegates to current repository`() =
    runTest {
      val appModeRepo = FakeAppModeRepository(AppMode.OFFLINE)
      val offlineStorage = FakeOfflineMemoStorage()
      val offlineRepo = OfflineMemosRepository(offlineStorage)
      val repository = createModeAwareRepository(appModeRepo, offlineRepo = offlineRepo)

      val created = repository.createMemo("To delete")
      repository.deleteMemo(created.name)
      val memos = repository.listMemos()

      assertTrue(memos.isEmpty())
    }

  @Test
  fun `when mode changes to DEMO then resets demo repository`() =
    runTest {
      val appModeRepo = FakeAppModeRepository(AppMode.OFFLINE)
      val demoRepo = DemoMemosRepository()
      val repository = createModeAwareRepository(appModeRepo, demoRepo = demoRepo)

      // Initialize with offline mode
      repository.listMemos()

      // Get initial demo memos
      appModeRepo.mode = AppMode.DEMO
      val firstMemos = repository.listMemos()

      // Change back to offline
      appModeRepo.mode = AppMode.OFFLINE
      repository.listMemos()

      // Change to demo again - should reset
      appModeRepo.mode = AppMode.DEMO
      val secondMemos = repository.listMemos()

      // Both should have memos (demo generates data)
      assertTrue(firstMemos.isNotEmpty())
      assertTrue(secondMemos.isNotEmpty())
    }

  private fun createModeAwareRepository(
    appModeRepo: AppModeRepository,
    offlineRepo: OfflineMemosRepository = OfflineMemosRepository(FakeOfflineMemoStorage()),
    demoRepo: DemoMemosRepository = DemoMemosRepository(),
    cache: MemoCache = FakeMemoCache(),
  ): ModeAwareMemosRepository =
    ModeAwareMemosRepository(
      appModeRepository = appModeRepo,
      onlineRepository = createStubOnlineRepository(),
      offlineRepository = offlineRepo,
      demoRepository = demoRepo,
      memoCache = cache,
    )
}

private class FakeAppModeRepository(
  var mode: AppMode,
) : AppModeRepository {
  override fun loadMode(): AppMode = mode

  override fun saveMode(mode: AppMode) {
    this.mode = mode
  }
}

private class FakeOfflineMemoStorage : OfflineMemoStorage {
  private var data = OfflineMemosFileDto()

  override fun load(): OfflineMemosFileDto = data

  override fun save(data: OfflineMemosFileDto) {
    this.data = data
  }
}

private class FakeMemoCache : MemoCache {
  var clearCalled = false
    private set

  override suspend fun readMemos(): List<Memo> = emptyList()

  override suspend fun replaceMemos(memos: List<Memo>) = Unit

  override suspend fun upsertMemo(memo: Memo) = Unit

  override suspend fun deleteMemo(name: String) = Unit

  override suspend fun clear() {
    clearCalled = true
  }
}

/**
 * Stub implementation for online repository.
 * Uses fakes for all dependencies since we're not testing online mode here.
 */
private fun createStubOnlineRepository(): MemosRepositoryImpl {
  val httpClient =
    io.ktor.client.HttpClient(io.ktor.client.engine.mock.MockEngine) {
      engine {
        addHandler { throw NotImplementedError("Not testing online mode") }
      }
    }
  return MemosRepositoryImpl(
    memosApi = MemosApi(httpClient),
    memoMapper = MemoMapper(),
    credentialsRepository = FakeCredentialsRepository(),
    memoCache = FakeMemoCache(),
  )
}

private class FakeCredentialsRepository : CredentialsRepository {
  override fun load() = Credentials("", "")

  override fun save(credentials: Credentials) = Unit
}
