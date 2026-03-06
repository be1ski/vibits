package space.be1ski.vibits.feature.changelog.data

import kotlin.test.Test
import kotlin.test.assertFalse

class DefaultInstallationSourceTest {
  @Test
  fun `when isHomebrew then returns false`() {
    val source = DefaultInstallationSource()

    assertFalse(source.isHomebrew())
  }
}
