package space.be1ski.vibits.shared.feature.habits.presentation

import kotlinx.datetime.LocalDate
import space.be1ski.vibits.shared.app.domain.model.ActivityMode
import space.be1ski.vibits.shared.app.domain.model.ActivityRange
import space.be1ski.vibits.shared.core.elm.test
import space.be1ski.vibits.shared.feature.habits.domain.model.ActivityWeek
import space.be1ski.vibits.shared.feature.habits.domain.model.ActivityWeekData
import space.be1ski.vibits.shared.feature.habits.domain.model.ContributionDay
import space.be1ski.vibits.shared.feature.habits.domain.model.DailyMemoInfo
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitConfig
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitStatus
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import kotlin.test.Test
import kotlin.test.assertEquals

class HabitsReducerTest {
  private val testDay =
    ContributionDay(
      date = LocalDate(2024, 1, 15),
      count = 1,
      totalHabits = 2,
      completionRatio = 0.5f,
      habitStatuses =
        listOf(
          HabitStatus("#habits/exercise", "Exercise", done = true),
          HabitStatus("#habits/reading", "Reading", done = false),
        ),
      dailyMemo = null,
      inRange = true,
    )

  private val testConfig =
    listOf(
      HabitConfig("#habits/exercise", "Exercise"),
      HabitConfig("#habits/reading", "Reading"),
    )

  private val testConfigMemo =
    Memo(
      name = "memos/config_old",
      content = "#habits/config\\n\\nexercise | #habits/exercise | #4CAF50\\nreading | #habits/reading | #2196F3",
      createTime = null,
      updateTime = null,
    )

  @Test
  fun `when OpenEditor then sets editor state`() =
    habitsReducer.test(HabitsState()) {
      send(HabitsAction.Editor.OpenEditor(day = testDay, config = testConfig))

      assertState {
        editorDay == testDay &&
          editorConfig == testConfig &&
          editorSelections.isNotEmpty() &&
          editorError == null
      }
      assertNoEffects()
    }

  @Test
  fun `when OpenEditor with existing memo then sets editorExisting`() =
    habitsReducer.test(HabitsState()) {
      val dayWithMemo = testDay.copy(dailyMemo = DailyMemoInfo("memos/1", "content"))

      send(HabitsAction.Editor.OpenEditor(day = dayWithMemo, config = testConfig))

      assertState { editorExisting?.name == "memos/1" }
    }

  @Test
  fun `when CloseEditor then clears editor state`() =
    habitsReducer.test(
      HabitsState(
        editorDay = testDay,
        editorConfig = testConfig,
        editorSelections = mapOf("#habits/exercise" to true),
        editorError = "some error",
        showDeleteConfirm = true,
      ),
    ) {
      send(HabitsAction.Editor.CloseEditor)

      assertState {
        editorDay == null &&
          editorConfig.isEmpty() &&
          editorSelections.isEmpty() &&
          editorError == null &&
          !showDeleteConfirm
      }
      assertNoEffects()
    }

  @Test
  fun `when ToggleHabit then updates selection`() =
    habitsReducer.test(HabitsState(editorSelections = mapOf("#habits/exercise" to false))) {
      send(HabitsAction.Editor.ToggleHabit("#habits/exercise", true))

      assertState { editorSelections["#habits/exercise"] == true }
      assertNoEffects()
    }

  @Test
  fun `when ConfirmEditor with no selection and existing memo then shows delete confirm`() =
    habitsReducer.test(
      HabitsState(
        editorDay = testDay,
        editorConfig = testConfig,
        editorSelections = mapOf("#habits/exercise" to false),
        editorExisting = DailyMemoInfo("memos/1", "content"),
      ),
    ) {
      send(HabitsAction.Editor.ConfirmEditor)

      assertState { showDeleteConfirm }
      assertNoEffects()
    }

  @Test
  fun `when ConfirmEditor with no selection and no existing memo then shows error`() =
    habitsReducer.test(
      HabitsState(
        editorDay = testDay,
        editorConfig = testConfig,
        editorSelections = mapOf("#habits/exercise" to false),
        editorExisting = null,
      ),
    ) {
      send(HabitsAction.Editor.ConfirmEditor)

      assertState { editorError == "Select at least one habit." }
      assertNoEffects()
    }

