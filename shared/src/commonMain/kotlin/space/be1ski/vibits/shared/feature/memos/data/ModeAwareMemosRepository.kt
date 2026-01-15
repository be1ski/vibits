package space.be1ski.vibits.shared.feature.memos.data

import space.be1ski.vibits.shared.feature.memos.data.demo.DemoMemosRepository
import space.be1ski.vibits.shared.feature.memos.data.local.MemoCache
import space.be1ski.vibits.shared.feature.memos.data.offline.OfflineMemosRepository
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.memos.domain.repository.MemosRepository
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.mode.domain.repository.AppModeRepository

/**
 * Repository that delegates to online, offline, or demo implementation based on current mode.
 * Clears cache when switching modes to ensure data isolation.
 */
class ModeAwareMemosRepository(
  private val appModeRepository: AppModeRepository,
  private val onlineRepository: MemosRepositoryImpl,
  private val offlineRepository: OfflineMemosRepository,
  private val demoRepository: DemoMemosRepository,
  private val memoCache: MemoCache
) : MemosRepository {

  private var lastKnownMode: AppMode? = null

  /**
   * When true, all operations are delegated to the in-memory demo repository.
   */
  var demoMode: Boolean = false
    private set

  /**
   * Enables or disables demo mode.
   * When enabled, resets demo data to initial state.
   */
  fun setDemoMode(enabled: Boolean) {
    if (enabled && !demoMode) {
      demoRepository.reset()
    }
    demoMode = enabled
  }

  private fun currentRepository(): MemosRepository {
    if (demoMode) {
      return demoRepository
    }
    val currentMode = appModeRepository.loadMode()
    return when (currentMode) {
      AppMode.Offline -> offlineRepository
      else -> onlineRepository
    }
  }

  override suspend fun listMemos(): List<Memo> {
    checkModeChange()
    return currentRepository().listMemos()
  }

  override suspend fun cachedMemos(): List<Memo> {
    checkModeChange()
    return currentRepository().cachedMemos()
  }

  override suspend fun updateMemo(name: String, content: String): Memo {
    return currentRepository().updateMemo(name, content)
  }

  override suspend fun createMemo(content: String): Memo {
    return currentRepository().createMemo(content)
  }

  override suspend fun deleteMemo(name: String) {
    currentRepository().deleteMemo(name)
  }

  /**
   * Clears Room cache when mode changes to ensure data isolation between modes.
   */
  suspend fun clearCacheOnModeChange() {
    memoCache.clear()
  }

  private suspend fun checkModeChange() {
    val currentMode = appModeRepository.loadMode()
    if (lastKnownMode != null && lastKnownMode != currentMode) {
      memoCache.clear()
    }
    lastKnownMode = currentMode
  }
}
