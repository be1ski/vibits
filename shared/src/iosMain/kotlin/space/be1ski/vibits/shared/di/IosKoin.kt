package space.be1ski.vibits.shared.di

import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import space.be1ski.vibits.shared.app.AppDependencies

private var koinApp: KoinApplication? = null

/**
 * Initializes Koin on iOS and returns AppDependencies.
 */
fun initKoinAndGetDependencies(): AppDependencies {
  val koin =
    if (koinApp == null) {
      startKoin { modules(sharedModule()) }.also { koinApp = it }.koin
    } else {
      koinApp!!.koin
    }
  return koin.createAppDependencies()
}