  @Test
  fun `when ConfirmEditor with selection and no existing memo then emits CreateMemo`() =
    habitsReducer.test(
      HabitsState(
        editorDay = testDay,
        editorConfig = testConfig,
        editorSelections = mapOf("#habits/exercise" to true),
      ),
    ) {
      send(HabitsAction.Editor.ConfirmEditor)

      assertState { isLoading }
      assertHasCommand<HabitsEffect.CreateMemo>()
    }

  @Test
  fun `when ConfirmEditor with selection and existing memo then emits UpdateMemo`() =
    habitsReducer.test(
      HabitsState(
        editorDay = testDay,
        editorConfig = testConfig,
        editorSelections = mapOf("#habits/exercise" to true),
        editorExisting = DailyMemoInfo("memos/1", "old content"),
      ),
    ) {
      send(HabitsAction.Editor.ConfirmEditor)

      assertState { isLoading }
      val effect = assertHasCommand<HabitsEffect.UpdateMemo>()
      assertEquals("memos/1", effect.name)
    }

  @Test
  fun `when ConfirmEditor with null editorDay then does nothing`() =
    habitsReducer.test(
      HabitsState(
        editorDay = null,
        editorConfig = testConfig,
        editorSelections = mapOf("#habits/exercise" to true),
      ),
    ) {
      send(HabitsAction.Editor.ConfirmEditor)

      assertNoEffects()
    }

  @Test
  fun `when RequestDelete then shows delete confirm`() =
    habitsReducer.test(HabitsState()) {
      send(HabitsAction.Editor.RequestDelete)

      assertState { showDeleteConfirm }
      assertNoEffects()
    }

  @Test
  fun `when ConfirmDelete then emits DeleteMemo effect`() =
    habitsReducer.test(HabitsState(editorExisting = DailyMemoInfo("memos/1", "content"))) {
      send(HabitsAction.Editor.ConfirmDelete)

      assertState { isLoading }
      val effect = assertHasCommand<HabitsEffect.DeleteMemo>()
      assertEquals("memos/1", effect.name)
    }

  @Test
  fun `when ConfirmDelete with null editorExisting then does nothing`() =
    habitsReducer.test(HabitsState(editorExisting = null)) {
      send(HabitsAction.Editor.ConfirmDelete)

      assertState { !isLoading }
      assertNoEffects()
    }

  @Test
  fun `when CancelDelete then hides delete confirm`() =
    habitsReducer.test(HabitsState(showDeleteConfirm = true)) {
      send(HabitsAction.Editor.CancelDelete)

      assertState { !showDeleteConfirm }
      assertNoEffects()
    }

  @Test
  fun `when SelectDay then updates selection state`() =
    habitsReducer.test(HabitsState()) {
      send(HabitsAction.Selection.SelectDay(testDay, "section-1"))

      assertState { selectedDate == testDay.date && activeSelectionId == "section-1" }
    }

  @Test
  fun `when SelectWeek then updates selected week`() =
    habitsReducer.test(HabitsState()) {
      val week = ActivityWeek(startDate = LocalDate(2024, 1, 15), days = emptyList(), weeklyCount = 0)

      send(HabitsAction.Selection.SelectWeek(week))

      assertState { selectedWeek == week }
    }

  @Test
  fun `when ClearSelection then clears all selection state`() =
    habitsReducer.test(
      HabitsState(
        selectedDate = LocalDate(2024, 1, 15),
        selectedWeek = ActivityWeek(LocalDate(2024, 1, 15), emptyList(), 0),
        activeSelectionId = "section-1",
      ),
    ) {
      send(HabitsAction.Selection.ClearSelection)

      assertState { selectedDate == null && selectedWeek == null && activeSelectionId == null }
    }

  @Test
  fun `when MemoCreated then clears editor and emits refresh`() =
    habitsReducer.test(
      HabitsState(
        isLoading = true,
        editorDay = testDay,
        editorConfig = testConfig,
      ),
    ) {
      send(HabitsAction.Response.MemoCreated(Memo(name = "memos/1")))

      assertState { !isLoading && editorDay == null && editorConfig.isEmpty() }
      assertCommands(HabitsEffect.RefreshMemos)
    }

