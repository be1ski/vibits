package space.be1ski.vibits.shared.feature.onboarding.di

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.shared.feature.onboarding.domain.usecase.CreateFirstCheckInUseCase
import space.be1ski.vibits.shared.feature.onboarding.domain.usecase.CreateFirstHabitUseCase
import space.be1ski.vibits.shared.feature.onboarding.domain.usecase.GetHabitPresetsUseCase
import space.be1ski.vibits.shared.feature.onboarding.domain.usecase.IsOnboardingCompletedUseCase
import space.be1ski.vibits.shared.feature.onboarding.domain.usecase.MarkOnboardingCompletedUseCase
import space.be1ski.vibits.shared.feature.onboarding.domain.usecase.ShouldShowOnboardingUseCase

@Inject
class OnboardingDependencies(
  val createFirstHabit: CreateFirstHabitUseCase,
  val createFirstCheckIn: CreateFirstCheckInUseCase,
  val markOnboardingCompleted: MarkOnboardingCompletedUseCase,
  val isOnboardingCompleted: IsOnboardingCompletedUseCase,
  val shouldShowOnboarding: ShouldShowOnboardingUseCase,
  val getHabitPresets: GetHabitPresetsUseCase,
)
