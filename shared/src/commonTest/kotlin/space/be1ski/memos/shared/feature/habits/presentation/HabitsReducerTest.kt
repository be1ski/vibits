package space.be1ski.memos.shared.feature.habits.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate
import space.be1ski.memos.shared.feature.habits.domain.model.ActivityWeek
import space.be1ski.memos.shared.feature.habits.domain.model.ContributionDay
import space.be1ski.memos.shared.feature.habits.domain.model.DailyMemoInfo
import space.be1ski.memos.shared.feature.habits.domain.model.HabitConfig
import space.be1ski.memos.shared.feature.habits.domain.model.HabitStatus
import space.be1ski.memos.shared.feature.memos.domain.model.Memo

class HabitsReducerTest {

  private val testDay = ContributionDay(
    date = LocalDate(2024, 1, 15),
    count = 1,
    totalHabits = 2,
    completionRatio = 0.5f,
    habitStatuses = listOf(
      HabitStatus("#habits/exercise", "Exercise", done = true),
      HabitStatus("#habits/reading", "Reading", done = false)
    ),
    dailyMemo = null,
    inRange = true
  )

  private val testConfig = listOf(
    HabitConfig("#habits/exercise", "Exercise"),
    HabitConfig("#habits/reading", "Reading")
  )

  // OpenEditor tests

  @Test
  fun `when OpenEditor then sets editor state`() {
    val (newState, effects) = habitsReducer(
      HabitsAction.OpenEditor(testDay, testConfig),
      HabitsState()
    )

    assertEquals(testDay, newState.editorDay)
    assertEquals(testConfig, newState.editorConfig)
    assertTrue(newState.editorSelections.isNotEmpty())
    assertNull(newState.editorError)
    assertTrue(effects.isEmpty())
  }

  @Test
  fun `when OpenEditor with existing memo then sets editorExisting`() {
    val dayWithMemo = testDay.copy(dailyMemo = DailyMemoInfo("memos/1", "content"))

    val (newState, _) = habitsReducer(
      HabitsAction.OpenEditor(dayWithMemo, testConfig),
      HabitsState()
    )

    assertEquals("memos/1", newState.editorExisting?.name)
  }

  // CloseEditor tests

  @Test
  fun `when CloseEditor then clears editor state`() {
    val state = HabitsState(
      editorDay = testDay,
      editorConfig = testConfig,
      editorSelections = mapOf("#habits/exercise" to true),
      editorError = "some error",
      showDeleteConfirm = true
    )

    val (newState, effects) = habitsReducer(HabitsAction.CloseEditor, state)

    assertNull(newState.editorDay)
    assertTrue(newState.editorConfig.isEmpty())
    assertTrue(newState.editorSelections.isEmpty())
    assertNull(newState.editorError)
    assertFalse(newState.showDeleteConfirm)
    assertTrue(effects.isEmpty())
  }

  // ToggleHabit tests

  @Test
  fun `when ToggleHabit then updates selection`() {
    val state = HabitsState(
      editorSelections = mapOf("#habits/exercise" to false)
    )

    val (newState, effects) = habitsReducer(
      HabitsAction.ToggleHabit("#habits/exercise", true),
      state
    )

    assertEquals(true, newState.editorSelections["#habits/exercise"])
    assertTrue(effects.isEmpty())
  }

  // ConfirmEditor tests

  @Test
  fun `when ConfirmEditor with no selection and existing memo then shows delete confirm`() {
    val state = HabitsState(
      editorDay = testDay,
      editorConfig = testConfig,
      editorSelections = mapOf("#habits/exercise" to false),
      editorExisting = DailyMemoInfo("memos/1", "content")
    )

    val (newState, effects) = habitsReducer(HabitsAction.ConfirmEditor, state)

    assertTrue(newState.showDeleteConfirm)
    assertTrue(effects.isEmpty())
  }

  @Test
  fun `when ConfirmEditor with no selection and no existing memo then shows error`() {
    val state = HabitsState(
      editorDay = testDay,
      editorConfig = testConfig,
      editorSelections = mapOf("#habits/exercise" to false),
      editorExisting = null
    )

    val (newState, effects) = habitsReducer(HabitsAction.ConfirmEditor, state)

    assertEquals("Select at least one habit.", newState.editorError)
    assertTrue(effects.isEmpty())
  }

  @Test
  fun `when ConfirmEditor with selection and no existing memo then emits CreateMemo`() {
    val state = HabitsState(
      editorDay = testDay,
      editorConfig = testConfig,
      editorSelections = mapOf("#habits/exercise" to true)
    )

    val (newState, effects) = habitsReducer(HabitsAction.ConfirmEditor, state)

    assertTrue(newState.isLoading)
    assertEquals(1, effects.size)
    assertIs<HabitsEffect.CreateMemo>(effects.first())
  }

