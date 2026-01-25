package space.be1ski.vibits.shared.feature.mode.di

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.shared.feature.auth.domain.usecase.InitializeCredentialsFromEnvUseCase
import space.be1ski.vibits.shared.feature.auth.domain.usecase.LoadCredentialsUseCase
import space.be1ski.vibits.shared.feature.auth.domain.usecase.SaveCredentialsUseCase
import space.be1ski.vibits.shared.feature.memos.data.ConnectionTester
import space.be1ski.vibits.shared.feature.mode.domain.usecase.SaveAppModeUseCase

@Inject
class ModeSelectionDependencies(
  val connectionTester: ConnectionTester,
  val initializeCredentialsFromEnv: InitializeCredentialsFromEnvUseCase,
  val loadCredentials: LoadCredentialsUseCase,
  val saveCredentials: SaveCredentialsUseCase,
  val saveAppMode: SaveAppModeUseCase,
)
