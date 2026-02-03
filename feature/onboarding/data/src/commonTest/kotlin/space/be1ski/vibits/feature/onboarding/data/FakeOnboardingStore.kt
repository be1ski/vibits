package space.be1ski.vibits.feature.onboarding.data

import space.be1ski.vibits.feature.onboarding.domain.repository.OnboardingStore

class FakeOnboardingStore(
  private var completed: Boolean = false,
) : OnboardingStore {
  override fun isOnboardingCompleted(): Boolean = completed

  override fun markOnboardingCompleted() {
    completed = true
  }

  override fun reset() {
    completed = false
  }
}
