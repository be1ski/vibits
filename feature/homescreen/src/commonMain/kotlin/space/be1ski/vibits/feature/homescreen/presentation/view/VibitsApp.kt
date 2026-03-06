package space.be1ski.vibits.feature.homescreen.presentation.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import space.be1ski.vibits.core.platform.date.currentLocalDate
import space.be1ski.vibits.core.ui.celebration.CelebrationAnimation
import space.be1ski.vibits.core.ui.celebration.CelebrationOverlay
import space.be1ski.vibits.core.ui.date.rememberDateFormatter
import space.be1ski.vibits.core.ui.theme.LocalWideLayout
import space.be1ski.vibits.core.utils.logging.LogEntry
import space.be1ski.vibits.feature.changelog.domain.model.ChangelogEntry
import space.be1ski.vibits.feature.changelog.domain.model.UpdateAvailability
import space.be1ski.vibits.feature.habits.presentation.view.components.rememberHabitsConfigTimeline
import space.be1ski.vibits.feature.homescreen.presentation.AppFeatures
import space.be1ski.vibits.feature.settings.presentation.view.SettingsDialog

@Suppress("LongMethod")
@Composable
internal fun VibitsApp(
  features: AppFeatures,
  justFinishedOnboarding: Boolean = false,
  config: AppContentConfig,
  callbacks: AppContentCallbacks = AppContentCallbacks({}, {}, {}),
  testLogs: List<LogEntry>? = null,
) {
  val appState by features.app.state.collectAsState()
  val memosState by features.memos.state.collectAsState()
  val habitsState by features.habits.state.collectAsState()
  val settingsState by features.settings.state.collectAsState()

  val timeZone = remember { TimeZone.currentSystemDefault() }
  val today = currentLocalDate()
  val habitsTimeline = rememberHabitsConfigTimeline(memosState.memos)
  val todayHabits = rememberTodayHabits(habitsTimeline, memosState.memos, timeZone, today)

  val coroutineScope = rememberCoroutineScope()
  var celebrationAnimation by remember { mutableStateOf<CelebrationAnimation?>(null) }
  val allJustCompleted = rememberAllHabitsJustCompleted(todayHabits, justFinishedOnboarding)
  LaunchedEffect(allJustCompleted) {
    if (allJustCompleted) celebrationAnimation = CelebrationAnimation.Confetti
  }

  var changelogEntries by remember { mutableStateOf<List<ChangelogEntry>?>(null) }
  var updateAvailability by remember { mutableStateOf<UpdateAvailability?>(null) }
  var upgradeState by remember { mutableStateOf(UpgradeState.IDLE) }

  LaunchedEffect(Unit) {
    val entries = config.getChangelog(config.currentVersion)
    if (entries.isNotEmpty()) {
      changelogEntries = entries
    }
  }

  LaunchedEffect(Unit) {
    val checkForUpdate = config.checkForUpdate
    if (checkForUpdate != null) {
      updateAvailability = checkForUpdate(config.currentVersion)
    }
  }

  FeatureCoordinator(
    features = features,
    appState = appState,
    memosState = memosState,
    settingsState = settingsState,
    currentLanguage = config.appLanguage,
    currentTheme = config.appTheme,
    callbacks = callbacks,
  )

  val wideLayout = LocalWideLayout.current
  if (wideLayout) {
    VibitsDesktopShell(
      features = features,
      appState = appState,
      memosState = memosState,
      habitsState = habitsState,
      settingsState = settingsState,
      currentLanguage = config.appLanguage,
      currentTheme = config.appTheme,
      syncDebounceSeconds = settingsState.selectedSyncDebounceSeconds,
      exportService = config.exportService,
      testLogs = testLogs,
      updateAvailability = updateAvailability,
      upgradeState = upgradeState,
      onUpgrade = {
        val appUpdater = config.appUpdater
        if (appUpdater != null) {
          upgradeState = UpgradeState.UPGRADING
          coroutineScope.launch {
            upgradeState = if (appUpdater.upgrade()) UpgradeState.DONE else UpgradeState.FAILED
          }
        }
      },
      onRestart = { config.appUpdater?.restart() },
    )
  } else {
    VibitsAppScaffold(
      features = features,
      appState = appState,
      memosState = memosState,
      habitsState = habitsState,
      currentLanguage = config.appLanguage,
      currentTheme = config.appTheme,
      syncDebounceSeconds = settingsState.selectedSyncDebounceSeconds,
    )
  }

  val dateFormatter = rememberDateFormatter()

  if (!wideLayout) {
    SettingsDialog(state = settingsState, dispatch = features.settings::send, exportService = config.exportService, testLogs = testLogs)
  }
  MemoCreateDialog(state = memosState, dispatch = features.memos::send)
  MemoEditDialog(state = memosState, dispatch = features.memos::send)
  HabitsDialogs(appState = appState, habitsState = habitsState, dateFormatter = dateFormatter, dispatch = features.habits::send)

  changelogEntries?.let { entries ->
    ChangelogDialog(
      entries = entries,
      onDismiss = { changelogEntries = null },
    )
  }

  CelebrationOverlay(
    animation = celebrationAnimation,
    onFinished = { celebrationAnimation = null },
  )
}
