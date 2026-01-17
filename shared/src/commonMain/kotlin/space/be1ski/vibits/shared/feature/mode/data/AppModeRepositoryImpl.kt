package space.be1ski.vibits.shared.feature.mode.data

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.mode.domain.repository.AppModeRepository

@Inject
class AppModeRepositoryImpl(
  private val store: AppModeStore,
) : AppModeRepository {
  override fun loadMode(): AppMode = store.load().mode

  override fun saveMode(mode: AppMode) {
    store.save(LocalAppMode(mode = mode))
  }
}
