package space.be1ski.memos.shared.di

import org.koin.core.module.Module
import org.koin.dsl.module
import space.be1ski.memos.shared.data.local.CredentialsStore
import space.be1ski.memos.shared.data.mapper.MemoMapper
import space.be1ski.memos.shared.data.remote.MemosApi
import space.be1ski.memos.shared.data.remote.createHttpClient
import space.be1ski.memos.shared.data.repository.CredentialsRepositoryImpl
import space.be1ski.memos.shared.data.repository.MemosRepositoryImpl
import space.be1ski.memos.shared.domain.repository.CredentialsRepository
import space.be1ski.memos.shared.domain.repository.MemosRepository
import space.be1ski.memos.shared.domain.usecase.LoadCredentialsUseCase
import space.be1ski.memos.shared.domain.usecase.LoadMemosUseCase
import space.be1ski.memos.shared.domain.usecase.SaveCredentialsUseCase
import space.be1ski.memos.shared.presentation.MemosViewModel

/**
 * Koin module that wires shared dependencies.
 */
fun sharedModule(): Module = module {
  single { createHttpClient() }
  single { CredentialsStore() }
  single { MemoMapper() }
  single { MemosApi(get()) }
  single<CredentialsRepository> { CredentialsRepositoryImpl(get()) }
  single<MemosRepository> { MemosRepositoryImpl(get(), get(), get()) }
  factory { LoadMemosUseCase(get()) }
  factory { LoadCredentialsUseCase(get()) }
  factory { SaveCredentialsUseCase(get()) }
  factory { MemosViewModel(get(), get(), get()) }
}
