package space.be1ski.vibits.feature.main.di

import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.createGraph
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import space.be1ski.vibits.core.platform.app.AppDetailsProvider
import space.be1ski.vibits.core.platform.di.AppScope
import space.be1ski.vibits.core.platform.env.LocalConfigProvider
import space.be1ski.vibits.core.platform.env.createLocalConfigProvider
import space.be1ski.vibits.core.platform.export.FileExporter
import space.be1ski.vibits.core.platform.export.createFileExporter
import space.be1ski.vibits.core.platform.locale.LocaleProvider
import space.be1ski.vibits.core.platform.network.createHttpClient
import space.be1ski.vibits.core.platform.storage.createKeyValueStore
import space.be1ski.vibits.feature.auth.data.CredentialsRepositoryImpl
import space.be1ski.vibits.feature.auth.data.platform.CredentialsStore
import space.be1ski.vibits.feature.auth.data.platform.createCredentialsStore
import space.be1ski.vibits.feature.auth.domain.repository.CredentialsRepository
import space.be1ski.vibits.feature.main.AppInitializer
import space.be1ski.vibits.feature.memos.data.ConnectionTesterImpl
import space.be1ski.vibits.feature.memos.data.MemoStorageManagerImpl
import space.be1ski.vibits.feature.memos.data.ModeAwareMemosRepository
import space.be1ski.vibits.feature.memos.data.platform.MemoCache
import space.be1ski.vibits.feature.memos.data.platform.OfflineMemoStorage
import space.be1ski.vibits.feature.memos.data.platform.createMemoCache
import space.be1ski.vibits.feature.memos.data.platform.createOfflineMemoStorage
import space.be1ski.vibits.feature.memos.domain.repository.ConnectionTester
import space.be1ski.vibits.feature.memos.domain.repository.MemoStorageManager
import space.be1ski.vibits.feature.memos.domain.repository.MemosRepository
import space.be1ski.vibits.feature.mode.data.AppModeRepositoryImpl
import space.be1ski.vibits.feature.mode.data.platform.AppModeStore
import space.be1ski.vibits.feature.mode.data.platform.createAppModeStore
import space.be1ski.vibits.feature.mode.domain.repository.AppModeRepository
import space.be1ski.vibits.feature.onboarding.data.HabitPresetsDataSource
import space.be1ski.vibits.feature.onboarding.data.HabitPresetsDataSourceImpl
import space.be1ski.vibits.feature.onboarding.data.OnboardingRepositoryImpl
import space.be1ski.vibits.feature.onboarding.data.OnboardingStoreImpl
import space.be1ski.vibits.feature.onboarding.domain.repository.OnboardingRepository
import space.be1ski.vibits.feature.onboarding.domain.repository.OnboardingStore
import space.be1ski.vibits.feature.settings.data.PreferencesRepositoryImpl
import space.be1ski.vibits.feature.settings.data.PreferencesStore
import space.be1ski.vibits.feature.settings.data.PreferencesStoreImpl
import space.be1ski.vibits.feature.settings.domain.repository.PreferencesRepository
import space.be1ski.vibits.feature.sync.data.OfflineFirstMemosRepository
import space.be1ski.vibits.feature.sync.data.SyncEngineImpl
import space.be1ski.vibits.feature.sync.data.SyncQueueRepositoryImpl
import space.be1ski.vibits.feature.sync.data.platform.SyncOperationStore
import space.be1ski.vibits.feature.sync.data.platform.createSyncOperationStore
import space.be1ski.vibits.feature.sync.domain.SyncEngine
import space.be1ski.vibits.feature.sync.domain.repository.OfflineFirstMemoOperations
import space.be1ski.vibits.feature.sync.domain.repository.SyncQueueRepository

@Suppress("TooManyFunctions")
@SingleIn(AppScope::class)
@DependencyGraph(AppScope::class)
abstract class AppGraph {
  abstract val appDependencies: AppDependencies
  abstract val appFeaturesFactory: AppFeaturesFactory
  abstract val appCoroutineScope: CoroutineScope
  abstract val appInitializer: AppInitializer

  companion object {
    private var instance: AppGraph? = null

    private fun getGraph(): AppGraph = instance ?: createGraph<AppGraph>().also { instance = it }

    fun createAppDependencies(): AppDependencies = getGraph().appDependencies

    fun getFeaturesFactory(): AppFeaturesFactory = getGraph().appFeaturesFactory

    fun getAppScope(): CoroutineScope = getGraph().appCoroutineScope

    fun initializeApp() {
      getGraph().appInitializer()
    }

    fun resetGraph() {
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

  // Repository bindings (explicit @Binds needed for native targets due to KT-75865)
  @Binds
  abstract fun bindAppModeRepository(impl: AppModeRepositoryImpl): AppModeRepository

  @Binds
  abstract fun bindConnectionTester(impl: ConnectionTesterImpl): ConnectionTester

  @Binds
  abstract fun bindCredentialsRepository(impl: CredentialsRepositoryImpl): CredentialsRepository

  @Binds
  abstract fun bindMemoStorageManager(impl: MemoStorageManagerImpl): MemoStorageManager

  @Binds
  abstract fun bindMemosRepository(impl: ModeAwareMemosRepository): MemosRepository

  @Binds
  abstract fun bindOnboardingRepository(impl: OnboardingRepositoryImpl): OnboardingRepository

  @Binds
  abstract fun bindPreferencesRepository(impl: PreferencesRepositoryImpl): PreferencesRepository

  @Binds
  abstract fun bindSyncEngine(impl: SyncEngineImpl): SyncEngine

  @Binds
  abstract fun bindSyncQueueRepository(impl: SyncQueueRepositoryImpl): SyncQueueRepository

  @Binds
  abstract fun bindHabitPresetsDataSource(impl: HabitPresetsDataSourceImpl): HabitPresetsDataSource

  @Binds
  abstract fun bindOfflineFirstMemoOperations(impl: OfflineFirstMemosRepository): OfflineFirstMemoOperations
}
