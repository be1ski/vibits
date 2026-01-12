package space.be1ski.memos.android

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import space.be1ski.memos.shared.data.local.AndroidContextHolder
import space.be1ski.memos.shared.di.sharedModule

/**
 * Android Application that initializes shared DI and context.
 */
class MemosApplication : Application() {
  /**
   * Initializes Koin and shared Android context holder.
   */
  override fun onCreate() {
    super.onCreate()
    AndroidContextHolder.set(this)
    startKoin {
      androidContext(this@MemosApplication)
      modules(sharedModule())
    }
  }
}
