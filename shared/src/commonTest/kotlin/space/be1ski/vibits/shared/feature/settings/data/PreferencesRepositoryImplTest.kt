package space.be1ski.vibits.shared.feature.settings.data

import space.be1ski.vibits.shared.feature.settings.domain.model.AppLanguage
import space.be1ski.vibits.shared.feature.settings.domain.model.AppTheme
import space.be1ski.vibits.shared.feature.settings.domain.model.TimeRangeTab
import space.be1ski.vibits.shared.feature.settings.domain.model.UserPreferences
import kotlin.test.Test
import kotlin.test.assertEquals

class PreferencesRepositoryImplTest {
  @Test
  fun `when load with valid values then parses correctly`() {
    val store =
      FakePreferencesStore(
        LocalUserPreferences(
          habitsTimeRangeTab = "MONTHS",
          postsTimeRangeTab = "QUARTERS",
          language = "ENGLISH",
          theme = "DARK",
        ),
      )
    val repository = PreferencesRepositoryImpl(store)

    val result = repository.load()

    assertEquals(TimeRangeTab.MONTHS, result.habitsTimeRangeTab)
    assertEquals(TimeRangeTab.QUARTERS, result.postsTimeRangeTab)
    assertEquals(AppLanguage.ENGLISH, result.language)
    assertEquals(AppTheme.DARK, result.theme)
  }

  @Test
  fun `when habitsTimeRangeTab is invalid then defaults to WEEKS`() {
    val store =
      FakePreferencesStore(
        LocalUserPreferences(
          habitsTimeRangeTab = "INVALID",
          postsTimeRangeTab = "WEEKS",
          language = "SYSTEM",
          theme = "SYSTEM",
        ),
      )
    val repository = PreferencesRepositoryImpl(store)

    val result = repository.load()

    assertEquals(TimeRangeTab.WEEKS, result.habitsTimeRangeTab)
  }

  @Test
  fun `when postsTimeRangeTab is invalid then defaults to WEEKS`() {
    val store =
      FakePreferencesStore(
        LocalUserPreferences(
          habitsTimeRangeTab = "WEEKS",
          postsTimeRangeTab = "GARBAGE",
          language = "SYSTEM",
          theme = "SYSTEM",
        ),
      )
    val repository = PreferencesRepositoryImpl(store)

    val result = repository.load()

    assertEquals(TimeRangeTab.WEEKS, result.postsTimeRangeTab)
  }

  @Test
  fun `when language is invalid then defaults to SYSTEM`() {
    val store =
      FakePreferencesStore(
        LocalUserPreferences(
          habitsTimeRangeTab = "WEEKS",
          postsTimeRangeTab = "WEEKS",
          language = "UNKNOWN_LANG",
          theme = "SYSTEM",
        ),
      )
    val repository = PreferencesRepositoryImpl(store)

    val result = repository.load()

    assertEquals(AppLanguage.SYSTEM, result.language)
  }

  @Test
  fun `when theme is invalid then defaults to SYSTEM`() {
    val store =
      FakePreferencesStore(
        LocalUserPreferences(
          habitsTimeRangeTab = "WEEKS",
          postsTimeRangeTab = "WEEKS",
          language = "SYSTEM",
          theme = "INVALID_THEME",
        ),
      )
    val repository = PreferencesRepositoryImpl(store)

    val result = repository.load()

    assertEquals(AppTheme.SYSTEM, result.theme)
  }

  @Test
  fun `when all values are invalid then defaults to all defaults`() {
    val store =
      FakePreferencesStore(
        LocalUserPreferences(
          habitsTimeRangeTab = "X",
          postsTimeRangeTab = "Y",
          language = "Z",
          theme = "W",
        ),
      )
    val repository = PreferencesRepositoryImpl(store)

    val result = repository.load()

    assertEquals(TimeRangeTab.WEEKS, result.habitsTimeRangeTab)
    assertEquals(TimeRangeTab.WEEKS, result.postsTimeRangeTab)
    assertEquals(AppLanguage.SYSTEM, result.language)
    assertEquals(AppTheme.SYSTEM, result.theme)
  }

  @Test
  fun `when save then serializes preferences to local format`() {
    val store = FakePreferencesStore()
    val repository = PreferencesRepositoryImpl(store)
    val preferences =
      UserPreferences(
        habitsTimeRangeTab = TimeRangeTab.YEARS,
        postsTimeRangeTab = TimeRangeTab.MONTHS,
        language = AppLanguage.RUSSIAN,
        theme = AppTheme.LIGHT,
      )

    repository.save(preferences)

    assertEquals("YEARS", store.saved?.habitsTimeRangeTab)
    assertEquals("MONTHS", store.saved?.postsTimeRangeTab)
    assertEquals("RUSSIAN", store.saved?.language)
    assertEquals("LIGHT", store.saved?.theme)
  }
}

private class FakePreferencesStore(
  private val initial: LocalUserPreferences =
    LocalUserPreferences(
      habitsTimeRangeTab = "WEEKS",
      postsTimeRangeTab = "WEEKS",
      language = "SYSTEM",
      theme = "SYSTEM",
    ),
) : PreferencesStore {
  var saved: LocalUserPreferences? = null
    private set

  override fun load(): LocalUserPreferences = initial

  override fun save(preferences: LocalUserPreferences) {
    saved = preferences
  }
}
