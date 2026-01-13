package space.be1ski.memos.shared.data.local

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StorageInfoProviderTest {
  @Test
  fun `when loading storage info then includes environment and paths`() {
    val original = System.getProperty("memos.env")
    try {
      System.setProperty("memos.env", "qa")
      // when
      val info = StorageInfoProvider().load()
      // then
      assertEquals("qa", info.environment)
      assertTrue(info.credentialsStore.contains("Preferences("))
      assertTrue(info.memosDatabase.endsWith("memos.db"))
    } finally {
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
