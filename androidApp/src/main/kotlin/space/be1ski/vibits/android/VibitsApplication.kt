package space.be1ski.vibits.android

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import space.be1ski.vibits.shared.data.local.AndroidContextHolder
import space.be1ski.vibits.shared.di.sharedModule

/**
 * Android Application that initializes shared DI and context.
 */
class VibitsApplication : Application() {
  /**
   * Initializes Koin and shared Android context holder.
   */
  override fun onCreate() {
    super.onCreate()
    AndroidContextHolder.set(this)
    startKoin {
      androidContext(this@VibitsApplication)
      modules(sharedModule())
    }
  }
}
