package space.be1ski.vibits.feature.mode.data.platform

import platform.Foundation.NSUserDefaults
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.mode.data.LocalAppMode

actual fun createAppModeStore(): AppModeStore = IosAppModeStore()

private class IosAppModeStore : AppModeStore {
  private val defaults = NSUserDefaults.standardUserDefaults
  private val keyMode = "app_mode"

  override fun load(): LocalAppMode {
    val modeName = defaults.stringForKey(keyMode)
    val mode = modeName?.let { runCatching { AppMode.valueOf(it) }.getOrNull() } ?: AppMode.NOT_SELECTED
    return LocalAppMode(mode = mode)
  }

  override fun save(mode: LocalAppMode) {
    defaults.setObject(mode.mode.name, forKey = keyMode)
  }
}
