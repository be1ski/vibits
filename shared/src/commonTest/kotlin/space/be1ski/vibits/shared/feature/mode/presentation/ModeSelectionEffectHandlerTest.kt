package space.be1ski.vibits.shared.feature.mode.presentation

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import space.be1ski.vibits.shared.feature.auth.domain.model.Credentials
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ModeSelectionEffectHandlerTest {
  @Test
  fun `ValidateCredentials emits ValidationSucceeded on success`() =
    runTest {
      val handler = createHandler(validateResult = Result.success(Unit))

      val actions =
        handler(
          ModeSelectionEffect.ValidateCredentials(baseUrl = "https://test.com", token = "token"),
        ).toList()

      assertEquals(listOf(ModeSelectionAction.ValidationSucceeded), actions)
    }

  @Test
  fun `ValidateCredentials emits ValidationFailed on failure`() =
    runTest {
      val handler = createHandler(validateResult = Result.failure(Exception("Connection failed")))

      val actions =
        handler(
          ModeSelectionEffect.ValidateCredentials(baseUrl = "https://test.com", token = "token"),
        ).toList()

      assertEquals(listOf(ModeSelectionAction.ValidationFailed), actions)
    }

  @Test
  fun `SaveCredentials calls saveCredentials function`() =
    runTest {
      var savedCredentials: Credentials? = null
      val handler =
        createHandler(
          saveCredentials = { savedCredentials = it },
        )

      handler(
        ModeSelectionEffect.SaveCredentials(baseUrl = "https://saved.com", token = "saved-token"),
      ).toList()

      assertEquals("https://saved.com", savedCredentials?.baseUrl)
      assertEquals("saved-token", savedCredentials?.token)
    }

  @Test
  fun `SaveMode calls saveAppMode function`() =
    runTest {
      var savedMode: AppMode? = null
      val handler =
        createHandler(
          saveAppMode = { savedMode = it },
        )

      handler(ModeSelectionEffect.SaveMode(mode = AppMode.OFFLINE)).toList()

      assertEquals(AppMode.OFFLINE, savedMode)
    }

  @Test
  fun `NotifyModeSelected returns empty flow`() =
    runTest {
      val handler = createHandler()

      val actions =
        handler(
          ModeSelectionEffect.NotifyModeSelected(mode = AppMode.ONLINE),
        ).toList()

      assertTrue(actions.isEmpty())
    }

  private fun createHandler(
    validateResult: Result<Unit> = Result.success(Unit),
    saveCredentials: (Credentials) -> Unit = {},
    saveAppMode: (AppMode) -> Unit = {},
  ): ModeSelectionEffectHandler {
    return ModeSelectionEffectHandler(
      validateCredentials = { _, _ -> validateResult },
      saveCredentials = saveCredentials,
      saveAppMode = saveAppMode,
    )
  }
}
