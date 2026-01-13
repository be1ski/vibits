package space.be1ski.memos.shared.presentation.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import space.be1ski.memos.shared.presentation.components.ActivityRange
import space.be1ski.memos.shared.domain.model.memo.Memo

internal class MemosAppUiState {
  var selectedTab by mutableStateOf(0)
  var activityRange by mutableStateOf<ActivityRange>(ActivityRange.Last90Days)
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
