package space.be1ski.vibits.shared.app.di
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import space.be1ski.vibits.shared.app.domain.usecase.LoadAppDetailsUseCase
import space.be1ski.vibits.shared.app.presentation.AppFeatures
import space.be1ski.vibits.shared.app.presentation.action.AppAction
import space.be1ski.vibits.shared.app.presentation.effect.AppEffect
import space.be1ski.vibits.shared.app.presentation.reducer.appReducer
import space.be1ski.vibits.shared.core.platform.date.currentLocalDate
import space.be1ski.vibits.shared.feature.habits.di.HabitsDependencies
import space.be1ski.vibits.shared.feature.habits.di.createHabitsFeature
import space.be1ski.vibits.shared.feature.memos.di.MemosDependencies
import space.be1ski.vibits.shared.feature.memos.di.createMemosFeature
import space.be1ski.vibits.shared.feature.memos.presentation.action.MemosAction
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.mode.domain.usecase.LoadAppModeUseCase
import space.be1ski.vibits.shared.feature.settings.di.SettingsDependencies
import space.be1ski.vibits.shared.feature.settings.di.createSettingsFeature
import space.be1ski.vibits.shared.feature.settings.domain.usecase.LoadPreferencesUseCase
import space.be1ski.vibits.shared.feature.settings.domain.usecase.SaveTimeRangeTabUseCase

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
        onRefresh = { memosFeature.send(MemosAction.Loading.LoadCachedMemos) },
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
