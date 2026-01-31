package space.be1ski.vibits.feature.memos.domain.usecase

import space.be1ski.vibits.feature.memos.domain.model.Memo
import space.be1ski.vibits.feature.memos.domain.model.PostFilter
import space.be1ski.vibits.feature.memos.domain.model.PostTags

object ClassifyPostTypeUseCase {
  operator fun invoke(memo: Memo): PostFilter {
    val content = memo.content.lowercase()

    return when {
      content.contains(PostTags.HABITS_CONFIG.lowercase()) ||
        content.contains(PostTags.HABITS_CONFIG_ALT.lowercase()) -> PostFilter.CONFIG
      content.contains(PostTags.HABITS_DAILY.lowercase()) ||
        content.contains(PostTags.DAILY.lowercase()) -> PostFilter.HABIT_TRACKING
      else -> PostFilter.REGULAR
    }
  }
}
