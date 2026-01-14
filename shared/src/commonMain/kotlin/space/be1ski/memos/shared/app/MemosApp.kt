@file:Suppress("TooManyFunctions")

package space.be1ski.memos.shared.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import space.be1ski.memos.shared.Res
import space.be1ski.memos.shared.app_name
import space.be1ski.memos.shared.action_cancel
import space.be1ski.memos.shared.action_create
import space.be1ski.memos.shared.action_create_memo
import space.be1ski.memos.shared.feature.memos.domain.model.Memo
import space.be1ski.memos.shared.title_edit_memo
import space.be1ski.memos.shared.nav_feed
import space.be1ski.memos.shared.nav_habits
import space.be1ski.memos.shared.title_new_memo
import space.be1ski.memos.shared.nav_posts
import space.be1ski.memos.shared.core.ui.ActivityMode
import space.be1ski.memos.shared.core.ui.ActivityRange
import space.be1ski.memos.shared.core.ui.Indent
import space.be1ski.memos.shared.feature.habits.presentation.calculateSuccessRate
import space.be1ski.memos.shared.feature.habits.presentation.components.earliestMemoDate
import space.be1ski.memos.shared.feature.habits.presentation.components.quarterIndex
import space.be1ski.memos.shared.feature.habits.presentation.components.rememberActivityWeekData
import space.be1ski.memos.shared.feature.habits.presentation.components.rememberHabitsConfigTimeline
import space.be1ski.memos.shared.feature.habits.presentation.components.startOfWeek
import space.be1ski.memos.shared.feature.auth.presentation.CredentialsDialog
import space.be1ski.memos.shared.feature.habits.presentation.HabitsAction
import space.be1ski.memos.shared.feature.habits.presentation.HabitsState
import space.be1ski.memos.shared.feature.habits.presentation.createHabitsFeature
import space.be1ski.memos.shared.feature.memos.presentation.MemosAction
import space.be1ski.memos.shared.feature.memos.presentation.MemosState
import space.be1ski.memos.shared.feature.memos.presentation.createMemosFeature
import space.be1ski.memos.shared.feature.memos.presentation.FeedScreen
import space.be1ski.memos.shared.feature.memos.presentation.PostsScreen
import space.be1ski.memos.shared.feature.habits.presentation.StatsScreen
import space.be1ski.memos.shared.feature.habits.presentation.StatsScreenState
import space.be1ski.memos.shared.feature.memos.domain.repository.MemosRepository
import space.be1ski.memos.shared.core.platform.currentLocalDate
import space.be1ski.memos.shared.core.platform.isDesktop
import space.be1ski.memos.shared.feature.preferences.domain.model.TimeRangeTab
import space.be1ski.memos.shared.feature.memos.domain.usecase.CreateMemoUseCase
import space.be1ski.memos.shared.feature.memos.domain.usecase.DeleteMemoUseCase
import space.be1ski.memos.shared.feature.memos.domain.usecase.LoadCachedMemosUseCase
import space.be1ski.memos.shared.feature.auth.domain.usecase.LoadCredentialsUseCase
import space.be1ski.memos.shared.feature.memos.domain.usecase.LoadMemosUseCase
import space.be1ski.memos.shared.feature.preferences.domain.usecase.LoadPreferencesUseCase
import space.be1ski.memos.shared.domain.usecase.LoadStorageInfoUseCase
import space.be1ski.memos.shared.feature.auth.domain.usecase.SaveCredentialsUseCase
import space.be1ski.memos.shared.feature.preferences.domain.usecase.SaveTimeRangeTabUseCase
import space.be1ski.memos.shared.feature.preferences.domain.usecase.TimeRangeScreen
import space.be1ski.memos.shared.feature.memos.domain.usecase.UpdateMemoUseCase
import space.be1ski.memos.shared.feature.memos.presentation.MemosUseCases
import space.be1ski.memos.shared.feature.mode.domain.model.AppMode
import space.be1ski.memos.shared.feature.mode.domain.usecase.LoadAppModeUseCase
import space.be1ski.memos.shared.feature.mode.domain.usecase.ResetAppUseCase
import space.be1ski.memos.shared.feature.mode.domain.usecase.SwitchAppModeUseCase
import space.be1ski.memos.shared.action_refresh
import space.be1ski.memos.shared.action_save
import space.be1ski.memos.shared.hint_write_memo
import space.be1ski.memos.shared.nav_settings

