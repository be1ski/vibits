package space.be1ski.memos.shared.di

import org.koin.core.module.Module
import org.koin.dsl.module
import space.be1ski.memos.shared.config.CredentialsStore
import space.be1ski.memos.shared.data.MemosRepository
import space.be1ski.memos.shared.network.createHttpClient
import space.be1ski.memos.shared.ui.MemosViewModel

fun sharedModule(): Module = module {
  single { createHttpClient() }
  single { CredentialsStore() }
  single { MemosRepository(get()) }
  factory { MemosViewModel(get(), get()) }
}
