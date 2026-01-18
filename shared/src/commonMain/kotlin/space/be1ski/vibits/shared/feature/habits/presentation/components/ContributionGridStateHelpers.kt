package space.be1ski.vibits.shared.feature.habits.presentation.components

import kotlinx.datetime.LocalDate
import space.be1ski.vibits.shared.feature.habits.domain.usecase.startOfWeek as domainStartOfWeek

internal fun startOfWeek(date: LocalDate): LocalDate = domainStartOfWeek(date)
