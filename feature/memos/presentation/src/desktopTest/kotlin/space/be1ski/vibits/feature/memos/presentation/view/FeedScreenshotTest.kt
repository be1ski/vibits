package space.be1ski.vibits.feature.memos.presentation.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month
import space.be1ski.vibits.core.ui.date.DateFormatter
import space.be1ski.vibits.core.ui.test.runAppUiTest
import space.be1ski.vibits.core.ui.test.saveScreenshot
import space.be1ski.vibits.core.ui.test.setThemedContent
import space.be1ski.vibits.feature.memos.domain.model.Memo
import space.be1ski.vibits.feature.memos.domain.model.PostFilter
import kotlin.test.Test
import kotlin.time.Duration.Companion.hours

@OptIn(ExperimentalTestApi::class)
class FeedScreenshotTest {
  private val testDateFormatter =
    DateFormatter(
      months = Month.entries.associateWith { it.name.lowercase().take(3) },
      days = DayOfWeek.entries.associateWith { it.name.lowercase().take(3) },
    )

  private val baseInstant = kotlin.time.Instant.fromEpochMilliseconds(1_704_067_200_000) // 2024-01-01T00:00:00Z

  private val testMemos =
    listOf(
      Memo(
        name = "memos/1",
        content = "#habits/config\n\nExercise | #habits/exercise | #4CAF50\nWater | #habits/water | #2196F3",
        createTime = baseInstant,
      ),
      Memo(name = "memos/2", content = "#habits/daily 2024-01-01\n\n#habits/exercise\n#habits/water", createTime = baseInstant + 1.hours),
      Memo(name = "memos/3", content = "Today was a good day!", createTime = baseInstant + 2.hours),
      Memo(name = "memos/4", content = "Remember to buy groceries", createTime = baseInstant + 3.hours),
    )

  @Test
  fun `when feed has memos then captures default feed`() =
    runAppUiTest {
      setThemedContent {
        FeedScreen(
          memos = testMemos,
          dateFormatter = testDateFormatter,
          enablePullRefresh = false,
        )
      }

      onNodeWithTag(FeedTestTags.FEED_SCREEN).assertIsDisplayed()
      saveScreenshot("memos", "FeedScreenshotTest", "feed_default")
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
      saveScreenshot("memos", "FeedScreenshotTest", "feed_empty")
    }

  @Test
  fun `when filtered to config then captures config filter`() =
    runAppUiTest {
      setThemedContent {
        FeedScreen(
          memos = testMemos,
          dateFormatter = testDateFormatter,
          activeFilter = PostFilter.CONFIG,
          enablePullRefresh = false,
        )
      }

      onNodeWithTag(FeedTestTags.FEED_SCREEN).assertIsDisplayed()
      saveScreenshot("memos", "FeedScreenshotTest", "feed_filtered_config")
    }

  @Test
  fun `when demo mode then captures feed with localized habit labels`() =
    runAppUiTest {
      setThemedContent {
        FeedScreen(
          memos = testMemos,
          dateFormatter = testDateFormatter,
          enablePullRefresh = false,
          demoMode = true,
        )
      }

      onNodeWithTag(FeedTestTags.FEED_SCREEN).assertIsDisplayed()
      saveScreenshot("memos", "FeedScreenshotTest", "feed_demo_mode")
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
      saveScreenshot("memos", "FeedScreenshotTest", "sync_conflict_dialog")
    }

  @Test
  fun `when sync log dialog shown then captures dialog`() =
    runAppUiTest {
      setThemedContent {
        SyncLogDialog(onDismiss = {})
      }

      onNodeWithTag(FeedTestTags.SYNC_LOG_DIALOG).assertIsDisplayed()
      saveScreenshot("memos", "FeedScreenshotTest", "sync_log_dialog")
    }
}