  @Test
  fun `when MemoUpdated then clears editor and emits refresh`() =
    habitsReducer.test(HabitsState(isLoading = true, editorDay = testDay)) {
      send(HabitsAction.Response.MemoUpdated(Memo(name = "memos/1")))

      assertState { !isLoading && editorDay == null }
      assertCommands(HabitsEffect.RefreshMemos)
    }

  @Test
  fun `when MemoDeleted then clears editor and emits refresh`() =
    habitsReducer.test(
      HabitsState(
        isLoading = true,
        editorDay = testDay,
        showDeleteConfirm = true,
      ),
    ) {
      send(HabitsAction.Response.MemoDeleted("memos/1"))

      assertState { !isLoading && editorDay == null && !showDeleteConfirm }
      assertCommands(HabitsEffect.RefreshMemos)
    }

  @Test
  fun `when MemoOperationFailed then sets error and stops loading`() =
    habitsReducer.test(HabitsState(isLoading = true)) {
      send(HabitsAction.Response.MemoOperationFailed("Network error"))

      assertState { !isLoading && editorError == "Network error" }
      assertNoEffects()
    }

  @Test
  fun `when OpenConfigDialog then shows dialog with editable habits`() =
    habitsReducer.test(HabitsState()) {
      send(HabitsAction.Config.OpenConfigDialog(testConfig))

      assertState { showConfigDialog && editingHabits.size == 2 && editingHabits.first().label == "Exercise" }
      assertNoEffects()
    }

  @Test
  fun `when CloseConfigDialog then hides dialog and clears habits`() =
    habitsReducer.test(
      HabitsState(
        showConfigDialog = true,
        editingHabits = listOf(EditableHabit("1", "#habits/test", "Test", 0xFF0000L)),
      ),
    ) {
      send(HabitsAction.Config.CloseConfigDialog)

      assertState { !showConfigDialog && editingHabits.isEmpty() }
      assertNoEffects()
    }

  @Test
  fun `when AddHabit then adds new empty habit`() =
    habitsReducer.test(HabitsState(editingHabits = emptyList())) {
      send(HabitsAction.Config.AddHabit)

      assertState { editingHabits.size == 1 && editingHabits.first().label == "" }
      assertNoEffects()
    }

  @Test
  fun `when UpdateHabitLabel then updates label only`() =
    habitsReducer.test(
      HabitsState(editingHabits = listOf(EditableHabit("habit_1", "", "", 0xFF0000L))),
    ) {
      send(HabitsAction.Config.UpdateHabitLabel("habit_1", "Morning Run"))

      assertState {
        editingHabits.first().label == "Morning Run" &&
          editingHabits.first().tag == ""
      }
      assertNoEffects()
    }

  @Test
  fun `when toHabitConfig with empty tag then generates tag from label`() {
    val editable = EditableHabit("habit_1", "", "Morning Run", 0xFF0000L)
    val config = editable.toHabitConfig()

    assertEquals("#habits/Morning_Run", config.tag)
    assertEquals("Morning Run", config.label)
  }

  @Test
  fun `when toHabitConfig with existing tag then keeps the tag`() {
    val editable = EditableHabit("habit_1", "#habits/custom", "Morning Run", 0xFF0000L)
    val config = editable.toHabitConfig()

    assertEquals("#habits/custom", config.tag)
    assertEquals("Morning Run", config.label)
  }

  @Test
  fun `when toHabitConfig with cyrillic label then generates correct tag`() {
    val editable = EditableHabit("habit_1", "", "фывфывфывфыв", 0xFF0000L)
    val config = editable.toHabitConfig()

    assertEquals("#habits/фывфывфывфыв", config.tag)
    assertEquals("фывфывфывфыв", config.label)
  }

  @Test
  fun `when UpdateHabitLabel for non-matching id then keeps other habits unchanged`() =
    habitsReducer.test(
      HabitsState(
        editingHabits =
          listOf(
            EditableHabit("habit_1", "#habits/a", "A", 0xFF0000L),
            EditableHabit("habit_2", "#habits/b", "B", 0x00FF00L),
          ),
      ),
    ) {
      send(HabitsAction.Config.UpdateHabitLabel("habit_1", "Updated A"))

      assertState {
        editingHabits[0].label == "Updated A" &&
          editingHabits[1].label == "B" &&
          editingHabits[1].tag == "#habits/b"
      }
    }

