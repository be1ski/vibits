package space.be1ski.vibits.shared.app.di

import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.createGraph
import io.ktor.client.HttpClient
import space.be1ski.vibits.shared.core.platform.app.AppDetailsProvider
import space.be1ski.vibits.shared.core.platform.export.FileExporter
import space.be1ski.vibits.shared.core.platform.export.createFileExporter
import space.be1ski.vibits.shared.core.platform.locale.LocaleProvider
import space.be1ski.vibits.shared.core.platform.network.createHttpClient
import space.be1ski.vibits.shared.feature.auth.data.platform.CredentialsStore
import space.be1ski.vibits.shared.feature.memos.data.platform.MemoCache
import space.be1ski.vibits.shared.feature.memos.data.platform.OfflineMemoStorage
import space.be1ski.vibits.shared.feature.memos.data.platform.createMemoCache
import space.be1ski.vibits.shared.feature.memos.data.platform.createOfflineMemoStorage
import space.be1ski.vibits.shared.feature.mode.data.platform.AppModeStore
import space.be1ski.vibits.shared.feature.settings.data.PreferencesStore
import space.be1ski.vibits.shared.feature.settings.data.createPreferencesStore

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
  fun memoCache(): MemoCache = createMemoCache()

  @Provides
  @SingleIn(AppScope::class)
  fun preferencesStore(): PreferencesStore = createPreferencesStore()

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
  fun offlineMemoStorage(): OfflineMemoStorage = createOfflineMemoStorage()

  @Provides
  @SingleIn(AppScope::class)
  fun fileExporter(): FileExporter = createFileExporter()
}
