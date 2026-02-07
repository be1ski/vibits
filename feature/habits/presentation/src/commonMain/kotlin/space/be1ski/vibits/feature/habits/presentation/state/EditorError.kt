package space.be1ski.vibits.feature.habits.presentation.state

sealed interface EditorError {
  data object NoHabitSelected : EditorError

  data class OperationFailed(
    val message: String,
  ) : EditorError
}