  @Test
  fun `when UpdateHabitColor then updates color`() =
    habitsReducer.test(
      HabitsState(editingHabits = listOf(EditableHabit("habit_1", "#habits/test", "Test", 0xFF0000L))),
    ) {
      send(HabitsAction.Config.UpdateHabitColor("habit_1", 0x00FF00L))

      assertState { editingHabits.first().color == 0x00FF00L }
      assertNoEffects()
    }

  @Test
  fun `when UpdateHabitColor for non-matching id then keeps other habits unchanged`() =
    habitsReducer.test(
      HabitsState(
        editingHabits =
          listOf(
            EditableHabit("habit_1", "#habits/a", "A", 0xFF0000L),
            EditableHabit("habit_2", "#habits/b", "B", 0x00FF00L),
          ),
      ),
    ) {
      send(HabitsAction.Config.UpdateHabitColor("habit_1", 0xFFFFFFFL))

      assertState {
        editingHabits[0].color == 0xFFFFFFFL &&
          editingHabits[1].color == 0x00FF00L
      }
    }

  @Test
  fun `when DeleteHabit then removes habit from list`() =
    habitsReducer.test(
      HabitsState(
        editingHabits =
          listOf(
            EditableHabit("habit_1", "#habits/a", "A", 0xFF0000L),
            EditableHabit("habit_2", "#habits/b", "B", 0x00FF00L),
          ),
      ),
    ) {
      send(HabitsAction.Config.DeleteHabit("habit_1"))

      assertState { editingHabits.size == 1 && editingHabits.first().id == "habit_2" }
      assertNoEffects()
    }

  @Test
  fun `when SaveConfigDialog then emits CreateMemo with config content`() =
    habitsReducer.test(
      HabitsState(
        editingHabits = listOf(EditableHabit("habit_1", "#habits/exercise", "Exercise", 0xFF0000L)),
      ),
    ) {
      send(HabitsAction.Config.SaveConfigDialog)

      assertState { isLoading }
      assertHasCommand<HabitsEffect.CreateMemo>()
    }

  @Test
  fun `when SaveConfigDialog with blank habits then filters them out`() =
    habitsReducer.test(
      HabitsState(
        editingHabits =
          listOf(
            EditableHabit("habit_1", "#habits/exercise", "Exercise", 0xFF0000L),
            EditableHabit("habit_2", "", "", 0x00FF00L),
            EditableHabit("habit_3", "#habits/reading", "Reading", 0x0000FFL),
          ),
      ),
    ) {
      send(HabitsAction.Config.SaveConfigDialog)

      val effect = assertHasCommand<HabitsEffect.CreateMemo>()
      assertEquals(true, effect.content.contains("Exercise"))
      assertEquals(true, effect.content.contains("Reading"))
      assertEquals(false, effect.content.contains("habit_2"))
    }

  @Test
  fun `when RequestSingleHabitToggle then sets single toggle state`() =
    habitsReducer.test(HabitsState()) {
      send(HabitsAction.SingleToggle.RequestSingleHabitToggle(testDay, "#habits/exercise", "Exercise", testConfig))

      assertState {
        singleToggleDay == testDay &&
          singleToggleHabitTag == "#habits/exercise" &&
          singleToggleHabitLabel == "Exercise" &&
          singleToggleConfig == testConfig
      }
      assertNoEffects()
    }

  @Test
  fun `when ConfirmSingleHabitToggle with no selection and existing memo then deletes memo`() =
    habitsReducer.test(
      HabitsState(
        singleToggleDay = testDay.copy(dailyMemo = DailyMemoInfo("memos/1", "content")),
        singleToggleHabitTag = "#habits/exercise",
        singleToggleConfig = testConfig,
      ),
    ) {
      send(HabitsAction.SingleToggle.ConfirmSingleHabitToggle)

      assertState { isLoading }
      val effect = assertHasCommand<HabitsEffect.DeleteMemo>()
      assertEquals("memos/1", effect.name)
    }

  @Test
  fun `when ConfirmSingleHabitToggle with selection and no existing memo then creates memo`() =
    habitsReducer.test(
      HabitsState(
        singleToggleDay = testDay.copy(dailyMemo = null),
        singleToggleHabitTag = "#habits/reading",
        singleToggleConfig = testConfig,
      ),
    ) {
      send(HabitsAction.SingleToggle.ConfirmSingleHabitToggle)

      assertState { isLoading }
      assertHasCommand<HabitsEffect.CreateMemo>()
    }

