package space.be1ski.vibits.shared.feature.onboarding.presentation

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import space.be1ski.vibits.shared.core.ui.theme.DefaultHabitColor
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.memos.domain.usecase.CreateMemoUseCase
import space.be1ski.vibits.shared.feature.onboarding.data.FakeOnboardingStore
import space.be1ski.vibits.shared.feature.onboarding.data.HabitPresetsDataSourceImpl
import space.be1ski.vibits.shared.feature.onboarding.data.OnboardingRepositoryImpl
import space.be1ski.vibits.shared.feature.onboarding.domain.model.HabitPreset
import space.be1ski.vibits.shared.feature.onboarding.domain.usecase.CreateFirstCheckInUseCase
import space.be1ski.vibits.shared.feature.onboarding.domain.usecase.CreateFirstHabitUseCase
import space.be1ski.vibits.shared.feature.onboarding.domain.usecase.GetHabitPresetsUseCase
import space.be1ski.vibits.shared.feature.onboarding.domain.usecase.MarkOnboardingCompletedUseCase
import space.be1ski.vibits.shared.feature.onboarding.presentation.handler.OnboardingCompletionEffectHandler
import space.be1ski.vibits.shared.feature.onboarding.presentation.handler.OnboardingEffectHandler
import space.be1ski.vibits.shared.feature.onboarding.presentation.handler.OnboardingPresetsEffectHandler
import space.be1ski.vibits.shared.feature.onboarding.presentation.handler.OnboardingSetupEffectHandler
import space.be1ski.vibits.shared.test.FakeMemosRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OnboardingEffectHandlerTest {
  @Test
  fun `when LoadPresets then emits PresetsLoaded with presets`() =
    runTest {
      val handler = createHandler()

      val actions = handler(OnboardingEffect.Command.LoadPresets).toList()

      assertEquals(1, actions.size)
      assertTrue(actions[0] is OnboardingAction.PresetsLoaded)
      val presetsLoaded = actions[0] as OnboardingAction.PresetsLoaded
      assertEquals(9, presetsLoaded.presets.size)
    }

  @Test
  fun `when CreateFirstHabit succeeds then emits HabitCreated`() =
    runTest {
      val memosRepo =
        FakeMemosRepository().apply {
          createMemoResult = Result.success(Memo("memos/1", "content"))
        }
      val handler = createHandler(memosRepository = memosRepo)

      val actions =
        handler(
          OnboardingEffect.Command.CreateFirstHabit(
            name = "Morning Exercise",
            presetId = "custom",
            color = DefaultHabitColor,
          ),
        ).toList()

      assertEquals(listOf(OnboardingAction.HabitCreated), actions)
    }

  @Test
  fun `when CreateFirstHabit fails then emits HabitCreationFailed`() =
    runTest {
      val memosRepo =
        FakeMemosRepository().apply {
          createMemoResult = Result.failure(Exception("Network error"))
        }
      val handler = createHandler(memosRepository = memosRepo)

      val actions =
        handler(
          OnboardingEffect.Command.CreateFirstHabit(
            name = "Exercise",
            presetId = "custom",
            color = DefaultHabitColor,
          ),
        ).toList()

      assertEquals(1, actions.size)
      assertTrue(actions[0] is OnboardingAction.HabitCreationFailed)
      val failed = actions[0] as OnboardingAction.HabitCreationFailed
      assertEquals("Network error", failed.error)
    }

  @Test
  fun `when CreateFirstHabit fails with null message then emits Unknown error`() =
    runTest {
      val memosRepo =
        FakeMemosRepository().apply {
          createMemoResult = Result.failure(RuntimeException())
        }
      val handler = createHandler(memosRepository = memosRepo)

      val actions =
        handler(
          OnboardingEffect.Command.CreateFirstHabit(
            name = "Exercise",
            presetId = "custom",
            color = DefaultHabitColor,
          ),
        ).toList()

      assertEquals(1, actions.size)
      val failed = actions[0] as OnboardingAction.HabitCreationFailed
      assertEquals("Unknown error", failed.error)
    }

  @Test
  fun `when MarkOnboardingCompleted then emits no actions`() =
    runTest {
      val handler = createHandler()

      val actions = handler(OnboardingEffect.Command.MarkOnboardingCompleted).toList()

      assertTrue(actions.isEmpty())
    }

  @Test
  fun `when MarkFirstCheckIn succeeds then emits FirstCheckInCreated`() =
    runTest {
      val memosRepo =
        FakeMemosRepository().apply {
          cachedMemosResult =
            listOf(
              Memo(
                name = "memos/1",
                content = "#habits/config\nWater | #habits/water",
                createTime = LocalDate(2026, 1, 28).atStartOfDayIn(TimeZone.UTC),
              ),
            )
          createMemoResult = Result.success(Memo("memos/2", "daily"))
        }
      val handler = createHandler(memosRepository = memosRepo)

      val actions = handler(OnboardingEffect.Command.MarkFirstCheckIn).toList()

      assertEquals(listOf(OnboardingAction.FirstCheckInCreated), actions)
    }

  @Test
  fun `when MarkFirstCheckIn fails then emits no actions`() =
    runTest {
      val memosRepo = FakeMemosRepository().apply { cachedMemosResult = emptyList() }
      val handler = createHandler(memosRepository = memosRepo)

      val actions = handler(OnboardingEffect.Command.MarkFirstCheckIn).toList()

      assertTrue(actions.isEmpty())
    }

  private fun createHandler(memosRepository: FakeMemosRepository = FakeMemosRepository()): OnboardingEffectHandler {
    val onboardingStore = FakeOnboardingStore()
    val presetsDataSource = HabitPresetsDataSourceImpl()
    val repository = OnboardingRepositoryImpl(onboardingStore, presetsDataSource)

    return OnboardingEffectHandler(
      presetsHandler =
        OnboardingPresetsEffectHandler(
          getHabitPresets = GetHabitPresetsUseCase(repository),
        ),
      setupHandler =
        OnboardingSetupEffectHandler(
          createFirstHabit = CreateFirstHabitUseCase(CreateMemoUseCase(memosRepository)),
          createFirstCheckIn = CreateFirstCheckInUseCase(memosRepository, CreateMemoUseCase(memosRepository)),
        ),
      completionHandler =
        OnboardingCompletionEffectHandler(
          markOnboardingCompleted = MarkOnboardingCompletedUseCase(repository),
        ),
    )
  }
}
