package space.be1ski.vibits.feature.homescreen.domain.model

import kotlinx.datetime.LocalDate
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.settings.domain.model.TimeRangeTab
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppStateTest {
  private val testDate = LocalDate(2024, 3, 15)

  @Test
  fun `when selectedScreen is HABITS then currentTimeRangeTab returns habitsTimeRangeTab`() {
    val state =
      AppState(
        selectedScreen = Screen.HABITS,
        habitsTimeRangeTab = TimeRangeTab.MONTHS,
        postsTimeRangeTab = TimeRangeTab.WEEKS,
        periodStartDate = testDate,
      )

    assertEquals(TimeRangeTab.MONTHS, state.currentTimeRangeTab)
  }

  @Test
  fun `when selectedScreen is STATS then currentTimeRangeTab returns postsTimeRangeTab`() {
    val state =
      AppState(
        selectedScreen = Screen.STATS,
        habitsTimeRangeTab = TimeRangeTab.MONTHS,
        postsTimeRangeTab = TimeRangeTab.YEARS,
        periodStartDate = testDate,
      )

    assertEquals(TimeRangeTab.YEARS, state.currentTimeRangeTab)
  }

  @Test
  fun `when selectedScreen is FEED then currentTimeRangeTab returns habitsTimeRangeTab`() {
    val state =
      AppState(
        selectedScreen = Screen.FEED,
        habitsTimeRangeTab = TimeRangeTab.QUARTERS,
        postsTimeRangeTab = TimeRangeTab.WEEKS,
        periodStartDate = testDate,
      )

    assertEquals(TimeRangeTab.QUARTERS, state.currentTimeRangeTab)
  }

  @Test
  fun `when appMode is DEMO then isDemoMode is true`() {
    val state = AppState(appMode = AppMode.DEMO, periodStartDate = testDate)

    assertTrue(state.isDemoMode)
  }

  @Test
  fun `when appMode is ONLINE then isDemoMode is false`() {
    val state = AppState(appMode = AppMode.ONLINE, periodStartDate = testDate)

    assertFalse(state.isDemoMode)
  }

  @Test
  fun `when appMode is OFFLINE then isDemoMode is false`() {
    val state = AppState(appMode = AppMode.OFFLINE, periodStartDate = testDate)

    assertFalse(state.isDemoMode)
  }

  @Test
  fun `when appMode is NOT_SELECTED then isDemoMode is false`() {
    val state = AppState(appMode = AppMode.NOT_SELECTED, periodStartDate = testDate)

    assertFalse(state.isDemoMode)
  }

  @Test
  fun `when appMode is DEMO then skipCredentialsCheck is true`() {
    val state = AppState(appMode = AppMode.DEMO, periodStartDate = testDate)

    assertTrue(state.skipCredentialsCheck)
  }

  @Test
  fun `when appMode is OFFLINE then skipCredentialsCheck is true`() {
    val state = AppState(appMode = AppMode.OFFLINE, periodStartDate = testDate)

    assertTrue(state.skipCredentialsCheck)
  }

  @Test
  fun `when appMode is ONLINE then skipCredentialsCheck is false`() {
    val state = AppState(appMode = AppMode.ONLINE, periodStartDate = testDate)

    assertFalse(state.skipCredentialsCheck)
  }

  @Test
  fun `when appMode is NOT_SELECTED then skipCredentialsCheck is false`() {
    val state = AppState(appMode = AppMode.NOT_SELECTED, periodStartDate = testDate)

    assertFalse(state.skipCredentialsCheck)
  }
}
