package space.be1ski.memos.shared.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import space.be1ski.memos.shared.feature.memos.domain.model.Memo
import space.be1ski.memos.shared.feature.preferences.domain.model.TimeRangeTab
import kotlinx.datetime.LocalDate
import space.be1ski.memos.shared.feature.habits.presentation.components.quarterIndex
import space.be1ski.memos.shared.feature.habits.presentation.components.startOfWeek

internal enum class MemosScreen {
  Habits,
  Stats,
  Feed
}

internal class MemosAppUiState(
  currentDate: LocalDate,
  initialTimeRangeTab: TimeRangeTab = TimeRangeTab.Weeks
) {
  var selectedScreen by mutableStateOf(MemosScreen.Habits)
  var selectedTimeRangeTab by mutableStateOf(initialTimeRangeTab)
  var weekStart by mutableStateOf(startOfWeek(currentDate))
  var monthYear by mutableStateOf(currentDate.year)
  var month by mutableStateOf(currentDate.month)
  var quarterYear by mutableStateOf(currentDate.year)
  var quarterIndex by mutableStateOf(quarterIndex(currentDate))
  var year by mutableStateOf(currentDate.year)
  var demoMode by mutableStateOf(false)
  var autoLoaded by mutableStateOf(false)
  var showCredentialsDialog by mutableStateOf(false)
  var credentialsInitialized by mutableStateOf(false)
  var credentialsDismissed by mutableStateOf(false)
  var editBaseUrl by mutableStateOf("")
  var editToken by mutableStateOf("")
  var showCreateMemoDialog by mutableStateOf(false)
  var createMemoContent by mutableStateOf("")
  var showEditMemoDialog by mutableStateOf(false)
  var editMemoContent by mutableStateOf("")
  var editMemoTarget by mutableStateOf<Memo?>(null)
}
