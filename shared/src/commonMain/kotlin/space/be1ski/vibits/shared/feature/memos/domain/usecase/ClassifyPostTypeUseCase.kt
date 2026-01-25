package space.be1ski.vibits.shared.feature.memos.domain.usecase

import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.memos.domain.model.PostFilter

object ClassifyPostTypeUseCase {
  operator fun invoke(memo: Memo): PostFilter {
    val content = memo.content.lowercase()

    return when {
      content.contains("#habits/config") || content.contains("#habits_config") -> PostFilter.CONFIG
      content.contains("#habits/daily") || content.contains("#daily") -> PostFilter.HABIT_TRACKING
      else -> PostFilter.REGULAR
    }
  }
}
