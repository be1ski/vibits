package space.be1ski.vibits.shared.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import space.be1ski.vibits.shared.Res
import space.be1ski.vibits.shared.action_create_memo
import space.be1ski.vibits.shared.core.platform.currentLocalDate
import space.be1ski.vibits.shared.core.ui.Indent
import space.be1ski.vibits.shared.feature.habits.domain.usecase.CalculateSuccessRateUseCase
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsAction
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsState
import space.be1ski.vibits.shared.feature.habits.presentation.components.earliestMemoDate
import space.be1ski.vibits.shared.feature.habits.presentation.components.rememberHabitsConfigTimeline
import space.be1ski.vibits.shared.feature.habits.presentation.createHabitsFeature
import space.be1ski.vibits.shared.feature.auth.domain.usecase.LoadCredentialsUseCase
import space.be1ski.vibits.shared.feature.auth.domain.usecase.SaveCredentialsUseCase
import space.be1ski.vibits.shared.feature.auth.presentation.CredentialsDialog
import space.be1ski.vibits.shared.feature.memos.domain.repository.MemosRepository
import space.be1ski.vibits.shared.feature.memos.domain.usecase.CreateMemoUseCase
import space.be1ski.vibits.shared.feature.memos.domain.usecase.DeleteMemoUseCase
import space.be1ski.vibits.shared.feature.memos.domain.usecase.LoadCachedMemosUseCase
import space.be1ski.vibits.shared.feature.memos.domain.usecase.LoadMemosUseCase
import space.be1ski.vibits.shared.feature.memos.domain.usecase.UpdateMemoUseCase
import space.be1ski.vibits.shared.feature.memos.presentation.MemosAction
import space.be1ski.vibits.shared.feature.memos.presentation.MemosState
import space.be1ski.vibits.shared.feature.memos.presentation.MemosUseCases
import space.be1ski.vibits.shared.feature.memos.presentation.createMemosFeature
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.mode.domain.usecase.LoadAppModeUseCase
import space.be1ski.vibits.shared.feature.mode.domain.usecase.ResetAppUseCase
import space.be1ski.vibits.shared.feature.mode.domain.usecase.SwitchAppModeUseCase
import space.be1ski.vibits.shared.feature.preferences.domain.usecase.LoadPreferencesUseCase
import space.be1ski.vibits.shared.feature.preferences.domain.usecase.SaveTimeRangeTabUseCase
import space.be1ski.vibits.shared.feature.preferences.domain.usecase.TimeRangeScreen
import space.be1ski.vibits.shared.domain.usecase.LoadAppDetailsUseCase

@Suppress("LongMethod")
@Composable
fun VibitsApp(onResetApp: () -> Unit = {}) {
  val loadPreferencesUseCase: LoadPreferencesUseCase = koinInject()
  val saveTimeRangeTabUseCase: SaveTimeRangeTabUseCase = koinInject()
  val loadAppDetailsUseCase: LoadAppDetailsUseCase = koinInject()
  val memosRepository: MemosRepository = koinInject()
  val loadMemosUseCase: LoadMemosUseCase = koinInject()
  val loadCachedMemosUseCase: LoadCachedMemosUseCase = koinInject()
  val loadCredentialsUseCase: LoadCredentialsUseCase = koinInject()
  val saveCredentialsUseCase: SaveCredentialsUseCase = koinInject()
  val createMemoUseCase: CreateMemoUseCase = koinInject()
  val updateMemoUseCase: UpdateMemoUseCase = koinInject()
  val deleteMemoUseCase: DeleteMemoUseCase = koinInject()
  val loadAppModeUseCase: LoadAppModeUseCase = koinInject()
  val switchAppModeUseCase: SwitchAppModeUseCase = koinInject()
  val resetAppUseCase: ResetAppUseCase = koinInject()
  val calculateSuccessRate: CalculateSuccessRateUseCase = koinInject()

  val initialPrefs = remember { loadPreferencesUseCase() }
  val initialMode = remember { loadAppModeUseCase() }
  val appState = remember {
    VibitsAppUiState(
      currentDate = currentLocalDate(),
      initialHabitsTimeRangeTab = initialPrefs.habitsTimeRangeTab,
      initialPostsTimeRangeTab = initialPrefs.postsTimeRangeTab
    ).also { it.appMode = initialMode }
  }

  // MemosFeature
  val memosUseCases = remember {
    MemosUseCases(
      loadMemos = loadMemosUseCase,
      loadCachedMemos = loadCachedMemosUseCase,
      loadCredentials = loadCredentialsUseCase,
      saveCredentials = saveCredentialsUseCase,
      createMemo = createMemoUseCase,
      updateMemo = updateMemoUseCase,
      deleteMemo = deleteMemoUseCase
    )
  }
  val memosFeature = remember {
    val skipCredentials = initialMode == AppMode.Offline || initialMode == AppMode.Demo
    createMemosFeature(memosUseCases, isOfflineMode = skipCredentials)
  }
  val scope = rememberCoroutineScope()
  LaunchedEffect(memosFeature) {
    memosFeature.launchIn(scope)
  }
  val memosState by memosFeature.state.collectAsState()
  val dispatchMemos: (MemosAction) -> Unit = memosFeature::send

  // HabitsFeature
  val habitsFeature = remember {
    createHabitsFeature(
      memosRepository = memosRepository,
      onRefresh = { dispatchMemos(MemosAction.LoadMemos) }
    )
  }
  LaunchedEffect(habitsFeature) {
    habitsFeature.launchIn(scope)
  }
  val habitsState by habitsFeature.state.collectAsState()

  val appDetails = remember { loadAppDetailsUseCase() }

  SyncAutoLoad(memosState, appState, dispatchMemos)
  SyncCredentialsDialog(memosState, appState)

  VibitsAppContent(
    memosState = memosState,
    appState = appState,
    dispatchMemos = dispatchMemos,
    saveTimeRangeTabUseCase = saveTimeRangeTabUseCase,
    habitsState = habitsState,
    onHabitsAction = habitsFeature::send,
    calculateSuccessRate = calculateSuccessRate
  )
  CredentialsDialog(
    memosState = memosState,
    appState = appState,
    dispatch = dispatchMemos,
    appDetails = appDetails,
    switchAppModeUseCase = switchAppModeUseCase,
    resetAppUseCase = resetAppUseCase,
    onResetComplete = onResetApp
  )
  MemoCreateDialog(appState, dispatchMemos)
  MemoEditDialog(appState, dispatchMemos)
}

