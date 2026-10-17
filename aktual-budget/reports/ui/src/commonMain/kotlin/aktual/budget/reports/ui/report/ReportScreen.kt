package aktual.budget.reports.ui.report

import aktual.budget.model.WidgetId
import aktual.budget.reports.ui.ActionListener
import aktual.budget.reports.ui.Tags
import aktual.budget.reports.ui.charts.ChartInfoSheet
import aktual.budget.reports.ui.charts.ChartZoomState
import aktual.budget.reports.ui.charts.PREVIEW_AGE_OF_MONEY_DATA
import aktual.budget.reports.ui.charts.PREVIEW_FORMULA_DATA
import aktual.budget.reports.ui.charts.ReportChart
import aktual.budget.reports.ui.charts.info
import aktual.budget.reports.ui.charts.rememberChartZoomState
import aktual.budget.reports.ui.montecarlo.MonteCarloReportScreen
import aktual.budget.reports.ui.string
import aktual.budget.reports.vm.AgeOfMoneyReportMeta
import aktual.budget.reports.vm.CustomData
import aktual.budget.reports.vm.FormulaData
import aktual.budget.reports.vm.FormulaReportMeta
import aktual.budget.reports.vm.QueryEditorData
import aktual.budget.reports.vm.dashboard.DashboardItem
import aktual.budget.reports.vm.report.ReportState
import aktual.budget.reports.vm.report.ReportViewModel
import aktual.core.icons.material.Edit
import aktual.core.icons.material.Info
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.ZoomOut
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.BackHandler
import aktual.core.ui.BareIconButton
import aktual.core.ui.BottomSpacing
import aktual.core.ui.CardShape
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.PrimaryIconButton
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.transparentTopAppBarColors
import aktual.core.ui.verticalScrollWithBar
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlinx.collections.immutable.persistentListOf

@Composable
fun ReportScreen(
  id: WidgetId,
  back: BackNavigator,
  viewModel: ReportViewModel = reportViewModel(id),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  if (state == MonteCarlo) {
    MonteCarloReportScreen(id = id, back = back)
    return
  }

  var hasUnsavedText by rememberSaveable { mutableStateOf(false) }
  var showDiscardDialog by remember { mutableStateOf(false) }
  var isEditing by rememberSaveable { mutableStateOf(false) }

  BackHandler(enabled = hasUnsavedText) { showDiscardDialog = true }
  BackHandler(enabled = isEditing) { isEditing = false }

  // Only loaded for formula cards, which are the only ones with queries
  val isFormula = (state as? Loaded)?.item?.meta is FormulaReportMeta
  val queryEditorData =
    if (isFormula) viewModel.queryEditorData.collectAsStateWithLifecycle().value else null

  ReportScaffold(
    state = state,
    queryEditorData = queryEditorData,
    isEditing = isEditing,
    onAction = { action ->
      @Suppress("ElseCaseInsteadOfExhaustiveWhen")
      when (action) {
        NavBack -> {
          when {
            isEditing -> isEditing = false
            hasUnsavedText -> showDiscardDialog = true
            else -> back()
          }
        }
        is SaveText -> {
          viewModel.saveText(action.content, action.align)
        }
        is SetUnsavedText -> {
          hasUnsavedText = action.hasUnsavedText
        }
        is SaveFormula -> {
          viewModel.saveFormula(action.edits)
          isEditing = false
        }
        EditReport -> {
          isEditing = true
        }
        else -> {}
      }
    },
  )

  if (showDiscardDialog) {
    DiscardTextDialog(
      onDiscard = {
        showDiscardDialog = false
        back()
      },
      onCancel = { showDiscardDialog = false },
    )
  }
}

@Composable
private fun DiscardTextDialog(onDiscard: () -> Unit, onCancel: () -> Unit) =
  AktualAlertDialog(
    title = Strings.reportsTextDiscardTitle,
    highlight = colors.warningText,
    onDismissRequest = onCancel,
    buttons = {
      TextButton(onClick = onCancel) { Text(Strings.reportsTextDiscardCancel) }
      TextButton(onClick = onDiscard) {
        Text(Strings.reportsTextDiscardConfirm, color = colors.warningText)
      }
    },
    content = { Text(Strings.reportsTextDiscardMessage) },
  )

@Composable
private fun reportViewModel(id: WidgetId) =
  assistedMetroViewModel<ReportViewModel, ReportViewModel.Factory>(key = id.value) { create(id) }

