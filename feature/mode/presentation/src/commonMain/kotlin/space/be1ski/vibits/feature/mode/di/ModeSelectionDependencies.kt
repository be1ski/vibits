package space.be1ski.vibits.feature.mode.di

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.feature.auth.domain.usecase.InitializeCredentialsFromEnvUseCase
import space.be1ski.vibits.feature.auth.domain.usecase.LoadCredentialsUseCase
import space.be1ski.vibits.feature.auth.domain.usecase.LoadKeychainCredentialsUseCase
import space.be1ski.vibits.feature.auth.domain.usecase.SaveCredentialsUseCase
import space.be1ski.vibits.feature.memos.domain.repository.ConnectionTester
import space.be1ski.vibits.feature.mode.domain.usecase.SaveAppModeUseCase

@Inject
class ModeSelectionDependencies(
  val connectionTester: ConnectionTester,
  val initializeCredentialsFromEnv: InitializeCredentialsFromEnvUseCase,
  val loadCredentials: LoadCredentialsUseCase,
  val loadKeychainCredentials: LoadKeychainCredentialsUseCase,
  val saveCredentials: SaveCredentialsUseCase,
  val saveAppMode: SaveAppModeUseCase,
)
