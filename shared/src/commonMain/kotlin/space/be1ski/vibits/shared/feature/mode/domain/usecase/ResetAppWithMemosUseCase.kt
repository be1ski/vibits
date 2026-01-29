package space.be1ski.vibits.shared.feature.mode.domain.usecase

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.shared.feature.memos.data.offline.OfflineMemosFileDto
import space.be1ski.vibits.shared.feature.memos.data.platform.OfflineMemoStorage

@Inject
class ResetAppWithMemosUseCase(
  private val resetApp: ResetAppUseCase,
  private val offlineMemoStorage: OfflineMemoStorage,
) {
  operator fun invoke() {
    // First reset app (credentials, preferences, mode, onboarding)
    resetApp()
    // Then clear all memos from offline storage
    offlineMemoStorage.save(OfflineMemosFileDto(memos = emptyList()))
  }
}
