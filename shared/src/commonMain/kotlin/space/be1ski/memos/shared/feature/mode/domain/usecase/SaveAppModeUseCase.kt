package space.be1ski.memos.shared.feature.mode.domain.usecase

import space.be1ski.memos.shared.feature.mode.domain.model.AppMode
import space.be1ski.memos.shared.feature.mode.domain.repository.AppModeRepository

class SaveAppModeUseCase(
  private val appModeRepository: AppModeRepository
) {
  operator fun invoke(mode: AppMode) = appModeRepository.saveMode(mode)
}
