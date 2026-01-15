package space.be1ski.vibits.shared.feature.mode.data

import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode

/**
 * In-memory mode store for web builds.
 */
actual class AppModeStore {
  private var cached: LocalAppMode? = null

  actual fun load(): LocalAppMode {
    return cached ?: LocalAppMode(mode = AppMode.NotSelected)
  }

  actual fun save(mode: LocalAppMode) {
    cached = mode
  }
}
