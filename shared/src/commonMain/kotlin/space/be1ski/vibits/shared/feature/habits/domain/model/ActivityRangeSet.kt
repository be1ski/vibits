package space.be1ski.vibits.shared.feature.habits.domain.model

import kotlinx.datetime.LocalDate

data class ActivityRangeSet(
  val startDate: LocalDate,
  val endDate: LocalDate,
)
