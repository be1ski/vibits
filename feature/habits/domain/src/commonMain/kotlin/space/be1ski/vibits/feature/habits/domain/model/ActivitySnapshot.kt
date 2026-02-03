package space.be1ski.vibits.feature.habits.domain.model

import kotlinx.datetime.LocalDate

data class ActivitySnapshot(
  val date: LocalDate,
  val hasActivity: Boolean,
)
