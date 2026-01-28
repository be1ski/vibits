package space.be1ski.vibits.shared.feature.onboarding.data

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
