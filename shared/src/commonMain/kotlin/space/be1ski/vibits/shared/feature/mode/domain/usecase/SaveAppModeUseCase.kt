package space.be1ski.vibits.shared.feature.mode.domain.usecase

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.mode.domain.repository.AppModeRepository

fun interface SaveAppMode {
  operator fun invoke(mode: AppMode)
}

@Inject
class SaveAppModeUseCase(
  private val appModeRepository: AppModeRepository,
) : SaveAppMode {
  override operator fun invoke(mode: AppMode) = appModeRepository.saveMode(mode)
}
