package space.be1ski.vibits.shared.feature.settings.di

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.shared.feature.auth.domain.usecase.SaveCredentialsUseCase
import space.be1ski.vibits.shared.feature.memos.data.ConnectionTester
import space.be1ski.vibits.shared.feature.mode.domain.usecase.ResetAppUseCase
import space.be1ski.vibits.shared.feature.mode.domain.usecase.SwitchAppModeUseCase
import space.be1ski.vibits.shared.feature.settings.domain.usecase.SaveLanguageUseCase
import space.be1ski.vibits.shared.feature.settings.domain.usecase.SaveThemeUseCase

@Inject
class SettingsDependencies(
  val connectionTester: ConnectionTester,
  val switchAppMode: SwitchAppModeUseCase,
  val saveCredentials: SaveCredentialsUseCase,
  val resetApp: ResetAppUseCase,
  val saveLanguage: SaveLanguageUseCase,
  val saveTheme: SaveThemeUseCase,
)
