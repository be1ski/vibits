package space.be1ski.vibits.feature.onboarding.domain.usecase

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.feature.onboarding.domain.repository.OnboardingRepository

@Inject
class ShouldShowOnboardingUseCase(
  private val onboardingRepository: OnboardingRepository,
) {
  suspend operator fun invoke(): Boolean {
    if (onboardingRepository.isOnboardingCompleted()) return false
    return !onboardingRepository.hasHabitsConfig()
  }
}
