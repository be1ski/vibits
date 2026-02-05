package space.be1ski.vibits.feature.mode.domain.test

import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.mode.domain.repository.AppModeRepository

class FakeAppModeRepository(
  initial: AppMode = AppMode.NOT_SELECTED,
) : AppModeRepository {
  var storedMode: AppMode = initial
    private set
  var saveCalls: Int = 0
    private set

  override fun loadMode(): AppMode = storedMode

  override fun saveMode(mode: AppMode) {
    storedMode = mode
    saveCalls += 1
  }
}
