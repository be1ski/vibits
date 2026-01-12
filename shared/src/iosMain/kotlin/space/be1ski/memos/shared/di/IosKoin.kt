package space.be1ski.memos.shared.di

import org.koin.core.context.startKoin

/**
 * Initializes Koin on iOS if it hasn't been started yet.
 */
fun initKoin() {
  runCatching {
    startKoin { modules(sharedModule()) }
  }
}
