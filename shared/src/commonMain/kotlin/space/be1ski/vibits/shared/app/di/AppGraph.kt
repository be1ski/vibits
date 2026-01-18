package space.be1ski.vibits.shared.app.di

import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.createGraph
import io.ktor.client.HttpClient
import space.be1ski.vibits.shared.core.network.createHttpClient
import space.be1ski.vibits.shared.core.platform.LocaleProvider
import space.be1ski.vibits.shared.data.local.AppDetailsProvider
import space.be1ski.vibits.shared.feature.auth.data.CredentialsStore
import space.be1ski.vibits.shared.feature.memos.data.local.MemoCache
import space.be1ski.vibits.shared.feature.memos.data.offline.OfflineMemoStorage
import space.be1ski.vibits.shared.feature.mode.data.AppModeStore
import space.be1ski.vibits.shared.feature.settings.data.PreferencesStore

/**
 * Metro dependency graph for the application.
 * Only expect/actual classes need @Provides here - all other bindings use @ContributesBinding.
 */
@SingleIn(AppScope::class)
@DependencyGraph(AppScope::class)
abstract class AppGraph {
  abstract val appDependencies: AppDependencies

  companion object {
    private var instance: AppGraph? = null

    fun createAppDependencies(): AppDependencies {
      val graph = instance ?: createGraph<AppGraph>().also { instance = it }
      return graph.appDependencies
    }
  }

  // Infrastructure - expect/actual classes need @Provides
  @Provides
  @SingleIn(AppScope::class)
  fun httpClient(): HttpClient = createHttpClient()

  @Provides
  @SingleIn(AppScope::class)
  fun credentialsStore(): CredentialsStore = CredentialsStore()

  @Provides
  @SingleIn(AppScope::class)
  fun memoCache(): MemoCache = MemoCache()

  @Provides
  @SingleIn(AppScope::class)
  fun preferencesStore(): PreferencesStore = PreferencesStore()

  @Provides
  @SingleIn(AppScope::class)
  fun localeProvider(): LocaleProvider = LocaleProvider()

  @Provides
  @SingleIn(AppScope::class)
  fun appDetailsProvider(): AppDetailsProvider = AppDetailsProvider()

  @Provides
  @SingleIn(AppScope::class)
  fun appModeStore(): AppModeStore = AppModeStore()

  @Provides
  @SingleIn(AppScope::class)
  fun offlineMemoStorage(): OfflineMemoStorage = OfflineMemoStorage()
}
