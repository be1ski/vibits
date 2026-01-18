package space.be1ski.vibits.shared.feature.mode.data

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import space.be1ski.vibits.shared.app.di.AppScope
import space.be1ski.vibits.shared.feature.mode.data.platform.AppModeStore
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.mode.domain.repository.AppModeRepository

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class AppModeRepositoryImpl(
  private val store: AppModeStore,
) : AppModeRepository {
  override fun loadMode(): AppMode = store.load().mode

  override fun saveMode(mode: AppMode) {
    store.save(LocalAppMode(mode = mode))
  }
}