@Suppress("LongParameterList", "LongMethod")
@Composable
private fun VibitsAppContent(
  memosState: MemosState,
  appState: VibitsAppUiState,
  dispatchMemos: (MemosAction) -> Unit,
  saveTimeRangeTabUseCase: SaveTimeRangeTabUseCase,
  habitsState: HabitsState,
  onHabitsAction: (HabitsAction) -> Unit,
  calculateSuccessRate: CalculateSuccessRateUseCase
) {
  Scaffold(
    floatingActionButton = {
      if (memosFabModeForScreen(appState.selectedScreen) == MemosFabMode.Memo) {
        FloatingActionButton(
          onClick = { appState.showCreateMemoDialog = true }
        ) {
          Icon(
            imageVector = Icons.Filled.Edit,
            contentDescription = stringResource(Res.string.action_create_memo)
          )
        }
      }
    },
    bottomBar = {
      MemosBottomNavigation(appState)
    }
  ) { padding ->
    val selectedTab = when (appState.selectedScreen) {
      MemosScreen.Habits -> appState.habitsTimeRangeTab
      MemosScreen.Stats -> appState.postsTimeRangeTab
      MemosScreen.Feed -> appState.habitsTimeRangeTab
    }
    val currentRange = currentRangeForTab(selectedTab, currentLocalDate())
    val activityRange = activityRangeForState(appState)
    val timeZone = remember { TimeZone.currentSystemDefault() }
    val earliestDate = remember(memosState.memos) { earliestMemoDate(memosState.memos, timeZone) }
    val minRange = minRangeForTab(selectedTab, earliestDate)
    Column(
      modifier = Modifier
        .padding(padding)
        .padding(Indent.m)
        .fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(Indent.s)
    ) {
      MemosHeader(appState, dispatchMemos)
      memosState.errorMessage?.let { message ->
        Text(message, color = MaterialTheme.colorScheme.error)
      }
      if (appState.selectedScreen != MemosScreen.Feed) {
        val successRate = if (appState.selectedScreen == MemosScreen.Habits) {
          val habitsTimeline = rememberHabitsConfigTimeline(memosState.memos)
          val hasHabits = remember(habitsTimeline) { habitsTimeline.lastOrNull()?.habits?.isNotEmpty() == true }
          if (hasHabits) {
            rememberSuccessRate(memosState.memos, activityRange, calculateSuccessRate)
          } else null
        } else null
        TimeRangeControls(
          selectedTab = selectedTab,
          selectedRange = activityRange,
          currentRange = currentRange,
          minRange = minRange,
          successRate = successRate,
          onTabChange = { newTab ->
            when (appState.selectedScreen) {
              MemosScreen.Habits -> {
                adjustDateForTabChange(appState, appState.habitsTimeRangeTab, newTab)
                appState.habitsTimeRangeTab = newTab
                saveTimeRangeTabUseCase(TimeRangeScreen.Habits, newTab)
              }
              MemosScreen.Stats -> {
                adjustDateForTabChange(appState, appState.postsTimeRangeTab, newTab)
                appState.postsTimeRangeTab = newTab
                saveTimeRangeTabUseCase(TimeRangeScreen.Posts, newTab)
              }
              MemosScreen.Feed -> {}
            }
          },
          onRangeChange = { range -> updateTimeRangeState(appState, range) }
        )
      }
      SwipeableTabContent(
        memosState = memosState,
        appState = appState,
        currentRange = currentRange,
        minRange = minRange,
        habitsState = habitsState,
        onHabitsAction = onHabitsAction
      )
    }
  }
}
