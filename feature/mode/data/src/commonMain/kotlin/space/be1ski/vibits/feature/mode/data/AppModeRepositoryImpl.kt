package space.be1ski.vibits.feature.mode.data

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import space.be1ski.vibits.core.platform.di.AppScope
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.mode.data.platform.AppModeStore
import space.be1ski.vibits.feature.mode.domain.repository.AppModeRepository

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
