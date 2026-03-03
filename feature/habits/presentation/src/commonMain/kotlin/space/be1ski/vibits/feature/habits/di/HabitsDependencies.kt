package space.be1ski.vibits.feature.habits.di

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.feature.habits.domain.usecase.BuildActivityDataUseCase
import space.be1ski.vibits.feature.habits.domain.usecase.SaveDailyHabitMemoUseCase
import space.be1ski.vibits.feature.habits.domain.usecase.SaveHabitsConfigMemoUseCase
import space.be1ski.vibits.feature.memos.domain.repository.MemosRepository

/**
 * Dependencies for HabitsFeature.
 */
@Inject
class HabitsDependencies(
  val memosRepository: MemosRepository,
  val buildActivityDataUseCase: BuildActivityDataUseCase,
  val saveDailyHabitMemo: SaveDailyHabitMemoUseCase,
  val saveHabitsConfigMemo: SaveHabitsConfigMemoUseCase,
)
