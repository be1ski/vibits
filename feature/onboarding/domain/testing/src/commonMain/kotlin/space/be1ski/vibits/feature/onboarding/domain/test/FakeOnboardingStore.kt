package space.be1ski.vibits.feature.onboarding.domain.test

import space.be1ski.vibits.feature.onboarding.domain.repository.OnboardingStore

class FakeOnboardingStore(
  private var completed: Boolean = false,
) : OnboardingStore {
  var resetCalls: Int = 0
    private set

  override fun isOnboardingCompleted(): Boolean = completed

  override fun markOnboardingCompleted() {
    completed = true
  }

  override fun reset() {
    resetCalls += 1
    completed = false
  }
}
