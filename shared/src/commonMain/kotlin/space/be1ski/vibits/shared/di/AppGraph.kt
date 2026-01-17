package space.be1ski.vibits.shared.di

import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.createGraph
import io.ktor.client.HttpClient
import space.be1ski.vibits.shared.app.AppDependencies
import space.be1ski.vibits.shared.app.ModeSelectionUseCases
import space.be1ski.vibits.shared.app.VibitsAppDependencies
import space.be1ski.vibits.shared.core.network.createHttpClient
import space.be1ski.vibits.shared.core.platform.LocaleProvider
import space.be1ski.vibits.shared.data.local.AppDetailsProvider
import space.be1ski.vibits.shared.domain.usecase.LoadAppDetailsUseCase
import space.be1ski.vibits.shared.feature.auth.data.CredentialsRepositoryImpl
import space.be1ski.vibits.shared.feature.auth.data.CredentialsStore
import space.be1ski.vibits.shared.feature.auth.domain.repository.CredentialsRepository
import space.be1ski.vibits.shared.feature.auth.domain.usecase.LoadCredentialsUseCase
import space.be1ski.vibits.shared.feature.auth.domain.usecase.SaveCredentialsUseCase
import space.be1ski.vibits.shared.feature.auth.domain.usecase.ValidateCredentialsUseCase
import space.be1ski.vibits.shared.feature.habits.domain.usecase.CalculateSuccessRateUseCase
import space.be1ski.vibits.shared.feature.memos.data.MemosRepositoryImpl
import space.be1ski.vibits.shared.feature.memos.data.ModeAwareMemosRepository
import space.be1ski.vibits.shared.feature.memos.data.demo.DemoMemosRepository
import space.be1ski.vibits.shared.feature.memos.data.local.MemoCache
import space.be1ski.vibits.shared.feature.memos.data.mapper.MemoMapper
import space.be1ski.vibits.shared.feature.memos.data.offline.OfflineMemoStorage
import space.be1ski.vibits.shared.feature.memos.data.offline.OfflineMemosRepository
import space.be1ski.vibits.shared.feature.memos.data.remote.MemosApi
import space.be1ski.vibits.shared.feature.memos.domain.repository.MemosRepository
import space.be1ski.vibits.shared.feature.memos.domain.usecase.CreateMemoUseCase
import space.be1ski.vibits.shared.feature.memos.domain.usecase.DeleteMemoUseCase
import space.be1ski.vibits.shared.feature.memos.domain.usecase.LoadCachedMemosUseCase
import space.be1ski.vibits.shared.feature.memos.domain.usecase.LoadMemosUseCase
import space.be1ski.vibits.shared.feature.memos.domain.usecase.UpdateMemoUseCase
import space.be1ski.vibits.shared.feature.memos.presentation.MemosUseCases
import space.be1ski.vibits.shared.feature.mode.data.AppModeRepositoryImpl
import space.be1ski.vibits.shared.feature.mode.data.AppModeStore
import space.be1ski.vibits.shared.feature.mode.domain.repository.AppModeRepository
import space.be1ski.vibits.shared.feature.mode.domain.usecase.FixInvalidOnlineModeUseCase
import space.be1ski.vibits.shared.feature.mode.domain.usecase.LoadAppModeUseCase
import space.be1ski.vibits.shared.feature.mode.domain.usecase.ResetAppUseCase
import space.be1ski.vibits.shared.feature.mode.domain.usecase.SaveAppModeUseCase
import space.be1ski.vibits.shared.feature.mode.domain.usecase.SwitchAppModeUseCase
import space.be1ski.vibits.shared.feature.settings.data.PreferencesRepositoryImpl
import space.be1ski.vibits.shared.feature.settings.data.PreferencesStore
import space.be1ski.vibits.shared.feature.settings.domain.repository.PreferencesRepository
import space.be1ski.vibits.shared.feature.settings.domain.usecase.LoadPreferencesUseCase
import space.be1ski.vibits.shared.feature.settings.domain.usecase.SaveLanguageUseCase
import space.be1ski.vibits.shared.feature.settings.domain.usecase.SaveThemeUseCase
import space.be1ski.vibits.shared.feature.settings.domain.usecase.SaveTimeRangeTabUseCase
import space.be1ski.vibits.shared.feature.settings.presentation.SettingsUseCases

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

  // Grouped dependencies
  @Provides
  fun memosUseCases(
    loadMemos: LoadMemosUseCase,
    loadCachedMemos: LoadCachedMemosUseCase,
    loadCredentials: LoadCredentialsUseCase,
    saveCredentials: SaveCredentialsUseCase,
    createMemo: CreateMemoUseCase,
    updateMemo: UpdateMemoUseCase,
    deleteMemo: DeleteMemoUseCase,
  ): MemosUseCases =
    MemosUseCases(
      loadMemos = loadMemos,
      loadCachedMemos = loadCachedMemos,
      loadCredentials = loadCredentials,
      saveCredentials = saveCredentials,
      createMemo = createMemo,
      updateMemo = updateMemo,
      deleteMemo = deleteMemo,
    )

  @Provides
  fun settingsUseCases(
    validateCredentials: ValidateCredentialsUseCase,
    switchAppMode: SwitchAppModeUseCase,
    saveCredentials: SaveCredentialsUseCase,
    resetApp: ResetAppUseCase,
    saveLanguage: SaveLanguageUseCase,
    saveTheme: SaveThemeUseCase,
  ): SettingsUseCases =
    SettingsUseCases(
      validateCredentials = validateCredentials,
      switchAppMode = switchAppMode,
      saveCredentials = saveCredentials,
      resetApp = resetApp,
      saveLanguage = saveLanguage,
      saveTheme = saveTheme,
    )

  @Provides
  fun modeSelectionUseCases(
    validateCredentials: ValidateCredentialsUseCase,
    saveCredentials: SaveCredentialsUseCase,
    saveAppMode: SaveAppModeUseCase,
  ): ModeSelectionUseCases =
    ModeSelectionUseCases(
      validateCredentials = validateCredentials,
      saveCredentials = saveCredentials,
      saveAppMode = saveAppMode,
    )

  @Provides
  fun vibitsAppDependencies(
    loadPreferences: LoadPreferencesUseCase,
    saveTimeRangeTab: SaveTimeRangeTabUseCase,
    loadAppDetails: LoadAppDetailsUseCase,
    loadAppMode: LoadAppModeUseCase,
    calculateSuccessRate: CalculateSuccessRateUseCase,
    memosRepository: MemosRepository,
    memosUseCases: MemosUseCases,
    settingsUseCases: SettingsUseCases,
  ): VibitsAppDependencies =
    VibitsAppDependencies(
      loadPreferences = loadPreferences,
      saveTimeRangeTab = saveTimeRangeTab,
      loadAppDetails = loadAppDetails,
      loadAppMode = loadAppMode,
      calculateSuccessRate = calculateSuccessRate,
      memosRepository = memosRepository,
      memosUseCases = memosUseCases,
      settingsUseCases = settingsUseCases,
    )

  @Provides
  fun appDependencies(
    localeProvider: LocaleProvider,
    loadPreferences: LoadPreferencesUseCase,
    fixInvalidOnlineMode: FixInvalidOnlineModeUseCase,
    modeSelection: ModeSelectionUseCases,
    vibitsApp: VibitsAppDependencies,
  ): AppDependencies =
    AppDependencies(
      localeProvider = localeProvider,
      loadPreferences = loadPreferences,
      fixInvalidOnlineMode = fixInvalidOnlineMode,
      modeSelection = modeSelection,
      vibitsApp = vibitsApp,
    )
}
