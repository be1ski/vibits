package space.be1ski.vibits.feature.mode.domain.usecase

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.mode.domain.repository.AppModeRepository

@Inject
class SwitchAppModeUseCase(
  private val appModeRepository: AppModeRepository,
) {
  operator fun invoke(mode: AppMode): Boolean {
    val currentMode = appModeRepository.loadMode()
    if (currentMode != mode) {
      appModeRepository.saveMode(mode)
      return true
    }
    return false
  }
}
