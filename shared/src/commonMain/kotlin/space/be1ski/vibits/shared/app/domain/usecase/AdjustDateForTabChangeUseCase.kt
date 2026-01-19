package space.be1ski.vibits.shared.app.domain.usecase

import kotlinx.datetime.LocalDate
import space.be1ski.vibits.shared.app.domain.model.ActivityRange
import space.be1ski.vibits.shared.feature.habits.view.components.quarterIndex
import space.be1ski.vibits.shared.feature.settings.domain.model.TimeRangeTab

/**
 * Adjusts the period start date when changing time range tabs.
 *
 * When switching from larger to smaller granularity (e.g., year to month),
 * moves the date to the end of the current period so we show the LAST
 * sub-period instead of the first.
 */
object AdjustDateForTabChangeUseCase {
  operator fun invoke(
    currentDate: LocalDate,
    oldTab: TimeRangeTab,
    newTab: TimeRangeTab,
  ): LocalDate =
    if (oldTab.ordinal <= newTab.ordinal || oldTab == TimeRangeTab.WEEKS) {
      currentDate
    } else {
      activityRangeForTab(oldTab, currentDate)?.let { GetActivityRangeEndDateUseCase(it) } ?: currentDate
    }

  private fun activityRangeForTab(
    tab: TimeRangeTab,
    date: LocalDate,
  ): ActivityRange? =
    when (tab) {
      TimeRangeTab.WEEKS -> null
      TimeRangeTab.MONTHS -> ActivityRange.Month(date.year, date.month)
      TimeRangeTab.QUARTERS -> ActivityRange.Quarter(date.year, quarterIndex(date))
      TimeRangeTab.YEARS -> ActivityRange.Year(date.year)
    }
}
