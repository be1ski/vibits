package space.be1ski.memos.shared.feature.mode.domain.usecase

import space.be1ski.memos.shared.feature.memos.data.ModeAwareMemosRepository
import space.be1ski.memos.shared.feature.mode.domain.model.AppMode
import space.be1ski.memos.shared.feature.mode.domain.repository.AppModeRepository

/**
 * Use case for switching app mode with cache invalidation.
 */
class SwitchAppModeUseCase(
  private val appModeRepository: AppModeRepository,
  private val modeAwareMemosRepository: ModeAwareMemosRepository
) {
  suspend operator fun invoke(mode: AppMode) {
    val currentMode = appModeRepository.loadMode()
    if (currentMode != mode) {
      modeAwareMemosRepository.clearCacheOnModeChange()
      appModeRepository.saveMode(mode)
    }
  }
}
