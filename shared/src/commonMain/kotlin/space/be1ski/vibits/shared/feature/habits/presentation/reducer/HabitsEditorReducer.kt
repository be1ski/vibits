package space.be1ski.vibits.shared.feature.habits.presentation.reducer

import kotlinx.datetime.TimeZone
import space.be1ski.vibits.shared.core.elm.ReducerResult
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.feature.habits.domain.buildDailyContent
import space.be1ski.vibits.shared.feature.habits.domain.buildHabitStatuses
import space.be1ski.vibits.shared.feature.habits.domain.buildHabitsEditorSelections
import space.be1ski.vibits.shared.feature.habits.domain.model.ContributionDay
import space.be1ski.vibits.shared.feature.habits.domain.model.DailyMemoInfo
import space.be1ski.vibits.shared.feature.habits.domain.usecase.parseDailyDateFromContent
import space.be1ski.vibits.shared.feature.habits.domain.usecase.parseMemoDate
import space.be1ski.vibits.shared.feature.habits.presentation.action.HabitsAction
import space.be1ski.vibits.shared.feature.habits.presentation.effect.HabitsEffect
import space.be1ski.vibits.shared.feature.habits.presentation.state.HabitsState

/**
 * Sub-reducer for editor lifecycle and interactions.
 */
@Suppress("LongMethod", "CyclomaticComplexMethod")
internal fun editorReducer(
  action: HabitsAction.Editor,
  state: HabitsState,
): ReducerResult<HabitsState, HabitsEffect, Nothing> =
  reducer<HabitsAction.Editor, HabitsState, HabitsEffect, Nothing> { a, s ->
    when (a) {
      is HabitsAction.Editor.OpenEditor -> {
        val day =
          when {
            a.day != null -> a.day
            a.memo != null -> {
              val timeZone = TimeZone.currentSystemDefault()
              val date =
                parseDailyDateFromContent(a.memo.content)
                  ?: parseMemoDate(a.memo, timeZone)
                  ?: return@reducer
              val habitStatuses = buildHabitStatuses(a.memo.content, a.config)
              val completedCount = habitStatuses.count { it.done }
              ContributionDay(
                date = date,
                count = completedCount,
                totalHabits = a.config.size,
                completionRatio = if (a.config.isNotEmpty()) completedCount.toFloat() / a.config.size else 0f,
                habitStatuses = habitStatuses,
                dailyMemo = DailyMemoInfo(name = a.memo.name, content = a.memo.content),
                inRange = true,
                isClickable = true,
              )
            }
            else -> return@reducer
          }
        val selections = buildHabitsEditorSelections(day, a.config)
        state {
          copy(
            editorDay = day,
            editorConfig = a.config,
            editorSelections = selections,
            editorExisting = day.dailyMemo,
            editorError = null,
            showDeleteConfirm = false,
          )
        }
      }

      is HabitsAction.Editor.CloseEditor -> {
        state {
          copy(
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
        state {
          copy(editorSelections = editorSelections + (a.tag to a.checked))
        }
      }

      is HabitsAction.Editor.ConfirmEditor -> {
        val hasSelection = s.editorSelections.values.any { it }
        when {
          !hasSelection && s.editorExisting != null -> {
            state { copy(showDeleteConfirm = true) }
          }
          !hasSelection -> {
            state { copy(editorError = "Select at least one habit.") }
          }
          else -> {
            val day = s.editorDay ?: return@reducer
            val content = buildDailyContent(day.date, s.editorConfig, s.editorSelections)
            val existing = s.editorExisting

            state { copy(isLoading = true, editorError = null) }

            if (existing != null) {
              command(HabitsEffect.UpdateMemo(existing.name, content))
            } else {
              command(HabitsEffect.CreateMemo(content))
            }
          }
        }
      }

      is HabitsAction.Editor.RequestDelete -> {
        state { copy(showDeleteConfirm = true) }
      }

      is HabitsAction.Editor.ConfirmDelete -> {
        val existing = s.editorExisting ?: return@reducer
        state { copy(isLoading = true) }
        command(HabitsEffect.DeleteMemo(existing.name))
      }

      is HabitsAction.Editor.CancelDelete -> {
        state { copy(showDeleteConfirm = false) }
      }
    }
  }(action, state)
