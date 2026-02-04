package space.be1ski.vibits.feature.habits.di

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.feature.habits.domain.usecase.CalculateSuccessRateUseCase
import space.be1ski.vibits.feature.habits.domain.usecase.SaveDailyHabitMemoUseCase
import space.be1ski.vibits.feature.memos.domain.repository.MemosRepository

/**
 * Dependencies for HabitsFeature.
 */
@Inject
class HabitsDependencies(
  val memosRepository: MemosRepository,
  val calculateSuccessRateUseCase: CalculateSuccessRateUseCase,
  val saveDailyHabitMemo: SaveDailyHabitMemoUseCase,
)
