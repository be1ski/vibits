package space.be1ski.vibits.shared.feature.mode.presentation

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import space.be1ski.vibits.shared.feature.auth.domain.usecase.SaveCredentialsUseCase
import space.be1ski.vibits.shared.feature.auth.domain.usecase.ValidateCredentialsUseCase
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.mode.domain.usecase.SaveAppModeUseCase
import space.be1ski.vibits.shared.test.FakeAppModeRepository
import space.be1ski.vibits.shared.test.FakeCredentialsRepository
import space.be1ski.vibits.shared.test.FakeMemosApiClient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ModeSelectionEffectHandlerTest {
  @Test
  fun `ValidateCredentials emits ValidationSucceeded on success`() =
    runTest {
      val handler = createHandler()

      val actions =
        handler(
          ModeSelectionEffect.ValidateCredentials(baseUrl = "https://test.com", token = "token"),
        ).toList()

      assertEquals(listOf(ModeSelectionAction.ValidationSucceeded), actions)
    }

  @Test
  fun `ValidateCredentials emits ValidationFailed on failure`() =
    runTest {
      val api = FakeMemosApiClient(listMemosResult = Result.failure(Exception("Connection failed")))
      val handler = createHandler(memosApi = api)

      val actions =
        handler(
          ModeSelectionEffect.ValidateCredentials(baseUrl = "https://test.com", token = "token"),
        ).toList()

      assertEquals(listOf(ModeSelectionAction.ValidationFailed), actions)
    }

  @Test
  fun `SaveCredentials calls saveCredentials function`() =
    runTest {
      val credentialsRepo = FakeCredentialsRepository()
      val handler = createHandler(credentialsRepository = credentialsRepo)

      handler(
        ModeSelectionEffect.SaveCredentials(baseUrl = "https://saved.com", token = "saved-token"),
      ).toList()

      assertEquals("https://saved.com", credentialsRepo.stored.baseUrl)
      assertEquals("saved-token", credentialsRepo.stored.token)
    }

  @Test
  fun `SaveMode calls saveAppMode function`() =
    runTest {
      val appModeRepo = FakeAppModeRepository()
      val handler = createHandler(appModeRepository = appModeRepo)

      handler(ModeSelectionEffect.SaveMode(mode = AppMode.OFFLINE)).toList()

      assertEquals(AppMode.OFFLINE, appModeRepo.storedMode)
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
    memosApi: FakeMemosApiClient = FakeMemosApiClient(),
    credentialsRepository: FakeCredentialsRepository = FakeCredentialsRepository(),
    appModeRepository: FakeAppModeRepository = FakeAppModeRepository(),
  ): ModeSelectionEffectHandler {
    return ModeSelectionEffectHandler(
      validateCredentials = ValidateCredentialsUseCase(memosApi),
      saveCredentials = SaveCredentialsUseCase(credentialsRepository),
      saveAppMode = SaveAppModeUseCase(appModeRepository),
    )
  }
}
