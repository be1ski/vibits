package space.be1ski.vibits.shared.feature.preferences.data

import space.be1ski.vibits.shared.feature.preferences.domain.model.TimeRangeTab
import space.be1ski.vibits.shared.feature.preferences.domain.model.UserPreferences
import space.be1ski.vibits.shared.feature.preferences.domain.repository.PreferencesRepository

/**
 * Repository implementation backed by platform preferences storage.
 */
internal class PreferencesRepositoryImpl(
  private val preferencesStore: PreferencesStore,
) : PreferencesRepository {
  override fun load(): UserPreferences {
    val local = preferencesStore.load()
    val habitsTab = parseTimeRangeTab(local.habitsTimeRangeTab)
    val postsTab = parseTimeRangeTab(local.postsTimeRangeTab)
    return UserPreferences(habitsTimeRangeTab = habitsTab, postsTimeRangeTab = postsTab)
  }

  override fun save(preferences: UserPreferences) {
    val local =
      LocalUserPreferences(
        habitsTimeRangeTab = preferences.habitsTimeRangeTab.name,
        postsTimeRangeTab = preferences.postsTimeRangeTab.name,
      )
    preferencesStore.save(local)
  }

  private fun parseTimeRangeTab(value: String): TimeRangeTab = runCatching { TimeRangeTab.valueOf(value) }.getOrDefault(TimeRangeTab.WEEKS)
}
