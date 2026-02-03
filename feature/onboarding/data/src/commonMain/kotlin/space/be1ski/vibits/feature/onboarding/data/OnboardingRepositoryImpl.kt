package space.be1ski.vibits.feature.onboarding.data

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import space.be1ski.vibits.core.logging.Log
import space.be1ski.vibits.core.platform.di.AppScope
import space.be1ski.vibits.feature.memos.data.platform.OfflineMemoStorage
import space.be1ski.vibits.feature.memos.domain.model.PostTags
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
  private val offlineMemoStorage: OfflineMemoStorage,
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
    val memosFile = offlineMemoStorage.load()
    val hasConfig =
      memosFile.memos.any { memo ->
        memo.content.contains(PostTags.HABITS_CONFIG) || memo.content.contains(PostTags.HABITS_CONFIG_ALT)
      }
    Log.d(TAG, "hasHabitsConfig() = $hasConfig")
    return hasConfig
  }
}
