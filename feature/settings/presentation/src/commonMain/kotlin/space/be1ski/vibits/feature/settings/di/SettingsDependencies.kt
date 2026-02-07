package space.be1ski.vibits.feature.settings.di

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.feature.auth.domain.usecase.SaveCredentialsUseCase
import space.be1ski.vibits.feature.memos.domain.repository.ConnectionTester
import space.be1ski.vibits.feature.memos.domain.repository.ExportService
import space.be1ski.vibits.feature.mode.domain.usecase.ResetAppUseCase
import space.be1ski.vibits.feature.mode.domain.usecase.ResetAppWithMemosUseCase
import space.be1ski.vibits.feature.mode.domain.usecase.SwitchAppModeUseCase
import space.be1ski.vibits.feature.settings.domain.usecase.SaveLanguageUseCase
import space.be1ski.vibits.feature.settings.domain.usecase.SaveThemeUseCase

@Inject
@Suppress("LongParameterList")
class SettingsDependencies(
  val connectionTester: ConnectionTester,
  val switchAppMode: SwitchAppModeUseCase,
  val saveCredentials: SaveCredentialsUseCase,
  val resetApp: ResetAppUseCase,
  val resetAppWithMemos: ResetAppWithMemosUseCase,
  val saveLanguage: SaveLanguageUseCase,
  val saveTheme: SaveThemeUseCase,
  val exportService: ExportService,
)
