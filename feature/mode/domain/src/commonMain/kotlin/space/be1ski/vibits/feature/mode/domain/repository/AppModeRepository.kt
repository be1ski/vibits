package space.be1ski.vibits.feature.mode.domain.repository

import space.be1ski.vibits.core.platform.mode.AppMode

interface AppModeRepository {
  fun loadMode(): AppMode

  fun saveMode(mode: AppMode)
}
