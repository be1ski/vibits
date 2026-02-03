package space.be1ski.vibits.feature.mode.domain.usecase

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.feature.memos.domain.repository.MemoStorageManager

@Inject
class ResetAppWithMemosUseCase(
  private val resetApp: ResetAppUseCase,
  private val memoStorageManager: MemoStorageManager,
) {
  operator fun invoke() {
    // First reset app (credentials, preferences, mode, onboarding)
    resetApp()
    // Then clear all memos from offline storage
    memoStorageManager.clearAll()
  }
}
