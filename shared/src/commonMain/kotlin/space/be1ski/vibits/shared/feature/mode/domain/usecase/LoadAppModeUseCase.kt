package space.be1ski.vibits.shared.feature.mode.domain.usecase

import javax.inject.Inject
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.mode.domain.repository.AppModeRepository

class LoadAppModeUseCase @Inject constructor(
  private val appModeRepository: AppModeRepository,
) {
  operator fun invoke(): AppMode = appModeRepository.loadMode()
}