@Suppress("LongMethod")
@Composable
fun MemosApp(onResetApp: () -> Unit = {}) {
  val loadPreferencesUseCase: LoadPreferencesUseCase = koinInject()
  val saveTimeRangeTabUseCase: SaveTimeRangeTabUseCase = koinInject()
  val loadStorageInfoUseCase: LoadStorageInfoUseCase = koinInject()
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

  val initialPrefs = remember { loadPreferencesUseCase() }
  val initialMode = remember { loadAppModeUseCase() }
  val appState = remember {
    MemosAppUiState(
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
    createMemosFeature(memosUseCases, isOfflineMode = initialMode == AppMode.Offline)
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

  val storageInfo = remember { loadStorageInfoUseCase() }

  SyncAutoLoad(memosState, appState, dispatchMemos)
  SyncCredentialsDialog(memosState, appState)

  MemosAppContent(memosState, appState, dispatchMemos, saveTimeRangeTabUseCase, habitsState, habitsFeature::send)
  CredentialsDialog(
    memosState = memosState,
    appState = appState,
    dispatch = dispatchMemos,
    storageInfo = storageInfo,
    switchAppModeUseCase = switchAppModeUseCase,
    resetAppUseCase = resetAppUseCase,
    onResetComplete = onResetApp
  )
  MemoCreateDialog(appState, dispatchMemos)
  MemoEditDialog(appState, dispatchMemos)
}

@Composable
private fun SyncAutoLoad(
  memosState: MemosState,
  appState: MemosAppUiState,
  dispatch: (MemosAction) -> Unit
) {
  LaunchedEffect(memosState.credentialsMode, appState.autoLoaded, memosState.isLoading) {
    val shouldAutoLoad = !memosState.credentialsMode &&
      !appState.autoLoaded &&
      !memosState.isLoading &&
      memosState.hasCredentials
    if (shouldAutoLoad) {
      appState.autoLoaded = true
      dispatch(MemosAction.LoadMemos)
    }
  }
}

@Composable
private fun SyncCredentialsDialog(
  memosState: MemosState,
  appState: MemosAppUiState
) {
  LaunchedEffect(memosState.credentialsMode, appState.showCredentialsDialog, appState.credentialsDismissed) {
    if (memosState.credentialsMode && !appState.showCredentialsDialog && !appState.credentialsDismissed) {
      appState.showCredentialsDialog = true
      appState.credentialsInitialized = false
    }
    if (!memosState.credentialsMode) {
      appState.credentialsDismissed = false
    }
  }
}

@Suppress("LongParameterList", "LongMethod")
@Composable
private fun MemosAppContent(
  memosState: MemosState,
  appState: MemosAppUiState,
  dispatchMemos: (MemosAction) -> Unit,
  saveTimeRangeTabUseCase: SaveTimeRangeTabUseCase,
  habitsState: HabitsState,
  onHabitsAction: (HabitsAction) -> Unit
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
              val weekData = rememberActivityWeekData(memosState.memos, activityRange, ActivityMode.Habits)
              val today = remember { currentLocalDate() }
              val data = remember(weekData, activityRange, today) {
                calculateSuccessRate(weekData, activityRange, today)
              }
              if (data.total > 0) data.rate else null
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
                  appState.habitsTimeRangeTab = newTab
                  saveTimeRangeTabUseCase(TimeRangeScreen.Habits, newTab)
                }
                MemosScreen.Stats -> {
                  appState.postsTimeRangeTab = newTab
                  saveTimeRangeTabUseCase(TimeRangeScreen.Posts, newTab)
                }
                MemosScreen.Feed -> {}
              }
            },
            onRangeChange = { range -> updateTimeRangeState(appState, range) }
          )
        }
        MemosTabContent(memosState, appState, activityRange, habitsState, onHabitsAction)
      }
    }
}

