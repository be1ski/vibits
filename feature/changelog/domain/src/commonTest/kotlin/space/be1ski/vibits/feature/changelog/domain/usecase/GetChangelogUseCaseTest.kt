package space.be1ski.vibits.feature.changelog.domain.usecase

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import space.be1ski.vibits.feature.changelog.domain.model.ChangelogEntry
import space.be1ski.vibits.feature.changelog.domain.test.FakeChangelogRepository
import space.be1ski.vibits.feature.changelog.domain.test.FakeLastSeenVersionStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GetChangelogUseCaseTest {
  private val repository = FakeChangelogRepository()
  private val store = FakeLastSeenVersionStore()
  private val useCase = GetChangelogUseCase(repository, store)

  @Test
  fun `when first install then saves version and returns empty`() =
    runTest {
      val result = useCase("1.2.0")

      assertTrue(result.isEmpty())
      assertEquals("1.2.0", store.lastSetVersion)
      assertEquals(0, repository.getReleaseCalls)
    }

  @Test
  fun `when same version then returns empty without fetching`() =
    runTest {
      store.setLastSeenVersion("1.2.0")

      val result = useCase("1.2.0")

      assertTrue(result.isEmpty())
      assertEquals(0, repository.getReleaseCalls)
    }

  @Test
  fun `when web version then returns empty`() =
    runTest {
      val result = useCase("web")

      assertTrue(result.isEmpty())
      assertEquals(0, repository.getReleaseCalls)
      assertNull(store.lastSetVersion)
    }

  @Test
  fun `when dev version then returns empty`() =
    runTest {
      val result = useCase("dev")

      assertTrue(result.isEmpty())
      assertEquals(0, repository.getReleaseCalls)
      assertNull(store.lastSetVersion)
    }

  @Test
  fun `when upgrade then returns filtered entries newest first`() =
    runTest {
      store.setLastSeenVersion("1.0.0")
      repository.releasesResult =
        Result.success(
          listOf(
            ChangelogEntry("0.9.0", "Old release", "old stuff", "2026-01-01"),
            ChangelogEntry("1.0.0", "Current release", "current", "2026-01-15"),
            ChangelogEntry("1.1.0", "Minor update", "minor changes", "2026-02-01"),
            ChangelogEntry("1.2.0", "Latest release", "new features", "2026-02-15"),
            ChangelogEntry("2.0.0", "Future release", "future", "2026-03-01"),
          ),
        )

      val result = useCase("1.2.0")

      assertEquals(2, result.size)
      assertEquals("1.2.0", result[0].version)
      assertEquals("1.1.0", result[1].version)
      assertEquals("1.2.0", store.lastSetVersion)
    }

  @Test
  fun `when network failure then returns empty and does not update version`() =
    runTest {
      store.setLastSeenVersion("1.0.0")
      repository.releasesResult = Result.failure(RuntimeException("Network error"))

      val result = useCase("1.1.0")

      assertTrue(result.isEmpty())
      assertEquals("1.0.0", store.getLastSeenVersion())
    }

  @Test
  fun `when unparseable last seen version then saves current and returns empty`() =
    runTest {
      store.setLastSeenVersion("invalid")
      repository.releasesResult =
        Result.success(
          listOf(ChangelogEntry("1.0.0", "Release", "body", "2026-01-01")),
        )

      val result = useCase("1.1.0")

      assertTrue(result.isEmpty())
      assertEquals("1.1.0", store.lastSetVersion)
    }

  @Test
  fun `when version with v prefix in releases then strips prefix for comparison`() =
    runTest {
      store.setLastSeenVersion("1.0.0")
      repository.releasesResult =
        Result.success(
          listOf(
            ChangelogEntry("v1.1.0", "Release v1.1.0", "changes", "2026-02-01"),
          ),
        )

      val result = useCase("1.1.0")

      assertEquals(1, result.size)
      assertEquals("v1.1.0", result[0].version)
    }

  @Test
  fun `when cancellation during fetch then propagates`() =
    runTest {
      store.setLastSeenVersion("1.0.0")
      repository.releasesResult = Result.failure(CancellationException("cancelled"))

      assertFailsWith<CancellationException> {
        useCase("1.1.0")
      }
    }

  @Test
  fun `when no releases match range then returns empty and updates version`() =
    runTest {
      store.setLastSeenVersion("1.0.0")
      repository.releasesResult =
        Result.success(
          listOf(
            ChangelogEntry("0.9.0", "Old", "old", "2026-01-01"),
            ChangelogEntry("1.0.0", "Current", "current", "2026-01-15"),
          ),
        )

      val result = useCase("1.1.0")

      assertTrue(result.isEmpty())
      assertEquals("1.1.0", store.lastSetVersion)
    }

  @Test
  fun `when parseVersion then parses semver correctly`() {
    assertEquals(listOf(1, 2, 3), parseVersion("1.2.3"))
    assertEquals(listOf(1, 2, 3), parseVersion("v1.2.3"))
    assertEquals(listOf(1, 0), parseVersion("1.0"))
    assertNull(parseVersion("invalid"))
    assertNull(parseVersion(""))
    assertNull(parseVersion("1.2.beta"))
  }

  @Test
  fun `when compareVersions then compares correctly`() {
    assertTrue(compareVersions(listOf(1, 1, 0), listOf(1, 0, 0)) > 0)
    assertTrue(compareVersions(listOf(1, 0, 0), listOf(1, 1, 0)) < 0)
    assertEquals(0, compareVersions(listOf(1, 0, 0), listOf(1, 0, 0)))
    assertTrue(compareVersions(listOf(2, 0), listOf(1, 9, 9)) > 0)
    assertEquals(0, compareVersions(listOf(1, 0), listOf(1, 0, 0)))
    assertTrue(compareVersions(listOf(1, 0, 1), listOf(1, 0)) > 0)
  }

  @Test
  fun `when upgrade with many releases then sorts newest first`() =
    runTest {
      store.setLastSeenVersion("1.0.0")
      repository.releasesResult =
        Result.success(
          listOf(
            ChangelogEntry("1.1.0", "First", "a", "2026-01-01"),
            ChangelogEntry("1.3.0", "Third", "c", "2026-03-01"),
            ChangelogEntry("1.2.0", "Second", "b", "2026-02-01"),
          ),
        )

      val result = useCase("1.3.0")

      assertEquals(3, result.size)
      assertEquals("1.3.0", result[0].version)
      assertEquals("1.2.0", result[1].version)
      assertEquals("1.1.0", result[2].version)
    }

  @Test
  fun `when release has unparseable version then filters it out`() =
    runTest {
      store.setLastSeenVersion("1.0.0")
      repository.releasesResult =
        Result.success(
          listOf(
            ChangelogEntry("1.1.0", "Valid", "body", "2026-01-01"),
            ChangelogEntry("nightly-20260201", "Nightly", "nightly", "2026-02-01"),
            ChangelogEntry("1.2.0", "Also valid", "body", "2026-02-15"),
          ),
        )

      val result = useCase("1.2.0")

      assertEquals(2, result.size)
      assertEquals("1.2.0", result[0].version)
      assertEquals("1.1.0", result[1].version)
    }
}
