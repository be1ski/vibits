package space.be1ski.vibits.shared.feature.mode.domain.usecase

import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.mode.domain.repository.AppModeRepository

class LoadAppModeUseCase(
  private val appModeRepository: AppModeRepository,
) {
  operator fun invoke(): AppMode = appModeRepository.loadMode()
}
