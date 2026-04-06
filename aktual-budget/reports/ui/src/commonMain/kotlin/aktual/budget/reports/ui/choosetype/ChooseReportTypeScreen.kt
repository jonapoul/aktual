package aktual.budget.reports.ui.choosetype

import aktual.app.nav.BackNavigator
import aktual.app.nav.ReportNavigator
import aktual.budget.model.BudgetId
import aktual.budget.model.WidgetType
import aktual.budget.reports.ui.charts.JUL_2025
import aktual.budget.reports.ui.charts.PER_TRANSACTION_DATA
import aktual.budget.reports.ui.charts.PREVIEW_CASH_FLOW_DATA
import aktual.budget.reports.ui.charts.PREVIEW_CUSTOM_DATA
import aktual.budget.reports.ui.charts.PREVIEW_NET_WORTH_DATA
import aktual.budget.reports.ui.charts.PREVIEW_SHORT_TEXT_DATA
import aktual.budget.reports.ui.charts.ReportChart
import aktual.budget.reports.ui.charts.THREE_MONTHS
import aktual.budget.reports.ui.string
import aktual.budget.reports.vm.ChartData
import aktual.budget.reports.vm.choosetype.ChooseReportTypeDialog
import aktual.budget.reports.vm.choosetype.ChooseReportTypeViewModel
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Warning
import aktual.core.l10n.Strings
import aktual.core.model.Token
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.AktualTypography
import aktual.core.ui.BottomNavBarSpacing
import aktual.core.ui.BottomStatusBarSpacing
import aktual.core.ui.CardShape
import aktual.core.ui.NormalIconButton
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithThemedParams
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.blurredTopBar
import aktual.core.ui.blurredTopBarContent
import aktual.core.ui.blurredTopBarContentPadding
import aktual.core.ui.disabledIf
import aktual.core.ui.rememberBlurredTopBarState
import aktual.core.ui.scrollbar
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ChooseReportTypeScreen(
  @Suppress("unused") back: BackNavigator,
  toReport: ReportNavigator,
  budgetId: BudgetId,
  token: Token,
  viewModel: ChooseReportTypeViewModel = metroViewModel(budgetId),
) {
  LaunchedEffect(Unit) {
    viewModel.shouldNavigateEvent.collectLatest { event ->
      toReport(token, budgetId, widgetId = event.id)
    }
  }

  val dialog by viewModel.dialog.collectAsStateWithLifecycle()

  ChooseReportTypeScaffold(
    dialog = dialog,
    onAction = { action ->
      when (action) {
        is ChooseReportTypeAction.Create -> viewModel.createReport(action.type)
        ChooseReportTypeAction.ShowDisabledDialog ->
          viewModel.show(ChooseReportTypeDialog.UnsupportedType)
        ChooseReportTypeAction.DismissDialog -> viewModel.hideDialogs()
      }
    },
  )
}

@Composable
internal fun ChooseReportTypeScaffold(
  dialog: ChooseReportTypeDialog?,
  onAction: (ChooseReportTypeAction) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  val lazyListState = rememberLazyListState()
  val blurState = rememberBlurredTopBarState()
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        modifier = Modifier.blurredTopBar(blurState, isScrolled = lazyListState.canScrollBackward),
        colors = theme.transparentTopAppBarColors(),
        title = { Text(Strings.reportsChooseTypeTitle) },
      )
    },
  ) { innerPadding ->
    ChooseReportTypeContent(
      modifier = Modifier.blurredTopBarContent(blurState, innerPadding),
      dialog = dialog,
      contentPadding = blurredTopBarContentPadding(blurState, innerPadding),
      lazyListState = lazyListState,
      onAction = onAction,
      theme = theme,
    )
  }
}

