package space.be1ski.vibits.shared.feature.mode.data

import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode

data class LocalAppMode(
  val mode: AppMode
)

expect class AppModeStore() {
  fun load(): LocalAppMode
  fun save(mode: LocalAppMode)
}
