package space.be1ski.vibits.shared.app.di
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.createGraph
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import space.be1ski.vibits.shared.app.AppInitializer
import space.be1ski.vibits.shared.app.presentation.action.AppAction
import space.be1ski.vibits.shared.app.presentation.effect.AppEffect
import space.be1ski.vibits.shared.app.presentation.reducer.appReducer
import space.be1ski.vibits.shared.core.platform.app.AppDetailsProvider
import space.be1ski.vibits.shared.core.platform.env.LocalConfigProvider
import space.be1ski.vibits.shared.core.platform.env.createLocalConfigProvider
import space.be1ski.vibits.shared.core.platform.export.FileExporter
import space.be1ski.vibits.shared.core.platform.export.createFileExporter
import space.be1ski.vibits.shared.core.platform.locale.LocaleProvider
import space.be1ski.vibits.shared.core.platform.network.createHttpClient
import space.be1ski.vibits.shared.core.platform.storage.createKeyValueStore
import space.be1ski.vibits.shared.feature.auth.data.platform.CredentialsStore
import space.be1ski.vibits.shared.feature.memos.data.platform.MemoCache
import space.be1ski.vibits.shared.feature.memos.data.platform.OfflineMemoStorage
import space.be1ski.vibits.shared.feature.memos.data.platform.createMemoCache
import space.be1ski.vibits.shared.feature.memos.data.platform.createOfflineMemoStorage
import space.be1ski.vibits.shared.feature.mode.data.platform.AppModeStore
import space.be1ski.vibits.shared.feature.onboarding.data.OnboardingStore
import space.be1ski.vibits.shared.feature.onboarding.data.OnboardingStoreImpl
import space.be1ski.vibits.shared.feature.settings.data.PreferencesStore
import space.be1ski.vibits.shared.feature.settings.data.PreferencesStoreImpl

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
  fun credentialsStore(): CredentialsStore = CredentialsStore()

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
  fun appModeStore(): AppModeStore = AppModeStore()

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
}
