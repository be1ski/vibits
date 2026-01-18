package space.be1ski.vibits.shared.feature.habits.domain.usecase

import space.be1ski.vibits.shared.feature.memos.domain.model.Memo

private const val HABITS_HASHTAG = "#habits"

/**
 * Filters out habit tracking memos from a list.
 * Returns only regular posts (memos without #habits hashtag).
 */
object FilterPostsUseCase {
  operator fun invoke(memos: List<Memo>): List<Memo> = memos.filter { !it.content.contains(HABITS_HASHTAG) }
}
