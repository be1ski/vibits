package space.be1ski.vibits.shared.feature.onboarding.data

import space.be1ski.vibits.shared.core.platform.storage.KeyValueStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OnboardingStoreImplTest {
  @Test
  fun `when onboarding not completed then returns false`() {
    val store = FakeKeyValueStore()
    val onboardingStore = OnboardingStoreImpl(store)

    val result = onboardingStore.isOnboardingCompleted()

    assertFalse(result)
  }

  @Test
  fun `when onboarding completed then returns true`() {
    val store = FakeKeyValueStore(mapOf("offline_onboarding_completed" to "true"))
    val onboardingStore = OnboardingStoreImpl(store)

    val result = onboardingStore.isOnboardingCompleted()

    assertTrue(result)
  }

  @Test
  fun `when mark onboarding completed then stores true`() {
    val store = FakeKeyValueStore()
    val onboardingStore = OnboardingStoreImpl(store)

    onboardingStore.markOnboardingCompleted()

    assertTrue(onboardingStore.isOnboardingCompleted())
    assertEquals("true", store.data["offline_onboarding_completed"])
  }

  @Test
  fun `when reset then removes completion flag`() {
    val store = FakeKeyValueStore(mapOf("offline_onboarding_completed" to "true"))
    val onboardingStore = OnboardingStoreImpl(store)

    onboardingStore.reset()

    assertFalse(onboardingStore.isOnboardingCompleted())
    assertEquals(null, store.data["offline_onboarding_completed"])
  }
}

private class FakeKeyValueStore(
  initial: Map<String, String> = emptyMap(),
) : KeyValueStore {
  val data = initial.toMutableMap()

  override fun getString(
    key: String,
    defaultValue: String?,
  ): String? = data[key] ?: defaultValue

  override fun putString(
    key: String,
    value: String,
  ) {
    data[key] = value
  }

  override fun remove(key: String) {
    data.remove(key)
  }
}
