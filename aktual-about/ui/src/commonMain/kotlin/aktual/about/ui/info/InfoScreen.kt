package aktual.about.ui.info

import aktual.about.vm.AboutViewModel
import aktual.about.vm.BuildState
import aktual.core.icons.AktualIcons
import aktual.core.icons.Calendar3
import aktual.core.icons.Cloud
import aktual.core.icons.material.Apps
import aktual.core.icons.material.Build
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.OpenInNew
import aktual.core.l10n.Drawables
import aktual.core.l10n.Strings
import aktual.core.model.AktualVersions
import aktual.core.nav.BackNavigator
import aktual.core.nav.CrashReportsNavigator
import aktual.core.nav.LicensesNavigator
import aktual.core.nav.LogsNavigator
import aktual.core.nav.ManageStorageNavigator
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.textSizes
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BottomSpacing
import aktual.core.ui.CardColumn
import aktual.core.ui.CardRow
import aktual.core.ui.ColorParameters
import aktual.core.ui.Dimens
import aktual.core.ui.HazedTopBarState
import aktual.core.ui.LandscapePreview
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.RounderCardShape
import aktual.core.ui.TabletPreview
import aktual.core.ui.WavyBackground
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.isCompactWidth
import aktual.core.ui.padding
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.transparentTopAppBarColors
import aktual.core.ui.verticalScrollWithBar
import alakazam.compose.HorizontalSpacer
import alakazam.kotlin.noOp
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
fun InfoScreen(
  back: BackNavigator,
  toLicenses: LicensesNavigator,
  toManageStorage: ManageStorageNavigator,
  toLogs: LogsNavigator,
  toCrashReports: CrashReportsNavigator,
  viewModel: AboutViewModel = metroViewModel(),
) {
  val uriHandler = LocalUriHandler.current
  val buildState by viewModel.buildState.collectAsStateWithLifecycle()
  val checkUpdatesState by viewModel.checkUpdatesState.collectAsStateWithLifecycle()
  val onCancel = { viewModel.cancelUpdateCheck() }

  when (val state = checkUpdatesState) {
    Inactive -> noOp()
    Checking -> CheckUpdatesLoadingDialog(onCancel)
    NoUpdateFound -> NoUpdateFoundDialog(onCancel)
    is Failed -> UpdateCheckFailedDialog(state.cause, onCancel)
    is UpdateFound ->
      UpdateFoundDialog(
        currentVersion = buildState.versions.app,
        latestVersion = state.version,
        latestUrl = state.url,
        onDismiss = onCancel,
        onOpenUrl = uriHandler::openUri,
      )
  }

  InfoScaffold(
    buildState = buildState,
    onAction = { action ->
      when (action) {
        ReportIssue -> uriHandler.openUri(viewModel.reportIssueUrl)
        CheckUpdates -> viewModel.fetchLatestRelease()
        NavBack -> back()
        ViewLicenses -> toLicenses()
        ManageStorage -> toManageStorage()
        AppLogs -> toLogs()
        ViewCrashReports -> toCrashReports()
        is OpenUrl -> uriHandler.openUri(action.url)
      }
    },
  )
}

@Composable
internal fun InfoScaffold(
  buildState: BuildState,
  onAction: InfoActionHandler,
  modifier: Modifier = Modifier,
) {
  val hazeState = rememberHazedTopBarState()
  val scrollState = rememberScrollState()

  Scaffold(
    modifier = modifier,
    topBar = { InfoTopBar(hazeState, scrollState, onAction) },
  ) { innerPadding ->
    Box {
      WavyBackground()
      InfoScreenContent(
        modifier = Modifier.hazedTopBarContent(hazeState, innerPadding),
        contentPadding = hazedTopBarContentPadding(hazeState, innerPadding),
        scrollState = scrollState,
        buildState = buildState,
        onAction = onAction,
      )
    }
  }
}

@Composable
private fun InfoTopBar(
  hazeState: HazedTopBarState,
  scrollState: ScrollState,
  onAction: InfoActionHandler,
) {
  TopAppBar(
    modifier = Modifier.hazedTopBar(hazeState, scrollOffset = { scrollState.value.toFloat() }),
    colors = colors.transparentTopAppBarColors(),
    navigationIcon = { NavBackIconButton { onAction(NavBack) } },
    title = {
      Text(text = Strings.infoToolbarTitle, maxLines = 1, overflow = Ellipsis)
    },
  )
}

