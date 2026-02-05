package space.be1ski.vibits.feature.auth.data

import space.be1ski.vibits.feature.auth.data.test.FakeCredentialsStore
import space.be1ski.vibits.feature.auth.domain.model.Credentials
import kotlin.test.Test
import kotlin.test.assertEquals

class CredentialsRepositoryImplTest {
  @Test
  fun `when load then returns credentials from store`() {
    val store =
      FakeCredentialsStore(
        initial = LocalCredentials(baseUrl = "https://example.com", token = "secret"),
      )
    val repository = CredentialsRepositoryImpl(store)

    val result = repository.load()

    assertEquals(Credentials(baseUrl = "https://example.com", token = "secret"), result)
  }

  @Test
  fun `when save then trims and persists credentials to store`() {
    val store = FakeCredentialsStore()
    val repository = CredentialsRepositoryImpl(store)
    val credentials = Credentials(baseUrl = "  https://example.com  ", token = "  token  ")

    repository.save(credentials)

    assertEquals(LocalCredentials(baseUrl = "https://example.com", token = "token"), store.stored)
    assertEquals(1, store.saveCalls)
  }

  @Test
  fun `when save credentials without whitespace then persists unchanged`() {
    val store = FakeCredentialsStore()
    val repository = CredentialsRepositoryImpl(store)
    val credentials = Credentials(baseUrl = "https://example.com", token = "token123")

    repository.save(credentials)

    assertEquals(LocalCredentials(baseUrl = "https://example.com", token = "token123"), store.stored)
  }

  @Test
  fun `when save with long url then url is masked for logging but saved in full`() {
    val store = FakeCredentialsStore()
    val repository = CredentialsRepositoryImpl(store)
    val longUrl = "https://example.com/very/long/path/that/exceeds/fifty/characters/for/testing"
    val credentials = Credentials(baseUrl = longUrl, token = "token")

    repository.save(credentials)

    assertEquals(LocalCredentials(baseUrl = longUrl, token = "token"), store.stored)
  }
}
