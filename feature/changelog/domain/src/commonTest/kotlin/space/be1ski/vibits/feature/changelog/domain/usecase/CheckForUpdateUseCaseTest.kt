package space.be1ski.vibits.feature.changelog.domain.usecase

import kotlinx.coroutines.test.runTest
import space.be1ski.vibits.feature.changelog.domain.model.ChangelogEntry
import space.be1ski.vibits.feature.changelog.domain.test.FakeChangelogRepository
import space.be1ski.vibits.feature.changelog.domain.test.FakeInstallationSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CheckForUpdateUseCaseTest {
  private val repository = FakeChangelogRepository()
  private val installationSource = FakeInstallationSource()
  private val useCase = CheckForUpdateUseCase(repository, installationSource)

  @Test
  fun `when current is dev then returns null`() =
    runTest {
      assertNull(useCase("dev"))
      assertEquals(0, repository.getReleaseCalls)
    }

  @Test
  fun `when current is web then returns null`() =
    runTest {
      assertNull(useCase("web"))
      assertEquals(0, repository.getReleaseCalls)
    }

  @Test
  fun `when no releases then returns null`() =
    runTest {
      repository.releasesResult = Result.success(emptyList())

      assertNull(useCase("1.0.0"))
    }

  @Test
  fun `when latest equals current then returns null`() =
    runTest {
      repository.releasesResult =
        Result.success(
          listOf(ChangelogEntry("1.0.0", "Release", "body", "2026-01-01")),
        )

      assertNull(useCase("1.0.0"))
    }

  @Test
  fun `when latest less than current then returns null`() =
    runTest {
      repository.releasesResult =
        Result.success(
          listOf(ChangelogEntry("0.9.0", "Release", "body", "2026-01-01")),
        )

      assertNull(useCase("1.0.0"))
    }

  @Test
  fun `when latest greater than current then returns update availability`() =
    runTest {
      repository.releasesResult =
        Result.success(
          listOf(
            ChangelogEntry("1.0.0", "Old", "body", "2026-01-01"),
            ChangelogEntry("1.5.0", "New", "body", "2026-02-01"),
          ),
        )

      val result = useCase("1.0.0")

      assertNotNull(result)
      assertEquals("1.5.0", result.latestVersion)
      assertEquals("1.0.0", result.currentVersion)
      assertFalse(result.isHomebrewInstallation)
    }

  @Test
  fun `when latest has v prefix then strips it`() =
    runTest {
      repository.releasesResult =
        Result.success(
          listOf(ChangelogEntry("v2.0.0", "Release", "body", "2026-01-01")),
        )

      val result = useCase("1.0.0")

      assertNotNull(result)
      assertEquals("2.0.0", result.latestVersion)
    }

  @Test
  fun `when fetch fails then returns null`() =
    runTest {
      repository.releasesResult = Result.failure(RuntimeException("Network error"))

      assertNull(useCase("1.0.0"))
    }

  @Test
  fun `when homebrew then isHomebrewInstallation is true`() =
    runTest {
      val homebrewSource = FakeInstallationSource(homebrew = true)
      val homebrewUseCase = CheckForUpdateUseCase(repository, homebrewSource)
      repository.releasesResult =
        Result.success(
          listOf(ChangelogEntry("2.0.0", "Release", "body", "2026-01-01")),
        )

      val result = homebrewUseCase("1.0.0")

      assertNotNull(result)
      assertTrue(result.isHomebrewInstallation)
    }

  @Test
  fun `when current version is unparseable then returns null`() =
    runTest {
      repository.releasesResult =
        Result.success(
          listOf(ChangelogEntry("2.0.0", "Release", "body", "2026-01-01")),
        )

      assertNull(useCase("invalid-version"))
    }

  @Test
  fun `when all releases have unparseable versions then returns null`() =
    runTest {
      repository.releasesResult =
        Result.success(
          listOf(
            ChangelogEntry("beta1", "Beta", "body", "2026-01-01"),
            ChangelogEntry("rc-2", "RC", "body", "2026-01-15"),
          ),
        )

      assertNull(useCase("1.0.0"))
    }

  @Test
  fun `when multiple releases then picks the latest`() =
    runTest {
      repository.releasesResult =
        Result.success(
          listOf(
            ChangelogEntry("1.1.0", "Minor", "body", "2026-01-01"),
            ChangelogEntry("2.0.0", "Major", "body", "2026-02-01"),
            ChangelogEntry("1.5.0", "Mid", "body", "2026-01-15"),
          ),
        )

      val result = useCase("1.0.0")

      assertNotNull(result)
      assertEquals("2.0.0", result.latestVersion)
    }
}
