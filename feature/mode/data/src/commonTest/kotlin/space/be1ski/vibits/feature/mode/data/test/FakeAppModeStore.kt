package space.be1ski.vibits.feature.mode.data.test

import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.mode.data.LocalAppMode
import space.be1ski.vibits.feature.mode.data.platform.AppModeStore

class FakeAppModeStore(
  initial: LocalAppMode = LocalAppMode(mode = AppMode.NOT_SELECTED),
) : AppModeStore {
  var stored: LocalAppMode = initial
    private set
  var saveCalls: Int = 0
    private set

  override fun load(): LocalAppMode = stored

  override fun save(mode: LocalAppMode) {
    stored = mode
    saveCalls += 1
  }
}
