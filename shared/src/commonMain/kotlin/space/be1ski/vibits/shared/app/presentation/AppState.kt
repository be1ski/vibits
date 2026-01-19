package space.be1ski.vibits.shared.app.presentation

import kotlinx.datetime.LocalDate
import space.be1ski.vibits.shared.app.view.model.MemosScreen
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.settings.domain.model.TimeRangeTab

/**
 * State for the App coordinator feature.
 */
internal data class AppState(
  val appMode: AppMode = AppMode.NOT_SELECTED,
  val selectedScreen: MemosScreen = MemosScreen.HABITS,
  val habitsTimeRangeTab: TimeRangeTab = TimeRangeTab.WEEKS,
  val postsTimeRangeTab: TimeRangeTab = TimeRangeTab.WEEKS,
  val periodStartDate: LocalDate,
  val autoLoaded: Boolean = false,
  val postsListExpanded: Boolean = false,
) {
  val currentTimeRangeTab: TimeRangeTab
    get() =
      when (selectedScreen) {
        MemosScreen.HABITS -> habitsTimeRangeTab
        MemosScreen.STATS -> postsTimeRangeTab
        MemosScreen.FEED -> habitsTimeRangeTab
      }
}
