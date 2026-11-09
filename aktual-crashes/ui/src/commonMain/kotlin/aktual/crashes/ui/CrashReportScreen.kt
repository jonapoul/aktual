package aktual.crashes.ui

import aktual.about.components.CollectLogExports
import aktual.about.components.rememberLogExportHandler
import aktual.core.icons.AktualIcons
import aktual.core.icons.Trash
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.OpenInNew
import aktual.core.icons.material.Share
import aktual.core.l10n.Strings
import aktual.core.logging.CrashReport
import aktual.core.model.CrashFile
import aktual.core.nav.BackNavigator
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BottomSpacing
import aktual.core.ui.CardColumn
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.Dimens
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.NormalTextButton
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.PrimaryTextButton
import aktual.core.ui.SearchMessage
import aktual.core.ui.WavyBackground
import aktual.core.ui.horizontalScrollWithBar
import aktual.core.ui.transparentTopAppBarColors
import aktual.core.ui.verticalScrollWithBar
import aktual.crashes.vm.CrashReportState
import aktual.crashes.vm.CrashReportViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel

@Composable
fun CrashReportScreen(
  file: CrashFile,
  back: BackNavigator,
  viewModel: CrashReportViewModel =
    assistedMetroViewModel<CrashReportViewModel, CrashReportViewModel.Factory>(
      key = file.name,
      createViewModel = { create(file) },
    ),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val uriHandler = LocalUriHandler.current
  val handler = rememberLogExportHandler()
  CollectLogExports(viewModel.exports, handler)

  LaunchedEffect(viewModel) { viewModel.deleted.collect { back() } }

  CrashReportScaffold(
    state = state,
    canShare = handler.canShare,
    onAction = { action ->
      when (action) {
        NavBack -> back()
        is Delete -> viewModel.delete()
        is Export -> viewModel.export(action.includeLogs, action.target)
        ReportIssue -> (state as? Loaded)?.let { uriHandler.openUri(it.issueUrl) }
        is Open,
        DeleteAll -> Unit
      }
    },
  )
}

@Composable
internal fun CrashReportScaffold(
  state: CrashReportState,
  canShare: Boolean,
  onAction: CrashesActionHandler,
  modifier: Modifier = Modifier,
) {
  var showExport by remember { mutableStateOf(false) }
  var showReport by remember { mutableStateOf(false) }
  var showDelete by remember { mutableStateOf(false) }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = { NavBackIconButton { onAction(NavBack) } },
        title = {
          val title = (state as? Loaded)?.report?.exceptionName.orEmpty()
          Text(text = title, maxLines = 1, overflow = Ellipsis)
        },
        actions = {
          if (state is Loaded) {
            IconButton(onClick = { showDelete = true }) {
              Icon(imageVector = AktualIcons.Trash, contentDescription = Strings.crashesDelete)
            }
          }
        },
      )
    },
    bottomBar = {
      if (state is Loaded) {
        CrashReportBottomBar(
          onReport = { showReport = true },
          onExport = { showExport = true },
        )
      }
    },
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
      WavyBackground()
      when (state) {
        Loading -> Unit
        Missing -> SearchMessage(text = Strings.crashesMissing, icon = AktualIcons.Trash)
        is Loaded -> CrashReportContent(state.report)
      }
    }
  }

  if (showExport) {
    CrashExportSheet(
      canShare = canShare,
      onExport = { includeLogs, target -> onAction(Export(includeLogs, target)) },
      onDismiss = { showExport = false },
    )
  }

  if (showReport) {
    ReportIssueDialog(
      onExport = {
        showReport = false
        showExport = true
      },
      onOpen = {
        showReport = false
        onAction(ReportIssue)
      },
      onDismiss = { showReport = false },
    )
  }

  if (showDelete && state is Loaded) {
    CrashDeleteDialog(
      title = Strings.crashesDeleteTitle,
      message = Strings.crashesDeleteMessage,
      onConfirm = {
        showDelete = false
        onAction(Delete(state.report.file))
      },
      onDismiss = { showDelete = false },
    )
  }
}

