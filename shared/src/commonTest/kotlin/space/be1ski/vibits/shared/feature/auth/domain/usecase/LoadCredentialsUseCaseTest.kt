package space.be1ski.vibits.shared.feature.auth.domain.usecase

import space.be1ski.vibits.shared.feature.auth.domain.model.Credentials
import space.be1ski.vibits.shared.test.FakeCredentialsRepository
import kotlin.test.Test
import kotlin.test.assertEquals

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
