package space.be1ski.vibits.feature.auth.domain.usecase

import space.be1ski.vibits.feature.auth.domain.model.Credentials
import space.be1ski.vibits.feature.main.test.FakeCredentialsRepository
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
    assertEquals(1, repository.saveCount)
  }
}
