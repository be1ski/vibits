package space.be1ski.vibits.feature.mode.presentation.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.core.elm.Feature
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.core.strings.generated.Res
import space.be1ski.vibits.core.strings.generated.action_cancel
import space.be1ski.vibits.core.strings.generated.action_save
import space.be1ski.vibits.core.strings.generated.action_use_saved
import space.be1ski.vibits.core.strings.generated.mode_demo_desc
import space.be1ski.vibits.core.strings.generated.mode_demo_title
import space.be1ski.vibits.core.strings.generated.mode_offline_desc
import space.be1ski.vibits.core.strings.generated.mode_offline_title
import space.be1ski.vibits.core.strings.generated.mode_online_desc
import space.be1ski.vibits.core.strings.generated.mode_online_title
import space.be1ski.vibits.core.strings.generated.mode_quick_online_desc
import space.be1ski.vibits.core.strings.generated.mode_quick_online_title
import space.be1ski.vibits.core.strings.generated.mode_select_subtitle
import space.be1ski.vibits.core.strings.generated.mode_select_title
import space.be1ski.vibits.core.strings.generated.msg_connection_failed_hint
import space.be1ski.vibits.core.ui.Indent
import space.be1ski.vibits.core.ui.form.CredentialFields
import space.be1ski.vibits.core.ui.form.CredentialValidationError
import space.be1ski.vibits.core.ui.form.credentialValidationErrorMessage
import space.be1ski.vibits.feature.mode.presentation.action.ModeSelectionAction
import space.be1ski.vibits.feature.mode.presentation.effect.ModeSelectionEffect
import space.be1ski.vibits.feature.mode.presentation.state.ModeSelectionState

@Composable
fun ModeSelectionScreen(
  feature: Feature<ModeSelectionAction, ModeSelectionState, ModeSelectionEffect.Command, ModeSelectionEffect.Notification>,
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
      icon = Icons.Outlined.Cloud,
      title = stringResource(Res.string.mode_online_title),
      description = stringResource(Res.string.mode_online_desc),
      isPrimary = true,
      onClick = { dispatch(ModeSelectionAction.Dialog.Show) },
      modifier = Modifier.testTag(ModeSelectionTestTags.ONLINE_CARD),
      buttonTag = ModeSelectionTestTags.ONLINE_BUTTON,
    )

    Spacer(modifier = Modifier.height(Indent.m))

    ModeCard(
      icon = Icons.Outlined.PhoneAndroid,
      title = stringResource(Res.string.mode_offline_title),
      description = stringResource(Res.string.mode_offline_desc),
      isPrimary = false,
      onClick = { dispatch(ModeSelectionAction.Selection.SelectMode(AppMode.OFFLINE)) },
      modifier = Modifier.testTag(ModeSelectionTestTags.OFFLINE_CARD),
      buttonTag = ModeSelectionTestTags.OFFLINE_BUTTON,
    )

    Spacer(modifier = Modifier.height(Indent.m))

    ModeCard(
      icon = Icons.Outlined.PlayCircle,
      title = stringResource(Res.string.mode_demo_title),
      description = stringResource(Res.string.mode_demo_desc),
      isPrimary = false,
      onClick = { dispatch(ModeSelectionAction.Selection.SelectMode(AppMode.DEMO)) },
      modifier = Modifier.testTag(ModeSelectionTestTags.DEMO_CARD),
      buttonTag = ModeSelectionTestTags.DEMO_BUTTON,
    )
  }

  ModeSelectionDialogs(state = state, dispatch = dispatch)
}

@Composable
private fun ModeSelectionDialogs(
  state: ModeSelectionState,
  dispatch: (ModeSelectionAction) -> Unit,
) {
  if (state.showQuickOnlineDialog) {
    QuickOnlineDialog(state = state, dispatch = dispatch)
  }
  if (state.showCredentialsDialog) {
    CredentialsSetupDialog(state = state, dispatch = dispatch)
  }
}

