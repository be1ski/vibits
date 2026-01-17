package space.be1ski.vibits.shared.feature.settings.presentation

import space.be1ski.vibits.shared.feature.auth.domain.usecase.SaveCredentialsUseCase
import space.be1ski.vibits.shared.feature.auth.domain.usecase.ValidateCredentialsUseCase
import space.be1ski.vibits.shared.feature.mode.domain.usecase.ResetAppUseCase
import space.be1ski.vibits.shared.feature.mode.domain.usecase.SwitchAppModeUseCase

data class SettingsUseCases(
  val validateCredentials: ValidateCredentialsUseCase,
  val switchAppMode: SwitchAppModeUseCase,
  val saveCredentials: SaveCredentialsUseCase,
  val resetApp: ResetAppUseCase
)
