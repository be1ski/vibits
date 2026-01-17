package space.be1ski.vibits.shared.di

import org.koin.core.module.Module
import org.koin.dsl.module
import space.be1ski.vibits.shared.core.network.createHttpClient
import space.be1ski.vibits.shared.data.local.AppDetailsProvider
import space.be1ski.vibits.shared.domain.usecase.LoadAppDetailsUseCase
import space.be1ski.vibits.shared.feature.auth.data.CredentialsRepositoryImpl
import space.be1ski.vibits.shared.feature.auth.data.CredentialsStore
import space.be1ski.vibits.shared.feature.auth.domain.repository.CredentialsRepository
import space.be1ski.vibits.shared.feature.auth.domain.usecase.LoadCredentialsUseCase
import space.be1ski.vibits.shared.feature.auth.domain.usecase.SaveCredentialsUseCase
import space.be1ski.vibits.shared.feature.auth.domain.usecase.ValidateCredentialsUseCase
import space.be1ski.vibits.shared.feature.habits.domain.usecase.BuildActivityDataUseCase
import space.be1ski.vibits.shared.feature.habits.domain.usecase.BuildHabitDayUseCase
import space.be1ski.vibits.shared.feature.habits.domain.usecase.CalculateSuccessRateUseCase
import space.be1ski.vibits.shared.feature.habits.domain.usecase.CountDailyPostsUseCase
import space.be1ski.vibits.shared.feature.habits.domain.usecase.DateCalculationsUseCase
import space.be1ski.vibits.shared.feature.habits.domain.usecase.ExtractDailyMemosUseCase
import space.be1ski.vibits.shared.feature.habits.domain.usecase.ExtractHabitsConfigUseCase
import space.be1ski.vibits.shared.feature.habits.domain.usecase.NavigateActivityRangeUseCase
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
import space.be1ski.vibits.shared.feature.mode.data.AppModeRepositoryImpl
import space.be1ski.vibits.shared.feature.mode.data.AppModeStore
import space.be1ski.vibits.shared.feature.mode.domain.repository.AppModeRepository
import space.be1ski.vibits.shared.feature.mode.domain.usecase.FixInvalidOnlineModeUseCase
import space.be1ski.vibits.shared.feature.mode.domain.usecase.LoadAppModeUseCase
import space.be1ski.vibits.shared.feature.mode.domain.usecase.ResetAppUseCase
import space.be1ski.vibits.shared.feature.mode.domain.usecase.SaveAppModeUseCase
import space.be1ski.vibits.shared.feature.mode.domain.usecase.SwitchAppModeUseCase
import space.be1ski.vibits.shared.feature.preferences.data.PreferencesRepositoryImpl
import space.be1ski.vibits.shared.feature.preferences.data.PreferencesStore
import space.be1ski.vibits.shared.feature.preferences.domain.repository.PreferencesRepository
import space.be1ski.vibits.shared.feature.preferences.domain.usecase.LoadPreferencesUseCase
import space.be1ski.vibits.shared.feature.preferences.domain.usecase.SaveTimeRangeTabUseCase

/**
 * Koin module that wires shared dependencies.
 */
fun sharedModule(): Module =
  module {
    single { createHttpClient() }
    single { CredentialsStore() }
    single { MemoCache() }
    single { PreferencesStore() }
    single { AppDetailsProvider() }
    single { MemoMapper() }
    single { MemosApi(get()) }
    single<CredentialsRepository> { CredentialsRepositoryImpl(get()) }
    single<PreferencesRepository> { PreferencesRepositoryImpl(get()) }
    single { AppModeStore() }
    single<AppModeRepository> { AppModeRepositoryImpl(get()) }
    single { OfflineMemoStorage() }
    single { MemosRepositoryImpl(get(), get(), get(), get()) }
    single { OfflineMemosRepository(get()) }
    single { DemoMemosRepository() }
    single { ModeAwareMemosRepository(get(), get(), get(), get(), get()) }
    single<MemosRepository> { get<ModeAwareMemosRepository>() }
    factory { LoadCachedMemosUseCase(get()) }
    factory { LoadMemosUseCase(get()) }
    factory { LoadCredentialsUseCase(get()) }
    factory { LoadPreferencesUseCase(get()) }
    factory { LoadAppDetailsUseCase(get()) }
    factory { SaveCredentialsUseCase(get()) }
    factory { ValidateCredentialsUseCase(get()) }
    factory { SaveTimeRangeTabUseCase(get()) }
    factory { CreateMemoUseCase(get()) }
    factory { UpdateMemoUseCase(get()) }
    factory { DeleteMemoUseCase(get()) }
    factory { LoadAppModeUseCase(get()) }
    factory { SaveAppModeUseCase(get()) }
    factory { SwitchAppModeUseCase(get(), get()) }
    factory { ResetAppUseCase(get(), get(), get(), get(), get()) }
    factory { FixInvalidOnlineModeUseCase(get(), get(), get()) }
    factory { CalculateSuccessRateUseCase() }
    factory { ExtractDailyMemosUseCase() }
    factory { ExtractHabitsConfigUseCase() }
    factory { CountDailyPostsUseCase() }
    factory { NavigateActivityRangeUseCase() }
    factory { DateCalculationsUseCase() }
    factory { BuildActivityDataUseCase() }
    factory { BuildHabitDayUseCase() }
  }
