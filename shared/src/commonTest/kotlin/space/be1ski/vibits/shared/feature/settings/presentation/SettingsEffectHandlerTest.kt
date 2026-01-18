package space.be1ski.vibits.shared.feature.settings.presentation

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import space.be1ski.vibits.shared.feature.auth.domain.model.Credentials
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.settings.domain.model.AppLanguage
import space.be1ski.vibits.shared.feature.settings.domain.model.AppTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SettingsEffectHandlerTest {
  @Test
  fun `ValidateCredentials emits ValidationSucceeded on success`() =
    runTest {
      val handler = createHandler(validateResult = Result.success(Unit))

      val actions =
        handler(
          SettingsEffect.ValidateCredentials(
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
      val handler = createHandler(validateResult = Result.failure(Exception("Connection failed")))

      val actions =
        handler(
          SettingsEffect.ValidateCredentials(
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
      var switchedMode: AppMode? = null
      val handler =
        createHandler(
          switchAppMode = { switchedMode = it },
        )

      val actions = handler(SettingsEffect.SwitchMode(mode = AppMode.OFFLINE)).toList()

      assertEquals(listOf(SettingsAction.ModeSwitched), actions)
      assertEquals(AppMode.OFFLINE, switchedMode)
    }

  @Test
  fun `SaveCredentials saves to repository`() =
    runTest {
      var savedCredentials: Credentials? = null
      val handler =
        createHandler(
          saveCredentials = { savedCredentials = it },
        )

      handler(
        SettingsEffect.SaveCredentials(baseUrl = "https://saved.com", token = "saved-token"),
      ).toList()

      assertEquals("https://saved.com", savedCredentials?.baseUrl)
      assertEquals("saved-token", savedCredentials?.token)
    }

  @Test
  fun `ResetApp resets and emits ResetCompleted`() =
    runTest {
      var resetCalled = false
      val handler =
        createHandler(
          resetApp = { resetCalled = true },
        )

      val actions = handler(SettingsEffect.ResetApp).toList()

      assertEquals(listOf(SettingsAction.ResetCompleted), actions)
      assertTrue(resetCalled)
    }

  @Test
  fun `SaveLanguage calls saveLanguage function`() =
    runTest {
      var savedLanguage: AppLanguage? = null
      val handler =
        createHandler(
          saveLanguage = {
            savedLanguage = it
            false
          },
        )

      handler(SettingsEffect.SaveLanguage(language = AppLanguage.ENGLISH)).toList()

      assertEquals(AppLanguage.ENGLISH, savedLanguage)
    }

  @Test
  fun `SaveTheme calls saveTheme function`() =
    runTest {
      var savedTheme: AppTheme? = null
      val handler =
        createHandler(
          saveTheme = { savedTheme = it },
        )

      handler(SettingsEffect.SaveTheme(theme = AppTheme.DARK)).toList()

      assertEquals(AppTheme.DARK, savedTheme)
    }

  @Test
  fun `Notification effects return empty flow`() =
    runTest {
      val handler = createHandler()

      val notifyModeActions =
        handler(
          SettingsEffect.NotifyModeChanged(newMode = AppMode.OFFLINE),
        ).toList()
      val notifyResetActions = handler(SettingsEffect.NotifyResetCompleted).toList()
      val notifyCredentialsActions =
        handler(
          SettingsEffect.NotifyCredentialsSaved(baseUrl = "url", token = "token"),
        ).toList()
      val notifyLanguageActions =
        handler(
          SettingsEffect.NotifyLanguageChanged(language = AppLanguage.ENGLISH),
        ).toList()
      val notifyThemeActions =
        handler(
          SettingsEffect.NotifyThemeChanged(theme = AppTheme.DARK),
        ).toList()
      val notifyDialogActions = handler(SettingsEffect.NotifyDialogClosed).toList()

      assertTrue(notifyModeActions.isEmpty())
      assertTrue(notifyResetActions.isEmpty())
      assertTrue(notifyCredentialsActions.isEmpty())
      assertTrue(notifyLanguageActions.isEmpty())
      assertTrue(notifyThemeActions.isEmpty())
      assertTrue(notifyDialogActions.isEmpty())
    }

  private fun createHandler(
    validateResult: Result<Unit> = Result.success(Unit),
    switchAppMode: suspend (AppMode) -> Unit = {},
    saveCredentials: (Credentials) -> Unit = {},
    resetApp: suspend () -> Unit = {},
    saveLanguage: (AppLanguage) -> Boolean = { false },
    saveTheme: (AppTheme) -> Unit = {},
  ): SettingsEffectHandler {
    return SettingsEffectHandler(
      validateCredentials = { _, _ -> validateResult },
      switchAppMode = switchAppMode,
      saveCredentials = saveCredentials,
      resetApp = resetApp,
      saveLanguage = saveLanguage,
      saveTheme = saveTheme,
    )
  }
}
