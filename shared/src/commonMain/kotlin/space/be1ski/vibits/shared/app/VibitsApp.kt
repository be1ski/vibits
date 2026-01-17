package space.be1ski.vibits.shared.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.shared.Res
import space.be1ski.vibits.shared.action_create_memo
import space.be1ski.vibits.shared.action_track_today
import space.be1ski.vibits.shared.core.platform.currentLocalDate
import space.be1ski.vibits.shared.core.ui.ActivityRange
import space.be1ski.vibits.shared.core.ui.Indent
import space.be1ski.vibits.shared.feature.habits.domain.usecase.CalculateSuccessRateUseCase
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsAction
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsState
import space.be1ski.vibits.shared.feature.habits.presentation.buildHabitDay
import space.be1ski.vibits.shared.feature.habits.presentation.components.ActivityWeekDataCache
import space.be1ski.vibits.shared.feature.habits.presentation.components.LocalActivityWeekDataCache
import space.be1ski.vibits.shared.feature.habits.presentation.components.earliestMemoDate
import space.be1ski.vibits.shared.feature.habits.presentation.components.findDailyMemoForDate
import space.be1ski.vibits.shared.feature.habits.presentation.components.habitsConfigForDate
import space.be1ski.vibits.shared.feature.habits.presentation.components.rememberHabitsConfigTimeline
import space.be1ski.vibits.shared.feature.habits.presentation.createHabitsFeature
import space.be1ski.vibits.shared.feature.memos.presentation.MemosAction
import space.be1ski.vibits.shared.feature.memos.presentation.MemosState
import space.be1ski.vibits.shared.feature.memos.presentation.createMemosFeature
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.settings.domain.model.AppLanguage
import space.be1ski.vibits.shared.feature.settings.domain.model.AppTheme
import space.be1ski.vibits.shared.feature.settings.domain.model.TimeRangeTab
import space.be1ski.vibits.shared.feature.settings.domain.usecase.SaveTimeRangeTabUseCase
import space.be1ski.vibits.shared.feature.settings.domain.usecase.TimeRangeScreen
import space.be1ski.vibits.shared.feature.settings.presentation.SettingsAction
import space.be1ski.vibits.shared.feature.settings.presentation.SettingsEffect
import space.be1ski.vibits.shared.feature.settings.presentation.components.SettingsDialog
import space.be1ski.vibits.shared.feature.settings.presentation.createSettingsFeature

