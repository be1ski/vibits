package space.be1ski.vibits.feature.homescreen.presentation

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import space.be1ski.vibits.core.elm.test.test
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.habits.domain.model.ActivityRange
import space.be1ski.vibits.feature.homescreen.domain.model.AppState
import space.be1ski.vibits.feature.homescreen.domain.model.Screen
import space.be1ski.vibits.feature.homescreen.presentation.action.AppAction
import space.be1ski.vibits.feature.homescreen.presentation.effect.AppEffect
import space.be1ski.vibits.feature.homescreen.presentation.reducer.appReducer
import space.be1ski.vibits.feature.settings.domain.model.TimeRangeTab
import kotlin.test.Test
import kotlin.test.assertEquals

class AppReducerTest {
  private val testDate = LocalDate(2024, Month.MARCH, 15)

  @Test
  fun `when Navigation SelectScreen then updates selected screen`() =
    appReducer.test(AppState(periodStartDate = testDate)) {
      send(AppAction.Navigation.SelectScreen(Screen.STATS))

      assertState { selectedScreen == Screen.STATS }
      assertNoEffects()
    }

  @Test
  fun `when TimeRange SetHabitsTab then updates tab and emits save effect`() =
    appReducer.test(AppState(periodStartDate = testDate)) {
      send(AppAction.TimeRange.SetHabitsTab(TimeRangeTab.MONTHS))

      assertState { habitsTimeRangeTab == TimeRangeTab.MONTHS }
      val effect = assertHasCommand<AppEffect.SaveHabitsTimeRangeTab>()
      assertEquals(TimeRangeTab.MONTHS, effect.tab)
    }

  @Test
  fun `when TimeRange SetPostsTab then updates tab and emits save effect`() =
    appReducer.test(AppState(periodStartDate = testDate)) {
      send(AppAction.TimeRange.SetPostsTab(TimeRangeTab.QUARTERS))

      assertState { postsTimeRangeTab == TimeRangeTab.QUARTERS }
      val effect = assertHasCommand<AppEffect.SavePostsTimeRangeTab>()
      assertEquals(TimeRangeTab.QUARTERS, effect.tab)
    }

  @Test
  fun `when TimeRange SetPeriodStartDate then updates date`() =
    appReducer.test(AppState(periodStartDate = testDate)) {
      val newDate = LocalDate(2024, Month.JUNE, 1)

      send(AppAction.TimeRange.SetPeriodStartDate(newDate))

      assertState { periodStartDate == newDate }
      assertNoEffects()
    }

  @Test
  fun `when TimeRange SetActivityRange then updates period start date from range`() =
    appReducer.test(AppState(periodStartDate = testDate)) {
      val range = ActivityRange.Month(2024, Month.JUNE)

      send(AppAction.TimeRange.SetActivityRange(range))

      assertState { periodStartDate == LocalDate(2024, Month.JUNE, 1) }
      assertNoEffects()
    }

  @Test
  fun `when TimeRange ChangeHabitsTab then updates tab and adjusts date`() =
    appReducer.test(AppState(periodStartDate = testDate, habitsTimeRangeTab = TimeRangeTab.MONTHS)) {
      send(AppAction.TimeRange.ChangeHabitsTab(oldTab = TimeRangeTab.MONTHS, newTab = TimeRangeTab.WEEKS))

      assertState { habitsTimeRangeTab == TimeRangeTab.WEEKS && periodStartDate == LocalDate(2024, Month.MARCH, 31) }
      assertHasCommand<AppEffect.SaveHabitsTimeRangeTab>()
    }

  @Test
  fun `when TimeRange ChangePostsTab then updates tab and adjusts date`() =
    appReducer.test(AppState(periodStartDate = testDate, postsTimeRangeTab = TimeRangeTab.YEARS)) {
      send(AppAction.TimeRange.ChangePostsTab(oldTab = TimeRangeTab.YEARS, newTab = TimeRangeTab.QUARTERS))

      assertState { postsTimeRangeTab == TimeRangeTab.QUARTERS && periodStartDate == LocalDate(2024, Month.DECEMBER, 31) }
      assertHasCommand<AppEffect.SavePostsTimeRangeTab>()
    }

  @Test
  fun `when TimeRange ResetToHome on HABITS screen then resets date and tab`() =
    appReducer.test(
      AppState(
        periodStartDate = testDate,
        selectedScreen = Screen.HABITS,
        habitsTimeRangeTab = TimeRangeTab.YEARS,
      ),
    ) {
      val today = LocalDate(2024, Month.JANUARY, 1)

      send(AppAction.TimeRange.ResetToHome(today))

      assertState { periodStartDate == today && habitsTimeRangeTab == TimeRangeTab.WEEKS }
      assertNoEffects()
    }

  @Test
  fun `when TimeRange ResetToHome on STATS screen then resets date and tab`() =
    appReducer.test(
      AppState(
        periodStartDate = testDate,
        selectedScreen = Screen.STATS,
        postsTimeRangeTab = TimeRangeTab.YEARS,
      ),
    ) {
      val today = LocalDate(2024, Month.JANUARY, 1)

      send(AppAction.TimeRange.ResetToHome(today))

      assertState { periodStartDate == today && postsTimeRangeTab == TimeRangeTab.WEEKS }
      assertNoEffects()
    }

  @Test
  fun `when TimeRange ResetToHome on FEED screen then resets only date`() =
    appReducer.test(AppState(periodStartDate = testDate, selectedScreen = Screen.FEED)) {
      val today = LocalDate(2024, Month.JANUARY, 1)

      send(AppAction.TimeRange.ResetToHome(today))

      assertState { periodStartDate == today }
      assertNoEffects()
    }

  @Test
  fun `when TimeRange ResetToHome on SETTINGS screen then resets only date`() =
    appReducer.test(AppState(periodStartDate = testDate, selectedScreen = Screen.SETTINGS)) {
      val today = LocalDate(2024, Month.JANUARY, 1)

      send(AppAction.TimeRange.ResetToHome(today))

      assertState { periodStartDate == today }
      assertNoEffects()
    }

  @Test
  fun `when Mode SetAppMode then updates app mode`() =
    appReducer.test(AppState(periodStartDate = testDate)) {
      send(AppAction.Mode.SetAppMode(AppMode.ONLINE))

      assertState { appMode == AppMode.ONLINE }
      assertNoEffects()
    }

  @Test
  fun `when UI MarkAutoLoaded then sets autoLoaded to true`() =
    appReducer.test(AppState(periodStartDate = testDate, autoLoaded = false)) {
      send(AppAction.UI.MarkAutoLoaded)

      assertState { autoLoaded }
      assertNoEffects()
    }

  @Test
  fun `when UI SetPostsListExpanded then updates expanded state`() =
    appReducer.test(AppState(periodStartDate = testDate, postsListExpanded = false)) {
      send(AppAction.UI.SetPostsListExpanded(true))

      assertState { postsListExpanded }
      assertNoEffects()
    }

  @Test
  fun `when UI SetSelectedHabitTag then updates selected habit tag`() =
    appReducer.test(AppState(periodStartDate = testDate)) {
      send(AppAction.UI.SetSelectedHabitTag("#habits/exercise"))

      assertState { selectedHabitTag == "#habits/exercise" }
      assertNoEffects()
    }

  @Test
  fun `when UI SetSelectedHabitTag with null then clears selected habit tag`() =
    appReducer.test(AppState(periodStartDate = testDate, selectedHabitTag = "#habits/exercise")) {
      send(AppAction.UI.SetSelectedHabitTag(null))

      assertState { selectedHabitTag == null }
      assertNoEffects()
    }
}
