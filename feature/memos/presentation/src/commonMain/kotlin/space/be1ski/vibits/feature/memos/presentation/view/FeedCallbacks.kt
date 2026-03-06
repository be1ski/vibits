package space.be1ski.vibits.feature.memos.presentation.view

import space.be1ski.vibits.feature.memos.domain.model.Memo
import space.be1ski.vibits.feature.memos.domain.model.PostFilter

class FeedCallbacks(
  val onFilterChange: (PostFilter) -> Unit = {},
  val onRefresh: () -> Unit = {},
  val onMemoClick: (Memo) -> Unit = {},
  val onDeleteMemo: ((Memo) -> Unit)? = null,
)
