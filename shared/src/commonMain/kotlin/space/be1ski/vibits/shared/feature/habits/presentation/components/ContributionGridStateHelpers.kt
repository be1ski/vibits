package space.be1ski.vibits.shared.feature.habits.presentation.components

import kotlinx.datetime.LocalDate
import space.be1ski.vibits.shared.feature.habits.domain.usecase.DateCalculationsUseCase

private val dateCalculationsUseCase = DateCalculationsUseCase()

internal fun startOfWeek(date: LocalDate): LocalDate = dateCalculationsUseCase.startOfWeek(date)
