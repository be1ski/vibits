package space.be1ski.vibits.feature.mode.presentation
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.auth.domain.usecase.SaveCredentialsUseCase
import space.be1ski.vibits.feature.homescreen.test.FakeAppModeRepository
import space.be1ski.vibits.feature.homescreen.test.FakeCredentialsRepository
import space.be1ski.vibits.feature.memos.domain.repository.ConnectionTester
import space.be1ski.vibits.feature.mode.domain.usecase.SaveAppModeUseCase
import space.be1ski.vibits.feature.mode.presentation.action.ModeSelectionAction
import space.be1ski.vibits.feature.mode.presentation.effect.ModeSelectionCredentialsEffectHandler
import space.be1ski.vibits.feature.mode.presentation.effect.ModeSelectionEffect.Command
import space.be1ski.vibits.feature.mode.presentation.effect.ModeSelectionEffectHandler
import space.be1ski.vibits.feature.mode.presentation.effect.ModeSelectionModeEffectHandler
import kotlin.test.Test
import kotlin.test.assertEquals

class ModeSelectionEffectHandlerTest {
  @Test
  fun `when ValidateCredentials effect succeeds then emits ValidationSucceeded`() =
    runTest {
      val handler = createHandler()

      val actions =
        handler(
          Command.ValidateCredentials(baseUrl = "https://test.com", token = "token"),
        ).toList()

      assertEquals(listOf(ModeSelectionAction.Validation.Succeeded), actions)
    }

  @Test
  fun `when ValidateCredentials effect fails then emits ValidationFailed`() =
    runTest {
      val handler = createHandler(connectionResult = Result.failure(Exception("Connection failed")))

      val actions =
        handler(
          Command.ValidateCredentials(baseUrl = "https://test.com", token = "token"),
        ).toList()

      assertEquals(listOf(ModeSelectionAction.Validation.Failed), actions)
    }

  @Test
  fun `when SaveCredentials effect then saves to repository`() =
    runTest {
      val credentialsRepo = FakeCredentialsRepository()
      val handler = createHandler(credentialsRepository = credentialsRepo)

      handler(
        Command.SaveCredentials(baseUrl = "https://saved.com", token = "saved-token"),
      ).toList()

      assertEquals("https://saved.com", credentialsRepo.stored.baseUrl)
      assertEquals("saved-token", credentialsRepo.stored.token)
    }

  @Test
  fun `when SaveMode effect then saves mode to repository`() =
    runTest {
      val appModeRepo = FakeAppModeRepository()
      val handler = createHandler(appModeRepository = appModeRepo)

      handler(Command.SaveMode(mode = AppMode.OFFLINE)).toList()

      assertEquals(AppMode.OFFLINE, appModeRepo.storedMode)
    }

  @Test
  fun `when InitializeFromLocalConfig with credentials then emits StoredCredentialsFound`() =
    runTest {
      val credentialsRepo = FakeCredentialsRepository()
      val configProvider =
        space.be1ski.vibits.feature.homescreen.test.createFakeLocalConfigProvider(
          config =
            mapOf(
              "memos.baseUrl" to "https://config.com",
              "memos.token" to "config-token",
            ),
        )
      val handler = createHandler(credentialsRepository = credentialsRepo, localConfigProvider = configProvider)

      val actions = handler(Command.InitializeFromLocalConfig).toList()

      assertEquals(listOf(ModeSelectionAction.StoredCredentials.Found), actions)
    }

  @Test
  fun `when InitializeFromLocalConfig without credentials then emits StoredCredentialsNotFound`() =
    runTest {
      val credentialsRepo = FakeCredentialsRepository()
      val configProvider =
        space.be1ski.vibits.feature.homescreen.test
          .createFakeLocalConfigProvider(config = emptyMap())
      val handler = createHandler(credentialsRepository = credentialsRepo, localConfigProvider = configProvider)

      val actions = handler(Command.InitializeFromLocalConfig).toList()

      assertEquals(listOf(ModeSelectionAction.StoredCredentials.NotFound), actions)
    }

