package space.be1ski.vibits.shared.app.di

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.shared.app.domain.usecase.LoadAppDetailsUseCase
import space.be1ski.vibits.shared.core.platform.locale.LocaleProvider
import space.be1ski.vibits.shared.feature.habits.di.HabitsDependencies
import space.be1ski.vibits.shared.feature.habits.domain.usecase.BuildActivityDataUseCase
import space.be1ski.vibits.shared.feature.habits.domain.usecase.CalculateSuccessRateUseCase
import space.be1ski.vibits.shared.feature.habits.view.components.ActivityWeekDataCache
import space.be1ski.vibits.shared.feature.memos.di.MemosDependencies
import space.be1ski.vibits.shared.feature.mode.di.ModeSelectionDependencies
import space.be1ski.vibits.shared.feature.mode.domain.usecase.FixInvalidOnlineModeUseCase
import space.be1ski.vibits.shared.feature.mode.domain.usecase.LoadAppModeUseCase
import space.be1ski.vibits.shared.feature.settings.di.SettingsDependencies
import space.be1ski.vibits.shared.feature.settings.domain.usecase.LoadPreferencesUseCase
import space.be1ski.vibits.shared.feature.settings.domain.usecase.SaveTimeRangeTabUseCase

/**
 * Single entry point for all app dependencies.
 */
@Suppress("LongParameterList")
@Inject
class AppDependencies(
  val localeProvider: LocaleProvider,
  val loadPreferences: LoadPreferencesUseCase,
  val fixInvalidOnlineMode: FixInvalidOnlineModeUseCase,
  val saveTimeRangeTab: SaveTimeRangeTabUseCase,
  val loadAppDetails: LoadAppDetailsUseCase,
  val loadAppMode: LoadAppModeUseCase,
  val calculateSuccessRate: CalculateSuccessRateUseCase,
  val buildActivityData: BuildActivityDataUseCase,
  val activityWeekDataCache: ActivityWeekDataCache,
  val modeSelectionDependencies: ModeSelectionDependencies,
  val habitsDependencies: HabitsDependencies,
  val memosDependencies: MemosDependencies,
  val settingsDependencies: SettingsDependencies,
)
