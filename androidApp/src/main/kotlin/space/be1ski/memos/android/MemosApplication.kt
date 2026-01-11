package space.be1ski.memos.android

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import space.be1ski.memos.shared.di.sharedModule

class MemosApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    startKoin {
      androidContext(this@MemosApplication)
      modules(sharedModule())
    }
  }
}
