package space.be1ski.vibits.feature.settings.domain.test

import space.be1ski.vibits.feature.settings.domain.model.TimeRangeTab
import space.be1ski.vibits.feature.settings.domain.model.UserPreferences
import space.be1ski.vibits.feature.settings.domain.repository.PreferencesRepository

class FakePreferencesRepository(
  initial: UserPreferences = UserPreferences(TimeRangeTab.WEEKS, TimeRangeTab.WEEKS),
) : PreferencesRepository {
  var stored: UserPreferences = initial
    private set
  var saveCalls: Int = 0
    private set

  override fun load(): UserPreferences = stored

  override fun save(preferences: UserPreferences) {
    stored = preferences
    saveCalls += 1
  }
}
