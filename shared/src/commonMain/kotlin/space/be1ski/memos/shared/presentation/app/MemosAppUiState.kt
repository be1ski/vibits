package space.be1ski.memos.shared.presentation.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import space.be1ski.memos.shared.presentation.components.ActivityRange

internal class MemosAppUiState {
  var selectedTab by mutableStateOf(0)
  var activityRange by mutableStateOf<ActivityRange>(ActivityRange.Last90Days)
  var autoLoaded by mutableStateOf(false)
  var showCredentialsDialog by mutableStateOf(false)
  var credentialsInitialized by mutableStateOf(false)
  var credentialsDismissed by mutableStateOf(false)
  var editBaseUrl by mutableStateOf("")
  var editToken by mutableStateOf("")
}
