package aktual.crashes.ui

import aktual.core.icons.AktualIcons
import aktual.core.icons.AlertTriangle
import aktual.core.icons.CheckCircle1
import aktual.core.icons.CheveronRight
import aktual.core.icons.Trash
import aktual.core.l10n.Plurals
import aktual.core.l10n.Strings
import aktual.core.logging.CrashReport
import aktual.core.model.CrashFile
import aktual.core.nav.BackNavigator
import aktual.core.nav.CrashReportNavigator
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.Dimens
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.NormalTextButton
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.RowShape
import aktual.core.ui.SearchMessage
import aktual.core.ui.SwipeAction
import aktual.core.ui.SwipeToReveal
import aktual.core.ui.WavyBackground
import aktual.core.ui.card
import aktual.core.ui.contrastingTextColor
import aktual.core.ui.errorButton
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.normalButton
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.scrollbar
import aktual.core.ui.transparentTopAppBarColors
import aktual.crashes.vm.CrashReportsState
import aktual.crashes.vm.CrashReportsViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.persistentListOf

@Composable
fun CrashReportsScreen(
  back: BackNavigator,
  toReport: CrashReportNavigator,
  viewModel: CrashReportsViewModel = metroViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  // Picks up reports deleted from the detail screen
  @Suppress("ComposeViewModelForwarding")
  LifecycleResumeEffect(viewModel) {
    viewModel.load()
    onPauseOrDispose {}
  }

  CrashReportsScaffold(
    state = state,
    onAction = { action ->
      when (action) {
        NavBack -> back()
        is Open -> toReport(action.file)
        is Delete -> viewModel.delete(action.file)
        DeleteAll -> viewModel.deleteAll()
        is Export,
        ReportIssue -> Unit
      }
    },
  )
}

@Composable
internal fun CrashReportsScaffold(
  state: CrashReportsState,
  onAction: CrashesActionHandler,
  modifier: Modifier = Modifier,
) {
  var showDeleteAll by remember { mutableStateOf(false) }
  val hazeState = rememberHazedTopBarState()
  val listState = rememberLazyListState()

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        modifier = Modifier.hazedTopBar(hazeState, listState),
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = { NavBackIconButton { onAction(NavBack) } },
        title = { Text(text = Strings.crashesToolbar, maxLines = 1, overflow = Ellipsis) },
      )
    },
    bottomBar = {
      if (state is Loaded) {
        Column(modifier = Modifier.fillMaxWidth().background(colors.cardBackground)) {
          NormalTextButton(
            modifier =
              Modifier.fillMaxWidth()
                .padding(horizontal = Dimens.Large, vertical = Dimens.VeryLarge),
            text = Strings.crashesDeleteAll,
            colors = { pressed -> colors.errorButton(pressed) },
            border = colors.errorBorder,
            onClick = { showDeleteAll = true },
          )
          BottomSpacing()
        }
      }
    },
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize()) {
      WavyBackground()
      Box(modifier = Modifier.hazedTopBarContent(hazeState, innerPadding)) {
        val contentPadding = hazedTopBarContentPadding(hazeState, innerPadding)
        when (state) {
          Loading -> Unit
          Empty -> SearchMessage(text = Strings.crashesEmpty, icon = AktualIcons.CheckCircle1)
          is Loaded -> CrashReportsList(state, contentPadding, listState, onAction)
        }
      }
    }
  }

  if (showDeleteAll) {
    CrashDeleteDialog(
      title = Strings.crashesDeleteAllTitle,
      message = Strings.crashesDeleteAllMessage,
      onConfirm = {
        showDeleteAll = false
        onAction(DeleteAll)
      },
      onDismiss = { showDeleteAll = false },
    )
  }
}

