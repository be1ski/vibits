package space.be1ski.vibits.shared.feature.onboarding.domain.usecase

import dev.zacsweers.metro.Inject
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import space.be1ski.vibits.shared.feature.habits.domain.buildDailyContent
import space.be1ski.vibits.shared.feature.habits.domain.usecase.ExtractHabitsConfigUseCase
import space.be1ski.vibits.shared.feature.memos.domain.repository.MemosRepository
import space.be1ski.vibits.shared.feature.memos.domain.usecase.CreateMemoUseCase

@Inject
class CreateFirstCheckInUseCase(
  private val memosRepository: MemosRepository,
  private val createMemo: CreateMemoUseCase,
) {
  suspend operator fun invoke(date: LocalDate): Result<Unit> =
    runCatching {
      val memos = memosRepository.cachedMemos()
      val timeZone = TimeZone.currentSystemDefault()
      val configEntries = ExtractHabitsConfigUseCase(memos, timeZone)
      val latestConfig =
        ExtractHabitsConfigUseCase.forDate(configEntries, date)
          ?: error("No habits config found")

      val firstHabit =
        latestConfig.habits.firstOrNull()
          ?: error("Config has no habits")

      val selections = mapOf(firstHabit.tag to true)
      val content = buildDailyContent(date, latestConfig.habits, selections)
      createMemo(content)
    }
}
