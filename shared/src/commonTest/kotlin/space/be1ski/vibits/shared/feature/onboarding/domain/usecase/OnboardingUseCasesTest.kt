package space.be1ski.vibits.shared.feature.onboarding.domain.usecase

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import space.be1ski.vibits.shared.feature.memos.data.offline.OfflineMemoDto
import space.be1ski.vibits.shared.feature.memos.data.offline.OfflineMemosFileDto
import space.be1ski.vibits.shared.feature.memos.data.platform.OfflineMemoStorage
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.memos.domain.usecase.CreateMemoUseCase
import space.be1ski.vibits.shared.feature.onboarding.data.FakeOnboardingStore
import space.be1ski.vibits.shared.feature.onboarding.data.HabitPresetsDataSourceImpl
import space.be1ski.vibits.shared.feature.onboarding.data.OnboardingRepositoryImpl
import space.be1ski.vibits.shared.test.FakeMemosRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OnboardingUseCasesTest {
  @Test
  fun `when is onboarding completed then returns correct state`() =
    runTest {
      val store = FakeOnboardingStore(completed = false)
      val dataSource = HabitPresetsDataSourceImpl()
      val repository = OnboardingRepositoryImpl(store, dataSource)
      val useCase = IsOnboardingCompletedUseCase(repository)

      val result = useCase()

      assertFalse(result)
    }

  @Test
  fun `when mark onboarding completed then persists completion`() =
    runTest {
      val store = FakeOnboardingStore(completed = false)
      val dataSource = HabitPresetsDataSourceImpl()
      val repository = OnboardingRepositoryImpl(store, dataSource)
      val markCompleted = MarkOnboardingCompletedUseCase(repository)
      val isCompleted = IsOnboardingCompletedUseCase(repository)

      markCompleted()

      assertTrue(isCompleted())
    }

  @Test
  fun `when get habit presets then returns all presets`() =
    runTest {
      val store = FakeOnboardingStore()
      val dataSource = HabitPresetsDataSourceImpl()
      val repository = OnboardingRepositoryImpl(store, dataSource)
      val useCase = GetHabitPresetsUseCase(repository)

      val result = useCase()

      assertEquals(9, result.size)
      assertTrue(result.any { it.id == "exercise" })
      assertTrue(result.any { it.id == "water" })
      assertTrue(result.any { it.id == "reading" })
      assertTrue(result.any { it.id == "meditation" })
      assertTrue(result.any { it.id == "walking" })
      assertTrue(result.any { it.id == "learning" })
      assertTrue(result.any { it.id == "no_sugar" })
      assertTrue(result.any { it.id == "early_sleep" })
      assertTrue(result.any { it.id == "custom" })
    }

  @Test
  fun `when create first habit then creates habits config memo`() =
    runTest {
      val memosRepository =
        FakeMemosRepository().apply {
          createMemoResult = Result.success(Memo("memos/1", "content"))
        }
      val createMemo = CreateMemoUseCase(memosRepository)
      val useCase = CreateFirstHabitUseCase(createMemo)

      val result = useCase("Morning Exercise", "custom")

      assertTrue(result.isSuccess)
      assertEquals(1, memosRepository.createMemoCalls)
      val createdContent = memosRepository.lastCreatedContent
      assertTrue(createdContent.contains("#habits/config"))
      assertTrue(createdContent.contains("Morning Exercise"))
      assertTrue(createdContent.contains("#habits/morning_exercise"))
    }

  @Test
  fun `when create first check-in with existing habit then creates daily post with first habit checked`() =
    runTest {
      val habitConfigContent = "#habits/config\nWater | #habits/water"
      val configCreateTime = LocalDate(2026, 1, 28).atStartOfDayIn(TimeZone.UTC)
      val memosRepository =
        FakeMemosRepository().apply {
          cachedMemosResult =
            listOf(
              Memo(
                name = "memos/1",
                content = habitConfigContent,
                createTime = configCreateTime,
              ),
            )
          createMemoResult = Result.success(Memo("memos/2", "daily content"))
        }
      val createMemo = CreateMemoUseCase(memosRepository)
      val useCase = CreateFirstCheckInUseCase(memosRepository, createMemo)

      val result = useCase(LocalDate(2026, 1, 29))

      assertTrue(result.isSuccess, "Expected success but got $result")
      assertEquals(1, memosRepository.createMemoCalls)
      val createdContent = memosRepository.lastCreatedContent
      assertTrue(createdContent.contains("#habits/daily"))
      assertTrue(createdContent.contains("2026-01-29"))
      assertTrue(createdContent.contains("#habits/water"))
    }

  @Test
  fun `when create first check-in without habits then returns error`() =
    runTest {
      val memosRepository = FakeMemosRepository().apply { cachedMemosResult = emptyList() }
      val createMemo = CreateMemoUseCase(memosRepository)
      val useCase = CreateFirstCheckInUseCase(memosRepository, createMemo)

      val result = useCase(LocalDate(2026, 1, 29))

      assertTrue(result.isFailure)
      assertEquals(0, memosRepository.createMemoCalls)
    }

  @Test
  fun `when create first habit fails then returns error`() =
    runTest {
      val memosRepository =
        FakeMemosRepository().apply {
          createMemoResult = Result.failure(Exception("Network error"))
        }
      val createMemo = CreateMemoUseCase(memosRepository)
      val useCase = CreateFirstHabitUseCase(createMemo)

      val result = useCase("Exercise", "custom")

      assertTrue(result.isFailure)
      assertEquals(1, memosRepository.createMemoCalls)
    }

  @Test
  fun `when create first habit with spaces then generates tag with underscores`() =
    runTest {
      val memosRepository =
        FakeMemosRepository().apply {
          createMemoResult = Result.success(Memo("memos/1", "content"))
        }
      val createMemo = CreateMemoUseCase(memosRepository)
      val useCase = CreateFirstHabitUseCase(createMemo)

      val result = useCase("Read Every Day", "read")

      assertTrue(result.isSuccess)
      val createdContent = memosRepository.lastCreatedContent
      assertTrue(createdContent.contains("#habits/read_every_day"))
      assertTrue(createdContent.contains("Read Every Day"))
    }

  @Test
  fun `when should show onboarding and onboarding completed then returns false`() =
    runTest {
      val store = FakeOnboardingStore(completed = true)
      val storage =
        object : OfflineMemoStorage {
          override fun load() = OfflineMemosFileDto(emptyList())

          override fun save(data: OfflineMemosFileDto) {}
        }
      val dataSource = HabitPresetsDataSourceImpl()
      val repository = OnboardingRepositoryImpl(store, dataSource)
      val useCase = ShouldShowOnboardingUseCase(repository, storage)

      val result = useCase()

      assertFalse(result)
    }

  @Test
  fun `when should show onboarding and habits config exists then returns false`() =
    runTest {
      val store = FakeOnboardingStore(completed = false)
      val storage =
        object : OfflineMemoStorage {
          override fun load() =
            OfflineMemosFileDto(
              listOf(
                OfflineMemoDto(
                  name = "memos/1",
                  content = "#habits/config\nWater | #habits/water",
                ),
              ),
            )

          override fun save(data: OfflineMemosFileDto) {}
        }
      val dataSource = HabitPresetsDataSourceImpl()
      val repository = OnboardingRepositoryImpl(store, dataSource)
      val useCase = ShouldShowOnboardingUseCase(repository, storage)

      val result = useCase()

      assertFalse(result)
    }

  @Test
  fun `when should show onboarding and no habits config then returns true`() =
    runTest {
      val store = FakeOnboardingStore(completed = false)
      val storage =
        object : OfflineMemoStorage {
          override fun load() = OfflineMemosFileDto(emptyList())

          override fun save(data: OfflineMemosFileDto) {}
        }
      val dataSource = HabitPresetsDataSourceImpl()
      val repository = OnboardingRepositoryImpl(store, dataSource)
      val useCase = ShouldShowOnboardingUseCase(repository, storage)

      val result = useCase()

      assertTrue(result)
    }

  @Test
  fun `when should show onboarding and alternative habits config exists then returns false`() =
    runTest {
      val store = FakeOnboardingStore(completed = false)
      val storage =
        object : OfflineMemoStorage {
          override fun load() =
            OfflineMemosFileDto(
              listOf(
                OfflineMemoDto(
                  name = "memos/1",
                  content = "#habits_config\nWater | #habits/water",
                ),
              ),
            )

          override fun save(data: OfflineMemosFileDto) {}
        }
      val dataSource = HabitPresetsDataSourceImpl()
      val repository = OnboardingRepositoryImpl(store, dataSource)
      val useCase = ShouldShowOnboardingUseCase(repository, storage)

      val result = useCase()

      assertFalse(result)
    }
}
