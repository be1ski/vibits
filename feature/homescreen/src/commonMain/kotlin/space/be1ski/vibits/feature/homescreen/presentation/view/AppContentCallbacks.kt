package space.be1ski.vibits.feature.homescreen.presentation.view

import space.be1ski.vibits.core.platform.locale.AppLanguage
import space.be1ski.vibits.feature.settings.domain.model.AppTheme

internal class AppContentCallbacks(
  val onResetApp: () -> Unit,
  val onThemeChanged: (AppTheme) -> Unit,
  val onLanguageChanged: (AppLanguage) -> Unit,
)
