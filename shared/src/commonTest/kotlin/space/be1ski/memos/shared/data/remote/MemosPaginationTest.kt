package space.be1ski.memos.shared.data.remote

import kotlin.test.Test
import kotlin.test.assertEquals

class MemosPaginationTest {
  @Test
  fun `when pagination guard then max pages is stable`() {
    assertEquals(100, MemosPagination.MAX_PAGES)
  }
}
