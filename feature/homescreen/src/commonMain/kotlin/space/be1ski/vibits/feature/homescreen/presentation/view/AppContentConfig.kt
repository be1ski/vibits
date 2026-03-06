package space.be1ski.vibits.feature.homescreen.presentation.view

import space.be1ski.vibits.core.platform.app.AppUpdater
import space.be1ski.vibits.core.platform.locale.AppLanguage
import space.be1ski.vibits.feature.changelog.domain.usecase.CheckForUpdateUseCase
import space.be1ski.vibits.feature.changelog.domain.usecase.GetChangelogUseCase
import space.be1ski.vibits.feature.memos.domain.repository.ExportService
import space.be1ski.vibits.feature.settings.domain.model.AppTheme

@Suppress("LongParameterList")
internal class AppContentConfig(
  val appTheme: AppTheme,
  val appLanguage: AppLanguage,
  val exportService: ExportService,
  val currentVersion: String,
  val getChangelog: GetChangelogUseCase,
  val checkForUpdate: CheckForUpdateUseCase?,
  val appUpdater: AppUpdater?,
)
