package space.be1ski.vibits.shared.feature.habits.di

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.shared.feature.memos.domain.repository.MemosRepository

/**
 * Dependencies for HabitsFeature.
 */
@Inject
class HabitsDependencies(
  val memosRepository: MemosRepository,
)