@Composable
private fun ChooseReportTypeContent(
  dialog: ChooseReportTypeDialog?,
  onAction: (ChooseReportTypeAction) -> Unit,
  contentPadding: PaddingValues,
  lazyListState: LazyListState,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Box(modifier = modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier.fillMaxWidth().background(theme.pageBackground).scrollbar(lazyListState),
      state = lazyListState,
      verticalArrangement = Arrangement.spacedBy(8.dp),
      contentPadding = contentPadding,
    ) {
      items(WIDGET_TYPES) { type ->
        WidgetType(
          modifier = Modifier.fillMaxWidth(),
          type = type,
          onAction = onAction,
          theme = theme,
        )
      }

      item {
        BottomStatusBarSpacing()
        BottomNavBarSpacing()
      }
    }

    ChooseReportTypeDialogs(dialog, onAction)
  }
}

@Composable
private fun WidgetType(
  type: WidgetType,
  onAction: (ChooseReportTypeAction) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  val enabled = type.isEnabled()
  Column(
    modifier =
      modifier
        .clip(CardShape)
        .background(theme.tableBackground.disabledIf(!enabled), CardShape)
        .border(Dp.Hairline, theme.pillBorderDark, CardShape)
        .clickable(enabled) { onAction(ChooseReportTypeAction.Create(type)) }
        .padding(8.dp)
  ) {
    Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      Text(
        modifier = Modifier.weight(1f),
        text = type.string(),
        color = theme.pageText.disabledIf(!enabled),
        style = AktualTypography.titleLarge,
      )

      if (!enabled) {
        NormalIconButton(
          imageVector = MaterialIcons.Warning,
          contentDescription = Strings.reportsChooseTypeDisabled,
          onClick = { onAction(ChooseReportTypeAction.ShowDisabledDialog) },
          colors = NormalRed,
        )
      }
    }

    ReportChart(
      modifier = Modifier.fillMaxWidth().height(REPORT_HEIGHT),
      data = type.sampleData(),
      compact = true,
      includeHeader = false,
      onAction = {}, // no-op
    )
  }
}

@Stable
@Suppress("NotImplementedDeclaration")
private fun WidgetType.sampleData(): ChartData =
  when (this) {
    WidgetType.NetWorth -> PREVIEW_NET_WORTH_DATA
    WidgetType.CashFlow -> PREVIEW_CASH_FLOW_DATA
    WidgetType.Spending -> JUL_2025
    WidgetType.Custom -> PREVIEW_CUSTOM_DATA
    WidgetType.Markdown -> PREVIEW_SHORT_TEXT_DATA
    WidgetType.Summary -> PER_TRANSACTION_DATA
    WidgetType.Calendar -> THREE_MONTHS
    WidgetType.BudgetAnalysis -> TODO("https://github.com/jonapoul/aktual/issues/1035")
    WidgetType.Formula -> TODO("https://github.com/jonapoul/aktual/issues/1054")
  }

@Composable
private fun metroViewModel(budgetId: BudgetId) =
  assistedMetroViewModel<ChooseReportTypeViewModel, ChooseReportTypeViewModel.Factory>(
    key = budgetId.value,
    createViewModel = { create(budgetId) },
  )

@Stable
private fun WidgetType.isEnabled(): Boolean =
  when (this) {
    WidgetType.BudgetAnalysis,
    WidgetType.Formula,
    WidgetType.Custom -> false

    WidgetType.NetWorth,
    WidgetType.CashFlow,
    WidgetType.Spending,
    WidgetType.Markdown,
    WidgetType.Summary,
    WidgetType.Calendar -> true
  }

private val WIDGET_TYPES = WidgetType.entries.toImmutableList()
private val REPORT_HEIGHT = 250.dp

@PortraitPreview
@Composable
private fun PreviewChooseReportTypeScaffold(
  @PreviewParameter(ChooseReportTypeScaffoldParameters::class)
  params: ThemedParams<ChooseReportTypeScaffoldParams>
) = PreviewWithThemedParams(params) { ChooseReportTypeScaffold(dialog = dialog, onAction = {}) }

private data class ChooseReportTypeScaffoldParams(val dialog: ChooseReportTypeDialog?)

private class ChooseReportTypeScaffoldParameters :
  ThemedParameterProvider<ChooseReportTypeScaffoldParams>(
    ChooseReportTypeScaffoldParams(dialog = null),
    ChooseReportTypeScaffoldParams(dialog = ChooseReportTypeDialog.UnsupportedType),
  )
