package space.be1ski.vibits.android

import android.app.Application
import space.be1ski.vibits.shared.data.local.AndroidContextHolder

/**
 * Android Application that initializes shared context.
 */
class VibitsApplication : Application() {
  /**
   * Initializes shared Android context holder.
   */
  override fun onCreate() {
    super.onCreate()
    AndroidContextHolder.set(this)
  }
}
