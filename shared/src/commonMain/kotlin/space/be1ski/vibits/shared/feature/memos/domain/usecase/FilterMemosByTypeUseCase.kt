package space.be1ski.vibits.shared.feature.memos.domain.usecase

import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.memos.domain.model.PostFilter

object FilterMemosByTypeUseCase {
  operator fun invoke(
    memos: List<Memo>,
    filter: PostFilter,
  ): List<Memo> {
    if (filter == PostFilter.ALL) return memos

    return memos.filter { memo ->
      ClassifyPostTypeUseCase(memo) == filter
    }
  }
}
