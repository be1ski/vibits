package space.be1ski.vibits.feature.settings.presentation.state

import space.be1ski.vibits.core.platform.app.AppDetails
import space.be1ski.vibits.core.platform.locale.AppLanguage
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.settings.domain.model.AppTheme

data class SettingsState(
  val isOpen: Boolean = false,
  val editBaseUrl: String = "",
  val editToken: String = "",
  val appMode: AppMode = AppMode.NOT_SELECTED,
  val selectedLanguage: AppLanguage = AppLanguage.SYSTEM,
  val languageChanged: Boolean = false,
  val selectedTheme: AppTheme = AppTheme.SYSTEM,
  val isValidating: Boolean = false,
  val validationError: String? = null,
  val showResetConfirmation: Boolean = false,
  val isResetting: Boolean = false,
  val showLogsDialog: Boolean = false,
  val appDetails: AppDetails? = null,
  val pendingSave: Boolean = false,
)
