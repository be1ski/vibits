package space.be1ski.vibits.feature.homescreen.presentation.view

@Suppress("LongParameterList")
internal class SidebarCallbacks(
  val onClearSelection: () -> Unit,
  val onFeedScrollToTop: () -> Unit,
  val onOpenTodayEditor: () -> Unit,
  val onOpenConfigDialog: () -> Unit,
  val onShowCreateMemoDialog: () -> Unit,
  val onSettingsClick: () -> Unit,
  val onUpgrade: () -> Unit,
  val onRestart: () -> Unit,
)
