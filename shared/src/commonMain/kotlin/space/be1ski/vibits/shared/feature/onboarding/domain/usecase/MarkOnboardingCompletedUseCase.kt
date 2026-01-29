package space.be1ski.vibits.shared.feature.onboarding.domain.usecase

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.shared.feature.onboarding.domain.repository.OnboardingRepository

@Inject
class MarkOnboardingCompletedUseCase(
  private val repository: OnboardingRepository,
) {
  suspend operator fun invoke() {
    repository.markOnboardingCompleted()
  }
}
