package space.be1ski.vibits.shared.feature.mode.di

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.shared.feature.auth.domain.usecase.SaveCredentialsUseCase
import space.be1ski.vibits.shared.feature.auth.domain.usecase.ValidateCredentialsUseCase
import space.be1ski.vibits.shared.feature.mode.domain.usecase.SaveAppModeUseCase

/**
 * Dependencies for ModeSelectionFeature.
 */
@Inject
class ModeSelectionDependencies(
  val validateCredentials: ValidateCredentialsUseCase,
  val saveCredentials: SaveCredentialsUseCase,
  val saveAppMode: SaveAppModeUseCase,
)