  @Test
  fun `when ConfirmSingleHabitToggle with selection and existing memo then updates memo`() =
    habitsReducer.test(
      HabitsState(
        singleToggleDay = testDay.copy(dailyMemo = DailyMemoInfo("memos/1", "old")),
        singleToggleHabitTag = "#habits/reading",
        singleToggleConfig = testConfig,
      ),
    ) {
      send(HabitsAction.SingleToggle.ConfirmSingleHabitToggle)

      assertState { isLoading }
      val effect = assertHasCommand<HabitsEffect.UpdateMemo>()
      assertEquals("memos/1", effect.name)
    }

  @Test
  fun `when ConfirmSingleHabitToggle toggles off last habit with no memo then just closes`() =
    habitsReducer.test(
      HabitsState(
        singleToggleDay =
          testDay.copy(
            habitStatuses = listOf(HabitStatus("#habits/ex", "Ex", done = true)),
            dailyMemo = null,
          ),
        singleToggleHabitTag = "#habits/ex",
        singleToggleConfig = listOf(HabitConfig("#habits/ex", "Ex")),
      ),
    ) {
      send(HabitsAction.SingleToggle.ConfirmSingleHabitToggle)

      assertState { singleToggleDay == null && singleToggleHabitTag == null }
      assertNoEffects()
    }

  @Test
  fun `when ConfirmSingleHabitToggle with null day then does nothing`() =
    habitsReducer.test(
      HabitsState(
        singleToggleDay = null,
        singleToggleHabitTag = "#habits/ex",
        singleToggleConfig = testConfig,
      ),
    ) {
      send(HabitsAction.SingleToggle.ConfirmSingleHabitToggle)

      assertNoEffects()
    }

  @Test
  fun `when ConfirmSingleHabitToggle with null habit tag then does nothing`() =
    habitsReducer.test(
      HabitsState(
        singleToggleDay = testDay,
        singleToggleHabitTag = null,
        singleToggleConfig = testConfig,
      ),
    ) {
      send(HabitsAction.SingleToggle.ConfirmSingleHabitToggle)

      assertNoEffects()
    }

  @Test
  fun `when CancelSingleHabitToggle then clears single toggle state`() =
    habitsReducer.test(
      HabitsState(
        singleToggleDay = testDay,
        singleToggleHabitTag = "#habits/exercise",
        singleToggleHabitLabel = "Exercise",
        singleToggleConfig = testConfig,
      ),
    ) {
      send(HabitsAction.SingleToggle.CancelSingleHabitToggle)

      assertState {
        singleToggleDay == null &&
          singleToggleHabitTag == null &&
          singleToggleHabitLabel == null &&
          singleToggleConfig.isEmpty()
      }
      assertNoEffects()
    }

  @Test
  fun `when OpenConfigDialog with existing memo then opens dialog for editing`() =
    habitsReducer.test(HabitsState()) {
      send(HabitsAction.Config.OpenConfigDialog(testConfig, testConfigMemo))

      assertState {
        showConfigDialog &&
          editingHabits.size == 2 &&
          editingConfigMemo == testConfigMemo &&
          !showEditConfigWarning
      }
      assertNoEffects()
    }

  @Test
  fun `when OpenConfigDialog without existing memo then opens dialog for new config`() =
    habitsReducer.test(HabitsState()) {
      send(HabitsAction.Config.OpenConfigDialog(testConfig, existingMemo = null))

      assertState {
        showConfigDialog &&
          editingHabits.size == 2 &&
          editingConfigMemo == null &&
          !showEditConfigWarning
      }
      assertNoEffects()
    }

  @Test
  fun `when SaveConfigDialog with existing memo then shows warning`() =
    habitsReducer.test(
      HabitsState(
        showConfigDialog = true,
        editingHabits =
          listOf(
            EditableHabit("1", "#habits/exercise", "Exercise", 0xFF4CAF50L),
            EditableHabit("2", "#habits/reading", "Reading", 0xFF2196F3L),
          ),
        editingConfigMemo = testConfigMemo,
      ),
    ) {
      send(HabitsAction.Config.SaveConfigDialog)

      assertState {
        showEditConfigWarning &&
          !showConfigDialog &&
          pendingConfigEdit.size == 2
      }
      assertNoEffects()
    }

