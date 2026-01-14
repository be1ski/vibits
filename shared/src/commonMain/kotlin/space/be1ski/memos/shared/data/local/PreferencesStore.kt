package space.be1ski.memos.shared.data.local

/**
 * Platform-specific UI preferences storage.
 */
expect class PreferencesStore() {
  /**
   * Loads saved preferences or default values when not available.
   */
  fun load(): LocalUserPreferences

  /**
   * Persists user preferences.
   */
  fun save(preferences: LocalUserPreferences)
}