@Composable
private fun InfoScreenContent(
  contentPadding: PaddingValues,
  scrollState: ScrollState,
  buildState: BuildState,
  onAction: InfoActionHandler,
  modifier: Modifier = Modifier,
) =
  BoxWithConstraints(modifier = modifier) {
    val contentMaxWidth = if (isCompactWidth()) maxWidth else maxWidth / 2
    val contentModifier = Modifier.widthIn(max = contentMaxWidth)

    Column(
      modifier =
        Modifier.fillMaxWidth()
          .verticalScrollWithBar(scrollState)
          .padding(contentPadding)
          .padding(Dimens.Large),
      horizontalAlignment = CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(Dimens.Medium),
    ) {
      InfoHeader(buildState.year, contentModifier)
      InfoBuildState(buildState, onAction, contentModifier)
      InfoButtons(onAction, contentModifier)

      BottomSpacing()
    }
  }

@Composable
private fun InfoHeader(year: Int, modifier: Modifier = Modifier) {
  CardRow(
    modifier = modifier.fillMaxWidth(),
    shape = RounderCardShape,
    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = CenterVertically,
  ) {
    val appName = Strings.appName

    Image(
      modifier = Modifier.size(50.dp),
      painter = Drawables.appIcon192,
      contentDescription = appName,
    )

    HorizontalSpacer(10.dp)

    Column(
      horizontalAlignment = CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Text(
        text = appName,
        fontSize = textSizes.heading,
        fontWeight = W700,
        color = colors.pillTextHighlighted,
      )
      Text(text = Strings.infoSubtitle1(year), color = colors.pillText)
      Text(text = Strings.infoSubtitle2, color = colors.pillText)
    }
  }
}

@Composable
private fun InfoBuildState(
  buildState: BuildState,
  onAction: InfoActionHandler,
  modifier: Modifier = Modifier,
) {
  CardColumn(
    modifier = modifier,
    shape = RounderCardShape,
    contentPadding = Dimens.Large.padding,
    verticalArrangement = Arrangement.spacedBy(Dimens.Medium),
  ) {
    BuildStateItem(
      icon = MaterialIcons.Apps,
      title = Strings.infoAppVersion,
      subtitle = buildState.versions.app,
      onClick = { onAction(OpenUrl(buildState.appSourceUrl)) },
    )

    BuildStateItem(
      modifier = Modifier.testTag(Tags.ServerVersionText),
      icon = AktualIcons.Cloud,
      title = Strings.infoServerVersion,
      subtitle = buildState.versions.server ?: Strings.infoServerVersionUnknown,
      onClick = { onAction(OpenUrl(buildState.serverSourceUrl)) },
    )

    BuildStateItem(
      icon = AktualIcons.Calendar3,
      title = Strings.infoDate,
      subtitle = buildState.buildDate,
    )

    buildState.buildType?.let { buildType ->
      BuildStateItem(
        icon = MaterialIcons.Build,
        title = Strings.infoBuildType,
        subtitle = buildType,
      )
    }
  }
}

@Composable
private fun BuildStateItem(
  icon: ImageVector,
  title: String,
  subtitle: String,
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .height(ItemHeight)
        .clickable(
          enabled = onClick != null,
          role = Button,
          onClick = { onClick?.invoke() },
        )
        .padding(horizontal = ItemPadding),
    verticalAlignment = CenterVertically,
  ) {
    Icon(
      modifier = Modifier.padding(ItemPadding),
      imageVector = icon,
      contentDescription = title,
      tint = colors.pillText,
    )

    Column(modifier = Modifier.weight(1f).padding(ItemPadding)) {
      Text(
        modifier = Modifier.testTag(Tags.BuildStateItemTitle),
        text = title,
        style = typography.bodyLarge,
        color = colors.pillText,
      )
      Text(
        modifier = Modifier.testTag(Tags.BuildStateItemValue),
        text = subtitle,
        style = typography.labelMedium,
        color = colors.pillTextSubdued,
      )
    }

    if (onClick != null) {
      Icon(
        modifier = Modifier.padding(ItemPadding),
        imageVector = MaterialIcons.OpenInNew,
        contentDescription = null,
        tint = colors.pillText,
      )
    }
  }
}

private val ItemPadding = 8.dp
private val ItemHeight = 50.dp

@PortraitPreview
@LandscapePreview
@TabletPreview
@Composable
private fun PreviewInfoScaffold(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    InfoScaffold(modifier = Modifier.fillMaxSize(), buildState = PreviewBuildState, onAction = {})
  }

private val PreviewBuildState =
  BuildState(
    versions = AktualVersions(app = "1.2.3", server = "2.3.4"),
    buildDate = "12:34 GMT, 1st Feb 2026",
    buildType = "debug",
    year = 2026,
    appSourceUrl = "https://github.com/jonapoul/aktual/tree/v1.2.3",
    serverSourceUrl = "https://github.com/actualbudget/actual/tree/v2.3.4",
  )
