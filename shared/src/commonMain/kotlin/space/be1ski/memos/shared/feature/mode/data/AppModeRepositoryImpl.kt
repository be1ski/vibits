package space.be1ski.memos.shared.feature.mode.data

import space.be1ski.memos.shared.feature.mode.domain.model.AppMode
import space.be1ski.memos.shared.feature.mode.domain.repository.AppModeRepository

class AppModeRepositoryImpl(
  private val store: AppModeStore
) : AppModeRepository {

  override fun loadMode(): AppMode {
    return store.load().mode
  }

  override fun saveMode(mode: AppMode) {
    store.save(LocalAppMode(mode = mode))
  }
}