  @Test
  fun `when ConfirmEditor with selection and existing memo then emits UpdateMemo`() {
    val state = HabitsState(
      editorDay = testDay,
      editorConfig = testConfig,
      editorSelections = mapOf("#habits/exercise" to true),
      editorExisting = DailyMemoInfo("memos/1", "old content")
    )

    val (newState, effects) = habitsReducer(HabitsAction.ConfirmEditor, state)

    assertTrue(newState.isLoading)
    assertEquals(1, effects.size)
    val effect = effects.first()
    assertIs<HabitsEffect.UpdateMemo>(effect)
    assertEquals("memos/1", effect.name)
  }

  // Delete flow tests

  @Test
  fun `when RequestDelete then shows delete confirm`() {
    val (newState, effects) = habitsReducer(
      HabitsAction.RequestDelete,
      HabitsState()
    )

    assertTrue(newState.showDeleteConfirm)
    assertTrue(effects.isEmpty())
  }

  @Test
  fun `when ConfirmDelete then emits DeleteMemo effect`() {
    val state = HabitsState(
      editorExisting = DailyMemoInfo("memos/1", "content")
    )

    val (newState, effects) = habitsReducer(HabitsAction.ConfirmDelete, state)

    assertTrue(newState.isLoading)
    assertEquals(1, effects.size)
    val effect = effects.first()
    assertIs<HabitsEffect.DeleteMemo>(effect)
    assertEquals("memos/1", effect.name)
  }

  @Test
  fun `when CancelDelete then hides delete confirm`() {
    val state = HabitsState(showDeleteConfirm = true)

    val (newState, effects) = habitsReducer(HabitsAction.CancelDelete, state)

    assertFalse(newState.showDeleteConfirm)
    assertTrue(effects.isEmpty())
  }

  // Selection tests

  @Test
  fun `when SelectDay then updates selection state`() {
    val (newState, _) = habitsReducer(
      HabitsAction.SelectDay(testDay, "section-1"),
      HabitsState()
    )

    assertEquals(testDay.date, newState.selectedDate)
    assertEquals("section-1", newState.activeSelectionId)
  }

  @Test
  fun `when SelectWeek then updates selected week`() {
    val week = ActivityWeek(
      startDate = LocalDate(2024, 1, 15),
      days = emptyList(),
      weeklyCount = 0
    )

    val (newState, _) = habitsReducer(HabitsAction.SelectWeek(week), HabitsState())

    assertEquals(week, newState.selectedWeek)
  }

  @Test
  fun `when ClearSelection then clears all selection state`() {
    val state = HabitsState(
      selectedDate = LocalDate(2024, 1, 15),
      selectedWeek = ActivityWeek(LocalDate(2024, 1, 15), emptyList(), 0),
      activeSelectionId = "section-1"
    )

    val (newState, _) = habitsReducer(HabitsAction.ClearSelection, state)

    assertNull(newState.selectedDate)
    assertNull(newState.selectedWeek)
    assertNull(newState.activeSelectionId)
  }

  // API response tests

  @Test
  fun `when MemoCreated then clears editor and emits refresh`() {
    val state = HabitsState(
      isLoading = true,
      editorDay = testDay,
      editorConfig = testConfig
    )

    val (newState, effects) = habitsReducer(
      HabitsAction.MemoCreated(Memo(name = "memos/1")),
      state
    )

    assertFalse(newState.isLoading)
    assertNull(newState.editorDay)
    assertTrue(newState.editorConfig.isEmpty())
    assertEquals(1, effects.size)
    assertIs<HabitsEffect.RefreshMemos>(effects.first())
  }

  @Test
  fun `when MemoDeleted then clears editor and emits refresh`() {
    val state = HabitsState(
      isLoading = true,
      editorDay = testDay,
      showDeleteConfirm = true
    )

    val (newState, effects) = habitsReducer(
      HabitsAction.MemoDeleted("memos/1"),
      state
    )

    assertFalse(newState.isLoading)
    assertNull(newState.editorDay)
    assertFalse(newState.showDeleteConfirm)
    assertEquals(1, effects.size)
    assertIs<HabitsEffect.RefreshMemos>(effects.first())
  }

  @Test
  fun `when MemoOperationFailed then sets error and stops loading`() {
    val state = HabitsState(isLoading = true)

    val (newState, effects) = habitsReducer(
      HabitsAction.MemoOperationFailed("Network error"),
      state
    )

    assertFalse(newState.isLoading)
    assertEquals("Network error", newState.editorError)
    assertTrue(effects.isEmpty())
  }
}
