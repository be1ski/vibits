package space.be1ski.vibits.feature.homescreen.presentation.action

import kotlinx.datetime.LocalDate
import space.be1ski.vibits.core.elm.Action
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.habits.domain.model.ActivityRange
import space.be1ski.vibits.feature.homescreen.domain.model.Screen
import space.be1ski.vibits.feature.settings.domain.model.TimeRangeTab

sealed interface AppAction : Action {
  /** Navigation actions. */
  sealed interface Navigation : AppAction {
    data class SelectScreen(
      val screen: Screen,
    ) : Navigation
  }

  /** Time range and date-related actions. */
  sealed interface TimeRange : AppAction {
    data class SetHabitsTab(
      val tab: TimeRangeTab,
    ) : TimeRange

    data class SetPostsTab(
      val tab: TimeRangeTab,
    ) : TimeRange

    data class SetPeriodStartDate(
      val date: LocalDate,
    ) : TimeRange

    data class SetActivityRange(
      val range: ActivityRange,
    ) : TimeRange

    data class ChangeHabitsTab(
      val oldTab: TimeRangeTab,
      val newTab: TimeRangeTab,
    ) : TimeRange

    data class ChangePostsTab(
      val oldTab: TimeRangeTab,
      val newTab: TimeRangeTab,
    ) : TimeRange

    data class ResetToHome(
      val today: LocalDate,
    ) : TimeRange
  }

  /** App mode actions. */
  sealed interface Mode : AppAction {
    data class SetAppMode(
      val mode: AppMode,
    ) : Mode
  }

  /** UI state actions. */
  sealed interface UI : AppAction {
    data object MarkAutoLoaded : UI

    data class SetPostsListExpanded(
      val expanded: Boolean,
    ) : UI
  }
}
