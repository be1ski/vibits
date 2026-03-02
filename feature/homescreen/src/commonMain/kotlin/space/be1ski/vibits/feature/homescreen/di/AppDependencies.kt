package space.be1ski.vibits.feature.homescreen.di

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.core.platform.locale.LocaleProvider
import space.be1ski.vibits.feature.changelog.domain.usecase.GetChangelogUseCase
import space.be1ski.vibits.feature.habits.di.HabitsDependencies
import space.be1ski.vibits.feature.homescreen.domain.usecase.LoadAppDetailsUseCase
import space.be1ski.vibits.feature.memos.di.MemosDependencies
import space.be1ski.vibits.feature.mode.di.ModeSelectionDependencies
import space.be1ski.vibits.feature.mode.domain.usecase.FixInvalidOnlineModeUseCase
import space.be1ski.vibits.feature.mode.domain.usecase.LoadAppModeUseCase
import space.be1ski.vibits.feature.onboarding.di.OnboardingDependencies
import space.be1ski.vibits.feature.onboarding.domain.usecase.ShouldShowOnboardingUseCase
import space.be1ski.vibits.feature.settings.di.SettingsDependencies
import space.be1ski.vibits.feature.settings.domain.usecase.LoadPreferencesUseCase
import space.be1ski.vibits.feature.settings.domain.usecase.SaveTimeRangeTabUseCase

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
  val getChangelog: GetChangelogUseCase,
  val loadAppDetails: LoadAppDetailsUseCase,
  val loadAppMode: LoadAppModeUseCase,
  val shouldShowOnboarding: ShouldShowOnboardingUseCase,
  val modeSelectionDependencies: ModeSelectionDependencies,
  val onboardingDependencies: OnboardingDependencies,
  val habitsDependencies: HabitsDependencies,
  val memosDependencies: MemosDependencies,
  val settingsDependencies: SettingsDependencies,
)
