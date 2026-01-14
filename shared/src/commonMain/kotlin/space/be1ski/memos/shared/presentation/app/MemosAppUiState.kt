package space.be1ski.memos.shared.presentation.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import space.be1ski.memos.shared.domain.model.memo.Memo
import space.be1ski.memos.shared.domain.model.preferences.TimeRangeTab
import kotlinx.datetime.LocalDate
import space.be1ski.memos.shared.presentation.components.startOfWeek

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
  var quarterIndex by mutableStateOf(currentQuarterIndex(currentDate))
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

private fun currentQuarterIndex(date: LocalDate): Int {
  return date.month.ordinal / MONTHS_IN_QUARTER + FIRST_QUARTER_INDEX
}

private const val MONTHS_IN_QUARTER = 3
private const val FIRST_QUARTER_INDEX = 1
