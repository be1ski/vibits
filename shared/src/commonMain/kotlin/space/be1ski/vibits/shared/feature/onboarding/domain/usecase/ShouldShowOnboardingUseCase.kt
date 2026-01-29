package space.be1ski.vibits.shared.feature.onboarding.domain.usecase

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.shared.feature.memos.data.platform.OfflineMemoStorage
import space.be1ski.vibits.shared.feature.memos.domain.model.PostTags
import space.be1ski.vibits.shared.feature.onboarding.domain.repository.OnboardingRepository

@Inject
class ShouldShowOnboardingUseCase(
  private val onboardingRepository: OnboardingRepository,
  private val offlineMemoStorage: OfflineMemoStorage,
) {
  suspend operator fun invoke(): Boolean {
    val onboardingCompleted = onboardingRepository.isOnboardingCompleted()
    if (onboardingCompleted) return false

    val memosFile = offlineMemoStorage.load()
    val hasHabitsConfig =
      memosFile.memos.any { memo ->
        memo.content.contains(PostTags.HABITS_CONFIG) || memo.content.contains(PostTags.HABITS_CONFIG_ALT)
      }

    return !hasHabitsConfig
  }
}
