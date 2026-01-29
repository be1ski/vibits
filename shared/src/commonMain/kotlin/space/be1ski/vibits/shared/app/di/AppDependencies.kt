package space.be1ski.vibits.shared.app.di

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.shared.app.domain.usecase.LoadAppDetailsUseCase
import space.be1ski.vibits.shared.core.platform.locale.LocaleProvider
import space.be1ski.vibits.shared.feature.habits.di.HabitsDependencies
import space.be1ski.vibits.shared.feature.memos.di.MemosDependencies
import space.be1ski.vibits.shared.feature.mode.di.ModeSelectionDependencies
import space.be1ski.vibits.shared.feature.mode.domain.usecase.FixInvalidOnlineModeUseCase
import space.be1ski.vibits.shared.feature.mode.domain.usecase.LoadAppModeUseCase
import space.be1ski.vibits.shared.feature.onboarding.di.OnboardingDependencies
import space.be1ski.vibits.shared.feature.onboarding.domain.usecase.ShouldShowOnboardingUseCase
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
  val saveTimeRangeTab: SaveTimeRangeTabUseCase,
  val loadAppDetails: LoadAppDetailsUseCase,
  val loadAppMode: LoadAppModeUseCase,
  val shouldShowOnboarding: ShouldShowOnboardingUseCase,
  val modeSelectionDependencies: ModeSelectionDependencies,
  val onboardingDependencies: OnboardingDependencies,
  val habitsDependencies: HabitsDependencies,
  val memosDependencies: MemosDependencies,
  val settingsDependencies: SettingsDependencies,
)
