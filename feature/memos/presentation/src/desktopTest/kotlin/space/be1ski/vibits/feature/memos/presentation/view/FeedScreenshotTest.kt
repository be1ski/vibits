package space.be1ski.vibits.feature.memos.presentation.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month
import space.be1ski.vibits.core.platform.logging.LogLevel
import space.be1ski.vibits.core.ui.date.DateFormatter
import space.be1ski.vibits.core.ui.test.runAppUiTest
import space.be1ski.vibits.core.ui.test.saveScreenshot
import space.be1ski.vibits.core.ui.test.setThemedContent
import space.be1ski.vibits.core.utils.logging.LogEntry
import space.be1ski.vibits.feature.memos.domain.model.Memo
import space.be1ski.vibits.feature.memos.domain.model.PostFilter
import kotlin.test.Test
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

@OptIn(ExperimentalTestApi::class)
class FeedScreenshotTest {
  private val testDateFormatter =
    DateFormatter(
      months = Month.entries.associateWith { it.name.lowercase().take(3) },
      days = DayOfWeek.entries.associateWith { it.name.lowercase().take(3) },
    )

  private val baseInstant = Instant.fromEpochMilliseconds(1_704_067_200_000) // 2024-01-01T00:00:00Z

  private val demoMemos =
    listOf(
      Memo(
        name = "memos/1",
        content =
          "#habits/config\n\nExercise | #habits/exercise | #4CAF50\n" +
            "Water | #habits/water | #2196F3\nReading | #habits/reading | #FF9800",
        createTime = baseInstant,
      ),
      Memo(name = "memos/2", content = "#habits/daily 2024-01-01\n\n#habits/exercise\n#habits/water", createTime = baseInstant + 1.hours),
      Memo(
        name = "memos/3",
        content = "#habits/daily 2024-01-02\n\n#habits/exercise\n#habits/reading",
        createTime = baseInstant + 25.hours,
      ),
      Memo(name = "memos/4", content = "Today was a good day!", createTime = baseInstant + 2.hours),
      Memo(name = "memos/5", content = "Remember to buy groceries", createTime = baseInstant + 3.hours),
      Memo(name = "memos/6", content = "Interesting article about productivity I found today.", createTime = baseInstant + 26.hours),
      Memo(name = "memos/7", content = "Book recommendation from a friend - need to check it out.", createTime = baseInstant + 27.hours),
      Memo(name = "memos/8", content = "#habits/daily 2024-01-03\n\n#habits/exercise", createTime = baseInstant + 49.hours),
    )

  private val testSyncLogs =
    listOf(
      LogEntry("2025-01-06 10:00:00", LogLevel.INFO, "SyncEngine", "Sync started"),
      LogEntry("2025-01-06 10:00:01", LogLevel.DEBUG, "SyncEngine", "Fetching 42 memos from server"),
      LogEntry("2025-01-06 10:00:02", LogLevel.INFO, "SyncEngine", "Sync completed successfully"),
    )

  @Test
  fun `when feed has demo data then captures feed`() =
    runAppUiTest {
      setThemedContent {
        FeedScreen(
          memos = demoMemos,
          dateFormatter = testDateFormatter,
          enablePullRefresh = false,
          demoMode = true,
        )
      }
      onNodeWithTag(FeedTestTags.FEED_SCREEN).assertIsDisplayed()
      saveScreenshot("feed_demo_data")
    }

  @Test
  fun `when feed is empty then captures empty state`() =
    runAppUiTest {
      setThemedContent {
        FeedScreen(
          memos = emptyList(),
          dateFormatter = testDateFormatter,
          enablePullRefresh = false,
        )
      }
      onNodeWithTag(FeedTestTags.FEED_SCREEN).assertIsDisplayed()
      saveScreenshot("feed_empty")
    }

  @Test
  fun `when filtered to config then captures config filter`() =
    runAppUiTest {
      setThemedContent {
        FeedScreen(
          memos = demoMemos,
          dateFormatter = testDateFormatter,
          activeFilter = PostFilter.CONFIG,
          enablePullRefresh = false,
        )
      }
      onNodeWithTag(FeedTestTags.FEED_SCREEN).assertIsDisplayed()
      saveScreenshot("feed_filtered_config")
    }

  @Test
  fun `when filtered to tracking then captures tracking filter`() =
    runAppUiTest {
      setThemedContent {
        FeedScreen(
          memos = demoMemos,
          dateFormatter = testDateFormatter,
          activeFilter = PostFilter.HABIT_TRACKING,
          enablePullRefresh = false,
          demoMode = true,
        )
      }
      onNodeWithTag(FeedTestTags.FEED_SCREEN).assertIsDisplayed()
      saveScreenshot("feed_filtered_tracking")
    }

  @Test
  fun `when sync conflict dialog shown then captures dialog`() =
    runAppUiTest {
      setThemedContent {
        SyncConflictDialog(
          conflictCount = 3,
          onKeepLocal = {},
          onKeepServer = {},
          onDismiss = {},
        )
      }
      onNodeWithTag(FeedTestTags.SYNC_CONFLICT_DIALOG).assertIsDisplayed()
      saveScreenshot("sync_conflict_dialog")
    }

  @Test
  fun `when sync log dialog shown then captures dialog`() =
    runAppUiTest {
      setThemedContent {
        SyncLogDialog(
          onDismiss = {},
          initialLogs = testSyncLogs,
        )
      }
      onNodeWithTag(FeedTestTags.SYNC_LOG_DIALOG).assertIsDisplayed()
      saveScreenshot("sync_log_dialog")
    }
}
