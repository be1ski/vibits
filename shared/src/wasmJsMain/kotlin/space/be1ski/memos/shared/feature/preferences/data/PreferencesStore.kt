package space.be1ski.memos.shared.feature.preferences.data

/**
 * In-memory preferences store for web builds.
 */
actual class PreferencesStore {
  private var cached: LocalUserPreferences? = null

  actual fun load(): LocalUserPreferences {
    val defaultTab = LocalUserPreferences.DEFAULT_TIME_RANGE_TAB
    return cached ?: LocalUserPreferences(habitsTimeRangeTab = defaultTab, postsTimeRangeTab = defaultTab)
  }

  actual fun save(preferences: LocalUserPreferences) {
    cached = preferences
  }
}
