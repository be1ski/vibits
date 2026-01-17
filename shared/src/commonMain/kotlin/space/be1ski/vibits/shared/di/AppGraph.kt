package space.be1ski.vibits.shared.di

import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.createGraph
import io.ktor.client.HttpClient
import space.be1ski.vibits.shared.app.AppDependencies
import space.be1ski.vibits.shared.core.network.createHttpClient
import space.be1ski.vibits.shared.core.platform.LocaleProvider
import space.be1ski.vibits.shared.data.local.AppDetailsProvider
import space.be1ski.vibits.shared.feature.auth.data.CredentialsRepositoryImpl
import space.be1ski.vibits.shared.feature.auth.data.CredentialsStore
import space.be1ski.vibits.shared.feature.auth.domain.repository.CredentialsRepository
import space.be1ski.vibits.shared.feature.memos.data.ModeAwareMemosRepository
import space.be1ski.vibits.shared.feature.memos.data.demo.DemoMemosRepository
import space.be1ski.vibits.shared.feature.memos.data.local.MemoCache
import space.be1ski.vibits.shared.feature.memos.data.mapper.MemoMapper
import space.be1ski.vibits.shared.feature.memos.data.offline.OfflineMemoStorage
import space.be1ski.vibits.shared.feature.memos.data.remote.MemosApi
import space.be1ski.vibits.shared.feature.memos.domain.repository.MemosRepository
import space.be1ski.vibits.shared.feature.mode.data.AppModeRepositoryImpl
import space.be1ski.vibits.shared.feature.mode.data.AppModeStore
import space.be1ski.vibits.shared.feature.mode.domain.repository.AppModeRepository
import space.be1ski.vibits.shared.feature.settings.data.PreferencesRepositoryImpl
import space.be1ski.vibits.shared.feature.settings.data.PreferencesStore
import space.be1ski.vibits.shared.feature.settings.domain.repository.PreferencesRepository

/**
 * Metro dependency graph for the application.
 */
@Suppress("TooManyFunctions", "LongParameterList")
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

  @Provides
  @SingleIn(AppScope::class)
  fun memoMapper(): MemoMapper = MemoMapper()

  @Provides
  @SingleIn(AppScope::class)
  fun memosApi(httpClient: HttpClient): MemosApi = MemosApi(httpClient)

  @Provides
  @SingleIn(AppScope::class)
  fun demoMemosRepository(): DemoMemosRepository = DemoMemosRepository()

  // Repository bindings (interface -> implementation)
  @Binds
  abstract val CredentialsRepositoryImpl.bindCredentialsRepository: CredentialsRepository

  @Binds
  abstract val PreferencesRepositoryImpl.bindPreferencesRepository: PreferencesRepository

  @Binds
  abstract val AppModeRepositoryImpl.bindAppModeRepository: AppModeRepository

  @Binds
  abstract val ModeAwareMemosRepository.bindMemosRepository: MemosRepository
}
