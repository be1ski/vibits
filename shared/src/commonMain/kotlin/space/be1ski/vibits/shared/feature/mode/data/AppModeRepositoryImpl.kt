package space.be1ski.vibits.shared.feature.mode.data

import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.mode.domain.repository.AppModeRepository

class AppModeRepositoryImpl(
  private val store: AppModeStore,
) : AppModeRepository {
  override fun loadMode(): AppMode = store.load().mode

  override fun saveMode(mode: AppMode) {
    store.save(LocalAppMode(mode = mode))
  }
}
