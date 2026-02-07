@file:Suppress("TooManyFunctions")

package space.be1ski.vibits.feature.settings.presentation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.core.platform.app.AppDetails
import space.be1ski.vibits.core.platform.locale.AppLanguage
import space.be1ski.vibits.core.platform.logging.LogLevel
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.core.strings.generated.Res
import space.be1ski.vibits.core.strings.generated.action_cancel
import space.be1ski.vibits.core.strings.generated.action_clear
import space.be1ski.vibits.core.strings.generated.action_close
import space.be1ski.vibits.core.strings.generated.action_copied
import space.be1ski.vibits.core.strings.generated.action_export
import space.be1ski.vibits.core.strings.generated.action_export_logs
import space.be1ski.vibits.core.strings.generated.action_export_memos
import space.be1ski.vibits.core.strings.generated.action_reset
import space.be1ski.vibits.core.strings.generated.action_reset_app
import space.be1ski.vibits.core.strings.generated.action_reset_settings_only
import space.be1ski.vibits.core.strings.generated.action_reset_with_memos
import space.be1ski.vibits.core.strings.generated.action_save
import space.be1ski.vibits.core.strings.generated.action_view_logs
import space.be1ski.vibits.core.strings.generated.label_app_mode
import space.be1ski.vibits.core.strings.generated.label_credentials
import space.be1ski.vibits.core.strings.generated.label_environment
import space.be1ski.vibits.core.strings.generated.label_language
import space.be1ski.vibits.core.strings.generated.label_memos_db
import space.be1ski.vibits.core.strings.generated.label_storage
import space.be1ski.vibits.core.strings.generated.label_theme
import space.be1ski.vibits.core.strings.generated.label_version
import space.be1ski.vibits.core.strings.generated.language_arabic
import space.be1ski.vibits.core.strings.generated.language_azerbaijani
import space.be1ski.vibits.core.strings.generated.language_belarusian
import space.be1ski.vibits.core.strings.generated.language_chinese
import space.be1ski.vibits.core.strings.generated.language_english
import space.be1ski.vibits.core.strings.generated.language_french
import space.be1ski.vibits.core.strings.generated.language_georgian
import space.be1ski.vibits.core.strings.generated.language_german
import space.be1ski.vibits.core.strings.generated.language_hindi
import space.be1ski.vibits.core.strings.generated.language_japanese
import space.be1ski.vibits.core.strings.generated.language_kazakh
import space.be1ski.vibits.core.strings.generated.language_kyrgyz
import space.be1ski.vibits.core.strings.generated.language_portuguese
import space.be1ski.vibits.core.strings.generated.language_romanian
import space.be1ski.vibits.core.strings.generated.language_russian
import space.be1ski.vibits.core.strings.generated.language_spanish
import space.be1ski.vibits.core.strings.generated.language_system
import space.be1ski.vibits.core.strings.generated.language_tajik
import space.be1ski.vibits.core.strings.generated.language_turkmen
import space.be1ski.vibits.core.strings.generated.language_ukrainian
import space.be1ski.vibits.core.strings.generated.language_uzbek
import space.be1ski.vibits.core.strings.generated.mode_demo_title
import space.be1ski.vibits.core.strings.generated.mode_offline_title
import space.be1ski.vibits.core.strings.generated.mode_online_title
import space.be1ski.vibits.core.strings.generated.msg_export_failed
import space.be1ski.vibits.core.strings.generated.msg_export_success
import space.be1ski.vibits.core.strings.generated.msg_no_logs
import space.be1ski.vibits.core.strings.generated.msg_reset_choose_option
import space.be1ski.vibits.core.strings.generated.msg_restart_required
import space.be1ski.vibits.core.strings.generated.nav_settings
import space.be1ski.vibits.core.strings.generated.theme_dark
import space.be1ski.vibits.core.strings.generated.theme_light
import space.be1ski.vibits.core.strings.generated.theme_system
import space.be1ski.vibits.core.strings.generated.title_logs
import space.be1ski.vibits.core.ui.Indent
import space.be1ski.vibits.core.ui.LogViewer
import space.be1ski.vibits.core.ui.SegmentedSelector
import space.be1ski.vibits.core.ui.form.CredentialFields
import space.be1ski.vibits.core.ui.form.credentialValidationErrorMessage
import space.be1ski.vibits.core.utils.logging.Log
import space.be1ski.vibits.feature.memos.domain.model.ExportResult
import space.be1ski.vibits.feature.memos.domain.repository.ExportService
import space.be1ski.vibits.feature.settings.domain.model.AppTheme
import space.be1ski.vibits.feature.settings.presentation.action.SettingsAction
import space.be1ski.vibits.feature.settings.presentation.state.SettingsState

@Composable
fun SettingsDialog(
  state: SettingsState,
  dispatch: (SettingsAction) -> Unit,
  exportService: ExportService,
) {
  if (!state.isOpen) {
    return
  }
  AlertDialog(
    onDismissRequest = { dispatch(SettingsAction.Dialog.Dismiss) },
    title = { Text(stringResource(Res.string.nav_settings)) },
    text = {
      SettingsDialogBody(state = state, dispatch = dispatch, exportService = exportService)
    },
    confirmButton = { SettingsDialogConfirmButton(dispatch) },
    dismissButton = { SettingsDialogDismissButton(dispatch) },
  )

  if (state.showLogsDialog) {
    LogsDialog(onDismiss = { dispatch(SettingsAction.SaveAndLogs.CloseLogs) })
  }

  if (state.showResetConfirmation) {
    ResetConfirmationDialog(
      onConfirm = { dispatch(SettingsAction.Reset.ConfirmReset) },
      onConfirmWithMemos = { dispatch(SettingsAction.Reset.ConfirmResetWithMemos) },
      onDismiss = { dispatch(SettingsAction.Reset.CancelReset) },
    )
  }
}

