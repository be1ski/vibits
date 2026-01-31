package space.be1ski.vibits.shared.feature.memos.data

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import space.be1ski.vibits.shared.app.di.AppScope
import space.be1ski.vibits.shared.core.logging.Log
import space.be1ski.vibits.shared.feature.memos.data.demo.DemoMemosRepository
import space.be1ski.vibits.shared.feature.memos.data.offline.OfflineMemosRepository
import space.be1ski.vibits.shared.feature.memos.data.platform.MemoCache
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.memos.domain.repository.MemosRepository
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.mode.domain.repository.AppModeRepository
import space.be1ski.vibits.shared.feature.sync.data.OfflineFirstMemosRepository as OfflineFirstRepo

private const val TAG = "ModeAwareRepo"

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class ModeAwareMemosRepository(
  private val appModeRepository: AppModeRepository,
  private val onlineRepository: MemosRepositoryImpl,
  private val offlineRepository: OfflineMemosRepository,
  private val demoRepository: DemoMemosRepository,
  private val memoCache: MemoCache,
  private val offlineFirstRepository: OfflineFirstRepo,
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
      memoCache.clear()
      if (currentMode == AppMode.DEMO) {
        demoRepository.reset()
      }
    }
    lastKnownMode = currentMode
  }
}
