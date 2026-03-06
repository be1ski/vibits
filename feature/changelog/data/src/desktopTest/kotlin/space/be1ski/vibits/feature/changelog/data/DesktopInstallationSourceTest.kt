package space.be1ski.vibits.feature.changelog.data

import kotlin.test.Test
import kotlin.test.assertIs

class DesktopInstallationSourceTest {
  @Test
  fun `when isHomebrew then returns boolean`() {
    val source = DesktopInstallationSource()

    assertIs<Boolean>(source.isHomebrew())
  }
}