@Composable
private fun CrashReportContent(report: CrashReport) =
  Column(
    modifier = Modifier.fillMaxSize().verticalScrollWithBar().padding(Dimens.Large),
    verticalArrangement = spacedBy(Dimens.Large),
  ) {
    CardColumn(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = spacedBy(Dimens.Medium),
    ) {
      Text(
        text = report.summary,
        style = typography.bodyMedium,
        color = colors.errorText,
      )
      CrashDetail(Strings.crashesTime, report.timeLabel())
      CrashDetail(Strings.crashesVersion, report.version)
      CrashDetail(Strings.crashesPlatform, report.platform)
      CrashDetail(Strings.crashesThread, report.thread)
    }

    Text(
      modifier = Modifier.padding(horizontal = Dimens.Medium),
      text = Strings.crashesStackTrace,
      style = typography.labelLarge,
      color = colors.pageTextLight,
    )

    CardColumn(modifier = Modifier.fillMaxWidth()) {
      SelectionContainer {
        Text(
          modifier =
            Modifier.horizontalScrollWithBar(rememberScrollState()).padding(bottom = Dimens.Large),
          text = report.stackTrace.replace("\t", "    "),
          style = typography.labelSmall,
          fontFamily = Monospace,
          softWrap = false,
        )
      }
    }

    BottomSpacing()
  }

@Composable
private fun CrashDetail(label: String, value: String) =
  Row(horizontalArrangement = spacedBy(Dimens.Large)) {
    Text(
      modifier = Modifier.width(LabelWidth),
      text = label,
      style = typography.labelMedium,
      color = colors.pageTextLight,
    )
    SelectionContainer(modifier = Modifier.weight(1f)) {
      Text(text = value, style = typography.labelMedium)
    }
  }

@Composable
private fun CrashReportBottomBar(onReport: () -> Unit, onExport: () -> Unit) =
  Column(modifier = Modifier.fillMaxWidth().background(colors.cardBackground)) {
    Row(
      modifier =
        Modifier.fillMaxWidth().padding(horizontal = Dimens.Large, vertical = Dimens.VeryLarge),
      horizontalArrangement = spacedBy(Dimens.Large),
    ) {
      NormalTextButton(
        modifier = Modifier.weight(1f),
        text = Strings.crashesReport,
        onClick = onReport,
      )
      PrimaryTextButton(
        modifier = Modifier.weight(1f),
        text = Strings.crashesExport,
        onClick = onExport,
        prefix = { Icon(imageVector = MaterialIcons.Share, contentDescription = null) },
      )
    }
    BottomSpacing()
  }

@Composable
private fun ReportIssueDialog(onExport: () -> Unit, onOpen: () -> Unit, onDismiss: () -> Unit) =
  AktualAlertDialog(
    title = Strings.crashesReportTitle,
    onDismissRequest = onDismiss,
    icon = MaterialIcons.OpenInNew,
    buttons = {
      TextButton(onClick = onDismiss) { Text(Strings.crashesCancel) }
      TextButton(onClick = onExport) { Text(Strings.crashesReportExport) }
      TextButton(onClick = onOpen) {
        Text(Strings.crashesReportOpen, color = colors.pageTextPositive)
      }
    },
  ) {
    Text(text = Strings.crashesReportMessage)
  }

private val LabelWidth = 72.dp

@PortraitPreview
@Composable
private fun PreviewCrashReport(
  @PreviewParameter(CrashReportProvider::class) params: ColorParams<CrashReportState>,
) =
  PreviewWithColorParams(params) {
    CrashReportScaffold(state = this, canShare = true, onAction = {})
  }

private class CrashReportProvider :
  ColorParamsProvider<CrashReportState>(
    CrashReportState.Loaded(PREVIEW_REPORT, issueUrl = ""),
    CrashReportState.Missing,
  )
