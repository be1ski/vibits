package space.be1ski.vibits.shared.app.di

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.shared.core.platform.LocaleProvider
import space.be1ski.vibits.shared.domain.usecase.LoadAppDetailsUseCase
import space.be1ski.vibits.shared.feature.auth.domain.usecase.SaveCredentialsUseCase
import space.be1ski.vibits.shared.feature.auth.domain.usecase.ValidateCredentialsUseCase
import space.be1ski.vibits.shared.feature.habits.di.HabitsDependencies
import space.be1ski.vibits.shared.feature.habits.domain.usecase.BuildActivityDataUseCase
import space.be1ski.vibits.shared.feature.habits.domain.usecase.CalculateSuccessRateUseCase
import space.be1ski.vibits.shared.feature.memos.di.MemosDependencies
import space.be1ski.vibits.shared.feature.mode.domain.usecase.FixInvalidOnlineModeUseCase
import space.be1ski.vibits.shared.feature.mode.domain.usecase.LoadAppModeUseCase
import space.be1ski.vibits.shared.feature.mode.domain.usecase.SaveAppModeUseCase
import space.be1ski.vibits.shared.feature.settings.di.SettingsDependencies
import space.be1ski.vibits.shared.feature.settings.domain.usecase.LoadPreferencesUseCase
import space.be1ski.vibits.shared.feature.settings.domain.usecase.SaveTimeRangeTabUseCase

/**
 * Single entry point for all app dependencies.
 */
@Inject
class AppDependencies(
  val localeProvider: LocaleProvider,
  val loadPreferences: LoadPreferencesUseCase,
  val fixInvalidOnlineMode: FixInvalidOnlineModeUseCase,
  val modeSelection: ModeSelectionDependencies,
  val vibitsApp: VibitsAppDependencies,
)

/**
 * Dependencies for ModeSelectionFeature.
 */
@Inject
class ModeSelectionDependencies(
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
  val buildActivityData: BuildActivityDataUseCase,
  val habitsDependencies: HabitsDependencies,
  val memosDependencies: MemosDependencies,
  val settingsDependencies: SettingsDependencies,
)
