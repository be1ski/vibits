package space.be1ski.vibits.feature.habits.domain.usecase

import space.be1ski.vibits.feature.memos.domain.model.Memo
import space.be1ski.vibits.feature.memos.domain.model.PostTags

/**
 * Filters out habit tracking memos from a list.
 * Returns only regular posts (memos without #habits hashtag).
 */
object FilterPostsUseCase {
  operator fun invoke(memos: List<Memo>): List<Memo> = memos.filter { !it.content.contains(PostTags.HABITS_HASHTAG) }
}
