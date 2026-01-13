package space.be1ski.memos.shared.data.local

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant
import space.be1ski.memos.shared.domain.model.memo.Memo

class MemoEntityMapperTest {
  @Test
  fun `when mapping entity then converts millis to instant`() {
    // given
    val entity = MemoEntity(
      name = "memos/1",
      content = "Test",
      createTimeMillis = 1_700_000_000_000,
      updateTimeMillis = null
    )

    // when
    val memo = MemoEntityMapper.toDomain(entity)

    // then
    assertEquals(Instant.fromEpochMilliseconds(1_700_000_000_000), memo.createTime)
    assertNull(memo.updateTime)
  }

  @Test
  fun `when mapping memo then converts instant to millis`() {
    // given
    val memo = Memo(
      name = "memos/2",
      content = "Mapped",
      createTime = Instant.fromEpochMilliseconds(1_700_000_000_000),
      updateTime = null
    )

    // when
    val entity = MemoEntityMapper.toEntity(memo)

    // then
    assertEquals(1_700_000_000_000, entity.createTimeMillis)
    assertNull(entity.updateTimeMillis)
  }
}
