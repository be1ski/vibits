package space.be1ski.vibits.feature.changelog.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class VersionComparisonTest {
  @Test
  fun `when parseVersion with semver then returns parts`() {
    assertEquals(listOf(1, 2, 3), parseVersion("1.2.3"))
  }

  @Test
  fun `when parseVersion with v prefix then strips prefix`() {
    assertEquals(listOf(1, 2, 3), parseVersion("v1.2.3"))
  }

  @Test
  fun `when parseVersion with two parts then returns two parts`() {
    assertEquals(listOf(1, 0), parseVersion("1.0"))
  }

  @Test
  fun `when parseVersion with invalid then returns null`() {
    assertNull(parseVersion("invalid"))
  }

  @Test
  fun `when parseVersion with empty then returns null`() {
    assertNull(parseVersion(""))
  }

  @Test
  fun `when parseVersion with beta suffix then returns null`() {
    assertNull(parseVersion("1.2.beta"))
  }

  @Test
  fun `when compareVersions with greater then returns positive`() {
    assertTrue(compareVersions(listOf(1, 1, 0), listOf(1, 0, 0)) > 0)
  }

  @Test
  fun `when compareVersions with lesser then returns negative`() {
    assertTrue(compareVersions(listOf(1, 0, 0), listOf(1, 1, 0)) < 0)
  }

  @Test
  fun `when compareVersions with equal then returns zero`() {
    assertEquals(0, compareVersions(listOf(1, 0, 0), listOf(1, 0, 0)))
  }

  @Test
  fun `when compareVersions with major difference then major wins`() {
    assertTrue(compareVersions(listOf(2, 0), listOf(1, 9, 9)) > 0)
  }

  @Test
  fun `when compareVersions with different lengths then pads with zero`() {
    assertEquals(0, compareVersions(listOf(1, 0), listOf(1, 0, 0)))
  }

  @Test
  fun `when compareVersions with patch difference then detects correctly`() {
    assertTrue(compareVersions(listOf(1, 0, 1), listOf(1, 0)) > 0)
  }

  @Test
  fun `when parseVersion with single zero then returns list`() {
    assertEquals(listOf(0), parseVersion("0"))
  }

  @Test
  fun `when compareVersions with single element lists then compares correctly`() {
    assertTrue(compareVersions(listOf(2), listOf(1)) > 0)
  }

  @Test
  fun `when compareVersions with empty padded parts equal then returns zero`() {
    assertEquals(0, compareVersions(listOf(1), listOf(1, 0, 0)))
  }
}