@Composable
private fun MemosHeader(appState: MemosAppUiState, dispatch: (MemosAction) -> Unit) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(stringResource(Res.string.app_name), style = MaterialTheme.typography.headlineSmall)
    Row(horizontalArrangement = Arrangement.spacedBy(Indent.xs), verticalAlignment = Alignment.CenterVertically) {
      if (isDesktop) {
        IconButton(onClick = { dispatch(MemosAction.LoadMemos) }) {
          Icon(imageVector = Icons.Filled.Refresh, contentDescription = stringResource(Res.string.action_refresh))
        }
      }
      TextButton(
        onClick = {
          dispatch(MemosAction.EditCredentials)
          appState.showCredentialsDialog = true
          appState.credentialsInitialized = false
          appState.credentialsDismissed = false
        }
      ) {
        Text(stringResource(Res.string.nav_settings))
      }
    }
  }
}

@Composable
private fun MemosBottomNavigation(appState: MemosAppUiState) {
  NavigationBar {
    NavigationBarItem(
      selected = appState.selectedScreen == MemosScreen.Habits,
      onClick = { appState.selectedScreen = MemosScreen.Habits },
      icon = {
        Icon(
          imageVector = Icons.Filled.CheckCircle,
          contentDescription = stringResource(Res.string.nav_habits)
        )
      },
      label = { Text(stringResource(Res.string.nav_habits)) }
    )
    NavigationBarItem(
      selected = appState.selectedScreen == MemosScreen.Stats,
      onClick = { appState.selectedScreen = MemosScreen.Stats },
      icon = {
        Icon(
          imageVector = Icons.Filled.Description,
          contentDescription = stringResource(Res.string.nav_posts)
        )
      },
      label = { Text(stringResource(Res.string.nav_posts)) }
    )
    NavigationBarItem(
      selected = appState.selectedScreen == MemosScreen.Feed,
      onClick = { appState.selectedScreen = MemosScreen.Feed },
      icon = {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.List,
          contentDescription = stringResource(Res.string.nav_feed)
        )
      },
      label = { Text(stringResource(Res.string.nav_feed)) }
    )
  }
}

@Composable
private fun MemosTabContent(
  memosState: MemosState,
  appState: MemosAppUiState,
  activityRange: ActivityRange,
  habitsState: HabitsState,
  onHabitsAction: (HabitsAction) -> Unit
) {
  val memos = memosState.memos
  when (appState.selectedScreen) {
    MemosScreen.Habits -> StatsScreen(
      state = StatsScreenState(
        memos = memos,
        range = activityRange,
        activityMode = ActivityMode.Habits,
        useVerticalScroll = true,
        enablePullRefresh = false,
        demoMode = appState.demoMode
      ),
      habitsState = habitsState,
      onHabitsAction = onHabitsAction
    )
    MemosScreen.Stats -> PostsScreen(
      memos = memos,
      range = activityRange,
      demoMode = appState.demoMode
    )
    MemosScreen.Feed -> FeedScreen(
      memos = memos,
      isRefreshing = memosState.isLoading,
      onRefresh = {},
      enablePullRefresh = !isDesktop,
      demoMode = appState.demoMode,
      onMemoClick = { memo -> beginEditMemo(appState, memo) }
    )
  }
}

private fun activityRangeForState(appState: MemosAppUiState): ActivityRange {
  val selectedTab = when (appState.selectedScreen) {
    MemosScreen.Habits -> appState.habitsTimeRangeTab
    MemosScreen.Stats -> appState.postsTimeRangeTab
    MemosScreen.Feed -> appState.habitsTimeRangeTab
  }
  return when (selectedTab) {
    TimeRangeTab.Weeks -> ActivityRange.Week(appState.weekStart)
    TimeRangeTab.Months -> ActivityRange.Month(appState.monthYear, appState.month)
    TimeRangeTab.Quarters -> ActivityRange.Quarter(appState.quarterYear, appState.quarterIndex)
    TimeRangeTab.Years -> ActivityRange.Year(appState.year)
  }
}

