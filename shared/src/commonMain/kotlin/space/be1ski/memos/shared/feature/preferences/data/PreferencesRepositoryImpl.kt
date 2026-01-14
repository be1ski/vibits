package space.be1ski.memos.shared.feature.preferences.data

import space.be1ski.memos.shared.feature.preferences.domain.model.TimeRangeTab
import space.be1ski.memos.shared.feature.preferences.domain.model.UserPreferences
import space.be1ski.memos.shared.feature.preferences.domain.repository.PreferencesRepository

/**
 * Repository implementation backed by platform preferences storage.
 */
internal class PreferencesRepositoryImpl(
  private val preferencesStore: PreferencesStore
) : PreferencesRepository {

  override fun load(): UserPreferences {
    val local = preferencesStore.load()
    val timeRangeTab = parseTimeRangeTab(local.timeRangeTab)
    return UserPreferences(selectedTimeRangeTab = timeRangeTab)
  }

  override fun save(preferences: UserPreferences) {
    val local = LocalUserPreferences(
      timeRangeTab = preferences.selectedTimeRangeTab.name
    )
    preferencesStore.save(local)
  }

  private fun parseTimeRangeTab(value: String): TimeRangeTab {
    return runCatching { TimeRangeTab.valueOf(value) }.getOrDefault(TimeRangeTab.Weeks)
  }
}
