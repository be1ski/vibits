package space.be1ski.vibits.feature.settings.data

import space.be1ski.vibits.core.platform.storage.KeyValueStore
import kotlin.test.Test
import kotlin.test.assertEquals

class PreferencesStoreImplTest {
  @Test
  fun `when load with stored values then returns them`() {
    val store =
      FakeKeyValueStore(
        mapOf(
          "ui_habits_time_range_tab" to "MONTHS",
          "ui_posts_time_range_tab" to "QUARTERS",
          "ui_language" to "ENGLISH",
          "ui_theme" to "DARK",
          "ui_memos_auto_sync_debounce_seconds" to "45",
          "prefs_migrated_v1" to "1",
        ),
      )
    val preferencesStore = PreferencesStoreImpl(store)

    val result = preferencesStore.load()

    assertEquals("MONTHS", result.habitsTimeRangeTab)
    assertEquals("QUARTERS", result.postsTimeRangeTab)
    assertEquals("ENGLISH", result.language)
    assertEquals("DARK", result.theme)
    assertEquals(45, result.memosAutoSyncDebounceSeconds)
  }

  @Test
  fun `when load with empty store then returns defaults`() {
    val store = FakeKeyValueStore()
    val preferencesStore = PreferencesStoreImpl(store)

    val result = preferencesStore.load()

    assertEquals("WEEKS", result.habitsTimeRangeTab)
    assertEquals("WEEKS", result.postsTimeRangeTab)
    assertEquals("SYSTEM", result.language)
    assertEquals("SYSTEM", result.theme)
    assertEquals(5, result.memosAutoSyncDebounceSeconds)
  }

  @Test
  fun `when load with invalid debounce value then returns default`() {
    val store =
      FakeKeyValueStore(
        mapOf(
          "ui_memos_auto_sync_debounce_seconds" to "not_a_number",
          "prefs_migrated_v1" to "1",
        ),
      )
    val preferencesStore = PreferencesStoreImpl(store)

    val result = preferencesStore.load()

    assertEquals(5, result.memosAutoSyncDebounceSeconds)
  }

  @Test
  fun `when save then stores all values`() {
    val store = FakeKeyValueStore()
    val preferencesStore = PreferencesStoreImpl(store)
    val preferences =
      LocalUserPreferences(
        habitsTimeRangeTab = "YEARS",
        postsTimeRangeTab = "MONTHS",
        language = "RUSSIAN",
        theme = "LIGHT",
        memosAutoSyncDebounceSeconds = 60,
      )

    preferencesStore.save(preferences)

    assertEquals("YEARS", store.data["ui_habits_time_range_tab"])
    assertEquals("MONTHS", store.data["ui_posts_time_range_tab"])
    assertEquals("RUSSIAN", store.data["ui_language"])
    assertEquals("LIGHT", store.data["ui_theme"])
    assertEquals("60", store.data["ui_memos_auto_sync_debounce_seconds"])
  }
}

class PreferencesKeyMigrationTest {
  @Test
  fun `when legacy keys exist then migrates to new keys`() {
    val store =
      FakeKeyValueStore(
        mapOf(
          "vibits_habits_time_range_tab" to "MONTHS",
          "vibits_posts_time_range_tab" to "QUARTERS",
          "vibits_language" to "ENGLISH",
          "vibits_theme" to "DARK",
        ),
      )
    val migration = PreferencesKeyMigration(store)

    migration.runOnce(null)

    assertEquals("MONTHS", store.data["ui_habits_time_range_tab"])
    assertEquals("QUARTERS", store.data["ui_posts_time_range_tab"])
    assertEquals("ENGLISH", store.data["ui_language"])
    assertEquals("DARK", store.data["ui_theme"])
    assertEquals(null, store.data["vibits_habits_time_range_tab"])
    assertEquals("1", store.data["prefs_migrated_v1"])
  }

  @Test
  fun `when already migrated then skips`() {
    val store =
      FakeKeyValueStore(
        mapOf(
          "prefs_migrated_v1" to "1",
          "vibits_theme" to "DARK",
        ),
      )
    val migration = PreferencesKeyMigration(store)

    migration.runOnce(null)

    assertEquals(null, store.data["ui_theme"])
    assertEquals("DARK", store.data["vibits_theme"])
  }

  @Test
  fun `when new keys already exist then does not overwrite`() {
    val store =
      FakeKeyValueStore(
        mapOf(
          "ui_theme" to "LIGHT",
          "vibits_theme" to "DARK",
        ),
      )
    val migration = PreferencesKeyMigration(store)

    migration.runOnce(null)

    assertEquals("LIGHT", store.data["ui_theme"])
    assertEquals(null, store.data["vibits_theme"])
  }

  @Test
  fun `when legacy tab key provided then removes it`() {
    val store =
      FakeKeyValueStore(
        mapOf("ui_time_range_tab" to "MONTHS"),
      )
    val migration = PreferencesKeyMigration(store)

    migration.runOnce("ui_time_range_tab")

    assertEquals(null, store.data["ui_time_range_tab"])
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
