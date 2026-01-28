package space.be1ski.vibits.shared.feature.onboarding.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.shared.core.ui.Indent
import space.be1ski.vibits.shared.generated.Res
import space.be1ski.vibits.shared.generated.action_start_tracking
import space.be1ski.vibits.shared.generated.hint_habit_name
import space.be1ski.vibits.shared.generated.label_habit_name
import space.be1ski.vibits.shared.generated.msg_habit_setup
import space.be1ski.vibits.shared.generated.title_habit_setup

@Composable
fun HabitSetupScreen(
  habitName: String,
  isCreating: Boolean,
  error: String?,
  onUpdateName: (String) -> Unit,
  onCreate: () -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .padding(Indent.xl),
    verticalArrangement = Arrangement.spacedBy(Indent.m),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth(),
    ) {
      IconButton(onClick = onBack, enabled = !isCreating) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
      }
      Text(
        text = stringResource(Res.string.title_habit_setup),
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.weight(1f),
      )
    }

    Text(
      text = stringResource(Res.string.msg_habit_setup),
      style = MaterialTheme.typography.bodyMedium,
    )

    Spacer(modifier = Modifier.height(Indent.s))

    TextField(
      value = habitName,
      onValueChange = onUpdateName,
      label = { Text(stringResource(Res.string.label_habit_name)) },
      placeholder = { Text(stringResource(Res.string.hint_habit_name)) },
      enabled = !isCreating,
      isError = error != null,
      supportingText =
        error?.let {
          { Text(it, color = MaterialTheme.colorScheme.error) }
        },
      modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.weight(1f))

    Button(
      onClick = onCreate,
      enabled = !isCreating && habitName.isNotBlank(),
      modifier = Modifier.fillMaxWidth(),
    ) {
      if (isCreating) {
        CircularProgressIndicator(
          modifier = Modifier.padding(end = Indent.xs),
        )
      }
      Text(stringResource(Res.string.action_start_tracking))
    }
  }
}
