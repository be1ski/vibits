package space.be1ski.vibits.shared.feature.mode.domain.usecase

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.mode.domain.repository.AppModeRepository

@Inject
class LoadAppModeUseCase(
  private val appModeRepository: AppModeRepository,
) {
  operator fun invoke(): AppMode = appModeRepository.loadMode()
}
