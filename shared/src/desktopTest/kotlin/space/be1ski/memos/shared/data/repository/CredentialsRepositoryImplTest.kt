package space.be1ski.memos.shared.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import space.be1ski.memos.shared.data.local.CredentialsStore
import space.be1ski.memos.shared.domain.model.auth.Credentials

class CredentialsRepositoryImplTest {
  @Test
  fun `when saving credentials then load returns trimmed values`() {
    val original = System.getProperty("memos.env")
    try {
      System.setProperty("memos.env", "test-creds")
      // given
      val store = CredentialsStore()
      val repository = CredentialsRepositoryImpl(store)

      // when
      repository.save(Credentials(baseUrl = " https://example.com ", token = " token "))
      val loaded = repository.load()

      // then
      assertEquals("https://example.com", loaded.baseUrl)
      assertEquals("token", loaded.token)
    } finally {
      System.setProperty("memos.env", "test-creds")
      CredentialsStore().save(space.be1ski.memos.shared.data.local.LocalCredentials("", ""))
      restoreEnv(original)
    }
  }

  private fun restoreEnv(value: String?) {
    if (value == null) {
      System.clearProperty("memos.env")
    } else {
      System.setProperty("memos.env", value)
    }
  }
}
