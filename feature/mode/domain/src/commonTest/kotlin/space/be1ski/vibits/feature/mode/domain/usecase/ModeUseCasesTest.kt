package space.be1ski.vibits.feature.mode.domain.usecase

import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.auth.domain.model.Credentials
import space.be1ski.vibits.feature.auth.domain.usecase.LoadCredentialsUseCase
import space.be1ski.vibits.feature.main.test.FakeAppModeRepository
import space.be1ski.vibits.feature.main.test.FakeCredentialsRepository
import space.be1ski.vibits.feature.main.test.FakeOnboardingStore
import space.be1ski.vibits.feature.main.test.FakePreferencesRepository
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
    onboardingStore: FakeOnboardingStore = FakeOnboardingStore(),
  ) = ResetAppUseCase(appModeRepository, credentialsRepository, preferencesRepository, onboardingStore)

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

class SwitchAppModeUseCaseTest {
  @Test
  fun `when mode is different then saves new mode`() {
    val repository = FakeAppModeRepository(initial = AppMode.NOT_SELECTED)
    val useCase = SwitchAppModeUseCase(repository)

    useCase(AppMode.ONLINE)

    assertEquals(AppMode.ONLINE, repository.storedMode)
    assertEquals(1, repository.saveCalls)
  }

  @Test
  fun `when mode is same then does not save`() {
    val repository = FakeAppModeRepository(initial = AppMode.OFFLINE)
    val useCase = SwitchAppModeUseCase(repository)

    useCase(AppMode.OFFLINE)

    assertEquals(AppMode.OFFLINE, repository.storedMode)
    assertEquals(0, repository.saveCalls)
  }

  @Test
  fun `when switching from Demo to Online then saves`() {
    val repository = FakeAppModeRepository(initial = AppMode.DEMO)
    val useCase = SwitchAppModeUseCase(repository)

    useCase(AppMode.ONLINE)

    assertEquals(AppMode.ONLINE, repository.storedMode)
    assertEquals(1, repository.saveCalls)
  }

  @Test
  fun `when switching from Online to Offline then saves`() {
    val repository = FakeAppModeRepository(initial = AppMode.ONLINE)
    val useCase = SwitchAppModeUseCase(repository)

    useCase(AppMode.OFFLINE)

    assertEquals(AppMode.OFFLINE, repository.storedMode)
    assertEquals(1, repository.saveCalls)
  }
}

class FixInvalidOnlineModeUseCaseTest {
  private fun createUseCase(
    initialMode: AppMode = AppMode.NOT_SELECTED,
    initialCredentials: Credentials = Credentials(baseUrl = "", token = ""),
  ): Pair<FixInvalidOnlineModeUseCase, FakeAppModeRepository> {
    val appModeRepository = FakeAppModeRepository(initial = initialMode)
    val credentialsRepository = FakeCredentialsRepository(initial = initialCredentials)
    val useCase =
      FixInvalidOnlineModeUseCase(
        loadAppModeUseCase = LoadAppModeUseCase(appModeRepository),
        saveAppModeUseCase = SaveAppModeUseCase(appModeRepository),
        loadCredentialsUseCase = LoadCredentialsUseCase(credentialsRepository),
      )
    return useCase to appModeRepository
  }

  @Test
  fun `when mode is NotSelected then returns NotSelected without changes`() {
    val (useCase, repository) = createUseCase(initialMode = AppMode.NOT_SELECTED)

    val result = useCase()

    assertEquals(AppMode.NOT_SELECTED, result)
    assertEquals(0, repository.saveCalls)
  }

  @Test
  fun `when mode is Offline then returns Offline without changes`() {
    val (useCase, repository) = createUseCase(initialMode = AppMode.OFFLINE)

    val result = useCase()

    assertEquals(AppMode.OFFLINE, result)
    assertEquals(0, repository.saveCalls)
  }

  @Test
  fun `when mode is Demo then returns Demo without changes`() {
    val (useCase, repository) = createUseCase(initialMode = AppMode.DEMO)

    val result = useCase()

    assertEquals(AppMode.DEMO, result)
    assertEquals(0, repository.saveCalls)
  }

  @Test
  fun `when mode is Online with valid credentials then returns Online`() {
    val (useCase, repository) =
      createUseCase(
        initialMode = AppMode.ONLINE,
        initialCredentials = Credentials(baseUrl = "https://example.com", token = "token123"),
      )

    val result = useCase()

    assertEquals(AppMode.ONLINE, result)
    assertEquals(0, repository.saveCalls)
  }

  @Test
  fun `when mode is Online with blank baseUrl then saves NotSelected and returns it`() {
    val (useCase, repository) =
      createUseCase(
        initialMode = AppMode.ONLINE,
        initialCredentials = Credentials(baseUrl = "", token = "token123"),
      )

    val result = useCase()

    assertEquals(AppMode.NOT_SELECTED, result)
    assertEquals(AppMode.NOT_SELECTED, repository.storedMode)
    assertEquals(1, repository.saveCalls)
  }

  @Test
  fun `when mode is Online with blank token then saves NotSelected and returns it`() {
    val (useCase, repository) =
      createUseCase(
        initialMode = AppMode.ONLINE,
        initialCredentials = Credentials(baseUrl = "https://example.com", token = ""),
      )

    val result = useCase()

    assertEquals(AppMode.NOT_SELECTED, result)
    assertEquals(AppMode.NOT_SELECTED, repository.storedMode)
    assertEquals(1, repository.saveCalls)
  }

  @Test
  fun `when mode is Online with both blank then saves NotSelected and returns it`() {
    val (useCase, repository) =
      createUseCase(
        initialMode = AppMode.ONLINE,
        initialCredentials = Credentials(baseUrl = "", token = ""),
      )

    val result = useCase()

    assertEquals(AppMode.NOT_SELECTED, result)
    assertEquals(AppMode.NOT_SELECTED, repository.storedMode)
    assertEquals(1, repository.saveCalls)
  }
}
