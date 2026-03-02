package space.be1ski.vibits.feature.changelog.data

import space.be1ski.vibits.feature.changelog.domain.repository.LastSeenVersionStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LastSeenVersionStoreImplTest {
  @Test
  fun `when no version stored then returns null`() {
    val store: LastSeenVersionStore = LastSeenVersionStoreImpl(FakeKeyValueStore())

    assertNull(store.getLastSeenVersion())
  }

  @Test
  fun `when version set then returns it`() {
    val store: LastSeenVersionStore = LastSeenVersionStoreImpl(FakeKeyValueStore())

    store.setLastSeenVersion("1.2.0")

    assertEquals("1.2.0", store.getLastSeenVersion())
  }

  @Test
  fun `when version updated then returns latest`() {
    val store: LastSeenVersionStore = LastSeenVersionStoreImpl(FakeKeyValueStore())

    store.setLastSeenVersion("1.0.0")
    store.setLastSeenVersion("1.1.0")

    assertEquals("1.1.0", store.getLastSeenVersion())
  }
}

private class FakeKeyValueStore : space.be1ski.vibits.core.platform.storage.KeyValueStore {
  private val map = mutableMapOf<String, String>()

  override fun getString(
    key: String,
    defaultValue: String?,
  ): String? = map[key] ?: defaultValue

  override fun putString(
    key: String,
    value: String,
  ) {
    map[key] = value
  }

  override fun remove(key: String) {
    map.remove(key)
  }
}