@Composable
private fun QuickOnlineDialog(
  state: ModeSelectionState,
  dispatch: (ModeSelectionAction) -> Unit,
) {
  AlertDialog(
    onDismissRequest = { if (!state.isValidating) dispatch(ModeSelectionAction.QuickOnline.Dismiss) },
    modifier = Modifier.testTag(ModeSelectionTestTags.QUICK_ONLINE_DIALOG),
    title = { Text(stringResource(Res.string.mode_quick_online_title)) },
    text = { Text(stringResource(Res.string.mode_quick_online_desc)) },
    confirmButton = {
      Button(
        onClick = { dispatch(ModeSelectionAction.QuickOnline.UseStoredCredentials) },
        enabled = !state.isValidating,
      ) {
        if (state.isValidating) {
          CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.onPrimary,
          )
        } else {
          Text(stringResource(Res.string.action_use_saved))
        }
      }
    },
    dismissButton = {
      TextButton(
        onClick = { dispatch(ModeSelectionAction.QuickOnline.Dismiss) },
        enabled = !state.isValidating,
      ) {
        Text(stringResource(Res.string.action_cancel))
      }
    },
  )
}

@Composable
private fun CredentialsSetupDialog(
  state: ModeSelectionState,
  dispatch: (ModeSelectionAction) -> Unit,
) {
  AlertDialog(
    onDismissRequest = { if (!state.isValidating) dispatch(ModeSelectionAction.Dialog.Dismiss) },
    modifier = Modifier.testTag(ModeSelectionTestTags.CREDENTIALS_DIALOG),
    title = { Text(stringResource(Res.string.mode_online_title)) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(Indent.s)) {
        CredentialFields(
          baseUrl = state.baseUrl,
          token = state.token,
          onBaseUrlChange = { dispatch(ModeSelectionAction.Input.UpdateBaseUrl(it)) },
          onTokenChange = { dispatch(ModeSelectionAction.Input.UpdateToken(it)) },
          enabled = !state.isValidating,
        )
        state.error?.let { error ->
          Text(
            text = credentialValidationErrorMessage(error),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
          )
          if (error == CredentialValidationError.CONNECTION_FAILED) {
            Text(
              text = stringResource(Res.string.msg_connection_failed_hint),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { dispatch(ModeSelectionAction.Validation.Submit) },
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
        onClick = { dispatch(ModeSelectionAction.Dialog.Dismiss) },
        enabled = !state.isValidating,
      ) {
        Text(stringResource(Res.string.action_cancel))
      }
    },
  )
}

@Composable
private fun ModeCard(
  icon: ImageVector,
  title: String,
  description: String,
  isPrimary: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  buttonTag: String? = null,
) {
  OutlinedCard(
    modifier = modifier.fillMaxWidth(),
    colors =
      if (isPrimary) {
        CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = CARD_HIGHLIGHT_ALPHA))
      } else {
        CardDefaults.outlinedCardColors()
      },
    border =
      if (isPrimary) {
        BorderStroke(CARD_BORDER_WIDTH, MaterialTheme.colorScheme.primary.copy(alpha = CARD_BORDER_ALPHA))
      } else {
        CardDefaults.outlinedCardBorder()
      },
  ) {
    Column(
      modifier = Modifier.padding(Indent.m),
      verticalArrangement = Arrangement.spacedBy(Indent.s),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Indent.s),
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          modifier = Modifier.size(Indent.xl),
          tint = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
        )
      }
      Text(
        text = description,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      val buttonModifier = Modifier.fillMaxWidth().then(buttonTag?.let { Modifier.testTag(it) } ?: Modifier)
      if (isPrimary) {
        Button(onClick = onClick, modifier = buttonModifier) { Text(title) }
      } else {
        OutlinedButton(onClick = onClick, modifier = buttonModifier) { Text(title) }
      }
    }
  }
}

private const val CARD_HIGHLIGHT_ALPHA = 0.15f
private const val CARD_BORDER_ALPHA = 0.5f
private val CARD_BORDER_WIDTH = 1.5.dp
