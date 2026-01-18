package space.be1ski.vibits.shared.feature.mode.domain.usecase

import space.be1ski.vibits.shared.feature.auth.domain.model.Credentials
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.test.FakeAppModeRepository
import space.be1ski.vibits.shared.test.FakeCredentialsRepository
import space.be1ski.vibits.shared.test.FakePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class LoadAppModeUseCaseTest {
  @Test
  fun `when invoke then returns mode from repository`() {
    val repository = FakeAppModeRepository(initial = AppMode.ONLINE)
    val useCase = LoadAppModeUseCase(repository)

    val result = useCase()

    assertEquals(AppMode.ONLINE, result)
  }

  @Test
  fun `when mode is NotSelected then returns NotSelected`() {
    val repository = FakeAppModeRepository(initial = AppMode.NOT_SELECTED)
    val useCase = LoadAppModeUseCase(repository)

    val result = useCase()

    assertEquals(AppMode.NOT_SELECTED, result)
  }
}

class SaveAppModeUseCaseTest {
  @Test
  fun `when invoke then saves mode to repository`() {
    val repository = FakeAppModeRepository()
    val useCase = SaveAppModeUseCase(repository)

    useCase(AppMode.OFFLINE)

    assertEquals(AppMode.OFFLINE, repository.storedMode)
    assertEquals(1, repository.saveCalls)
  }
}

class ResetAppUseCaseTest {
  private fun createUseCase(
    appModeRepository: FakeAppModeRepository = FakeAppModeRepository(initial = AppMode.ONLINE),
    credentialsRepository: FakeCredentialsRepository = FakeCredentialsRepository(),
    preferencesRepository: FakePreferencesRepository = FakePreferencesRepository(),
  ) = ResetAppUseCase(appModeRepository, credentialsRepository, preferencesRepository)

  @Test
  fun `when invoke then clears credentials`() {
    val credentialsRepository =
      FakeCredentialsRepository(
        initial = Credentials(baseUrl = "https://example.com", token = "token"),
      )
    val useCase = createUseCase(credentialsRepository = credentialsRepository)

    useCase()

    assertEquals(Credentials(baseUrl = "", token = ""), credentialsRepository.stored)
  }

  @Test
  fun `when invoke then sets mode to NotSelected`() {
    val appModeRepository = FakeAppModeRepository(initial = AppMode.ONLINE)
    val useCase = createUseCase(appModeRepository = appModeRepository)

    useCase()

    assertEquals(AppMode.NOT_SELECTED, appModeRepository.storedMode)
  }

  @Test
  fun `when invoke then resets preferences`() {
    val preferencesRepository = FakePreferencesRepository()
    val useCase = createUseCase(preferencesRepository = preferencesRepository)

    useCase()

    assertEquals(1, preferencesRepository.saveCalls)
  }
}
