package space.be1ski.vibits.shared.feature.habits.di

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.shared.feature.habits.domain.usecase.BuildActivityDataUseCase
import space.be1ski.vibits.shared.feature.habits.domain.usecase.CalculateSuccessRateUseCase
import space.be1ski.vibits.shared.feature.memos.domain.repository.MemosRepository

/**
 * Dependencies for HabitsFeature.
 */
@Inject
class HabitsDependencies(
  val memosRepository: MemosRepository,
  val buildActivityDataUseCase: BuildActivityDataUseCase,
  val calculateSuccessRateUseCase: CalculateSuccessRateUseCase,
)