  @Test
  fun `when SaveConfigDialog without existing memo then creates new config`() =
    habitsReducer.test(
      HabitsState(
        showConfigDialog = true,
        editingHabits =
          listOf(
            EditableHabit("1", "#habits/exercise", "Exercise", 0xFF4CAF50L),
          ),
        editingConfigMemo = null,
      ),
    ) {
      send(HabitsAction.Config.SaveConfigDialog)

      assertState { isLoading }
      assertHasCommand<HabitsEffect.CreateMemo>()
    }

  @Test
  fun `when DismissEditConfigWarning then closes everything`() =
    habitsReducer.test(
      HabitsState(
        showEditConfigWarning = true,
        pendingConfigEdit = testConfig,
        editingConfigMemo = testConfigMemo,
      ),
    ) {
      send(HabitsAction.ConfigWarning.DismissEditConfigWarning)

      assertState {
        !showEditConfigWarning &&
          !showConfigDialog &&
          pendingConfigEdit.isEmpty() &&
          editingHabits.isEmpty() &&
          editingConfigMemo == null
      }
      assertNoEffects()
    }

  @Test
  fun `when ConfirmEditExistingConfig then updates existing config`() =
    habitsReducer.test(
      HabitsState(
        showEditConfigWarning = true,
        pendingConfigEdit = testConfig,
        editingConfigMemo = testConfigMemo,
      ),
    ) {
      send(HabitsAction.ConfigWarning.ConfirmEditExistingConfig)

      assertState {
        !showEditConfigWarning &&
          isLoading &&
          pendingConfigEdit.isEmpty()
      }
      assertHasCommand<HabitsEffect.UpdateMemo>()
    }

  @Test
  fun `when CreateNewConfigInstead then creates new config`() =
    habitsReducer.test(
      HabitsState(
        showEditConfigWarning = true,
        pendingConfigEdit = testConfig,
        editingConfigMemo = testConfigMemo,
      ),
    ) {
      send(HabitsAction.ConfigWarning.CreateNewConfigInstead)

      assertState {
        !showEditConfigWarning &&
          isLoading &&
          pendingConfigEdit.isEmpty()
      }
      assertHasCommand<HabitsEffect.CreateMemo>()
    }

  @Test
  fun `when RequestDeleteConfig then shows delete confirm`() =
    habitsReducer.test(HabitsState()) {
      send(HabitsAction.ConfigDelete.RequestDeleteConfig)

      assertState { showDeleteConfigConfirm }
      assertNoEffects()
    }

  @Test
  fun `when ConfirmDeleteConfig then emits DeleteMemo effect`() =
    habitsReducer.test(
      HabitsState(
        editingConfigMemo = testConfigMemo,
        showDeleteConfigConfirm = true,
      ),
    ) {
      send(HabitsAction.ConfigDelete.ConfirmDeleteConfig)

      assertState {
        !showDeleteConfigConfirm &&
          !showConfigDialog &&
          editingHabits.isEmpty() &&
          editingConfigMemo == null &&
          isLoading
      }
      val effect = assertHasCommand<HabitsEffect.DeleteMemo>()
      assertEquals("memos/config_old", effect.name)
    }

  @Test
  fun `when ConfirmDeleteConfig with null editingConfigMemo then does nothing`() =
    habitsReducer.test(
      HabitsState(
        editingConfigMemo = null,
        showDeleteConfigConfirm = true,
      ),
    ) {
      send(HabitsAction.ConfigDelete.ConfirmDeleteConfig)

      assertState { !isLoading }
      assertNoEffects()
    }

  @Test
  fun `when CancelDeleteConfig then hides delete confirm`() =
    habitsReducer.test(HabitsState(showDeleteConfigConfirm = true)) {
      send(HabitsAction.ConfigDelete.CancelDeleteConfig)

      assertState { !showDeleteConfigConfirm }
      assertNoEffects()
    }

