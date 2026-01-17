package space.be1ski.vibits.shared.data.local

import space.be1ski.vibits.shared.feature.memos.data.local.MemoEntity
import space.be1ski.vibits.shared.feature.memos.data.local.MemoEntityMapper
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class MemoEntityMapperTest {
  @Test
  fun `when mapping entity then converts millis to instant`() {
    val entity =
      MemoEntity(
        name = "memos/1",
        content = "Test",
        createTimeMillis = 1_700_000_000_000,
        updateTimeMillis = null,
      )

    val memo = MemoEntityMapper.toDomain(entity)

    assertEquals(Instant.fromEpochMilliseconds(1_700_000_000_000), memo.createTime)
    assertNull(memo.updateTime)
  }

  @Test
  fun `when mapping memo then converts instant to millis`() {
    val memo =
      Memo(
        name = "memos/2",
        content = "Mapped",
        createTime = Instant.fromEpochMilliseconds(1_700_000_000_000),
        updateTime = null,
      )

    val entity = MemoEntityMapper.toEntity(memo)

    assertEquals(1_700_000_000_000, entity.createTimeMillis)
    assertNull(entity.updateTimeMillis)
  }
}
