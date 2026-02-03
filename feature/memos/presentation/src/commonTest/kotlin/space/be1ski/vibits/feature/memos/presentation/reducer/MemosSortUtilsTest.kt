package space.be1ski.vibits.feature.memos.presentation.reducer

import space.be1ski.vibits.feature.memos.domain.model.Memo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class MemosSortUtilsTest {
  @Test
  fun `when empty list then returns empty list`() {
    val result = sortedMemos(emptyList())

    assertEquals(emptyList(), result)
  }

  @Test
  fun `when single memo then returns same memo`() {
    val memo = Memo(name = "memos/1", content = "Test")

    val result = sortedMemos(listOf(memo))

    assertEquals(listOf(memo), result)
  }

  @Test
  fun `when regular memos then sorted by createTime descending`() {
    val older =
      Memo(
        name = "memos/older",
        content = "Older post",
        createTime = Instant.fromEpochMilliseconds(1000L),
      )
    val newer =
      Memo(
        name = "memos/newer",
        content = "Newer post",
        createTime = Instant.fromEpochMilliseconds(2000L),
      )

    val result = sortedMemos(listOf(older, newer))

    assertEquals("memos/newer", result[0].name)
    assertEquals("memos/older", result[1].name)
  }

  @Test
  fun `when regular memos with only updateTime then sorted by updateTime descending`() {
    val older =
      Memo(
        name = "memos/older",
        content = "Older post",
        updateTime = Instant.fromEpochMilliseconds(1000L),
      )
    val newer =
      Memo(
        name = "memos/newer",
        content = "Newer post",
        updateTime = Instant.fromEpochMilliseconds(2000L),
      )

    val result = sortedMemos(listOf(older, newer))

    assertEquals("memos/newer", result[0].name)
    assertEquals("memos/older", result[1].name)
  }

  @Test
  fun `when regular memos without timestamps then sorted last`() {
    val withTimestamp =
      Memo(
        name = "memos/with-timestamp",
        content = "Post with timestamp",
        createTime = Instant.fromEpochMilliseconds(1000L),
      )
    val withoutTimestamp =
      Memo(
        name = "memos/no-timestamp",
        content = "Post without timestamp",
      )

    val result = sortedMemos(listOf(withoutTimestamp, withTimestamp))

    assertEquals("memos/with-timestamp", result[0].name)
    assertEquals("memos/no-timestamp", result[1].name)
  }

  @Test
  fun `when tracking posts then sorted by tracked date not createTime`() {
    val trackingJan15 =
      Memo(
        name = "memos/jan15",
        content = "#daily/2026-01-15\n\n#habits/exercise",
        createTime = Instant.fromEpochMilliseconds(3000L),
      )
    val trackingJan20 =
      Memo(
        name = "memos/jan20",
        content = "#daily/2026-01-20\n\n#habits/reading",
        createTime = Instant.fromEpochMilliseconds(1000L),
      )

    val result = sortedMemos(listOf(trackingJan15, trackingJan20))

    assertEquals("memos/jan20", result[0].name)
    assertEquals("memos/jan15", result[1].name)
  }

  @Test
  fun `when mixed tracking and regular posts then tracking sorted by tracked date`() {
    val trackingJan15 =
      Memo(
        name = "memos/jan15",
        content = "#daily/2026-01-15\n\n#habits/exercise",
        createTime = Instant.fromEpochMilliseconds(3000L),
      )
    val trackingJan20 =
      Memo(
        name = "memos/jan20",
        content = "#daily/2026-01-20\n\n#habits/reading",
        createTime = Instant.fromEpochMilliseconds(1000L),
      )
    val regularPost =
      Memo(
        name = "memos/regular",
        content = "Just a regular post",
        createTime = Instant.fromEpochMilliseconds(2000L),
      )

    val result = sortedMemos(listOf(trackingJan15, regularPost, trackingJan20))

    assertEquals("memos/jan20", result[0].name)
    assertEquals("memos/jan15", result[1].name)
    assertEquals("memos/regular", result[2].name)
  }

  @Test
  fun `when createTime preferred over updateTime for regular memos`() {
    val memo =
      Memo(
        name = "memos/1",
        content = "Test",
        createTime = Instant.fromEpochMilliseconds(1000L),
        updateTime = Instant.fromEpochMilliseconds(5000L),
      )
    val olderByCreateTime =
      Memo(
        name = "memos/2",
        content = "Test 2",
        createTime = Instant.fromEpochMilliseconds(500L),
        updateTime = Instant.fromEpochMilliseconds(6000L),
      )

    val result = sortedMemos(listOf(olderByCreateTime, memo))

    assertEquals("memos/1", result[0].name)
    assertEquals("memos/2", result[1].name)
  }
}
