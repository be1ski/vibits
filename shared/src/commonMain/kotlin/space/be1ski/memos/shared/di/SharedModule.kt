package space.be1ski.memos.shared.di

import org.koin.core.module.Module
import org.koin.dsl.module
import space.be1ski.memos.shared.feature.auth.data.CredentialsStore
import space.be1ski.memos.shared.feature.memos.data.local.MemoCache
import space.be1ski.memos.shared.feature.preferences.data.PreferencesStore
import space.be1ski.memos.shared.data.local.StorageInfoProvider
import space.be1ski.memos.shared.feature.memos.data.mapper.MemoMapper
import space.be1ski.memos.shared.feature.memos.data.remote.MemosApi
import space.be1ski.memos.shared.core.network.createHttpClient
import space.be1ski.memos.shared.feature.auth.data.CredentialsRepositoryImpl
import space.be1ski.memos.shared.feature.memos.data.MemosRepositoryImpl
import space.be1ski.memos.shared.feature.preferences.data.PreferencesRepositoryImpl
import space.be1ski.memos.shared.feature.auth.domain.repository.CredentialsRepository
import space.be1ski.memos.shared.feature.memos.domain.repository.MemosRepository
import space.be1ski.memos.shared.feature.preferences.domain.repository.PreferencesRepository
import space.be1ski.memos.shared.feature.auth.domain.usecase.LoadCredentialsUseCase
import space.be1ski.memos.shared.feature.memos.domain.usecase.LoadCachedMemosUseCase
import space.be1ski.memos.shared.feature.memos.domain.usecase.LoadMemosUseCase
import space.be1ski.memos.shared.feature.preferences.domain.usecase.LoadPreferencesUseCase
import space.be1ski.memos.shared.domain.usecase.LoadStorageInfoUseCase
import space.be1ski.memos.shared.feature.auth.domain.usecase.SaveCredentialsUseCase
import space.be1ski.memos.shared.feature.preferences.domain.usecase.SaveTimeRangeTabUseCase
import space.be1ski.memos.shared.feature.memos.domain.usecase.CreateMemoUseCase
import space.be1ski.memos.shared.feature.memos.domain.usecase.DeleteMemoUseCase
import space.be1ski.memos.shared.feature.memos.domain.usecase.UpdateMemoUseCase

/**
 * Koin module that wires shared dependencies.
 */
fun sharedModule(): Module = module {
  single { createHttpClient() }
  single { CredentialsStore() }
  single { MemoCache() }
  single { PreferencesStore() }
  single { StorageInfoProvider() }
  single { MemoMapper() }
  single { MemosApi(get()) }
  single<CredentialsRepository> { CredentialsRepositoryImpl(get()) }
  single<MemosRepository> { MemosRepositoryImpl(get(), get(), get(), get()) }
  single<PreferencesRepository> { PreferencesRepositoryImpl(get()) }
  factory { LoadCachedMemosUseCase(get()) }
  factory { LoadMemosUseCase(get()) }
  factory { LoadCredentialsUseCase(get()) }
  factory { LoadPreferencesUseCase(get()) }
  factory { LoadStorageInfoUseCase(get()) }
  factory { SaveCredentialsUseCase(get()) }
  factory { SaveTimeRangeTabUseCase(get()) }
  factory { CreateMemoUseCase(get()) }
  factory { UpdateMemoUseCase(get()) }
  factory { DeleteMemoUseCase(get()) }
}