@Suppress("LongMethod")
@Composable
fun VibitsApp(
  dependencies: VibitsAppDependencies,
  currentTheme: AppTheme,
  currentLanguage: AppLanguage,
  onResetApp: () -> Unit = {},
  onThemeChanged: (AppTheme) -> Unit = {},
  onLanguageChanged: (AppLanguage) -> Unit = {},
) {
  val initialPrefs = remember { dependencies.loadPreferences() }
  val initialMode = remember { dependencies.loadAppMode() }
  val appState =
    remember {
      VibitsAppUiState(
        currentDate = currentLocalDate(),
        initialHabitsTimeRangeTab = initialPrefs.habitsTimeRangeTab,
        initialPostsTimeRangeTab = initialPrefs.postsTimeRangeTab,
      ).also { it.appMode = initialMode }
    }

  // MemosFeature
  val memosFeature =
    remember {
      val skipCredentials = initialMode == AppMode.OFFLINE || initialMode == AppMode.DEMO
      createMemosFeature(dependencies.memosUseCases, isOfflineMode = skipCredentials)
    }
  val scope = rememberCoroutineScope()
  LaunchedEffect(memosFeature) {
    memosFeature.launchIn(scope)
  }
  val memosState by memosFeature.state.collectAsState()
  val dispatchMemos: (MemosAction) -> Unit = memosFeature::send

  // HabitsFeature
  val habitsFeature =
    remember {
      createHabitsFeature(
        memosRepository = dependencies.memosRepository,
        onRefresh = { dispatchMemos(MemosAction.LoadMemos) },
      )
    }
  LaunchedEffect(habitsFeature) {
    habitsFeature.launchIn(scope)
  }
  val habitsState by habitsFeature.state.collectAsState()

  val appDetails = remember { dependencies.loadAppDetails() }
  val activityWeekDataCache = remember { ActivityWeekDataCache() }

  // SettingsFeature
  val settingsFeature =
    remember {
      createSettingsFeature(
        useCases = dependencies.settingsUseCases,
        initialMode = initialMode,
        appDetails = appDetails,
      )
    }
  LaunchedEffect(settingsFeature) {
    settingsFeature.launchIn(scope)
  }
  val settingsState by settingsFeature.state.collectAsState()
  val dispatchSettings: (SettingsAction) -> Unit = settingsFeature::send

  // Observe settings effects for parent coordination
  LaunchedEffect(settingsFeature) {
    settingsFeature.effects.collect { effect ->
      when (effect) {
        is SettingsEffect.NotifyModeChanged -> {
          appState.appMode = effect.newMode
          dispatchMemos(MemosAction.LoadMemos)
        }
        is SettingsEffect.NotifyResetCompleted -> onResetApp()
        is SettingsEffect.NotifyCredentialsSaved -> {
          dispatchMemos(MemosAction.UpdateBaseUrl(effect.baseUrl))
          dispatchMemos(MemosAction.UpdateToken(effect.token))
          dispatchMemos(MemosAction.LoadMemos)
        }
        is SettingsEffect.NotifyThemeChanged -> {
          onThemeChanged(effect.theme)
        }
        is SettingsEffect.NotifyLanguageChanged -> {
          onLanguageChanged(effect.language)
        }
        is SettingsEffect.NotifyDialogClosed -> {
          // Dialog closed, nothing extra needed
        }
        else -> {
          // Internal effects handled by EffectHandler
        }
      }
    }
  }

  // Open settings when credentials are required
  LaunchedEffect(memosState.credentialsMode, settingsState.isOpen) {
    if (memosState.credentialsMode && !settingsState.isOpen) {
      dispatchSettings(
        SettingsAction.Open(
          baseUrl = memosState.baseUrl,
          token = memosState.token,
          appMode = appState.appMode,
          language = currentLanguage,
          theme = currentTheme,
        ),
      )
    }
  }

  SyncAutoLoad(memosState, appState, dispatchMemos)

  CompositionLocalProvider(LocalActivityWeekDataCache provides activityWeekDataCache) {
    VibitsAppContent(
      memosState = memosState,
      appState = appState,
      dispatchMemos = dispatchMemos,
      dispatchSettings = dispatchSettings,
      saveTimeRangeTab = dependencies.saveTimeRangeTab,
      habitsState = habitsState,
      onHabitsAction = habitsFeature::send,
      calculateSuccessRate = dependencies.calculateSuccessRate,
      language = currentLanguage,
      theme = currentTheme,
    )
    SettingsDialog(
      state = settingsState,
      dispatch = dispatchSettings,
    )
    MemoCreateDialog(appState, dispatchMemos)
    MemoEditDialog(appState, dispatchMemos)
  }
}

