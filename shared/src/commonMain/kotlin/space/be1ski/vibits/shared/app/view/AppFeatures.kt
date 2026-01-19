package space.be1ski.vibits.shared.app.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import space.be1ski.vibits.shared.app.di.AppDependencies
import space.be1ski.vibits.shared.app.domain.model.AppDetails
import space.be1ski.vibits.shared.app.view.model.VibitsAppUiState
import space.be1ski.vibits.shared.core.elm.Feature
import space.be1ski.vibits.shared.core.platform.date.currentLocalDate
import space.be1ski.vibits.shared.feature.habits.di.createHabitsFeature
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsAction
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsEffect
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsState
import space.be1ski.vibits.shared.feature.habits.view.components.ActivityWeekDataCache
import space.be1ski.vibits.shared.feature.memos.di.createMemosFeature
import space.be1ski.vibits.shared.feature.memos.presentation.MemosAction
import space.be1ski.vibits.shared.feature.memos.presentation.MemosEffect
import space.be1ski.vibits.shared.feature.memos.presentation.MemosState
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.settings.di.createSettingsFeature
import space.be1ski.vibits.shared.feature.settings.presentation.SettingsAction
import space.be1ski.vibits.shared.feature.settings.presentation.SettingsEffect
import space.be1ski.vibits.shared.feature.settings.presentation.SettingsState

internal class AppFeatures(
  val memos: Feature<MemosAction, MemosState, MemosEffect>,
  val habits: Feature<HabitsAction, HabitsState, HabitsEffect>,
  val settings: Feature<SettingsAction, SettingsState, SettingsEffect>,
  val appState: VibitsAppUiState,
  val appDetails: AppDetails,
  val cache: ActivityWeekDataCache,
)

@Composable
internal fun rememberAppFeatures(dependencies: AppDependencies): AppFeatures {
  val initialPrefs = remember { dependencies.loadPreferences() }
  val initialMode = remember { dependencies.loadAppMode() }
  val appDetails = remember { dependencies.loadAppDetails() }
  val cache = remember { ActivityWeekDataCache() }

  val appState =
    remember {
      VibitsAppUiState(
        currentDate = currentLocalDate(),
        initialHabitsTimeRangeTab = initialPrefs.habitsTimeRangeTab,
        initialPostsTimeRangeTab = initialPrefs.postsTimeRangeTab,
      ).also { it.appMode = initialMode }
    }

  val memosFeature =
    remember {
      val skipCredentials = initialMode == AppMode.OFFLINE || initialMode == AppMode.DEMO
      createMemosFeature(dependencies.memosDependencies, isOfflineMode = skipCredentials)
    }

  val dispatchMemos: (MemosAction) -> Unit = memosFeature::send

  val habitsFeature =
    remember {
      createHabitsFeature(
        dependencies = dependencies.habitsDependencies,
        onRefresh = { dispatchMemos(MemosAction.LoadMemos) },
      )
    }

  val settingsFeature =
    remember {
      createSettingsFeature(
        dependencies = dependencies.settingsDependencies,
        initialMode = initialMode,
        appDetails = appDetails,
      )
    }

  val scope = rememberCoroutineScope()
  LaunchedEffect(Unit) {
    memosFeature.launchIn(scope)
    habitsFeature.launchIn(scope)
    settingsFeature.launchIn(scope)
  }

  return remember(memosFeature, habitsFeature, settingsFeature, appState, appDetails, cache) {
    AppFeatures(
      memos = memosFeature,
      habits = habitsFeature,
      settings = settingsFeature,
      appState = appState,
      appDetails = appDetails,
      cache = cache,
    )
  }
}
