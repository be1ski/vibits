package space.be1ski.vibits.feature.settings.data

import space.be1ski.vibits.core.platform.storage.KeyValueStore

interface PreferencesStore {
  fun load(): LocalUserPreferences

  fun save(preferences: LocalUserPreferences)
}

class PreferencesStoreImpl(
  private val store: KeyValueStore,
  private val migration: PreferencesKeyMigration = PreferencesKeyMigration(store),
) : PreferencesStore {
  override fun load(): LocalUserPreferences {
    migration.runOnce(KEY_LEGACY_TAB)
    val defaultTab = LocalUserPreferences.DEFAULT_TIME_RANGE_TAB
    val defaultLanguage = LocalUserPreferences.DEFAULT_LANGUAGE
    val defaultTheme = LocalUserPreferences.DEFAULT_THEME
    return LocalUserPreferences(
      habitsTimeRangeTab = store.getString(KEY_HABITS_TAB, defaultTab) ?: defaultTab,
      postsTimeRangeTab = store.getString(KEY_POSTS_TAB, defaultTab) ?: defaultTab,
      language = store.getString(KEY_LANGUAGE, defaultLanguage) ?: defaultLanguage,
      theme = store.getString(KEY_THEME, defaultTheme) ?: defaultTheme,
    )
  }

  override fun save(preferences: LocalUserPreferences) {
    store.putString(KEY_HABITS_TAB, preferences.habitsTimeRangeTab)
    store.putString(KEY_POSTS_TAB, preferences.postsTimeRangeTab)
    store.putString(KEY_LANGUAGE, preferences.language)
    store.putString(KEY_THEME, preferences.theme)
  }

  private companion object {
    const val KEY_LEGACY_TAB = "ui_time_range_tab"
    const val KEY_HABITS_TAB = "ui_habits_time_range_tab"
    const val KEY_POSTS_TAB = "ui_posts_time_range_tab"
    const val KEY_LANGUAGE = "ui_language"
    const val KEY_THEME = "ui_theme"
  }
}
