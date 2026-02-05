package space.be1ski.vibits.feature.homescreen.data

import space.be1ski.vibits.core.platform.app.DesktopStoragePaths
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DesktopStoragePathsTest {
  private val versionProperty = "memos.version"
  private val envProperty = "memos.env"

  @AfterTest
  fun cleanup() {
    System.clearProperty(versionProperty)
    System.clearProperty(envProperty)
  }

  @Test
  fun `when version property is set then appVersion returns it`() {
    System.setProperty(versionProperty, "1.2.3")

    val version = DesktopStoragePaths.appVersion()

    assertEquals("1.2.3", version)
  }

  @Test
  fun `when version property is blank then appVersion returns dev`() {
    System.setProperty(versionProperty, "   ")

    val version = DesktopStoragePaths.appVersion()

    assertEquals("dev", version)
  }

  @Test
  fun `when no environment set then preferencesNode returns base app id`() {
    System.clearProperty(envProperty)

    val node = DesktopStoragePaths.preferencesNode()

    assertEquals("space.be1ski.vibits", node)
  }

  @Test
  fun `when environment is dev then preferencesNode includes suffix`() {
    System.setProperty(envProperty, "dev")

    val node = DesktopStoragePaths.preferencesNode()

    assertEquals("space.be1ski.vibits.dev", node)
  }

  @Test
  fun `when environment has whitespace then preferencesNode trims and lowercases`() {
    System.setProperty(envProperty, "  DEV  ")

    val node = DesktopStoragePaths.preferencesNode()

    assertEquals("space.be1ski.vibits.dev", node)
  }

  @Test
  fun `when no environment set then environmentLabel returns prod`() {
    System.clearProperty(envProperty)

    val label = DesktopStoragePaths.environmentLabel()

    assertEquals("prod", label)
  }

  @Test
  fun `when environment is staging then environmentLabel returns staging`() {
    System.setProperty(envProperty, "staging")

    val label = DesktopStoragePaths.environmentLabel()

    assertEquals("staging", label)
  }

  @Test
  fun `when databasePath called then returns path ending with memos db`() {
    val path = DesktopStoragePaths.databasePath()

    assertTrue(path.endsWith("memos.db"))
  }

  @Test
  fun `when environment is set then databasePath includes environment in path`() {
    System.setProperty(envProperty, "test")

    val path = DesktopStoragePaths.databasePath()

    assertTrue(path.contains("Memos-test"))
  }

  @Test
  fun `when no environment set then databasePath includes prod in path`() {
    System.clearProperty(envProperty)

    val path = DesktopStoragePaths.databasePath()

    assertTrue(path.contains("Memos-prod"))
  }

  @Test
  fun `when databasePath called then uses OS-specific app data directory`() {
    val path = DesktopStoragePaths.databasePath()
    val osName = System.getProperty("os.name").lowercase()

    when {
      osName.contains("mac") -> assertTrue(path.contains("Library/Application Support"))
      osName.contains("win") -> assertTrue(path.contains("AppData"))
      else -> assertTrue(path.contains(".local/share"))
    }
  }
}
