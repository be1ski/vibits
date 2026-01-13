package space.be1ski.memos.shared.data.mapper

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant
import space.be1ski.memos.shared.data.remote.dto.MemoDto

class MemoMapperTest {
  private val mapper = MemoMapper()

  @Test
  fun `when ISO timestamp then maps to instant`() {
    // given
    val dto = MemoDto(
      name = "memos/1",
      content = "Hello",
      createTime = "2024-01-02T03:04:05Z"
    )

    // when
    val memo = mapper.toDomain(dto)

    // then
    assertEquals(Instant.parse("2024-01-02T03:04:05Z"), memo.createTime)
  }

  @Test
  fun `when ISO timestamp without zone then adds UTC`() {
    // given
    val dto = MemoDto(
      name = "memos/1",
      content = "Hello",
      updateTime = "2024-01-02T03:04:05"
    )

    // when
    val memo = mapper.toDomain(dto)

    // then
    assertEquals(Instant.parse("2024-01-02T03:04:05Z"), memo.updateTime)
  }

  @Test
  fun `when epoch seconds then converts to millis`() {
    // given
    val dto = MemoDto(
      name = "memos/1",
      content = "Hello",
      createTime = "1700000000"
    )

    // when
    val memo = mapper.toDomain(dto)

    // then
    assertEquals(Instant.fromEpochMilliseconds(1_700_000_000_000), memo.createTime)
  }

  @Test
  fun `when epoch millis then keeps millis`() {
    // given
    val dto = MemoDto(
      name = "memos/1",
      content = "Hello",
      updateTime = "1700000000000"
    )

    // when
    val memo = mapper.toDomain(dto)

    // then
    assertEquals(Instant.fromEpochMilliseconds(1_700_000_000_000), memo.updateTime)
  }

  @Test
  fun `when timestamp invalid then returns null`() {
    // given
    val dto = MemoDto(
      name = "memos/1",
      content = "Hello",
      createTime = "not-a-date"
    )

    // when
    val memo = mapper.toDomain(dto)

    // then
    assertNull(memo.createTime)
  }
}
