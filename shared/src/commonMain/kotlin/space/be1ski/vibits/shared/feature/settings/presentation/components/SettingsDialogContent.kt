@file:Suppress("TooManyFunctions")

package space.be1ski.vibits.shared.feature.settings.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.shared.Res
import space.be1ski.vibits.shared.action_cancel
import space.be1ski.vibits.shared.action_clear
import space.be1ski.vibits.shared.action_close
import space.be1ski.vibits.shared.action_copied
import space.be1ski.vibits.shared.action_export
import space.be1ski.vibits.shared.action_export_logs
import space.be1ski.vibits.shared.action_export_memos
import space.be1ski.vibits.shared.action_reset
import space.be1ski.vibits.shared.action_reset_app
import space.be1ski.vibits.shared.action_save
import space.be1ski.vibits.shared.action_view_logs
import space.be1ski.vibits.shared.core.logging.Log
import space.be1ski.vibits.shared.core.logging.LogLevel
import space.be1ski.vibits.shared.core.ui.SegmentedSelector
import space.be1ski.vibits.shared.data.export.ExportResult
import space.be1ski.vibits.shared.data.export.Exporter
import space.be1ski.vibits.shared.domain.model.app.AppDetails
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.settings.domain.model.AppLanguage
import space.be1ski.vibits.shared.feature.settings.domain.model.AppTheme
import space.be1ski.vibits.shared.feature.settings.presentation.SettingsAction
import space.be1ski.vibits.shared.feature.settings.presentation.SettingsState
import space.be1ski.vibits.shared.hint_base_url
import space.be1ski.vibits.shared.label_access_token
import space.be1ski.vibits.shared.label_app_mode
import space.be1ski.vibits.shared.label_base_url
import space.be1ski.vibits.shared.label_credentials
import space.be1ski.vibits.shared.label_environment
import space.be1ski.vibits.shared.label_language
import space.be1ski.vibits.shared.label_memos_db
import space.be1ski.vibits.shared.label_storage
import space.be1ski.vibits.shared.label_theme
import space.be1ski.vibits.shared.label_version
import space.be1ski.vibits.shared.language_arabic
import space.be1ski.vibits.shared.language_azerbaijani
import space.be1ski.vibits.shared.language_belarusian
import space.be1ski.vibits.shared.language_chinese
import space.be1ski.vibits.shared.language_english
import space.be1ski.vibits.shared.language_french
import space.be1ski.vibits.shared.language_georgian
import space.be1ski.vibits.shared.language_german
import space.be1ski.vibits.shared.language_hindi
import space.be1ski.vibits.shared.language_japanese
import space.be1ski.vibits.shared.language_kazakh
import space.be1ski.vibits.shared.language_kyrgyz
import space.be1ski.vibits.shared.language_portuguese
import space.be1ski.vibits.shared.language_romanian
import space.be1ski.vibits.shared.language_russian
import space.be1ski.vibits.shared.language_spanish
import space.be1ski.vibits.shared.language_system
import space.be1ski.vibits.shared.language_tajik
import space.be1ski.vibits.shared.language_turkmen
import space.be1ski.vibits.shared.language_ukrainian
import space.be1ski.vibits.shared.language_uzbek
import space.be1ski.vibits.shared.mode_demo_title
import space.be1ski.vibits.shared.mode_offline_title
import space.be1ski.vibits.shared.mode_online_title
import space.be1ski.vibits.shared.msg_connection_failed
import space.be1ski.vibits.shared.msg_export_failed
import space.be1ski.vibits.shared.msg_export_success
import space.be1ski.vibits.shared.msg_fill_all_fields
import space.be1ski.vibits.shared.msg_no_logs
import space.be1ski.vibits.shared.msg_reset_confirm
import space.be1ski.vibits.shared.msg_restart_required
import space.be1ski.vibits.shared.nav_settings
import space.be1ski.vibits.shared.theme_dark
import space.be1ski.vibits.shared.theme_light
import space.be1ski.vibits.shared.theme_system
import space.be1ski.vibits.shared.title_logs

@Composable
internal fun SettingsDialog(
  state: SettingsState,
  dispatch: (SettingsAction) -> Unit,
) {
  if (!state.isOpen) {
    return
  }
  AlertDialog(
    onDismissRequest = { dispatch(SettingsAction.Dismiss) },
    title = { Text(stringResource(Res.string.nav_settings)) },
    text = {
      SettingsDialogBody(state = state, dispatch = dispatch)
    },
    confirmButton = { SettingsDialogConfirmButton(dispatch) },
    dismissButton = { SettingsDialogDismissButton(dispatch) },
  )

  if (state.showLogsDialog) {
    LogsDialog(onDismiss = { dispatch(SettingsAction.CloseLogs) })
  }

  if (state.showResetConfirmation) {
    ResetConfirmationDialog(
      onConfirm = { dispatch(SettingsAction.ConfirmReset) },
      onDismiss = { dispatch(SettingsAction.CancelReset) },
    )
  }
}

