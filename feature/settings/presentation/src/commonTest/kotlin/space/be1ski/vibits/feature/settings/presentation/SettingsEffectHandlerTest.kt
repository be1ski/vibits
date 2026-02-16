package space.be1ski.vibits.feature.settings.presentation

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import space.be1ski.vibits.core.platform.locale.AppLanguage
import space.be1ski.vibits.core.platform.locale.LocaleProvider
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.auth.domain.test.FakeCredentialsRepository
import space.be1ski.vibits.feature.auth.domain.usecase.SaveCredentialsUseCase
import space.be1ski.vibits.feature.memos.domain.test.FakeMemoStorageManager
import space.be1ski.vibits.feature.mode.domain.test.FakeAppModeRepository
import space.be1ski.vibits.feature.mode.domain.usecase.ResetAppUseCase
import space.be1ski.vibits.feature.mode.domain.usecase.ResetAppWithMemosUseCase
import space.be1ski.vibits.feature.mode.domain.usecase.SwitchAppModeUseCase
import space.be1ski.vibits.feature.onboarding.domain.test.FakeOnboardingStore
import space.be1ski.vibits.feature.settings.domain.model.AppTheme
import space.be1ski.vibits.feature.settings.domain.test.FakePreferencesRepository
import space.be1ski.vibits.feature.settings.domain.usecase.SaveLanguageUseCase
import space.be1ski.vibits.feature.settings.domain.usecase.SaveSyncDebounceUseCase
import space.be1ski.vibits.feature.settings.domain.usecase.SaveThemeUseCase
import space.be1ski.vibits.feature.settings.presentation.action.SettingsAction
import space.be1ski.vibits.feature.settings.presentation.effect.SettingsCredentialsEffectHandler
import space.be1ski.vibits.feature.settings.presentation.effect.SettingsEffect
import space.be1ski.vibits.feature.settings.presentation.effect.SettingsEffectHandler
import space.be1ski.vibits.feature.settings.presentation.effect.SettingsModeEffectHandler
import space.be1ski.vibits.feature.settings.presentation.effect.SettingsPreferencesEffectHandler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class SettingsEffectHandlerTest {
  @Test
  fun `when ValidateCredentials succeeds then emits ValidationSucceeded`() =
    runTest {
      val handler = createHandler()

      val actions =
        handler(
          SettingsEffect.Command.ValidateCredentials(
            baseUrl = "https://test.com",
            token = "token",
            targetMode = AppMode.ONLINE,
          ),
        ).toList()

      assertEquals(listOf(SettingsAction.Validation.ValidationSucceeded), actions)
    }

  @Test
  fun `when ValidateCredentials fails then emits ValidationFailed`() =
    runTest {
      val handler = createHandler(connectionResult = Result.failure(Exception("Connection failed")))

      val actions =
        handler(
          SettingsEffect.Command.ValidateCredentials(
            baseUrl = "https://test.com",
            token = "token",
            targetMode = AppMode.ONLINE,
          ),
        ).toList()

      assertEquals(1, actions.size)
      assertTrue(actions[0] is SettingsAction.Validation.ValidationFailed)
    }

  @Test
  fun `when SwitchMode to different mode then saves and emits ModeSwitched`() =
    runTest {
      val appModeRepo = FakeAppModeRepository(initial = AppMode.ONLINE)
      val handler = createHandler(appModeRepository = appModeRepo)

      val actions = handler(SettingsEffect.Command.SwitchMode(mode = AppMode.OFFLINE)).toList()

      assertEquals(listOf(SettingsAction.Input.ModeSwitched), actions)
      assertEquals(AppMode.OFFLINE, appModeRepo.storedMode)
    }

  @Test
  fun `when SwitchMode to same mode then does not emit ModeSwitched`() =
    runTest {
      val appModeRepo = FakeAppModeRepository(initial = AppMode.ONLINE)
      val handler = createHandler(appModeRepository = appModeRepo)

      val actions = handler(SettingsEffect.Command.SwitchMode(mode = AppMode.ONLINE)).toList()

      assertTrue(actions.isEmpty())
    }

  @Test
  fun `when SaveCredentials then saves to repository`() =
    runTest {
      val credentialsRepo = FakeCredentialsRepository()
      val handler = createHandler(credentialsRepository = credentialsRepo)

      handler(
        SettingsEffect.Command.SaveCredentials(baseUrl = "https://saved.com", token = "saved-token"),
      ).toList()

      assertEquals("https://saved.com", credentialsRepo.stored.baseUrl)
      assertEquals("saved-token", credentialsRepo.stored.token)
    }

  @Test
  fun `when ResetApp then resets and emits ResetCompleted`() =
    runTest {
      val appModeRepo = FakeAppModeRepository(initial = AppMode.ONLINE)
      val handler = createHandler(appModeRepository = appModeRepo)

      val actions = handler(SettingsEffect.Command.ResetApp).toList()

      assertEquals(listOf(SettingsAction.Reset.ResetCompleted), actions)
      assertEquals(AppMode.NOT_SELECTED, appModeRepo.storedMode)
    }

  @Test
  fun `when ResetAppWithMemos then resets and clears memos and emits ResetCompleted`() =
    runTest {
      val appModeRepo = FakeAppModeRepository(initial = AppMode.ONLINE)
      val memoStorageManager = FakeMemoStorageManager()
      val handler = createHandler(appModeRepository = appModeRepo, memoStorageManager = memoStorageManager)

      val actions = handler(SettingsEffect.Command.ResetAppWithMemos).toList()

      assertEquals(listOf(SettingsAction.Reset.ResetCompleted), actions)
      assertEquals(AppMode.NOT_SELECTED, appModeRepo.storedMode)
      assertEquals(1, memoStorageManager.clearAllCalls)
    }

  @Test
  fun `when SaveLanguage then saves language to repository`() =
    runTest {
      val prefsRepo = FakePreferencesRepository()
      val handler = createHandler(preferencesRepository = prefsRepo)

      handler(SettingsEffect.Command.SaveLanguage(language = AppLanguage.ENGLISH)).toList()

      assertEquals(AppLanguage.ENGLISH, prefsRepo.stored.language)
    }

  @Test
  fun `when SaveTheme then saves theme to repository`() =
    runTest {
      val prefsRepo = FakePreferencesRepository()
      val handler = createHandler(preferencesRepository = prefsRepo)

      handler(SettingsEffect.Command.SaveTheme(theme = AppTheme.DARK)).toList()

      assertEquals(AppTheme.DARK, prefsRepo.stored.theme)
    }

  @Test
  fun `when SaveSyncDebounce then saves debounce to repository`() =
    runTest {
      val prefsRepo = FakePreferencesRepository()
      val handler = createHandler(preferencesRepository = prefsRepo)

      handler(SettingsEffect.Command.SaveSyncDebounce(seconds = 60)).toList()

      assertEquals(60.seconds, prefsRepo.stored.memosAutoSyncDebounceDuration)
    }

  private fun createHandler(
    connectionResult: Result<Unit> = Result.success(Unit),
    appModeRepository: FakeAppModeRepository = FakeAppModeRepository(),
    credentialsRepository: FakeCredentialsRepository = FakeCredentialsRepository(),
    preferencesRepository: FakePreferencesRepository = FakePreferencesRepository(),
    memoStorageManager: FakeMemoStorageManager = FakeMemoStorageManager(),
  ): SettingsEffectHandler {
    val resetApp =
      ResetAppUseCase(
        appModeRepository,
        credentialsRepository,
        preferencesRepository,
        FakeOnboardingStore(),
      )
    return SettingsEffectHandler(
      credentialsHandler =
        SettingsCredentialsEffectHandler(
          connectionTester = { _, _ -> connectionResult },
          saveCredentials = SaveCredentialsUseCase(credentialsRepository),
        ),
      modeHandler =
        SettingsModeEffectHandler(
          switchAppMode = SwitchAppModeUseCase(appModeRepository),
          resetApp = resetApp,
          resetAppWithMemos = ResetAppWithMemosUseCase(resetApp, memoStorageManager),
        ),
      preferencesHandler =
        SettingsPreferencesEffectHandler(
          saveLanguage = SaveLanguageUseCase(preferencesRepository, LocaleProvider()),
          saveTheme = SaveThemeUseCase(preferencesRepository),
          saveSyncDebounce = SaveSyncDebounceUseCase(preferencesRepository),
        ),
    )
  }
}
