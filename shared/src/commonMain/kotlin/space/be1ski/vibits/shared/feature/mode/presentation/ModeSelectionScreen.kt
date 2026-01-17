package space.be1ski.vibits.shared.feature.mode.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.shared.Res
import space.be1ski.vibits.shared.action_cancel
import space.be1ski.vibits.shared.action_save
import space.be1ski.vibits.shared.core.elm.Feature
import space.be1ski.vibits.shared.core.ui.Indent
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.hint_base_url
import space.be1ski.vibits.shared.label_access_token
import space.be1ski.vibits.shared.label_base_url
import space.be1ski.vibits.shared.mode_demo_desc
import space.be1ski.vibits.shared.mode_demo_title
import space.be1ski.vibits.shared.mode_offline_desc
import space.be1ski.vibits.shared.mode_offline_title
import space.be1ski.vibits.shared.mode_online_desc
import space.be1ski.vibits.shared.mode_online_title
import space.be1ski.vibits.shared.mode_select_subtitle
import space.be1ski.vibits.shared.mode_select_title
import space.be1ski.vibits.shared.msg_connection_failed
import space.be1ski.vibits.shared.msg_fill_all_fields

@Composable
fun ModeSelectionScreen(
  feature: Feature<ModeSelectionAction, ModeSelectionState, ModeSelectionEffect>,
) {
  val state by feature.state.collectAsState()
  val dispatch: (ModeSelectionAction) -> Unit = feature::send

  Column(
    modifier =
      Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(Indent.l),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Text(
      text = stringResource(Res.string.mode_select_title),
      style = MaterialTheme.typography.headlineMedium,
      color = MaterialTheme.colorScheme.onBackground,
    )
    Spacer(modifier = Modifier.height(Indent.xs))
    Text(
      text = stringResource(Res.string.mode_select_subtitle),
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(Indent.l))

    ModeCard(
      title = stringResource(Res.string.mode_online_title),
      description = stringResource(Res.string.mode_online_desc),
      isPrimary = true,
      onClick = { dispatch(ModeSelectionAction.ShowCredentialsDialog) },
    )

    Spacer(modifier = Modifier.height(Indent.m))

    ModeCard(
      title = stringResource(Res.string.mode_offline_title),
      description = stringResource(Res.string.mode_offline_desc),
      isPrimary = false,
      onClick = { dispatch(ModeSelectionAction.SelectMode(AppMode.OFFLINE)) },
    )

    Spacer(modifier = Modifier.height(Indent.m))

    ModeCard(
      title = stringResource(Res.string.mode_demo_title),
      description = stringResource(Res.string.mode_demo_desc),
      isPrimary = false,
      onClick = { dispatch(ModeSelectionAction.SelectMode(AppMode.DEMO)) },
    )
  }

  if (state.showCredentialsDialog) {
    CredentialsSetupDialog(
      state = state,
      dispatch = dispatch,
    )
  }
}

@Suppress("LongMethod")
@Composable
private fun CredentialsSetupDialog(
  state: ModeSelectionState,
  dispatch: (ModeSelectionAction) -> Unit,
) {
  val errorText = when (state.error) {
    ModeSelectionError.FILL_ALL_FIELDS -> stringResource(Res.string.msg_fill_all_fields)
    ModeSelectionError.CONNECTION_FAILED -> stringResource(Res.string.msg_connection_failed)
    null -> null
  }

  AlertDialog(
    onDismissRequest = { if (!state.isValidating) dispatch(ModeSelectionAction.DismissCredentialsDialog) },
    title = { Text(stringResource(Res.string.mode_online_title)) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(Indent.s)) {
        TextField(
          value = state.baseUrl,
          onValueChange = { dispatch(ModeSelectionAction.UpdateBaseUrl(it)) },
          label = { Text(stringResource(Res.string.label_base_url)) },
          placeholder = { Text(stringResource(Res.string.hint_base_url)) },
          enabled = !state.isValidating,
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
        )
        TextField(
          value = state.token,
          onValueChange = { dispatch(ModeSelectionAction.UpdateToken(it)) },
          label = { Text(stringResource(Res.string.label_access_token)) },
          visualTransformation = PasswordVisualTransformation(),
          enabled = !state.isValidating,
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
        )
        errorText?.let { error ->
          Text(
            text = error,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { dispatch(ModeSelectionAction.Submit) },
        enabled = !state.isValidating,
      ) {
        if (state.isValidating) {
          CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.onPrimary,
          )
        } else {
          Text(stringResource(Res.string.action_save))
        }
      }
    },
    dismissButton = {
      TextButton(
        onClick = { dispatch(ModeSelectionAction.DismissCredentialsDialog) },
        enabled = !state.isValidating,
      ) {
        Text(stringResource(Res.string.action_cancel))
      }
    },
  )
}

@Composable
private fun ModeCard(
  title: String,
  description: String,
  isPrimary: Boolean,
  onClick: () -> Unit,
) {
  OutlinedCard(
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier.padding(Indent.m),
      verticalArrangement = Arrangement.spacedBy(Indent.s),
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
      )
      Text(
        text = description,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      if (isPrimary) {
        Button(
          onClick = onClick,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(title)
        }
      } else {
        OutlinedButton(
          onClick = onClick,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(title)
        }
      }
    }
  }
}
