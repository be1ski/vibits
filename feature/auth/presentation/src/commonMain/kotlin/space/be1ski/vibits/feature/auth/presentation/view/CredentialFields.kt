package space.be1ski.vibits.feature.auth.presentation.view

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.core.strings.generated.Res
import space.be1ski.vibits.core.strings.generated.hint_base_url
import space.be1ski.vibits.core.strings.generated.label_access_token
import space.be1ski.vibits.core.strings.generated.label_base_url
import space.be1ski.vibits.core.strings.generated.msg_connection_failed
import space.be1ski.vibits.core.strings.generated.msg_fill_all_fields
import space.be1ski.vibits.feature.auth.domain.model.CredentialValidationError

/**
 * Emits two TextFields (base URL + access token) into the calling Column scope.
 * Does not wrap in its own Column so consumers control spacing and layout.
 */
@Composable
fun ColumnScope.CredentialFields(
  baseUrl: String,
  token: String,
  onBaseUrlChange: (String) -> Unit,
  onTokenChange: (String) -> Unit,
  enabled: Boolean = true,
) {
  TextField(
    value = baseUrl,
    onValueChange = onBaseUrlChange,
    label = { Text(stringResource(Res.string.label_base_url)) },
    placeholder = { Text(stringResource(Res.string.hint_base_url)) },
    enabled = enabled,
    modifier = Modifier.fillMaxWidth(),
    singleLine = true,
  )
  TextField(
    value = token,
    onValueChange = onTokenChange,
    label = { Text(stringResource(Res.string.label_access_token)) },
    visualTransformation = PasswordVisualTransformation(),
    enabled = enabled,
    modifier = Modifier.fillMaxWidth(),
    singleLine = true,
  )
}

@Composable
fun credentialValidationErrorMessage(error: CredentialValidationError): String =
  when (error) {
    CredentialValidationError.FILL_ALL_FIELDS -> stringResource(Res.string.msg_fill_all_fields)
    CredentialValidationError.CONNECTION_FAILED -> stringResource(Res.string.msg_connection_failed)
  }
