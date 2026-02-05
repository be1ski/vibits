package space.be1ski.vibits.feature.auth.domain.usecase

import space.be1ski.vibits.core.platform.test.createFakeLocalConfigProvider
import space.be1ski.vibits.feature.auth.domain.model.Credentials
import space.be1ski.vibits.feature.auth.domain.test.FakeCredentialsRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class InitializeCredentialsFromEnvUseCaseTest {
  @Test
  fun `when credentials already exist then does not initialize from config`() {
    val existingCredentials = Credentials(baseUrl = "https://existing.com", token = "existing-token")
    val repository = FakeCredentialsRepository(initial = existingCredentials)
    val configProvider =
      createFakeLocalConfigProvider(
        config =
          mapOf(
            "memos.baseUrl" to "https://config.com",
            "memos.token" to "config-token",
          ),
      )
    val useCase =
      InitializeCredentialsFromEnvUseCase(
        loadCredentials = LoadCredentialsUseCase(repository),
        saveCredentials = SaveCredentialsUseCase(repository),
        localConfigProvider = configProvider,
      )

    useCase()

    assertEquals(existingCredentials, repository.stored)
    assertEquals(0, repository.saveCount)
  }

  @Test
  fun `when credentials empty and config set then initializes from config`() {
    val repository = FakeCredentialsRepository()
    val configProvider =
      createFakeLocalConfigProvider(
        config =
          mapOf(
            "memos.baseUrl" to "https://config.com",
            "memos.token" to "config-token",
          ),
      )
    val useCase =
      InitializeCredentialsFromEnvUseCase(
        loadCredentials = LoadCredentialsUseCase(repository),
        saveCredentials = SaveCredentialsUseCase(repository),
        localConfigProvider = configProvider,
      )

    useCase()

    assertEquals(Credentials(baseUrl = "https://config.com", token = "config-token"), repository.stored)
    assertEquals(1, repository.saveCount)
  }

  @Test
  fun `when credentials empty and config missing then does not initialize`() {
    val repository = FakeCredentialsRepository()
    val configProvider = createFakeLocalConfigProvider(config = emptyMap())
    val useCase =
      InitializeCredentialsFromEnvUseCase(
        loadCredentials = LoadCredentialsUseCase(repository),
        saveCredentials = SaveCredentialsUseCase(repository),
        localConfigProvider = configProvider,
      )

    useCase()

    assertEquals(Credentials(baseUrl = "", token = ""), repository.stored)
    assertEquals(0, repository.saveCount)
  }

  @Test
  fun `when credentials empty and only base url set then does not initialize`() {
    val repository = FakeCredentialsRepository()
    val configProvider =
      createFakeLocalConfigProvider(
        config = mapOf("memos.baseUrl" to "https://config.com"),
      )
    val useCase =
      InitializeCredentialsFromEnvUseCase(
        loadCredentials = LoadCredentialsUseCase(repository),
        saveCredentials = SaveCredentialsUseCase(repository),
        localConfigProvider = configProvider,
      )

    useCase()

    assertEquals(Credentials(baseUrl = "", token = ""), repository.stored)
    assertEquals(0, repository.saveCount)
  }

  @Test
  fun `when credentials empty and only token set then does not initialize`() {
    val repository = FakeCredentialsRepository()
    val configProvider =
      createFakeLocalConfigProvider(
        config = mapOf("memos.token" to "config-token"),
      )
    val useCase =
      InitializeCredentialsFromEnvUseCase(
        loadCredentials = LoadCredentialsUseCase(repository),
        saveCredentials = SaveCredentialsUseCase(repository),
        localConfigProvider = configProvider,
      )

    useCase()

    assertEquals(Credentials(baseUrl = "", token = ""), repository.stored)
    assertEquals(0, repository.saveCount)
  }
}
