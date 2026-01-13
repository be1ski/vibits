package space.be1ski.memos.shared.domain.config

import kotlin.test.Test
import kotlin.test.assertEquals

class MemosDefaultsTest {
  @Test
  fun `when defaults then page size is 200`() {
    // when/then
    assertEquals(200, MemosDefaults.DEFAULT_PAGE_SIZE)
  }
}
