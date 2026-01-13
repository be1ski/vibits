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
    val dto = MemoDto(
      name = "memos/1",
      content = "Hello",
      createTime = "2024-01-02T03:04:05Z"
    )

    val memo = mapper.toDomain(dto)

    assertEquals(Instant.parse("2024-01-02T03:04:05Z"), memo.createTime)
  }

  @Test
  fun `when ISO timestamp without zone then adds UTC`() {
    val dto = MemoDto(
      name = "memos/1",
      content = "Hello",
      updateTime = "2024-01-02T03:04:05"
    )

    val memo = mapper.toDomain(dto)

    assertEquals(Instant.parse("2024-01-02T03:04:05Z"), memo.updateTime)
  }

  @Test
  fun `when epoch seconds then converts to millis`() {
    val dto = MemoDto(
      name = "memos/1",
      content = "Hello",
      createTime = "1700000000"
    )

    val memo = mapper.toDomain(dto)

    assertEquals(Instant.fromEpochMilliseconds(1_700_000_000_000), memo.createTime)
  }

  @Test
  fun `when epoch millis then keeps millis`() {
    val dto = MemoDto(
      name = "memos/1",
      content = "Hello",
      updateTime = "1700000000000"
    )

    val memo = mapper.toDomain(dto)

    assertEquals(Instant.fromEpochMilliseconds(1_700_000_000_000), memo.updateTime)
  }

  @Test
  fun `when timestamp invalid then returns null`() {
    val dto = MemoDto(
      name = "memos/1",
      content = "Hello",
      createTime = "not-a-date"
    )

    val memo = mapper.toDomain(dto)

    assertNull(memo.createTime)
  }

  @Test
  fun `when mapping list then preserves order and size`() {
    val dtos = listOf(
      MemoDto(name = "memos/1", content = "First"),
      MemoDto(name = "memos/2", content = "Second")
    )

    val memos = mapper.toDomainList(dtos)

    assertEquals(2, memos.size)
    assertEquals("memos/1", memos.first().name)
    assertEquals("memos/2", memos.last().name)
  }
}
