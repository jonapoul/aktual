package aktual.about.ui.info

import aktual.about.vm.AboutViewModel
import aktual.about.vm.BuildState
import aktual.about.vm.CheckUpdatesState
import aktual.core.icons.material.Apps
import aktual.core.icons.material.CalendarToday
import aktual.core.icons.material.Cloud
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Drawables
import aktual.core.l10n.Strings
import aktual.core.model.AktualVersions
import aktual.core.nav.BackNavigator
import aktual.core.nav.LicensesNavigator
import aktual.core.nav.ManageStorageNavigator
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BottomSpacing
import aktual.core.ui.CardShape
import aktual.core.ui.ColoredParameters
import aktual.core.ui.Dimens
import aktual.core.ui.LandscapePreview
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.TabletPreview
import aktual.core.ui.WavyBackground
import aktual.core.ui.isCompactWidth
import aktual.core.ui.transparentTopAppBarColors
import aktual.core.ui.verticalScrollWithBar
import alakazam.compose.HorizontalSpacer
import alakazam.kotlin.noOp
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
fun InfoScreen(
  back: BackNavigator,
  toLicenses: LicensesNavigator,
  toManageStorage: ManageStorageNavigator,
  viewModel: AboutViewModel = metroViewModel(),
) {
  val buildState by viewModel.buildState.collectAsStateWithLifecycle()

  val onCancel = { viewModel.cancelUpdateCheck() }

  val checkUpdatesState by viewModel.checkUpdatesState.collectAsStateWithLifecycle()
  when (val state = checkUpdatesState) {
    CheckUpdatesState.Inactive -> noOp()

    CheckUpdatesState.Checking -> CheckUpdatesLoadingDialog(onCancel)

    CheckUpdatesState.NoUpdateFound -> NoUpdateFoundDialog(onCancel)

    is CheckUpdatesState.Failed -> UpdateCheckFailedDialog(state.cause, onCancel)

    is CheckUpdatesState.UpdateFound ->
      UpdateFoundDialog(
        currentVersion = buildState.versions.app,
        latestVersion = state.version,
        latestUrl = state.url,
        onDismiss = onCancel,
        onOpenUrl = { url -> viewModel.openUrl(url) },
      )
  }

  InfoScaffold(
    buildState = buildState,
    onAction = { action ->
      when (action) {
        OpenSourceCode -> viewModel.openRepo()
        ReportIssue -> viewModel.reportIssues()
        CheckUpdates -> viewModel.fetchLatestRelease()
        NavBack -> back()
        ViewLicenses -> toLicenses()
        ManageStorage -> toManageStorage()
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
  Scaffold(modifier = modifier, topBar = { InfoTopBar(onAction) }) { innerPadding ->
    Box {
      WavyBackground()
      InfoScreenContent(
        modifier = Modifier.padding(innerPadding),
        buildState = buildState,
        onAction = onAction,
      )
    }
  }
}

@Composable
private fun InfoTopBar(onAction: InfoActionHandler) {
  TopAppBar(
    colors = colors.transparentTopAppBarColors(),
    navigationIcon = { NavBackIconButton { onAction(NavBack) } },
    title = {
      Text(text = Strings.infoToolbarTitle, maxLines = 1, overflow = TextOverflow.Ellipsis)
    },
  )
}

@Composable
private fun InfoScreenContent(
  buildState: BuildState,
  onAction: InfoActionHandler,
  modifier: Modifier = Modifier,
) =
  BoxWithConstraints(modifier = modifier.padding(Dimens.Huge)) {
    val contentMaxWidth = if (isCompactWidth()) maxWidth else maxWidth / 2
    val contentModifier = Modifier.widthIn(max = contentMaxWidth)

    Column(
      modifier = Modifier.fillMaxWidth().verticalScrollWithBar(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(Dimens.Huge),
    ) {
      InfoHeader(buildState.year, contentModifier)
      InfoBuildState(buildState, contentModifier)
      InfoButtons(onAction, contentModifier)

      BottomSpacing()
    }
  }

@Composable
private fun InfoHeader(year: Int, modifier: Modifier = Modifier) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .background(colors.tableBackground, CardShape)
        .border(Dp.Hairline, colors.tableBorder, CardShape)
        .padding(horizontal = 20.dp, vertical = 10.dp),
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    val appName = Strings.appName

    Image(
      modifier = Modifier.size(50.dp),
      painter = Drawables.appIcon192,
      contentDescription = appName,
    )

    HorizontalSpacer(10.dp)

    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Text(
        text = appName,
        fontSize = 25.sp,
        fontWeight = FontWeight.W700,
        color = colors.pillTextHighlighted,
      )
      Text(text = Strings.infoSubtitle1(year), color = colors.pillText)
      Text(text = Strings.infoSubtitle2, color = colors.pillText)
    }
  }
}

@Composable
private fun InfoBuildState(buildState: BuildState, modifier: Modifier = Modifier) {
  Column(
    modifier =
      modifier
        .background(colors.tableBackground, CardShape)
        .border(Dp.Hairline, colors.tableBorder, CardShape)
  ) {
    BuildStateItem(
      modifier = Modifier.padding(ItemMargin).clip(CardShape),
      icon = MaterialIcons.Apps,
      title = Strings.infoAppVersion,
      subtitle = buildState.versions.app,
    )

    BuildStateItem(
      modifier = Modifier.testTag(Tags.ServerVersionText).padding(ItemMargin).clip(CardShape),
      icon = MaterialIcons.Cloud,
      title = Strings.infoServerVersion,
      subtitle = buildState.versions.server ?: Strings.infoServerVersionUnknown,
    )

    BuildStateItem(
      modifier = Modifier.padding(ItemMargin).clip(CardShape),
      icon = MaterialIcons.CalendarToday,
      title = Strings.infoDate,
      subtitle = buildState.buildDate,
    )
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
        .clickable(enabled = onClick != null, onClick = { onClick?.invoke() })
        .padding(horizontal = ItemPadding),
    verticalAlignment = Alignment.CenterVertically,
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
  }
}

private val ItemMargin = PaddingValues(horizontal = 6.dp, vertical = 3.dp)
private val ItemPadding = 8.dp
private val ItemHeight = 50.dp

@PortraitPreview
@LandscapePreview
@TabletPreview
@Composable
private fun PreviewInfoScaffold(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    InfoScaffold(modifier = Modifier.fillMaxSize(), buildState = PreviewBuildState, onAction = {})
  }

private val PreviewBuildState =
  BuildState(
    versions = AktualVersions(app = "1.2.3", server = "2.3.4"),
    buildDate = "12:34 GMT, 1st Feb 2026",
    year = 2026,
  )