@Suppress("LongMethod", "CyclomaticComplexMethod")
@Composable
private fun SettingsDialogBody(
  state: SettingsState,
  dispatch: (SettingsAction) -> Unit,
) {
  val validationErrorMessage =
    state.validationError?.let { errorKey ->
      when (errorKey) {
        "fill_all_fields" -> stringResource(Res.string.msg_fill_all_fields)
        "connection_failed" -> stringResource(Res.string.msg_connection_failed)
        else -> errorKey
      }
    }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    AppModeSelector(
      currentMode = state.appMode,
      isValidating = state.isValidating,
      onModeChange = { mode -> dispatch(SettingsAction.SelectMode(mode)) },
    )
    validationErrorMessage?.let { error ->
      Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
    SegmentedSelector(
      label = stringResource(Res.string.label_theme),
      options = AppTheme.entries,
      selected = state.selectedTheme,
      onSelect = { theme -> dispatch(SettingsAction.SelectTheme(theme)) },
      optionLabel = { theme ->
        when (theme) {
          AppTheme.SYSTEM -> stringResource(Res.string.theme_system)
          AppTheme.LIGHT -> stringResource(Res.string.theme_light)
          AppTheme.DARK -> stringResource(Res.string.theme_dark)
        }
      },
    )
    LanguageDropdown(
      selectedLanguage = state.selectedLanguage,
      onSelect = { language -> dispatch(SettingsAction.SelectLanguage(language)) },
    )
    if (state.languageChanged) {
      Text(
        text = stringResource(Res.string.msg_restart_required),
        color = MaterialTheme.colorScheme.tertiary,
        style = MaterialTheme.typography.bodySmall,
      )
    }
    if (state.appMode == AppMode.ONLINE) {
      TextField(
        value = state.editBaseUrl,
        onValueChange = { dispatch(SettingsAction.UpdateBaseUrl(it)) },
        label = { Text(stringResource(Res.string.label_base_url)) },
        placeholder = { Text(stringResource(Res.string.hint_base_url)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
      )
      TextField(
        value = state.editToken,
        onValueChange = { dispatch(SettingsAction.UpdateToken(it)) },
        label = { Text(stringResource(Res.string.label_access_token)) },
        visualTransformation = PasswordVisualTransformation(),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
      )
    }
    ActionsRow(
      showMemos = state.appMode == AppMode.OFFLINE,
      onOpenLogs = { dispatch(SettingsAction.OpenLogs) },
      onReset = { dispatch(SettingsAction.RequestReset) },
    )
    state.appDetails?.let { appDetails ->
      AppDetailsSection(appDetails, state.appMode)
    }
  }
}

@Composable
private fun AppModeSelector(
  currentMode: AppMode,
  isValidating: Boolean,
  onModeChange: (AppMode) -> Unit,
) {
  Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Text(stringResource(Res.string.label_app_mode))
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
      SegmentedButton(
        selected = currentMode == AppMode.ONLINE,
        onClick = { if (!isValidating) onModeChange(AppMode.ONLINE) },
        enabled = !isValidating,
        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
      ) {
        if (isValidating && currentMode != AppMode.ONLINE) {
          CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
        } else {
          Text(stringResource(Res.string.mode_online_title))
        }
      }
      SegmentedButton(
        selected = currentMode == AppMode.OFFLINE,
        onClick = { if (!isValidating) onModeChange(AppMode.OFFLINE) },
        enabled = !isValidating,
        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
      ) {
        Text(stringResource(Res.string.mode_offline_title))
      }
      SegmentedButton(
        selected = currentMode == AppMode.DEMO,
        onClick = { if (!isValidating) onModeChange(AppMode.DEMO) },
        enabled = !isValidating,
        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
      ) {
        Text(stringResource(Res.string.mode_demo_title))
      }
    }
  }
}

private const val TOAST_DURATION_MS = 1500L

