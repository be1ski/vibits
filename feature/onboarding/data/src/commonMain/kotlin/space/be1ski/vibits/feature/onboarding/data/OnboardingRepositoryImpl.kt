package space.be1ski.vibits.feature.onboarding.data

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import space.be1ski.vibits.core.platform.di.AppScope
import space.be1ski.vibits.core.utils.logging.Log
import space.be1ski.vibits.feature.memos.domain.model.isConfigMemo
import space.be1ski.vibits.feature.memos.domain.repository.MemosRepository
import space.be1ski.vibits.feature.onboarding.domain.model.HabitPreset
import space.be1ski.vibits.feature.onboarding.domain.repository.OnboardingRepository
import space.be1ski.vibits.feature.onboarding.domain.repository.OnboardingStore

private const val TAG = "Onboarding"

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class OnboardingRepositoryImpl(
  private val onboardingStore: OnboardingStore,
  private val presetsDataSource: HabitPresetsDataSource,
  private val memosRepository: MemosRepository,
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

  override suspend fun hasHabitsConfig(): Boolean {
    val hasConfig =
      memosRepository.cachedMemos().any { memo ->
        memo.content.isConfigMemo()
      }
    Log.d(TAG, "hasHabitsConfig() = $hasConfig")
    return hasConfig
  }
}
