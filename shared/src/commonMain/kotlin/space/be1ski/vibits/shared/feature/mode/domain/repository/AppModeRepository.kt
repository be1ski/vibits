package space.be1ski.vibits.shared.feature.mode.domain.repository

import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode

interface AppModeRepository {
  fun loadMode(): AppMode
  fun saveMode(mode: AppMode)
}
