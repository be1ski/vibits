package space.be1ski.vibits.feature.memos.domain.usecase

import space.be1ski.vibits.feature.memos.domain.model.Memo
import space.be1ski.vibits.feature.memos.domain.model.PostFilter
import space.be1ski.vibits.feature.memos.domain.model.PostTags

object ClassifyPostTypeUseCase {
  operator fun invoke(memo: Memo): PostFilter {
    val content = memo.content

    return when {
      content.contains(PostTags.HABITS_CONFIG, ignoreCase = true) ||
        content.contains(PostTags.HABITS_CONFIG_ALT, ignoreCase = true) -> PostFilter.CONFIG
      content.contains(PostTags.HABITS_DAILY, ignoreCase = true) ||
        content.contains(PostTags.DAILY, ignoreCase = true) -> PostFilter.HABIT_TRACKING
      else -> PostFilter.REGULAR
    }
  }
}
