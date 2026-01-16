package space.be1ski.vibits.shared.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.preferences.domain.model.TimeRangeTab
import kotlinx.datetime.LocalDate

internal enum class MemosScreen {
  Habits,
  Stats,
  Feed
}

internal class VibitsAppUiState(
  currentDate: LocalDate,
  initialHabitsTimeRangeTab: TimeRangeTab = TimeRangeTab.Weeks,
  initialPostsTimeRangeTab: TimeRangeTab = TimeRangeTab.Weeks
) {
  var selectedScreen by mutableStateOf(MemosScreen.Habits)
  var habitsTimeRangeTab by mutableStateOf(initialHabitsTimeRangeTab)
  var postsTimeRangeTab by mutableStateOf(initialPostsTimeRangeTab)
  var periodStartDate by mutableStateOf(currentDate)
  var autoLoaded by mutableStateOf(false)
  var showSettingsDialog by mutableStateOf(false)
  var settingsInitialized by mutableStateOf(false)
  var settingsDismissed by mutableStateOf(false)
  var editBaseUrl by mutableStateOf("")
  var editToken by mutableStateOf("")
  var showCreateMemoDialog by mutableStateOf(false)
  var createMemoContent by mutableStateOf("")
  var showEditMemoDialog by mutableStateOf(false)
  var editMemoContent by mutableStateOf("")
  var editMemoTarget by mutableStateOf<Memo?>(null)
  var appMode by mutableStateOf(AppMode.NotSelected)
}
