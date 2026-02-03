package space.be1ski.vibits.feature.onboarding.domain.repository

interface OnboardingStore {
  fun isOnboardingCompleted(): Boolean

  fun markOnboardingCompleted()

  fun reset()
}
