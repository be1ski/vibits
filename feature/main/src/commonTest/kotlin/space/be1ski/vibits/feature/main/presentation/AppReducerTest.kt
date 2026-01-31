package space.be1ski.vibits.feature.main.presentation
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import space.be1ski.vibits.core.elm.test.test
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.habits.domain.model.ActivityRange
import space.be1ski.vibits.feature.main.domain.model.AppState
import space.be1ski.vibits.feature.main.domain.model.Screen
import space.be1ski.vibits.feature.main.presentation.action.AppAction
import space.be1ski.vibits.feature.main.presentation.effect.AppEffect
import space.be1ski.vibits.feature.main.presentation.reducer.appReducer
import space.be1ski.vibits.feature.settings.domain.model.TimeRangeTab
import kotlin.test.Test
import kotlin.test.assertEquals

class AppReducerTest {
  private val testDate = LocalDate(2024, Month.MARCH, 15)

  @Test
  fun `when SelectScreen then updates selected screen`() =
    appReducer.test(AppState(periodStartDate = testDate)) {
      send(AppAction.SelectScreen(Screen.STATS))

      assertState { selectedScreen == Screen.STATS }
      assertNoEffects()
    }

  @Test
  fun `when SetHabitsTimeRangeTab then updates tab and emits save effect`() =
    appReducer.test(AppState(periodStartDate = testDate)) {
      send(AppAction.SetHabitsTimeRangeTab(TimeRangeTab.MONTHS))

      assertState { habitsTimeRangeTab == TimeRangeTab.MONTHS }
      val effect = assertHasCommand<AppEffect.SaveHabitsTimeRangeTab>()
      assertEquals(TimeRangeTab.MONTHS, effect.tab)
    }

  @Test
  fun `when SetPostsTimeRangeTab then updates tab and emits save effect`() =
    appReducer.test(AppState(periodStartDate = testDate)) {
      send(AppAction.SetPostsTimeRangeTab(TimeRangeTab.QUARTERS))

      assertState { postsTimeRangeTab == TimeRangeTab.QUARTERS }
      val effect = assertHasCommand<AppEffect.SavePostsTimeRangeTab>()
      assertEquals(TimeRangeTab.QUARTERS, effect.tab)
    }

  @Test
  fun `when SetPeriodStartDate then updates date`() =
    appReducer.test(AppState(periodStartDate = testDate)) {
      val newDate = LocalDate(2024, Month.JUNE, 1)

      send(AppAction.SetPeriodStartDate(newDate))

      assertState { periodStartDate == newDate }
      assertNoEffects()
    }

  @Test
  fun `when SetActivityRange then updates period start date from range`() =
    appReducer.test(AppState(periodStartDate = testDate)) {
      val range = ActivityRange.Month(2024, Month.JUNE)

      send(AppAction.SetActivityRange(range))

      assertState { periodStartDate == LocalDate(2024, Month.JUNE, 1) }
      assertNoEffects()
    }

  @Test
  fun `when ChangeHabitsTab then updates tab and adjusts date`() =
    appReducer.test(AppState(periodStartDate = testDate, habitsTimeRangeTab = TimeRangeTab.MONTHS)) {
      send(AppAction.ChangeHabitsTab(oldTab = TimeRangeTab.MONTHS, newTab = TimeRangeTab.WEEKS))

      assertState { habitsTimeRangeTab == TimeRangeTab.WEEKS && periodStartDate == LocalDate(2024, Month.MARCH, 31) }
      assertHasCommand<AppEffect.SaveHabitsTimeRangeTab>()
    }

  @Test
  fun `when ChangePostsTab then updates tab and adjusts date`() =
    appReducer.test(AppState(periodStartDate = testDate, postsTimeRangeTab = TimeRangeTab.YEARS)) {
      send(AppAction.ChangePostsTab(oldTab = TimeRangeTab.YEARS, newTab = TimeRangeTab.QUARTERS))

      assertState { postsTimeRangeTab == TimeRangeTab.QUARTERS && periodStartDate == LocalDate(2024, Month.DECEMBER, 31) }
      assertHasCommand<AppEffect.SavePostsTimeRangeTab>()
    }

  @Test
  fun `when ResetToHome on HABITS screen then resets date and tab`() =
    appReducer.test(
      AppState(
        periodStartDate = testDate,
        selectedScreen = Screen.HABITS,
        habitsTimeRangeTab = TimeRangeTab.YEARS,
      ),
    ) {
      val today = LocalDate(2024, Month.JANUARY, 1)

      send(AppAction.ResetToHome(today))

      assertState { periodStartDate == today && habitsTimeRangeTab == TimeRangeTab.WEEKS }
      assertNoEffects()
    }

  @Test
  fun `when ResetToHome on STATS screen then resets date and tab`() =
    appReducer.test(
      AppState(
        periodStartDate = testDate,
        selectedScreen = Screen.STATS,
        postsTimeRangeTab = TimeRangeTab.YEARS,
      ),
    ) {
      val today = LocalDate(2024, Month.JANUARY, 1)

      send(AppAction.ResetToHome(today))

      assertState { periodStartDate == today && postsTimeRangeTab == TimeRangeTab.WEEKS }
      assertNoEffects()
    }

  @Test
  fun `when ResetToHome on FEED screen then resets only date`() =
    appReducer.test(AppState(periodStartDate = testDate, selectedScreen = Screen.FEED)) {
      val today = LocalDate(2024, Month.JANUARY, 1)

      send(AppAction.ResetToHome(today))

      assertState { periodStartDate == today }
      assertNoEffects()
    }

  @Test
  fun `when SetAppMode then updates app mode`() =
    appReducer.test(AppState(periodStartDate = testDate)) {
      send(AppAction.SetAppMode(AppMode.ONLINE))

      assertState { appMode == AppMode.ONLINE }
      assertNoEffects()
    }

  @Test
  fun `when MarkAutoLoaded then sets autoLoaded to true`() =
    appReducer.test(AppState(periodStartDate = testDate, autoLoaded = false)) {
      send(AppAction.MarkAutoLoaded)

      assertState { autoLoaded }
      assertNoEffects()
    }

  @Test
  fun `when SetPostsListExpanded then updates expanded state`() =
    appReducer.test(AppState(periodStartDate = testDate, postsListExpanded = false)) {
      send(AppAction.SetPostsListExpanded(true))

      assertState { postsListExpanded }
      assertNoEffects()
    }
}
