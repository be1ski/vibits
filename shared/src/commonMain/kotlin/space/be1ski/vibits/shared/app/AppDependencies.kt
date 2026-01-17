package space.be1ski.vibits.shared.app

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.shared.core.platform.LocaleProvider
import space.be1ski.vibits.shared.domain.usecase.LoadAppDetailsUseCase
import space.be1ski.vibits.shared.feature.auth.domain.usecase.SaveCredentialsUseCase
import space.be1ski.vibits.shared.feature.auth.domain.usecase.ValidateCredentialsUseCase
import space.be1ski.vibits.shared.feature.habits.domain.usecase.CalculateSuccessRateUseCase
import space.be1ski.vibits.shared.feature.memos.domain.repository.MemosRepository
import space.be1ski.vibits.shared.feature.memos.presentation.MemosUseCases
import space.be1ski.vibits.shared.feature.mode.domain.usecase.FixInvalidOnlineModeUseCase
import space.be1ski.vibits.shared.feature.mode.domain.usecase.LoadAppModeUseCase
import space.be1ski.vibits.shared.feature.mode.domain.usecase.SaveAppModeUseCase
import space.be1ski.vibits.shared.feature.settings.domain.usecase.LoadPreferencesUseCase
import space.be1ski.vibits.shared.feature.settings.domain.usecase.SaveTimeRangeTabUseCase
import space.be1ski.vibits.shared.feature.settings.presentation.SettingsUseCases

/**
 * Single entry point for all app dependencies.
 */
@Inject
class AppDependencies(
  val localeProvider: LocaleProvider,
  val loadPreferences: LoadPreferencesUseCase,
  val fixInvalidOnlineMode: FixInvalidOnlineModeUseCase,
  val modeSelection: ModeSelectionUseCases,
  val vibitsApp: VibitsAppDependencies,
)

/**
 * Dependencies for ModeSelectionFeature.
 */
@Inject
class ModeSelectionUseCases(
  val validateCredentials: ValidateCredentialsUseCase,
  val saveCredentials: SaveCredentialsUseCase,
  val saveAppMode: SaveAppModeUseCase,
)

/**
 * Dependencies for VibitsApp.
 */
@Suppress("LongParameterList")
@Inject
class VibitsAppDependencies(
  val loadPreferences: LoadPreferencesUseCase,
  val saveTimeRangeTab: SaveTimeRangeTabUseCase,
  val loadAppDetails: LoadAppDetailsUseCase,
  val loadAppMode: LoadAppModeUseCase,
  val calculateSuccessRate: CalculateSuccessRateUseCase,
  val memosRepository: MemosRepository,
  val memosUseCases: MemosUseCases,
  val settingsUseCases: SettingsUseCases,
)
