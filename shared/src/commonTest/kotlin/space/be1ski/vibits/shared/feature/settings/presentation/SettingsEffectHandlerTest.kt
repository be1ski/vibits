package space.be1ski.vibits.shared.feature.settings.presentation

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import space.be1ski.vibits.shared.core.platform.locale.LocaleProvider
import space.be1ski.vibits.shared.feature.auth.domain.usecase.SaveCredentialsUseCase
import space.be1ski.vibits.shared.feature.memos.data.ConnectionTester
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.mode.domain.usecase.ResetAppUseCase
import space.be1ski.vibits.shared.feature.mode.domain.usecase.SwitchAppModeUseCase
import space.be1ski.vibits.shared.feature.settings.domain.model.AppLanguage
import space.be1ski.vibits.shared.feature.settings.domain.model.AppTheme
import space.be1ski.vibits.shared.feature.settings.domain.usecase.SaveLanguageUseCase
import space.be1ski.vibits.shared.feature.settings.domain.usecase.SaveThemeUseCase
import space.be1ski.vibits.shared.test.FakeAppModeRepository
import space.be1ski.vibits.shared.test.FakeCredentialsRepository
import space.be1ski.vibits.shared.test.FakePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SettingsEffectHandlerTest {
  @Test
  fun `ValidateCredentials emits ValidationSucceeded on success`() =
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

      assertEquals(listOf(SettingsAction.ValidationSucceeded), actions)
    }

  @Test
  fun `ValidateCredentials emits ValidationFailed on failure`() =
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
      assertTrue(actions[0] is SettingsAction.ValidationFailed)
    }

  @Test
  fun `SwitchMode saves mode and emits ModeSwitched`() =
    runTest {
      val appModeRepo = FakeAppModeRepository(initial = AppMode.ONLINE)
      val handler = createHandler(appModeRepository = appModeRepo)

      val actions = handler(SettingsEffect.Command.SwitchMode(mode = AppMode.OFFLINE)).toList()

      assertEquals(listOf(SettingsAction.ModeSwitched), actions)
      assertEquals(AppMode.OFFLINE, appModeRepo.storedMode)
    }

  @Test
  fun `SaveCredentials saves to repository`() =
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
  fun `ResetApp resets and emits ResetCompleted`() =
    runTest {
      val appModeRepo = FakeAppModeRepository(initial = AppMode.ONLINE)
      val handler = createHandler(appModeRepository = appModeRepo)

      val actions = handler(SettingsEffect.Command.ResetApp).toList()

      assertEquals(listOf(SettingsAction.ResetCompleted), actions)
      assertEquals(AppMode.NOT_SELECTED, appModeRepo.storedMode)
    }

  @Test
  fun `SaveLanguage calls saveLanguage function`() =
    runTest {
      val prefsRepo = FakePreferencesRepository()
      val handler = createHandler(preferencesRepository = prefsRepo)

      handler(SettingsEffect.Command.SaveLanguage(language = AppLanguage.ENGLISH)).toList()

      assertEquals(AppLanguage.ENGLISH, prefsRepo.stored.language)
    }

  @Test
  fun `SaveTheme calls saveTheme function`() =
    runTest {
      val prefsRepo = FakePreferencesRepository()
      val handler = createHandler(preferencesRepository = prefsRepo)

      handler(SettingsEffect.Command.SaveTheme(theme = AppTheme.DARK)).toList()

      assertEquals(AppTheme.DARK, prefsRepo.stored.theme)
    }

  @Test
  fun `Notification effects return empty flow`() =
    runTest {
      val handler = createHandler()

      val notifyModeActions =
        handler(
          SettingsEffect.Notification.ModeChanged(newMode = AppMode.OFFLINE),
        ).toList()
      val notifyResetActions = handler(SettingsEffect.Notification.ResetCompleted).toList()
      val notifyCredentialsActions =
        handler(
          SettingsEffect.Notification.CredentialsSaved(baseUrl = "url", token = "token"),
        ).toList()
      val notifyLanguageActions =
        handler(
          SettingsEffect.Notification.LanguageChanged(language = AppLanguage.ENGLISH),
        ).toList()
      val notifyThemeActions =
        handler(
          SettingsEffect.Notification.ThemeChanged(theme = AppTheme.DARK),
        ).toList()
      val notifyDialogActions = handler(SettingsEffect.Notification.DialogClosed).toList()

      assertTrue(notifyModeActions.isEmpty())
      assertTrue(notifyResetActions.isEmpty())
      assertTrue(notifyCredentialsActions.isEmpty())
      assertTrue(notifyLanguageActions.isEmpty())
      assertTrue(notifyThemeActions.isEmpty())
      assertTrue(notifyDialogActions.isEmpty())
    }

  private fun createHandler(
    connectionResult: Result<Unit> = Result.success(Unit),
    appModeRepository: FakeAppModeRepository = FakeAppModeRepository(),
    credentialsRepository: FakeCredentialsRepository = FakeCredentialsRepository(),
    preferencesRepository: FakePreferencesRepository = FakePreferencesRepository(),
  ): SettingsEffectHandler {
    return SettingsEffectHandler(
      connectionTester = ConnectionTester { _, _ -> connectionResult },
      switchAppMode = SwitchAppModeUseCase(appModeRepository),
      saveCredentials = SaveCredentialsUseCase(credentialsRepository),
      resetApp = ResetAppUseCase(appModeRepository, credentialsRepository, preferencesRepository),
      saveLanguage = SaveLanguageUseCase(preferencesRepository, LocaleProvider()),
      saveTheme = SaveThemeUseCase(preferencesRepository),
    )
  }
}