@Composable
private fun CrashReportsList(
  state: CrashReportsState.Loaded,
  contentPadding: PaddingValues,
  listState: LazyListState,
  onAction: CrashesActionHandler,
) {
  // Only one row may be swiped open at a time
  var openFile by remember { mutableStateOf<CrashFile?>(null) }

  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(horizontal = Dimens.Large).scrollbar(listState),
    state = listState,
    contentPadding = contentPadding,
    verticalArrangement = spacedBy(Dimens.Medium),
  ) {
    item { CrashReportsSummary(state.reports.size, Modifier.padding(vertical = Dimens.Huge)) }

    items(state.reports, key = { it.file.name }) { report ->
      val deleteBackground = colors.errorText
      SwipeToReveal(
        modifier = Modifier.animateItem(),
        actions =
          persistentListOf(
            SwipeAction(
              text = Strings.crashesDelete,
              icon = AktualIcons.Trash,
              background = deleteBackground,
              foreground = deleteBackground.contrastingTextColor(),
              onClick = { onAction(Delete(report.file)) },
            ),
          ),
        isOpen = openFile == report.file,
        onOpenChange = { open ->
          openFile =
            when {
              open -> report.file
              openFile == report.file -> null
              else -> openFile
            }
        },
        shape = RowShape,
      ) {
        CrashReportItem(report, onClick = { onAction(Open(report.file)) })
      }
    }
  }
}

@Composable
private fun CrashReportsSummary(count: Int, modifier: Modifier = Modifier) =
  Column(
    modifier = modifier.fillMaxWidth().card(colors).padding(Dimens.VeryLarge),
    verticalArrangement = spacedBy(Dimens.Medium),
  ) {
    Text(
      text = Strings.crashesSummaryTitle,
      style = typography.labelMedium,
      color = colors.pageTextLight,
    )
    Text(
      text = Plurals.crashesSummaryCount(count, count),
      style = typography.headlineMedium,
    )
    Text(
      text = Strings.crashesSummaryRetention,
      style = typography.labelMedium,
      color = colors.pageTextSubdued,
    )
  }

@Composable
private fun CrashReportItem(
  report: CrashReport,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) =
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .heightIn(min = 56.dp)
        .normalButton(colors, RowShape)
        .clickable(onClick = onClick)
        .padding(
          start = Dimens.VeryLarge,
          end = Dimens.Medium,
          top = Dimens.Large,
          bottom = Dimens.Large,
        ),
    verticalAlignment = CenterVertically,
    horizontalArrangement = spacedBy(Dimens.VeryLarge),
  ) {
    Icon(
      imageVector = AktualIcons.AlertTriangle,
      contentDescription = null,
      tint = colors.errorText,
    )

    Column(modifier = Modifier.weight(1f), verticalArrangement = spacedBy(2.dp)) {
      Text(
        text = report.exceptionName,
        style = typography.bodyMedium,
        color = colors.tableText,
        maxLines = 1,
        overflow = Ellipsis,
      )
      Text(
        text = report.summary.substringAfter(": ", missingDelimiterValue = ""),
        style = typography.labelSmall,
        color = colors.pageTextLight,
        maxLines = 2,
        overflow = Ellipsis,
      )
      Text(
        text = "${report.timeLabel()} · ${report.version}",
        style = typography.labelSmall,
        color = colors.pageTextSubdued,
      )
    }

    Icon(
      imageVector = AktualIcons.CheveronRight,
      contentDescription = null,
      tint = colors.pageTextSubdued,
    )
  }

@Composable
internal fun CrashDeleteDialog(
  title: String,
  message: String,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
) =
  AktualAlertDialog(
    title = title,
    onDismissRequest = onDismiss,
    highlight = colors.errorText,
    buttons = {
      TextButton(onClick = onDismiss) { Text(Strings.crashesCancel) }
      TextButton(onClick = onConfirm) { Text(Strings.crashesDelete, color = colors.errorText) }
    },
  ) {
    Text(text = message)
  }

@PortraitPreview
@Composable
private fun PreviewCrashReports(
  @PreviewParameter(CrashReportsProvider::class) params: ColorParams<CrashReportsState>,
) = PreviewWithColorParams(params) { CrashReportsScaffold(state = this, onAction = {}) }

private class CrashReportsProvider :
  ColorParamsProvider<CrashReportsState>(
    CrashReportsState.Loaded(
      persistentListOf(
        PREVIEW_REPORT,
        PREVIEW_REPORT.copy(
          file = CrashFile("crash-20261012-091500-000.txt"),
          stackTrace = "java.lang.NullPointerException\n\tat aktual.Foo.bar(Foo.kt:1)",
        ),
      ),
    ),
    CrashReportsState.Empty,
  )
