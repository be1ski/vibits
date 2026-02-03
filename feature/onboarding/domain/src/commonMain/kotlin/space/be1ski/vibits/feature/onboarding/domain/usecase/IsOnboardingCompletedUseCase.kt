package space.be1ski.vibits.feature.onboarding.domain.usecase

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.feature.onboarding.domain.repository.OnboardingRepository

@Inject
class IsOnboardingCompletedUseCase(
  private val repository: OnboardingRepository,
) {
  suspend operator fun invoke(): Boolean = repository.isOnboardingCompleted()
}