  @Test
  fun `when RequestPrewarmAllRanges then sets initial loading and emits prewarm effect`() =
    habitsReducer.test(
      HabitsState(
        needsCacheRefresh = true,
        isInitialLoading = false,
      ),
    ) {
      val memos = listOf(Memo(name = "memos/1", content = "test"))
      send(HabitsAction.Cache.RequestPrewarmAllRanges(memos = memos, appMode = AppMode.ONLINE))

      assertState { !needsCacheRefresh && isInitialLoading }
      val effect = assertHasCommand<HabitsEffect.RunPrewarmAllRanges>()
      assertEquals(memos, effect.memos)
      assertEquals(AppMode.ONLINE, effect.appMode)
    }

  @Test
  fun `when UpdateActivityData then stores data in cache and clears recalculating flag`() =
    habitsReducer.test(
      HabitsState(
        activityDataCache = emptyMap(),
        isRecalculating = setOf(ActivityCacheKey(ActivityRange.Week(LocalDate(2024, 1, 1)), ActivityMode.HABITS, AppMode.ONLINE)),
      ),
    ) {
      val weekData = ActivityWeekData(weeks = emptyList(), maxDaily = 5, maxWeekly = 10)
      send(
        HabitsAction.Cache.UpdateActivityData(
          range = ActivityRange.Week(LocalDate(2024, 1, 1)),
          mode = ActivityMode.HABITS,
          appMode = AppMode.ONLINE,
          weekData = weekData,
          configTimeline = emptyList(),
          successRate = null,
        ),
      )

      assertState {
        activityDataCache.size == 1 &&
          activityDataCache.values.first().weekData == weekData &&
          isRecalculating.isEmpty()
      }
      assertNoEffects()
    }

  @Test
  fun `when InvalidateAllCache then clears cache and sets needsCacheRefresh`() =
    habitsReducer.test(
      HabitsState(
        activityDataCache =
          mapOf(
            ActivityCacheKey(ActivityRange.Week(LocalDate(2024, 1, 1)), ActivityMode.HABITS, AppMode.ONLINE) to
              CachedActivityData(ActivityWeekData(emptyList(), 0, 0), emptyList(), null),
          ),
        isRecalculating = setOf(ActivityCacheKey(ActivityRange.Week(LocalDate(2024, 1, 1)), ActivityMode.HABITS, AppMode.ONLINE)),
        needsCacheRefresh = false,
        isInitialLoading = false,
      ),
    ) {
      send(HabitsAction.Cache.InvalidateAllCache)

      assertState {
        activityDataCache.isEmpty() &&
          isRecalculating.isEmpty() &&
          needsCacheRefresh &&
          !isInitialLoading
      }
      assertNoEffects()
    }

  @Test
  fun `when InvalidateCache then marks key as recalculating and emits effect`() =
    habitsReducer.test(
      HabitsState(
        activityDataCache =
          mapOf(
            ActivityCacheKey(ActivityRange.Week(LocalDate(2024, 1, 1)), ActivityMode.HABITS, AppMode.ONLINE) to
              CachedActivityData(ActivityWeekData(emptyList(), 0, 0), emptyList(), null),
          ),
        isRecalculating = emptySet(),
        needsCacheRefresh = true,
      ),
    ) {
      val memos = listOf(Memo(name = "memos/1", content = "test"))
      send(
        HabitsAction.Cache.InvalidateCache(
          range = ActivityRange.Week(LocalDate(2024, 1, 1)),
          mode = ActivityMode.HABITS,
          appMode = AppMode.ONLINE,
          memos = memos,
        ),
      )

      assertState {
        isRecalculating.size == 1 &&
          isRecalculating.contains(ActivityCacheKey(ActivityRange.Week(LocalDate(2024, 1, 1)), ActivityMode.HABITS, AppMode.ONLINE)) &&
          !needsCacheRefresh
      }
      val effect = assertHasCommand<HabitsEffect.RecalculateActivityData>()
      assertEquals(ActivityRange.Week(LocalDate(2024, 1, 1)), effect.range)
      assertEquals(ActivityMode.HABITS, effect.mode)
      assertEquals(AppMode.ONLINE, effect.appMode)
      assertEquals(memos, effect.memos)
    }

  @Test
  fun `when PrewarmCompleted then clears initial loading flag`() =
    habitsReducer.test(
      HabitsState(
        isInitialLoading = true,
        needsCacheRefresh = true,
      ),
    ) {
      send(HabitsAction.Cache.PrewarmCompleted)

      assertState { !isInitialLoading && needsCacheRefresh }
      assertNoEffects()
    }
}
