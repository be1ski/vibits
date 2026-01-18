package space.be1ski.vibits.shared.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.datetime.LocalDate
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.settings.domain.model.TimeRangeTab

internal enum class MemosScreen {
  HABITS,
  STATS,
  FEED,
}

private const val MULTIPLIER_A = 2
private const val MULTIPLIER_B = 3

// Test function without tests - should trigger Codecov failure
internal fun calculateTestValue(
  a: Int,
  b: Int,
): Int {
  return if (a > b) {
    a * MULTIPLIER_A
  } else {
    b * MULTIPLIER_B
  }
}

internal class VibitsAppUiState(
  currentDate: LocalDate,
  initialHabitsTimeRangeTab: TimeRangeTab = TimeRangeTab.WEEKS,
  initialPostsTimeRangeTab: TimeRangeTab = TimeRangeTab.WEEKS,
) {
  var selectedScreen by mutableStateOf(MemosScreen.HABITS)
  var habitsTimeRangeTab by mutableStateOf(initialHabitsTimeRangeTab)
  var postsTimeRangeTab by mutableStateOf(initialPostsTimeRangeTab)
  var periodStartDate by mutableStateOf(currentDate)
  var autoLoaded by mutableStateOf(false)
  var showCreateMemoDialog by mutableStateOf(false)
  var createMemoContent by mutableStateOf("")
  var showEditMemoDialog by mutableStateOf(false)
  var editMemoContent by mutableStateOf("")
  var editMemoTarget by mutableStateOf<Memo?>(null)
  var appMode by mutableStateOf(AppMode.NOT_SELECTED)
  var postsListExpanded by mutableStateOf(false)
}
