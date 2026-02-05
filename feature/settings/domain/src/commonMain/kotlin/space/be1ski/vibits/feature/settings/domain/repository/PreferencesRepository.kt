package space.be1ski.vibits.feature.settings.domain.repository

import space.be1ski.vibits.feature.settings.domain.model.UserPreferences

interface PreferencesRepository {
  fun load(): UserPreferences

  fun save(preferences: UserPreferences)
}
