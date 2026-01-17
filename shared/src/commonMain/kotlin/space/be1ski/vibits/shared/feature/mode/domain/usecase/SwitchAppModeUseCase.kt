package space.be1ski.vibits.shared.feature.mode.domain.usecase

import javax.inject.Inject
import space.be1ski.vibits.shared.feature.memos.data.ModeAwareMemosRepository
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.mode.domain.repository.AppModeRepository

/**
 * Use case for switching app mode with cache invalidation.
 */
class SwitchAppModeUseCase @Inject constructor(
  private val appModeRepository: AppModeRepository,
  private val modeAwareMemosRepository: ModeAwareMemosRepository,
) {
  suspend operator fun invoke(mode: AppMode) {
    val currentMode = appModeRepository.loadMode()
    if (currentMode != mode) {
      modeAwareMemosRepository.clearCacheOnModeChange()
      appModeRepository.saveMode(mode)
    }
  }
}
