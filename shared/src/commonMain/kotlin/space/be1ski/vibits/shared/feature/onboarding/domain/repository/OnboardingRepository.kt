package space.be1ski.vibits.shared.feature.onboarding.domain.repository

interface OnboardingRepository {
  suspend fun isOnboardingCompleted(): Boolean

  suspend fun markOnboardingCompleted()

  suspend fun getHabitPresets(): List<space.be1ski.vibits.shared.feature.onboarding.domain.model.HabitPreset>
}