@Suppress("LongParameterList", "LongMethod", "CyclomaticComplexMethod")
@Composable
private fun VibitsAppContent(
  memosState: MemosState,
  appState: VibitsAppUiState,
  dispatchMemos: (MemosAction) -> Unit,
  dispatchSettings: (SettingsAction) -> Unit,
  saveTimeRangeTab: SaveTimeRangeTabUseCase,
  habitsState: HabitsState,
  onHabitsAction: (HabitsAction) -> Unit,
  calculateSuccessRate: CalculateSuccessRateUseCase,
  language: AppLanguage,
  theme: AppTheme,
) {
  val timeZone = remember { TimeZone.currentSystemDefault() }
  val today = currentLocalDate()
  val habitsTimeline = rememberHabitsConfigTimeline(memosState.memos)
  val todayConfig =
    remember(habitsTimeline, today) {
      habitsConfigForDate(habitsTimeline, today)?.habits.orEmpty()
    }
  val todayMemo =
    remember(memosState.memos, today) {
      findDailyMemoForDate(memosState.memos, timeZone, today)
    }
  val todayDay =
    remember(todayConfig, todayMemo, today) {
      buildHabitDay(date = today, habitsConfig = todayConfig, dailyMemo = todayMemo)
    }

  val onClearSelection =
    remember(onHabitsAction) {
      { onHabitsAction(HabitsAction.ClearSelection) }
    }
  val onShowCreateMemoDialog =
    remember(appState) {
      { appState.showCreateMemoDialog = true }
    }
  val onOpenTodayEditor =
    remember(onHabitsAction, todayDay, todayConfig) {
      { if (todayDay != null) onHabitsAction(HabitsAction.OpenEditor(todayDay, todayConfig)) }
    }
  val onRangeChange =
    remember(onHabitsAction, appState) {
      { range: ActivityRange ->
        onHabitsAction(HabitsAction.ClearSelection)
        updateTimeRangeState(appState, range)
      }
    }
  val onTabChange =
    remember(onHabitsAction, appState, saveTimeRangeTab) {
      { newTab: TimeRangeTab ->
        onHabitsAction(HabitsAction.ClearSelection)
        when (appState.selectedScreen) {
          MemosScreen.HABITS -> {
            adjustDateForTabChange(appState, appState.habitsTimeRangeTab, newTab)
            appState.habitsTimeRangeTab = newTab
            saveTimeRangeTab(TimeRangeScreen.HABITS, newTab)
          }
          MemosScreen.STATS -> {
            adjustDateForTabChange(appState, appState.postsTimeRangeTab, newTab)
            appState.postsTimeRangeTab = newTab
            saveTimeRangeTab(TimeRangeScreen.POSTS, newTab)
          }
          MemosScreen.FEED -> {}
        }
      }
    }

  Scaffold(
    floatingActionButton = {
      when (memosFabModeForScreen(appState.selectedScreen)) {
        MemosFabMode.MEMO -> {
          FloatingActionButton(
            onClick = onShowCreateMemoDialog,
          ) {
            Icon(
              imageVector = Icons.Filled.Edit,
              contentDescription = stringResource(Res.string.action_create_memo),
            )
          }
        }
        MemosFabMode.HABITS -> {
          if (todayConfig.isNotEmpty() && todayDay != null) {
            FloatingActionButton(
              onClick = onOpenTodayEditor,
            ) {
              Icon(
                imageVector = Icons.Filled.AddTask,
                contentDescription = stringResource(Res.string.action_track_today),
              )
            }
          }
        }
      }
    },
    bottomBar = {
      MemosBottomNavigation(appState, onClearSelection)
    },
  ) { padding ->
    val selectedTab =
      when (appState.selectedScreen) {
        MemosScreen.HABITS -> appState.habitsTimeRangeTab
        MemosScreen.STATS -> appState.postsTimeRangeTab
        MemosScreen.FEED -> appState.habitsTimeRangeTab
      }
    val currentRange = currentRangeForTab(selectedTab, today)
    val activityRange = activityRangeForState(appState)
    val earliestDate = remember(memosState.memos) { earliestMemoDate(memosState.memos, timeZone) }
    val minRange = minRangeForTab(selectedTab, earliestDate)
    Column(
      modifier =
        Modifier
          .padding(padding)
          .padding(Indent.m)
          .fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(Indent.s),
    ) {
      MemosHeader(memosState, appState, dispatchMemos, dispatchSettings, language, theme)
      memosState.errorMessage?.let { message ->
        Text(message, color = MaterialTheme.colorScheme.error)
      }
      if (appState.selectedScreen != MemosScreen.FEED) {
        val successRate =
          if (appState.selectedScreen == MemosScreen.HABITS) {
            val hasHabits = remember(habitsTimeline) { habitsTimeline.lastOrNull()?.habits?.isNotEmpty() == true }
            if (hasHabits) {
              rememberSuccessRate(memosState.memos, activityRange, calculateSuccessRate)
            } else {
              null
            }
          } else {
            null
          }
        TimeRangeControls(
          selectedTab = selectedTab,
          selectedRange = activityRange,
          currentRange = currentRange,
          minRange = minRange,
          successRate = successRate,
          onTabChange = onTabChange,
          onRangeChange = onRangeChange,
        )
      }
      SwipeableTabContent(
        memosState = memosState,
        appState = appState,
        currentRange = currentRange,
        minRange = minRange,
        habitsState = habitsState,
        onHabitsAction = onHabitsAction,
        calculateSuccessRate = calculateSuccessRate,
        dispatchMemos = dispatchMemos,
      )
    }
  }
}
