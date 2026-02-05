package space.be1ski.vibits.feature.homescreen.domain.model

import kotlinx.datetime.LocalDate
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.settings.domain.model.TimeRangeTab

/**
 * State for the App coordinator feature.
 */
data class AppState(
  val appMode: AppMode = AppMode.NOT_SELECTED,
  val selectedScreen: Screen = Screen.HABITS,
  val habitsTimeRangeTab: TimeRangeTab = TimeRangeTab.WEEKS,
  val postsTimeRangeTab: TimeRangeTab = TimeRangeTab.WEEKS,
  val periodStartDate: LocalDate,
  val autoLoaded: Boolean = false,
  val postsListExpanded: Boolean = false,
) {
  val currentTimeRangeTab: TimeRangeTab
    get() =
      when (selectedScreen) {
        Screen.HABITS -> habitsTimeRangeTab
        Screen.STATS -> postsTimeRangeTab
        Screen.FEED -> habitsTimeRangeTab
      }

  val isDemoMode: Boolean
    get() = appMode == AppMode.DEMO

  val skipCredentialsCheck: Boolean
    get() = appMode == AppMode.DEMO || appMode == AppMode.OFFLINE
}
