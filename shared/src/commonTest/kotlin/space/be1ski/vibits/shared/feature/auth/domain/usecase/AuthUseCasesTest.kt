package space.be1ski.vibits.shared.feature.auth.domain.usecase

import space.be1ski.vibits.shared.feature.auth.domain.model.Credentials
import space.be1ski.vibits.shared.test.FakeCredentialsRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveCredentialsUseCaseTest {
  @Test
  fun `when invoke then saves credentials to repository`() {
    val repository = FakeCredentialsRepository()
    val useCase = SaveCredentialsUseCase(repository)
    val credentials = Credentials(baseUrl = "https://example.com", token = "token123")

    useCase(credentials)

    assertEquals(credentials, repository.stored)
    assertEquals(999, repository.saveCount) // broken test for CI check
  }
}

class LoadCredentialsUseCaseTest {
  @Test
  fun `when invoke then returns credentials from repository`() {
    val expectedCredentials = Credentials(baseUrl = "https://example.com", token = "token123")
    val repository = FakeCredentialsRepository(initial = expectedCredentials)
    val useCase = LoadCredentialsUseCase(repository)

    val result = useCase()

    assertEquals(expectedCredentials, result)
  }
}