@Suppress("LongMethod", "CyclomaticComplexMethod")
@Composable
private fun SettingsDialogBody(
  state: SettingsState,
  dispatch: (SettingsAction) -> Unit,
  exportService: ExportService,
) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    AppModeSelector(
      currentMode = state.appMode,
      isValidating = state.isValidating,
      onModeChange = { mode -> dispatch(SettingsAction.Input.SelectMode(mode)) },
    )
    state.validationError?.let { error ->
      Text(
        credentialValidationErrorMessage(error),
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
      )
    }
    SegmentedSelector(
      label = stringResource(Res.string.label_theme),
      options = AppTheme.entries,
      selected = state.selectedTheme,
      onSelect = { theme -> dispatch(SettingsAction.Input.SelectTheme(theme)) },
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
      onSelect = { language -> dispatch(SettingsAction.Input.SelectLanguage(language)) },
    )
    if (state.languageChanged) {
      Text(
        text = stringResource(Res.string.msg_restart_required),
        color = MaterialTheme.colorScheme.tertiary,
        style = MaterialTheme.typography.bodySmall,
      )
    }
    if (state.appMode == AppMode.ONLINE) {
      CredentialFields(
        baseUrl = state.editBaseUrl,
        token = state.editToken,
        onBaseUrlChange = { dispatch(SettingsAction.Input.UpdateBaseUrl(it)) },
        onTokenChange = { dispatch(SettingsAction.Input.UpdateToken(it)) },
      )
    }
    ActionsRow(
      showMemos = state.appMode == AppMode.OFFLINE,
      onOpenLogs = { dispatch(SettingsAction.SaveAndLogs.OpenLogs) },
      onReset = { dispatch(SettingsAction.Reset.RequestReset) },
      exportService = exportService,
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
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(getLanguageLabel(selectedLanguage))
            Icon(
              Icons.Default.KeyboardArrowDown,
              contentDescription = null,
              modifier = Modifier.padding(start = Indent.x3s).size(18.dp),
            )
          }
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
  Button(onClick = { dispatch(SettingsAction.SaveAndLogs.Save) }) {
    Text(stringResource(Res.string.action_save))
  }
}

@Composable
private fun SettingsDialogDismissButton(dispatch: (SettingsAction) -> Unit) {
  TextButton(onClick = { dispatch(SettingsAction.Dialog.Dismiss) }) {
    Text(stringResource(Res.string.action_cancel))
  }
}

@Composable
private fun ResetConfirmationDialog(
  onConfirm: () -> Unit,
  onConfirmWithMemos: () -> Unit,
  onDismiss: () -> Unit,
) {
  ResetOptionsDialog(
    onResetSettings = onConfirm,
    onResetAll = onConfirmWithMemos,
    onDismiss = onDismiss,
  )
}

@Suppress("LongMethod")
@Composable
private fun ActionsRow(
  showMemos: Boolean,
  onOpenLogs: () -> Unit,
  onReset: () -> Unit,
  exportService: ExportService,
) {
  val scope = rememberCoroutineScope()
  var exportExpanded by remember { mutableStateOf(false) }
  var exportStatus by remember { mutableStateOf<String?>(null) }
  val exportFailedMsg = stringResource(Res.string.msg_export_failed)
  val exportSuccessTemplate = stringResource(Res.string.msg_export_success, "%s")

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
            onClick = { onExport(exportService.exportLogs()) },
          )
          if (showMemos) {
            DropdownMenuItem(
              text = { Text(stringResource(Res.string.action_export_memos)) },
              onClick = { onExport(exportService.exportMemos()) },
            )
          }
        }
      }
    }
  }
}

@Suppress("LongMethod")
@Composable
private fun ResetOptionsDialog(
  onResetSettings: () -> Unit,
  onResetAll: () -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(Res.string.action_reset_app)) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(Indent.s)) {
        Text(
          text = stringResource(Res.string.msg_reset_choose_option),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedButton(
          onClick = {
            onResetSettings()
            onDismiss()
          },
          modifier = Modifier.fillMaxWidth(),
        ) {
          Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = null,
            modifier = Modifier.size(Indent.m),
          )
          Text(
            text = stringResource(Res.string.action_reset_settings_only),
            modifier = Modifier.padding(start = Indent.xs),
          )
        }
        Button(
          onClick = {
            onResetAll()
            onDismiss()
          },
          modifier = Modifier.fillMaxWidth(),
          colors =
            ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.error,
              contentColor = MaterialTheme.colorScheme.onError,
            ),
        ) {
          Icon(
            imageVector = Icons.Default.DeleteForever,
            contentDescription = null,
            modifier = Modifier.size(Indent.m),
          )
          Text(
            text = stringResource(Res.string.action_reset_with_memos),
            modifier = Modifier.padding(start = Indent.xs),
          )
        }
      }
    },
    confirmButton = {},
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(Res.string.action_cancel))
      }
    },
  )
}

@Composable
private fun LogsDialog(onDismiss: () -> Unit) {
  var logs by remember { mutableStateOf(Log.logs) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(Res.string.title_logs, logs.size)) },
    text = {
      LogViewer(
        logs = logs,
        emptyMessage = stringResource(Res.string.msg_no_logs),
      )
    },
    confirmButton = {
      TextButton(
        onClick = {
          Log.clear()
          logs = emptyList()
        },
      ) {
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