private fun updateTimeRangeState(appState: MemosAppUiState, range: ActivityRange) {
  when (range) {
    is ActivityRange.Week -> appState.weekStart = range.startDate
    is ActivityRange.Month -> {
      appState.monthYear = range.year
      appState.month = range.month
    }
    is ActivityRange.Quarter -> {
      appState.quarterYear = range.year
      appState.quarterIndex = range.index
    }
    is ActivityRange.Year -> appState.year = range.year
  }
}

private fun currentRangeForTab(tab: TimeRangeTab, today: LocalDate): ActivityRange {
  return when (tab) {
    TimeRangeTab.Weeks -> ActivityRange.Week(startOfWeek(today))
    TimeRangeTab.Months -> ActivityRange.Month(today.year, today.month)
    TimeRangeTab.Quarters -> ActivityRange.Quarter(today.year, quarterIndex(today))
    TimeRangeTab.Years -> ActivityRange.Year(today.year)
  }
}

private fun minRangeForTab(tab: TimeRangeTab, earliestDate: LocalDate?): ActivityRange? {
  if (earliestDate == null) {
    return null
  }
  return when (tab) {
    TimeRangeTab.Weeks -> ActivityRange.Week(startOfWeek(earliestDate))
    TimeRangeTab.Months -> ActivityRange.Month(earliestDate.year, earliestDate.month)
    TimeRangeTab.Quarters -> ActivityRange.Quarter(earliestDate.year, quarterIndex(earliestDate))
    TimeRangeTab.Years -> ActivityRange.Year(earliestDate.year)
  }
}

@Composable
private fun MemoCreateDialog(appState: MemosAppUiState, dispatch: (MemosAction) -> Unit) {
  if (!appState.showCreateMemoDialog) {
    return
  }
  AlertDialog(
    onDismissRequest = {
      appState.showCreateMemoDialog = false
      appState.createMemoContent = ""
    },
    title = { Text(stringResource(Res.string.title_new_memo)) },
    text = {
      TextField(
        value = appState.createMemoContent,
        onValueChange = { appState.createMemoContent = it },
        placeholder = { Text(stringResource(Res.string.hint_write_memo)) },
        modifier = Modifier.fillMaxWidth()
      )
    },
    confirmButton = {
      val content = appState.createMemoContent.trim()
      Button(
        onClick = {
          dispatch(MemosAction.CreateMemo(content))
          appState.showCreateMemoDialog = false
          appState.createMemoContent = ""
        },
        enabled = content.isNotBlank()
      ) {
        Text(stringResource(Res.string.action_create))
      }
    },
    dismissButton = {
      TextButton(
        onClick = {
          appState.showCreateMemoDialog = false
          appState.createMemoContent = ""
        }
      ) {
        Text(stringResource(Res.string.action_cancel))
      }
    }
  )
}

@Composable
private fun MemoEditDialog(appState: MemosAppUiState, dispatch: (MemosAction) -> Unit) {
  if (!appState.showEditMemoDialog) {
    return
  }
  val memo = appState.editMemoTarget ?: return
  AlertDialog(
    onDismissRequest = { clearMemoEdit(appState) },
    title = { Text(stringResource(Res.string.title_edit_memo)) },
    text = {
      TextField(
        value = appState.editMemoContent,
        onValueChange = { appState.editMemoContent = it },
        modifier = Modifier.fillMaxWidth()
      )
    },
    confirmButton = {
      val content = appState.editMemoContent.trim()
      Button(
        onClick = {
          dispatch(MemosAction.UpdateMemo(memo.name, content))
          clearMemoEdit(appState)
        },
        enabled = content.isNotBlank()
      ) {
        Text(stringResource(Res.string.action_save))
      }
    },
    dismissButton = {
      TextButton(onClick = { clearMemoEdit(appState) }) {
        Text(stringResource(Res.string.action_cancel))
      }
    }
  )
}

private fun beginEditMemo(appState: MemosAppUiState, memo: Memo) {
  appState.editMemoTarget = memo
  appState.editMemoContent = memo.content
  appState.showEditMemoDialog = true
}

private fun clearMemoEdit(appState: MemosAppUiState) {
  appState.showEditMemoDialog = false
  appState.editMemoTarget = null
  appState.editMemoContent = ""
}
