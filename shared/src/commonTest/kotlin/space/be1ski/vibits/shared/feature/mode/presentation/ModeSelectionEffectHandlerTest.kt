package space.be1ski.vibits.shared.feature.mode.presentation

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import space.be1ski.vibits.shared.feature.auth.domain.usecase.SaveCredentialsUseCase
import space.be1ski.vibits.shared.feature.memos.data.ConnectionTester
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.mode.domain.usecase.SaveAppModeUseCase
import space.be1ski.vibits.shared.test.FakeAppModeRepository
import space.be1ski.vibits.shared.test.FakeCredentialsRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ModeSelectionEffectHandlerTest {
  @Test
  fun `when ValidateCredentials effect succeeds then emits ValidationSucceeded`() =
    runTest {
      val handler = createHandler()

      val actions =
        handler(
          ModeSelectionEffect.ValidateCredentials(baseUrl = "https://test.com", token = "token"),
        ).toList()

      assertEquals(listOf(ModeSelectionAction.ValidationSucceeded), actions)
    }

  @Test
  fun `when ValidateCredentials effect fails then emits ValidationFailed`() =
    runTest {
      val handler = createHandler(connectionResult = Result.failure(Exception("Connection failed")))

      val actions =
        handler(
          ModeSelectionEffect.ValidateCredentials(baseUrl = "https://test.com", token = "token"),
        ).toList()

      assertEquals(listOf(ModeSelectionAction.ValidationFailed), actions)
    }

  @Test
  fun `when SaveCredentials effect then saves to repository`() =
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
  fun `when SaveMode effect then saves mode to repository`() =
    runTest {
      val appModeRepo = FakeAppModeRepository()
      val handler = createHandler(appModeRepository = appModeRepo)

      handler(ModeSelectionEffect.SaveMode(mode = AppMode.OFFLINE)).toList()

      assertEquals(AppMode.OFFLINE, appModeRepo.storedMode)
    }

  @Test
  fun `when NotifyModeSelected effect then returns empty flow`() =
    runTest {
      val handler = createHandler()

      val actions =
        handler(
          ModeSelectionEffect.NotifyModeSelected(mode = AppMode.ONLINE),
        ).toList()

      assertTrue(actions.isEmpty())
    }

  private fun createHandler(
    connectionResult: Result<Unit> = Result.success(Unit),
    credentialsRepository: FakeCredentialsRepository = FakeCredentialsRepository(),
    appModeRepository: FakeAppModeRepository = FakeAppModeRepository(),
    localConfigProvider: space.be1ski.vibits.shared.core.platform.env.LocalConfigProvider =
      space.be1ski.vibits.shared.test
        .createFakeLocalConfigProvider(),
  ): ModeSelectionEffectHandler {
    return ModeSelectionEffectHandler(
      connectionTester = ConnectionTester { _, _ -> connectionResult },
      initializeCredentialsFromEnv =
        space.be1ski.vibits.shared.feature.auth.domain.usecase.InitializeCredentialsFromEnvUseCase(
          loadCredentials =
            space.be1ski.vibits.shared.feature.auth.domain.usecase
              .LoadCredentialsUseCase(credentialsRepository),
          saveCredentials = SaveCredentialsUseCase(credentialsRepository),
          localConfigProvider = localConfigProvider,
        ),
      loadCredentials =
        space.be1ski.vibits.shared.feature.auth.domain.usecase
          .LoadCredentialsUseCase(credentialsRepository),
      saveCredentials = SaveCredentialsUseCase(credentialsRepository),
      saveAppMode = SaveAppModeUseCase(appModeRepository),
    )
  }
}
