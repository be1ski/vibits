package space.be1ski.vibits.shared.feature.onboarding.data

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import space.be1ski.vibits.shared.app.di.AppScope
import space.be1ski.vibits.shared.core.logging.Log
import space.be1ski.vibits.shared.feature.onboarding.domain.model.HabitPreset
import space.be1ski.vibits.shared.feature.onboarding.domain.repository.OnboardingRepository

private const val TAG = "Onboarding"

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class OnboardingRepositoryImpl(
  private val onboardingStore: OnboardingStore,
  private val presetsDataSource: HabitPresetsDataSource,
) : OnboardingRepository {
  override suspend fun isOnboardingCompleted(): Boolean {
    val completed = onboardingStore.isOnboardingCompleted()
    Log.d(TAG, "isOnboardingCompleted() = $completed")
    return completed
  }

  override suspend fun markOnboardingCompleted() {
    Log.i(TAG, "markOnboardingCompleted()")
    onboardingStore.markOnboardingCompleted()
  }

  override suspend fun getHabitPresets(): List<HabitPreset> {
    val presets = presetsDataSource.getPresets()
    Log.d(TAG, "getHabitPresets() = ${presets.size} presets")
    return presets
  }
}
