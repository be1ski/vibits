package space.be1ski.memos.shared.ui.state

import space.be1ski.memos.shared.model.Memo

data class MemosUiState(
  val baseUrl: String = "",
  val token: String = "",
  val isLoading: Boolean = false,
  val errorMessage: String? = null,
  val memos: List<Memo> = emptyList()
)
