package space.be1ski.vibits.shared.feature.habits.presentation

/**
 * Side effects for the Habits feature.
 */
sealed interface HabitsEffect {
  data class CreateMemo(
    val content: String,
  ) : HabitsEffect

  data class UpdateMemo(
    val name: String,
    val content: String,
  ) : HabitsEffect

  data class DeleteMemo(
    val name: String,
  ) : HabitsEffect

  data object RefreshMemos : HabitsEffect
}
