package space.be1ski.vibits.feature.memos.data

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import space.be1ski.vibits.core.logging.Log
import space.be1ski.vibits.core.platform.di.AppScope
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.memos.data.demo.DemoMemosRepository
import space.be1ski.vibits.feature.memos.data.offline.OfflineMemosRepository
import space.be1ski.vibits.feature.memos.data.platform.MemoCache
import space.be1ski.vibits.feature.memos.domain.model.Memo
import space.be1ski.vibits.feature.memos.domain.repository.MemosRepository
import space.be1ski.vibits.feature.mode.domain.repository.AppModeRepository
import space.be1ski.vibits.feature.sync.domain.SyncLogTags
import space.be1ski.vibits.feature.sync.domain.repository.OfflineFirstMemoOperations

private val TAG = SyncLogTags.MODE_AWARE_REPO

@Inject
@SingleIn(AppScope::class)
class ModeAwareMemosRepository(
  private val appModeRepository: AppModeRepository,
  private val onlineRepository: MemosRepositoryImpl,
  private val offlineRepository: OfflineMemosRepository,
  private val demoRepository: DemoMemosRepository,
  private val memoCache: MemoCache,
  private val offlineFirstRepository: OfflineFirstMemoOperations,
) : MemosRepository {
  private var lastKnownMode: AppMode? = null

  private fun currentRepository(): MemosRepository {
    val currentMode = appModeRepository.loadMode()
    return when (currentMode) {
      AppMode.DEMO -> demoRepository
      AppMode.OFFLINE -> offlineRepository
      else -> onlineRepository
    }
  }

  override suspend fun listMemos(): List<Memo> {
    checkModeChange()
    return currentRepository().listMemos()
  }

  override suspend fun cachedMemos(): List<Memo> {
    checkModeChange()
    return when (appModeRepository.loadMode()) {
      AppMode.ONLINE -> offlineFirstRepository.getCachedMemos()
      else -> currentRepository().cachedMemos()
    }
  }

  override suspend fun updateMemo(
    name: String,
    content: String,
  ): Memo {
    return when (appModeRepository.loadMode()) {
      AppMode.ONLINE -> {
        Log.d(TAG, "Offline-first update: $name")
        offlineFirstRepository.updateMemoLocally(name, content)
      }
      else -> currentRepository().updateMemo(name, content)
    }
  }

  override suspend fun createMemo(content: String): Memo {
    return when (appModeRepository.loadMode()) {
      AppMode.ONLINE -> {
        Log.d(TAG, "Offline-first create")
        offlineFirstRepository.createMemoLocally(content)
      }
      else -> currentRepository().createMemo(content)
    }
  }

  override suspend fun deleteMemo(name: String) {
    when (appModeRepository.loadMode()) {
      AppMode.ONLINE -> {
        Log.d(TAG, "Offline-first delete: $name")
        offlineFirstRepository.deleteMemoLocally(name)
      }
      else -> currentRepository().deleteMemo(name)
    }
  }

  private suspend fun checkModeChange() {
    val currentMode = appModeRepository.loadMode()
    if (lastKnownMode != null && lastKnownMode != currentMode) {
      Log.i(TAG, "Mode changed: $lastKnownMode -> $currentMode")

      // When switching FROM ONLINE mode, clear the entire online data
      // (cache + pending operations) to prevent data leakage
      if (lastKnownMode == AppMode.ONLINE) {
        Log.i(TAG, "Clearing online data on mode switch")
        offlineFirstRepository.clearOnlineData()
      } else {
        memoCache.clear()
      }

      if (currentMode == AppMode.DEMO) {
        demoRepository.reset()
      }
    }
    lastKnownMode = currentMode
  }
}
