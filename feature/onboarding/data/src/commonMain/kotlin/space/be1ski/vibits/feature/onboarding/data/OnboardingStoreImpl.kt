package space.be1ski.vibits.feature.onboarding.data

import space.be1ski.vibits.core.platform.storage.KeyValueStore
import space.be1ski.vibits.feature.onboarding.domain.repository.OnboardingStore

class OnboardingStoreImpl(
  private val store: KeyValueStore,
) : OnboardingStore {
  override fun isOnboardingCompleted(): Boolean = store.getString(KEY_ONBOARDING_COMPLETED, "false") == "true"

  override fun markOnboardingCompleted() {
    store.putString(KEY_ONBOARDING_COMPLETED, "true")
  }

  override fun reset() {
    store.remove(KEY_ONBOARDING_COMPLETED)
  }

  private companion object {
    const val KEY_ONBOARDING_COMPLETED = "offline_onboarding_completed"
  }
}
