package space.be1ski.memos.shared.data.repository

import space.be1ski.memos.shared.data.local.LocalUserPreferences
import space.be1ski.memos.shared.data.local.PreferencesStore
import space.be1ski.memos.shared.domain.model.preferences.TimeRangeTab
import space.be1ski.memos.shared.domain.model.preferences.UserPreferences
import space.be1ski.memos.shared.domain.repository.PreferencesRepository

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
    return try {
      TimeRangeTab.valueOf(value)
    } catch (e: IllegalArgumentException) {
      TimeRangeTab.Weeks // Default fallback
    }
  }
}
