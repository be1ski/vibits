package space.be1ski.vibits.feature.mode.data.platform

import space.be1ski.vibits.feature.mode.data.LocalAppMode

interface AppModeStore {
  fun load(): LocalAppMode

  fun save(mode: LocalAppMode)
}

expect fun createAppModeStore(): AppModeStore
