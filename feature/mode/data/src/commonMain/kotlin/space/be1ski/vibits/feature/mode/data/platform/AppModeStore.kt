package space.be1ski.vibits.feature.mode.data.platform

import space.be1ski.vibits.feature.mode.data.LocalAppMode

expect class AppModeStore() {
  fun load(): LocalAppMode

  fun save(mode: LocalAppMode)
}
