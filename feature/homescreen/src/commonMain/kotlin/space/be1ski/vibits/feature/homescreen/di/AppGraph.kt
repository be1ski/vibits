package space.be1ski.vibits.feature.homescreen.di

import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.createGraph
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import space.be1ski.vibits.core.env.BuildConfig
import space.be1ski.vibits.core.platform.app.AppDetailsProvider
import space.be1ski.vibits.core.platform.app.AppUpdater
import space.be1ski.vibits.core.platform.di.AppScope
import space.be1ski.vibits.core.platform.env.LocalConfigProvider
import space.be1ski.vibits.core.platform.env.createLocalConfigProvider
import space.be1ski.vibits.core.platform.export.FileExporter
import space.be1ski.vibits.core.platform.export.createFileExporter
import space.be1ski.vibits.core.platform.locale.LocaleProvider
import space.be1ski.vibits.core.platform.network.createHttpClient
import space.be1ski.vibits.core.platform.storage.createKeyValueStore
import space.be1ski.vibits.feature.auth.data.platform.CredentialsStore
import space.be1ski.vibits.feature.auth.data.platform.createCredentialsStore
import space.be1ski.vibits.feature.changelog.data.GitHubReleasesApi
import space.be1ski.vibits.feature.changelog.data.LastSeenVersionStoreImpl
import space.be1ski.vibits.feature.changelog.data.platform.createInstallationSource
import space.be1ski.vibits.feature.changelog.domain.repository.InstallationSource
import space.be1ski.vibits.feature.changelog.domain.repository.LastSeenVersionStore
import space.be1ski.vibits.feature.memos.data.platform.OfflineMemoStorage
import space.be1ski.vibits.feature.memos.data.platform.createMemoCache
import space.be1ski.vibits.feature.memos.data.platform.createOfflineMemoStorage
import space.be1ski.vibits.feature.memos.domain.repository.MemoCache
import space.be1ski.vibits.feature.mode.data.platform.AppModeStore
import space.be1ski.vibits.feature.mode.data.platform.createAppModeStore
import space.be1ski.vibits.feature.onboarding.data.OnboardingStoreImpl
import space.be1ski.vibits.feature.onboarding.domain.repository.OnboardingStore
import space.be1ski.vibits.feature.settings.data.PreferencesStore
import space.be1ski.vibits.feature.settings.data.PreferencesStoreImpl
import space.be1ski.vibits.feature.sync.data.platform.SyncOperationStore
import space.be1ski.vibits.feature.sync.data.platform.createSyncOperationStore

@Suppress("TooManyFunctions")
@SingleIn(AppScope::class)
@DependencyGraph(AppScope::class)
abstract class AppGraph {
  abstract val appDependencies: AppDependencies
  abstract val appFeaturesFactory: AppFeaturesFactory
  abstract val appCoroutineScope: CoroutineScope

  companion object {
    private var instance: AppGraph? = null

    private fun getGraph(): AppGraph = instance ?: createGraph<AppGraph>().also { instance = it }

    fun createAppDependencies(): AppDependencies = getGraph().appDependencies

    fun getFeaturesFactory(): AppFeaturesFactory = getGraph().appFeaturesFactory

    fun getAppScope(): CoroutineScope = getGraph().appCoroutineScope

    fun resetGraph() {
      instance?.appCoroutineScope?.cancel()
      instance = null
    }
  }

  @Provides
  @SingleIn(AppScope::class)
  fun httpClient(): HttpClient = createHttpClient()

  @Provides
  @SingleIn(AppScope::class)
  fun credentialsStore(): CredentialsStore = createCredentialsStore()

  @Provides
  @SingleIn(AppScope::class)
  fun memoCache(): MemoCache = createMemoCache()

  @Provides
  @SingleIn(AppScope::class)
  fun preferencesStore(): PreferencesStore = PreferencesStoreImpl(createKeyValueStore())

  @Provides
  @SingleIn(AppScope::class)
  fun localeProvider(): LocaleProvider = LocaleProvider()

  @Provides
  @SingleIn(AppScope::class)
  fun appDetailsProvider(): AppDetailsProvider = AppDetailsProvider()

  @Provides
  @SingleIn(AppScope::class)
  fun appModeStore(): AppModeStore = createAppModeStore()

  @Provides
  @SingleIn(AppScope::class)
  fun offlineMemoStorage(): OfflineMemoStorage = createOfflineMemoStorage()

  @Provides
  @SingleIn(AppScope::class)
  fun fileExporter(): FileExporter = createFileExporter()

  @Provides
  @SingleIn(AppScope::class)
  fun localConfigProvider(): LocalConfigProvider = createLocalConfigProvider()

  @Provides
  @SingleIn(AppScope::class)
  fun onboardingStore(): OnboardingStore = OnboardingStoreImpl(createKeyValueStore())

  @Provides
  @SingleIn(AppScope::class)
  fun appCoroutineScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

  @Provides
  @SingleIn(AppScope::class)
  fun syncOperationStore(): SyncOperationStore = createSyncOperationStore()

  @Provides
  @SingleIn(AppScope::class)
  fun lastSeenVersionStore(): LastSeenVersionStore = LastSeenVersionStoreImpl(createKeyValueStore())

  @Provides
  @SingleIn(AppScope::class)
  fun gitHubReleasesApi(httpClient: HttpClient): GitHubReleasesApi = GitHubReleasesApi(httpClient, BuildConfig.RELEASES_URL)

  @Provides
  @SingleIn(AppScope::class)
  fun installationSource(): InstallationSource = createInstallationSource()

  @Provides
  @SingleIn(AppScope::class)
  fun appUpdater(): AppUpdater = AppUpdater()
}
