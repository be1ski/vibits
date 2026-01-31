package space.be1ski.vibits.feature.habits.domain.model

import kotlinx.datetime.LocalDate

/**
 * Date range bounds.
 */
data class RangeBounds(
  val start: LocalDate,
  val end: LocalDate,
)
