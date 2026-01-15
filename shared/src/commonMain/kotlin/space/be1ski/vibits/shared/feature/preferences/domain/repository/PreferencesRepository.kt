package space.be1ski.vibits.shared.feature.preferences.domain.repository

import space.be1ski.vibits.shared.feature.preferences.domain.model.UserPreferences

/**
 * Repository for reading and persisting user preferences.
 */
internal interface PreferencesRepository {
  /**
   * Loads stored preferences or default values.
   */
  fun load(): UserPreferences

  /**
   * Persists user preferences locally.
   */
  fun save(preferences: UserPreferences)
}
