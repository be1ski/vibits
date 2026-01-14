package space.be1ski.memos.shared.feature.memos.data.remote

/**
 * Pagination safeguards for network fetching.
 */
object MemosPagination {
  /**
   * Safety limit for pagination to avoid infinite loops.
   */
  const val MAX_PAGES: Int = 100
}
