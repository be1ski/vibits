package space.be1ski.vibits.shared.di

import org.koin.core.Koin
import space.be1ski.vibits.shared.app.AppDependencies
import space.be1ski.vibits.shared.app.ModeSelectionUseCases
import space.be1ski.vibits.shared.app.VibitsAppDependencies

/**
 * Creates [AppDependencies] from Koin.
 * When migrating to Metro, replace this with Metro's dependency graph.
 */
fun Koin.createAppDependencies(): AppDependencies =
  AppDependencies(
    localeProvider = get(),
    loadPreferences = get(),
    fixInvalidOnlineMode = get(),
    modeSelection =
      ModeSelectionUseCases(
        validateCredentials = get(),
        saveCredentials = get(),
        saveAppMode = get(),
      ),
    vibitsApp =
      VibitsAppDependencies(
        loadPreferences = get(),
        saveTimeRangeTab = get(),
        loadAppDetails = get(),
        loadAppMode = get(),
        calculateSuccessRate = get(),
        memosRepository = get(),
        memosUseCases = get(),
        settingsUseCases = get(),
      ),
  )