@Suppress("DEPRECATION")
@Composable
private fun AppDetailsSection(
  appDetails: AppDetails,
  appMode: AppMode,
) {
  val clipboardManager = LocalClipboardManager.current
  val scope = rememberCoroutineScope()
  var copiedKey by remember { mutableStateOf<String?>(null) }
  val copiedLabel = stringResource(Res.string.action_copied)

  val onCopy: (String, String) -> Unit = { key, value ->
    clipboardManager.setText(AnnotatedString(value))
    copiedKey = key
    scope.launch {
      delay(TOAST_DURATION_MS)
      if (copiedKey == key) copiedKey = null
    }
  }

  Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    if (appMode != AppMode.DEMO) {
      CopyableInfoRow(
        label = stringResource(Res.string.label_environment),
        value = appDetails.environment,
        isCopied = copiedKey == "environment",
        copiedLabel = copiedLabel,
        onCopy = { onCopy("environment", it) },
      )
      if (appMode == AppMode.OFFLINE) {
        CopyableInfoRow(
          label = stringResource(Res.string.label_storage),
          value = appDetails.offlineStorage,
          displayValue = shortenPath(appDetails.offlineStorage),
          isCopied = copiedKey == "storage",
          copiedLabel = copiedLabel,
          onCopy = { onCopy("storage", it) },
        )
      } else {
        CopyableInfoRow(
          label = stringResource(Res.string.label_credentials),
          value = appDetails.credentialsStore,
          isCopied = copiedKey == "credentials",
          copiedLabel = copiedLabel,
          onCopy = { onCopy("credentials", it) },
        )
        CopyableInfoRow(
          label = stringResource(Res.string.label_memos_db),
          value = appDetails.memosDatabase,
          displayValue = shortenPath(appDetails.memosDatabase),
          isCopied = copiedKey == "memosDb",
          copiedLabel = copiedLabel,
          onCopy = { onCopy("memosDb", it) },
        )
      }
    }
    CopyableInfoRow(
      label = stringResource(Res.string.label_version),
      value = appDetails.version,
      isCopied = copiedKey == "version",
      copiedLabel = copiedLabel,
      onCopy = { onCopy("version", it) },
    )
  }
}

@Suppress("LongParameterList")
@Composable
private fun CopyableInfoRow(
  label: String,
  value: String,
  displayValue: String = value,
  isCopied: Boolean,
  copiedLabel: String,
  onCopy: (String) -> Unit,
) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(4.dp),
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.clickable { onCopy(value) },
  ) {
    Text(
      text =
        buildAnnotatedString {
          append("$label: ")
          withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append(displayValue)
          }
        },
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      maxLines = 1,
    )
    if (isCopied) {
      Text(
        text = copiedLabel,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
      )
    }
  }
}

private const val PATH_MAX_LENGTH = 30
private const val ELLIPSIS_LENGTH = 3

private fun shortenPath(path: String): String =
  if (path.length > PATH_MAX_LENGTH) {
    "..." + path.takeLast(PATH_MAX_LENGTH - ELLIPSIS_LENGTH)
  } else {
    path
  }

@Composable
private fun LanguageDropdown(
  selectedLanguage: AppLanguage,
  onSelect: (AppLanguage) -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }

  Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Text(stringResource(Res.string.label_language))
    Column {
      SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        SegmentedButton(
          selected = false,
          onClick = { expanded = true },
          shape = SegmentedButtonDefaults.itemShape(index = 0, count = 1),
          icon = {},
        ) {
          Text(getLanguageLabel(selectedLanguage))
          Icon(
            Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
        }
      }
      DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
      ) {
        AppLanguage.entries.forEach { language ->
          DropdownMenuItem(
            text = { Text(getLanguageLabel(language)) },
            onClick = {
              onSelect(language)
              expanded = false
            },
          )
        }
      }
    }
  }
}

@Suppress("CyclomaticComplexMethod")
@Composable
private fun getLanguageLabel(language: AppLanguage): String =
  when (language) {
    AppLanguage.SYSTEM -> stringResource(Res.string.language_system)
    AppLanguage.ENGLISH -> stringResource(Res.string.language_english)
    AppLanguage.SPANISH -> stringResource(Res.string.language_spanish)
    AppLanguage.CHINESE -> stringResource(Res.string.language_chinese)
    AppLanguage.HINDI -> stringResource(Res.string.language_hindi)
    AppLanguage.ARABIC -> stringResource(Res.string.language_arabic)
    AppLanguage.PORTUGUESE -> stringResource(Res.string.language_portuguese)
    AppLanguage.RUSSIAN -> stringResource(Res.string.language_russian)
    AppLanguage.UKRAINIAN -> stringResource(Res.string.language_ukrainian)
    AppLanguage.BELARUSIAN -> stringResource(Res.string.language_belarusian)
    AppLanguage.KAZAKH -> stringResource(Res.string.language_kazakh)
    AppLanguage.UZBEK -> stringResource(Res.string.language_uzbek)
    AppLanguage.GEORGIAN -> stringResource(Res.string.language_georgian)
    AppLanguage.AZERBAIJANI -> stringResource(Res.string.language_azerbaijani)
    AppLanguage.KYRGYZ -> stringResource(Res.string.language_kyrgyz)
    AppLanguage.TAJIK -> stringResource(Res.string.language_tajik)
    AppLanguage.ROMANIAN -> stringResource(Res.string.language_romanian)
    AppLanguage.TURKMEN -> stringResource(Res.string.language_turkmen)
    AppLanguage.JAPANESE -> stringResource(Res.string.language_japanese)
    AppLanguage.GERMAN -> stringResource(Res.string.language_german)
    AppLanguage.FRENCH -> stringResource(Res.string.language_french)
  }