  @Test
  fun `when CheckStoredCredentials with credentials then emits StoredCredentialsFound`() =
    runTest {
      val credentialsRepo =
        FakeCredentialsRepository(
          initial =
            space.be1ski.vibits.feature.auth.domain.model.Credentials(
              baseUrl = "https://existing.com",
              token = "existing-token",
            ),
        )
      val handler = createHandler(credentialsRepository = credentialsRepo)

      val actions = handler(Command.CheckStoredCredentials).toList()

      assertEquals(listOf(ModeSelectionAction.StoredCredentials.Found), actions)
    }

  @Test
  fun `when CheckStoredCredentials without credentials then emits StoredCredentialsNotFound`() =
    runTest {
      val credentialsRepo = FakeCredentialsRepository()
      val handler = createHandler(credentialsRepository = credentialsRepo)

      val actions = handler(Command.CheckStoredCredentials).toList()

      assertEquals(listOf(ModeSelectionAction.StoredCredentials.NotFound), actions)
    }

  @Test
  fun `when UseStoredCredentialsWithValidation succeeds then emits ValidationSucceeded`() =
    runTest {
      val credentialsRepo =
        FakeCredentialsRepository(
          initial =
            space.be1ski.vibits.feature.auth.domain.model.Credentials(
              baseUrl = "https://existing.com",
              token = "existing-token",
            ),
        )
      val handler = createHandler(credentialsRepository = credentialsRepo)

      val actions = handler(Command.UseStoredCredentialsWithValidation).toList()

      assertEquals(listOf(ModeSelectionAction.Validation.Succeeded), actions)
    }

  @Test
  fun `when UseStoredCredentialsWithValidation fails then emits ValidationFailed`() =
    runTest {
      val credentialsRepo =
        FakeCredentialsRepository(
          initial =
            space.be1ski.vibits.feature.auth.domain.model.Credentials(
              baseUrl = "https://existing.com",
              token = "existing-token",
            ),
        )
      val handler = createHandler(connectionResult = Result.failure(Exception("Failed")), credentialsRepository = credentialsRepo)

      val actions = handler(Command.UseStoredCredentialsWithValidation).toList()

      assertEquals(listOf(ModeSelectionAction.Validation.Failed), actions)
    }

  private fun createHandler(
    connectionResult: Result<Unit> = Result.success(Unit),
    credentialsRepository: FakeCredentialsRepository = FakeCredentialsRepository(),
    appModeRepository: FakeAppModeRepository = FakeAppModeRepository(),
    localConfigProvider: space.be1ski.vibits.core.platform.env.LocalConfigProvider =
      space.be1ski.vibits.feature.homescreen.test
        .createFakeLocalConfigProvider(),
  ): ModeSelectionEffectHandler {
    return ModeSelectionEffectHandler(
      credentialsHandler =
        ModeSelectionCredentialsEffectHandler(
          connectionTester = ConnectionTester { _, _ -> connectionResult },
          initializeCredentialsFromEnv =
            space.be1ski.vibits.feature.auth.domain.usecase.InitializeCredentialsFromEnvUseCase(
              loadCredentials =
                space.be1ski.vibits.feature.auth.domain.usecase
                  .LoadCredentialsUseCase(credentialsRepository),
              saveCredentials = SaveCredentialsUseCase(credentialsRepository),
              localConfigProvider = localConfigProvider,
            ),
          loadCredentials =
            space.be1ski.vibits.feature.auth.domain.usecase
              .LoadCredentialsUseCase(credentialsRepository),
          saveCredentials = SaveCredentialsUseCase(credentialsRepository),
        ),
      modeHandler =
        ModeSelectionModeEffectHandler(
          saveAppMode = SaveAppModeUseCase(appModeRepository),
        ),
    )
  }
}
