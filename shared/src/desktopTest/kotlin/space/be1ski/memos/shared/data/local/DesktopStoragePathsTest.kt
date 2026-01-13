package space.be1ski.memos.shared.data.local

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DesktopStoragePathsTest {
  @Test
  fun `when no env property then environment label is prod`() {
    val original = System.getProperty("memos.env")
    try {
      System.clearProperty("memos.env")
      // when/then
      assertEquals("prod", DesktopStoragePaths.environmentLabel())
    } finally {
      restoreEnv(original)
    }
  }

  @Test
  fun `when env property set then preferences node uses suffix`() {
    val original = System.getProperty("memos.env")
    try {
      System.setProperty("memos.env", "  QA  ")
      // when/then
      assertEquals("space.be1ski.memos.qa", DesktopStoragePaths.preferencesNode())
    } finally {
      restoreEnv(original)
    }
  }

  @Test
  fun `when building database path then includes app name and db file`() {
    val original = System.getProperty("memos.env")
    try {
      System.setProperty("memos.env", "test")
      val path = DesktopStoragePaths.databasePath()
      // then
      assertTrue(path.endsWith("memos.db"))
      assertTrue(path.contains("Memos-test"))
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
