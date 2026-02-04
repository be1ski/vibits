package space.be1ski.vibits.feature.onboarding.domain.usecase

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.feature.habits.domain.formatHexColor
import space.be1ski.vibits.feature.memos.domain.model.PostTags
import space.be1ski.vibits.feature.memos.domain.usecase.CreateMemoUseCase

@Inject
class CreateFirstHabitUseCase(
  private val createMemo: CreateMemoUseCase,
) {
  suspend operator fun invoke(
    name: String,
    presetId: String?,
    color: Long,
  ): Result<Unit> =
    runCatching {
      val habitTag = name.lowercase().replace(" ", "_")
      val hexColor = formatHexColor(color)
      val content =
        buildString {
          appendLine(PostTags.HABITS_CONFIG)
          append(name)
          append(" | ")
          append(PostTags.HABITS_PREFIX)
          append(habitTag)
          append(" | ")
          append(hexColor)
        }
      createMemo(content)
    }
}
