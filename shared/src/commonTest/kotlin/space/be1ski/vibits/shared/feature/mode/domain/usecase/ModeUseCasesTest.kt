package space.be1ski.vibits.shared.feature.mode.domain.usecase

import kotlinx.coroutines.test.runTest
import space.be1ski.vibits.shared.feature.auth.domain.model.Credentials
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.test.FakeAppModeRepository
import space.be1ski.vibits.shared.test.FakeCredentialsRepository
import space.be1ski.vibits.shared.test.FakeMemoCache
import space.be1ski.vibits.shared.test.FakePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class LoadAppModeUseCaseTest {
  @Test
  fun `when invoke then returns mode from repository`() {
    val repository = FakeAppModeRepository(initial = AppMode.Online)
    val useCase = LoadAppModeUseCase(repository)

    val result = useCase()

    assertEquals(AppMode.Online, result)
  }

  @Test
  fun `when mode is NotSelected then returns NotSelected`() {
    val repository = FakeAppModeRepository(initial = AppMode.NotSelected)
    val useCase = LoadAppModeUseCase(repository)

    val result = useCase()

    assertEquals(AppMode.NotSelected, result)
  }
}

class SaveAppModeUseCaseTest {
  @Test
  fun `when invoke then saves mode to repository`() {
    val repository = FakeAppModeRepository()
    val useCase = SaveAppModeUseCase(repository)

    useCase(AppMode.Offline)

    assertEquals(AppMode.Offline, repository.storedMode)
    assertEquals(1, repository.saveCalls)
  }
}

class ResetAppUseCaseTest {
  @Test
  fun `when invoke then clears memo cache`() = runTest {
    val memoCache = FakeMemoCache()
    val appModeRepository = FakeAppModeRepository(initial = AppMode.Online)
    val credentialsRepository = FakeCredentialsRepository(
      initial = Credentials(baseUrl = "https://example.com", token = "token")
    )
    val preferencesRepository = FakePreferencesRepository()
    val useCase = ResetAppUseCase(appModeRepository, memoCache, credentialsRepository, preferencesRepository)

    useCase()

    assertEquals(1, memoCache.clearCalls)
  }

  @Test
  fun `when invoke then clears credentials`() = runTest {
    val memoCache = FakeMemoCache()
    val appModeRepository = FakeAppModeRepository(initial = AppMode.Online)
    val credentialsRepository = FakeCredentialsRepository(
      initial = Credentials(baseUrl = "https://example.com", token = "token")
    )
    val preferencesRepository = FakePreferencesRepository()
    val useCase = ResetAppUseCase(appModeRepository, memoCache, credentialsRepository, preferencesRepository)

    useCase()

    assertEquals(Credentials(baseUrl = "", token = ""), credentialsRepository.stored)
  }

  @Test
  fun `when invoke then sets mode to NotSelected`() = runTest {
    val memoCache = FakeMemoCache()
    val appModeRepository = FakeAppModeRepository(initial = AppMode.Online)
    val credentialsRepository = FakeCredentialsRepository()
    val preferencesRepository = FakePreferencesRepository()
    val useCase = ResetAppUseCase(appModeRepository, memoCache, credentialsRepository, preferencesRepository)

    useCase()

    assertEquals(AppMode.NotSelected, appModeRepository.storedMode)
  }

  @Test
  fun `when invoke then resets preferences`() = runTest {
    val memoCache = FakeMemoCache()
    val appModeRepository = FakeAppModeRepository(initial = AppMode.Online)
    val credentialsRepository = FakeCredentialsRepository()
    val preferencesRepository = FakePreferencesRepository()
    val useCase = ResetAppUseCase(appModeRepository, memoCache, credentialsRepository, preferencesRepository)

    useCase()

    assertEquals(1, preferencesRepository.saveCalls)
  }
}
