package space.be1ski.vibits.feature.onboarding.domain.repository

import space.be1ski.vibits.feature.onboarding.domain.model.HabitPreset

interface OnboardingRepository {
  suspend fun isOnboardingCompleted(): Boolean

  suspend fun markOnboardingCompleted()

  suspend fun getHabitPresets(): List<HabitPreset>

  suspend fun hasHabitsConfig(): Boolean
}
