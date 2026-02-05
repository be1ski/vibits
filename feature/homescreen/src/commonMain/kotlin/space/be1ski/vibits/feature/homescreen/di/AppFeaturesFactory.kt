package space.be1ski.vibits.feature.homescreen.di
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import space.be1ski.vibits.core.platform.date.currentLocalDate
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.habits.di.HabitsDependencies
import space.be1ski.vibits.feature.habits.di.createHabitsFeature
import space.be1ski.vibits.feature.homescreen.domain.usecase.LoadAppDetailsUseCase
import space.be1ski.vibits.feature.homescreen.presentation.AppFeatures
import space.be1ski.vibits.feature.memos.di.MemosDependencies
import space.be1ski.vibits.feature.memos.di.createMemosFeature
import space.be1ski.vibits.feature.memos.presentation.action.MemosAction
import space.be1ski.vibits.feature.mode.domain.usecase.LoadAppModeUseCase
import space.be1ski.vibits.feature.settings.di.SettingsDependencies
import space.be1ski.vibits.feature.settings.di.createSettingsFeature
import space.be1ski.vibits.feature.settings.domain.usecase.LoadPreferencesUseCase
import space.be1ski.vibits.feature.settings.domain.usecase.SaveTimeRangeTabUseCase

/**
 * Factory for creating and launching all application TEA features.
 * Extracted from DI graph to keep graph clean of business logic.
 */
@Suppress("LongParameterList")
@Inject
class AppFeaturesFactory(
  private val loadPreferences: LoadPreferencesUseCase,
  private val loadAppMode: LoadAppModeUseCase,
  private val loadAppDetails: LoadAppDetailsUseCase,
  private val saveTimeRangeTab: SaveTimeRangeTabUseCase,
  private val memosDependencies: MemosDependencies,
  private val habitsDependencies: HabitsDependencies,
  private val settingsDependencies: SettingsDependencies,
) {
  fun create(scope: CoroutineScope): AppFeatures {
    val initialPrefs = loadPreferences()
    val initialMode = loadAppMode()
    val appDetails = loadAppDetails()

    val appFeature =
      createAppFeature(
        saveTimeRangeTab = saveTimeRangeTab,
        initialMode = initialMode,
        currentDate = currentLocalDate(),
        initialHabitsTab = initialPrefs.habitsTimeRangeTab,
        initialPostsTab = initialPrefs.postsTimeRangeTab,
      )

    val skipCredentials = initialMode == AppMode.OFFLINE || initialMode == AppMode.DEMO
    val memosFeature = createMemosFeature(memosDependencies, isOfflineMode = skipCredentials)

    val habitsFeature =
      createHabitsFeature(
        dependencies = habitsDependencies,
        onRefresh = {
          memosFeature.send(MemosAction.Loading.RefreshMemos)
          memosFeature.send(MemosAction.Sync.StartSync)
        },
      )

    val settingsFeature =
      createSettingsFeature(
        dependencies = settingsDependencies,
        initialMode = initialMode,
        appDetails = appDetails,
      )

    appFeature.launchIn(scope)
    memosFeature.launchIn(scope)
    habitsFeature.launchIn(scope)
    settingsFeature.launchIn(scope)

    return AppFeatures(
      app = appFeature,
      memos = memosFeature,
      habits = habitsFeature,
      settings = settingsFeature,
    )
  }
}
