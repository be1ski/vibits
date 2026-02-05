package space.be1ski.vibits.feature.habits.presentation.reducer

import kotlinx.datetime.TimeZone
import space.be1ski.vibits.core.elm.Reducer
import space.be1ski.vibits.core.elm.reducer
import space.be1ski.vibits.feature.habits.domain.buildDailyContent
import space.be1ski.vibits.feature.habits.domain.buildHabitStatuses
import space.be1ski.vibits.feature.habits.domain.buildHabitsEditorSelections
import space.be1ski.vibits.feature.habits.domain.model.ContributionDay
import space.be1ski.vibits.feature.habits.domain.model.DailyMemo
import space.be1ski.vibits.feature.habits.domain.usecase.parseDailyDateFromContent
import space.be1ski.vibits.feature.habits.domain.usecase.parseMemoDate
import space.be1ski.vibits.feature.habits.presentation.action.HabitsAction
import space.be1ski.vibits.feature.habits.presentation.effect.HabitsEffect
import space.be1ski.vibits.feature.habits.presentation.state.HabitsState

/**
 * Sub-reducer for editor lifecycle and interactions.
 */
internal val editorReducer: Reducer<HabitsAction.Editor, HabitsState, HabitsEffect, Nothing> =
  reducer { action, state ->
    when (action) {
      is HabitsAction.Editor.OpenEditor -> {
        val day =
          when {
            action.day != null -> action.day
            action.memo != null -> {
              val timeZone = TimeZone.currentSystemDefault()
              val date =
                parseDailyDateFromContent(action.memo.content)
                  ?: parseMemoDate(action.memo, timeZone)
                  ?: return@reducer
              val habitStatuses = buildHabitStatuses(action.memo.content, action.config)
              val completedCount = habitStatuses.count { it.done }
              ContributionDay(
                date = date,
                count = completedCount,
                totalHabits = action.config.size,
                completionRatio = if (action.config.isNotEmpty()) completedCount.toFloat() / action.config.size else 0f,
                habitStatuses = habitStatuses,
                dailyMemo = DailyMemo(name = action.memo.name, content = action.memo.content),
                inRange = true,
                isClickable = true,
              )
            }
            else -> return@reducer
          }
        val selections = buildHabitsEditorSelections(day, action.config)
        state {
          state.copy(
            editorDay = day,
            editorConfig = action.config,
            editorSelections = selections,
            editorExisting = day.dailyMemo,
            editorError = null,
            showDeleteConfirm = false,
          )
        }
      }

      is HabitsAction.Editor.CloseEditor -> {
        state {
          state.copy(
            editorDay = null,
            editorConfig = emptyList(),
            editorSelections = emptyMap(),
            editorExisting = null,
            editorError = null,
            showDeleteConfirm = false,
          )
        }
      }

      is HabitsAction.Editor.ToggleHabit -> {
        state { state.copy(editorSelections = state.editorSelections + (action.tag to action.checked)) }
      }

      is HabitsAction.Editor.ConfirmEditor -> {
        val hasSelection = state.editorSelections.values.any { it }
        when {
          !hasSelection && state.editorExisting != null -> {
            state { state.copy(showDeleteConfirm = true) }
          }
          !hasSelection -> {
            state { state.copy(editorError = "Select at least one habit.") }
          }
          else -> {
            val day = state.editorDay ?: return@reducer
            val content = buildDailyContent(day.date, state.editorConfig, state.editorSelections)
            val existing = state.editorExisting

            state { state.copy(isLoading = true, editorError = null) }

            if (existing != null) {
              command(HabitsEffect.UpdateMemo(existing.name, content))
            } else {
              command(HabitsEffect.CreateMemo(content))
            }
          }
        }
      }

      is HabitsAction.Editor.RequestDelete -> {
        state { state.copy(showDeleteConfirm = true) }
      }

      is HabitsAction.Editor.ConfirmDelete -> {
        val existing = state.editorExisting ?: return@reducer
        state { state.copy(isLoading = true) }
        command(HabitsEffect.DeleteMemo(existing.name))
      }

      is HabitsAction.Editor.CancelDelete -> {
        state { state.copy(showDeleteConfirm = false) }
      }
    }
  }
