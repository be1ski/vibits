package space.be1ski.vibits.shared.feature.settings.data

import space.be1ski.vibits.shared.core.logging.Log
import space.be1ski.vibits.shared.core.platform.storage.KeyValueStore

private const val TAG = "PrefsMigration"

/**
 * One-time migration for legacy preference keys (old web implementation used different prefix).
 * Reads legacy keys, saves to new keys, then deletes legacy keys.
 *
 * Can be safely deleted once all users have migrated (after a few releases).
 *
 * To remove this migration:
 * 1. Delete this file
 * 2. Remove `migration.runOnce()` call from PreferencesStoreImpl.load()
 * 3. Remove `migration` parameter from PreferencesStoreImpl constructor
 */
class PreferencesKeyMigration(
  private val store: KeyValueStore,
) {
  fun runOnce(legacyTabKey: String?) {
    if (store.getString(MIGRATED_KEY) != null) {
      Log.d(TAG, "Already migrated, skipping")
      return
    }

    Log.i(TAG, "Starting migration...")
    var migratedCount = 0

    LEGACY_TO_NEW.forEach { (legacyKey, newKey) ->
      val legacyValue = store.getString(legacyKey)
      if (legacyValue != null && store.getString(newKey) == null) {
        Log.d(TAG, "Migrating $legacyKey -> $newKey = $legacyValue")
        store.putString(newKey, legacyValue)
        migratedCount++
      }
      store.remove(legacyKey)
    }

    if (legacyTabKey != null) {
      Log.d(TAG, "Removing legacy tab key: $legacyTabKey")
      store.remove(legacyTabKey)
    }

    store.putString(MIGRATED_KEY, "1")
    Log.i(TAG, "Migration complete, migrated $migratedCount keys")
  }

  private companion object {
    const val MIGRATED_KEY = "prefs_migrated_v1"

    // Old web (wasm) keys that used different prefix
    val LEGACY_TO_NEW =
      mapOf(
        "vibits_habits_time_range_tab" to "ui_habits_time_range_tab",
        "vibits_posts_time_range_tab" to "ui_posts_time_range_tab",
        "vibits_language" to "ui_language",
        "vibits_theme" to "ui_theme",
      )
  }
}