@Composable
private fun SettingsDialogConfirmButton(dispatch: (SettingsAction) -> Unit) {
  Button(onClick = { dispatch(SettingsAction.Save) }) {
    Text(stringResource(Res.string.action_save))
  }
}

@Composable
private fun SettingsDialogDismissButton(dispatch: (SettingsAction) -> Unit) {
  TextButton(onClick = { dispatch(SettingsAction.Dismiss) }) {
    Text(stringResource(Res.string.action_cancel))
  }
}

@Composable
private fun ResetConfirmationDialog(
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(Res.string.action_reset_app)) },
    text = { Text(stringResource(Res.string.msg_reset_confirm)) },
    confirmButton = {
      Button(onClick = onConfirm) {
        Text(stringResource(Res.string.action_reset))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(Res.string.action_cancel))
      }
    },
  )
}

@Suppress("LongMethod")
@Composable
private fun ActionsRow(
  showMemos: Boolean,
  onOpenLogs: () -> Unit,
  onReset: () -> Unit,
) {
  val scope = rememberCoroutineScope()
  var exportExpanded by remember { mutableStateOf(false) }
  var exportStatus by remember { mutableStateOf<String?>(null) }
  val exportFailedMsg = stringResource(Res.string.msg_export_failed)
  val exportSuccessTemplate = stringResource(Res.string.msg_export_success, "%s")
  val exporter = remember { Exporter() }

  val onExport: (ExportResult) -> Unit = { result ->
    exportExpanded = false
    exportStatus =
      when (result) {
        is ExportResult.Success -> exportSuccessTemplate.replace("%s", result.filePath)
        ExportResult.Failure -> exportFailedMsg
      }
    scope.launch {
      delay(TOAST_DURATION_MS * 2)
      exportStatus = null
    }
  }

  Column {
    exportStatus?.let { status ->
      Text(
        text = status,
        style = MaterialTheme.typography.bodySmall,
        color =
          if (status == exportFailedMsg) {
            MaterialTheme.colorScheme.error
          } else {
            MaterialTheme.colorScheme.primary
          },
        modifier = Modifier.padding(bottom = 4.dp),
      )
    }
    Row(
      horizontalArrangement = Arrangement.SpaceEvenly,
      modifier = Modifier.fillMaxWidth(),
    ) {
      TextButton(onClick = onOpenLogs) {
        Text(stringResource(Res.string.action_view_logs))
      }
      TextButton(onClick = onReset) {
        Text(stringResource(Res.string.action_reset))
      }
      Column {
        TextButton(onClick = { exportExpanded = true }) {
          Text(stringResource(Res.string.action_export))
          Icon(
            Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
        }
        DropdownMenu(
          expanded = exportExpanded,
          onDismissRequest = { exportExpanded = false },
        ) {
          DropdownMenuItem(
            text = { Text(stringResource(Res.string.action_export_logs)) },
            onClick = { onExport(exporter.exportLogs()) },
          )
          if (showMemos) {
            DropdownMenuItem(
              text = { Text(stringResource(Res.string.action_export_memos)) },
              onClick = { onExport(exporter.exportMemos()) },
            )
          }
        }
      }
    }
  }
}

private const val LOG_TIMESTAMP_LENGTH = 8

@Suppress("LongMethod")
@Composable
private fun LogsDialog(onDismiss: () -> Unit) {
  val logs = Log.logs

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(Res.string.title_logs, logs.size)) },
    text = {
      LazyColumn(
        modifier =
          Modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        items(logs) { entry ->
          val bgColor =
            when (entry.level) {
              LogLevel.ERROR -> MaterialTheme.colorScheme.errorContainer
              LogLevel.WARN -> MaterialTheme.colorScheme.tertiaryContainer
              else -> MaterialTheme.colorScheme.surfaceVariant
            }
          Column(
            modifier =
              Modifier
                .fillMaxWidth()
                .background(bgColor, RoundedCornerShape(4.dp))
                .padding(6.dp),
          ) {
            val time = entry.timestamp.substringAfter('T').take(LOG_TIMESTAMP_LENGTH)
            val header = "$time ${entry.level.name.first()}/${entry.tag}"
            Text(
              text = header,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
              text = entry.message,
              style =
                MaterialTheme.typography.bodySmall.copy(
                  fontFamily = FontFamily.Monospace,
                  fontSize = 11.sp,
                ),
            )
          }
        }
        if (logs.isEmpty()) {
          item {
            Text(
              stringResource(Res.string.msg_no_logs),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = { Log.clear() }) {
        Text(stringResource(Res.string.action_clear))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(Res.string.action_close))
      }
    },
  )
}