@Composable
internal fun ReportScaffold(
  state: ReportState,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
  queryEditorData: QueryEditorData? = null,
  isEditing: Boolean = false,
) {
  val data = (state as? Loaded)?.data
  val zoom = rememberChartZoomState(data)
  val info = data?.info() ?: persistentListOf()
  var showInfo by remember { mutableStateOf(false) }
  val hazeState = rememberHazedTopBarState()
  val scrollState = rememberScrollState()

  if (showInfo) {
    ChartInfoSheet(info = info, onDismiss = { showInfo = false })
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        modifier = Modifier.hazedTopBar(hazeState, scrollOffset = { scrollState.value.toFloat() }),
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = { NavBackIconButton(onClick = { onAction(NavBack) }) },
        title = {
          Text(
            text =
              when {
                data is CustomData && data.title.isNotBlank() -> data.title
                data is FormulaData && !data.title.isNullOrBlank() -> data.title.orEmpty()
                state is Loaded -> state.type.string()
                else -> Strings.reportsDashboardTitle
              },
          )
        },
        actions = {
          if (zoom.range != null) {
            BareIconButton(
              modifier = Modifier.testTag(Tags.ResetZoom),
              imageVector = MaterialIcons.ZoomOut,
              contentDescription = Strings.reportsResetZoom,
              onClick = zoom::reset,
            )
          }
          if (info.isNotEmpty()) {
            BareIconButton(
              modifier = Modifier.testTag(Tags.ChartInfo),
              imageVector = MaterialIcons.Info,
              contentDescription = Strings.reportsChartInfo,
              onClick = { showInfo = true },
            )
          }
          if (data is FormulaData && !isEditing) {
            PrimaryIconButton(
              modifier = Modifier.padding(horizontal = 8.dp).testTag(Tags.EditReport),
              imageVector = MaterialIcons.Edit,
              contentDescription = Strings.reportsFormulaViewerEdit,
              size = 20.dp,
              shape = CircleShape,
              onClick = { onAction(EditReport) },
            )
          }
        },
      )
    },
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize()) {
      PageBackground()
      ReportContent(
        modifier = Modifier.hazedTopBarContent(hazeState, innerPadding).fillMaxSize(),
        contentPadding = hazedTopBarContentPadding(hazeState, innerPadding),
        scrollState = scrollState,
        state = state,
        queryEditorData = queryEditorData,
        isEditing = isEditing,
        zoom = zoom,
        onAction = onAction,
      )
    }
  }
}

// Formula cards scroll behind the top and bottom bars. Other charts fill the space between them
@Composable
private fun ReportContent(
  state: ReportState,
  queryEditorData: QueryEditorData?,
  isEditing: Boolean,
  contentPadding: PaddingValues,
  scrollState: ScrollState,
  zoom: ChartZoomState,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
) {
  val meta = (state as? Loaded)?.item?.meta as? FormulaReportMeta
  val formulaData = (state as? Loaded)?.data as? FormulaData
  if (meta != null && formulaData != null) {
    val deleteState = rememberConfirmDeleteState()
    CompositionLocalProvider(LocalConfirmDeleteState provides deleteState) {
      Column(
        modifier =
          modifier
            .disarmDeletesOnTap(deleteState)
            .verticalScrollWithBar(scrollState)
            .padding(contentPadding)
            .padding(CONTENT_PADDING),
      ) {
        ReportChart(
          modifier =
            Modifier.fillMaxWidth()
              .height(FORMULA_CARD_HEIGHT)
              .background(colors.tableBackground, CardShape)
              .padding(8.dp),
          data = formulaData,
          compact = false,
          onAction = onAction,
          zoom = zoom,
        )
        if (isEditing) {
          FormulaEditor(
            modifier = Modifier.padding(top = 16.dp),
            meta = meta,
            queryEditorData = queryEditorData,
            onAction = onAction,
          )
        } else {
          FormulaViewer(
            modifier = Modifier.padding(top = 20.dp),
            meta = meta,
            data = formulaData,
            queryEditorData = queryEditorData,
            onEdit = { onAction(EditReport) },
          )
        }
        BottomSpacing()
      }
    }
    return
  }

  Column(modifier = modifier.padding(contentPadding).padding(CONTENT_PADDING)) {
    val contentModifier = Modifier.fillMaxWidth().weight(1f)
    when (state) {
      Loading ->
        Box(modifier = contentModifier, contentAlignment = Center) {
          CircularProgressIndicator(color = colors.pageText)
        }

      NotFound ->
        Box(modifier = contentModifier, contentAlignment = Center) {
          Text(text = Strings.reportsNotFound, color = colors.pageText)
        }

      // Shown by MonteCarloReportScreen instead
      MonteCarlo -> Box(modifier = contentModifier)

      is Loaded ->
        ReportChart(
          modifier = contentModifier.background(colors.tableBackground, CardShape).padding(8.dp),
          data = state.data,
          compact = false,
          onAction = onAction,
          zoom = zoom,
        )
    }
    BottomSpacing()
  }
}

private val CONTENT_PADDING = 8.dp
private val FORMULA_CARD_HEIGHT = 160.dp

@PortraitPreview
@Composable
private fun PreviewReportScaffold(
  @PreviewParameter(ReportScaffoldProvider::class) params: ColoredParams<ReportState>,
) = PreviewWithColoredParams(params) { ReportScaffold(state = this, onAction = {}) }

private class ReportScaffoldProvider :
  ColoredParameterProvider<ReportState>(
    ReportState.Loaded(
      type = AgeOfMoney,
      item = DashboardItem(id = WidgetId("preview"), x = 0, y = 0, meta = AgeOfMoneyReportMeta()),
      data = PREVIEW_AGE_OF_MONEY_DATA,
    ),
    ReportState.Loaded(
      type = Formula,
      item = DashboardItem(id = WidgetId("preview"), x = 0, y = 0, meta = PREVIEW_FORMULA_META),
      data = PREVIEW_FORMULA_DATA,
    ),
    Loading,
    NotFound,
  )
