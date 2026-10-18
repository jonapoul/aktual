package aktual.budget.reports.ui.choosetype

import aktual.budget.model.CustomReportId
import aktual.budget.model.DashboardPageId
import aktual.budget.model.WidgetType
import aktual.budget.reports.ui.charts.JUL_2025
import aktual.budget.reports.ui.charts.PER_TRANSACTION_DATA
import aktual.budget.reports.ui.charts.PREVIEW_AGE_OF_MONEY_DATA
import aktual.budget.reports.ui.charts.PREVIEW_BALANCE_FORECAST_DATA
import aktual.budget.reports.ui.charts.PREVIEW_CASH_FLOW_DATA
import aktual.budget.reports.ui.charts.PREVIEW_CROSSOVER_DATA
import aktual.budget.reports.ui.charts.PREVIEW_CUSTOM_DATA
import aktual.budget.reports.ui.charts.PREVIEW_FORMULA_DATA
import aktual.budget.reports.ui.charts.PREVIEW_MONTE_CARLO_DATA
import aktual.budget.reports.ui.charts.PREVIEW_NET_WORTH_DATA
import aktual.budget.reports.ui.charts.PREVIEW_SANKEY_DATA
import aktual.budget.reports.ui.charts.PREVIEW_SHORT_TEXT_DATA
import aktual.budget.reports.ui.charts.ReportChart
import aktual.budget.reports.ui.charts.THREE_MONTHS
import aktual.budget.reports.ui.string
import aktual.budget.reports.vm.ChartData
import aktual.budget.reports.vm.choosetype.ChooseReportTypeDialog
import aktual.budget.reports.vm.choosetype.ChooseReportTypeViewModel
import aktual.budget.reports.vm.choosetype.SavedCustomReport
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Warning
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.nav.NewCustomReportNavigator
import aktual.core.nav.ReportNavigator
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BottomSpacing
import aktual.core.ui.CardShape
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.NormalIconButton
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.disabledIf
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.rememberHazedTopBarState
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ChooseReportTypeScreen(
  page: DashboardPageId,
  @Suppress("unused") back: BackNavigator,
  toReport: ReportNavigator,
  toNewCustomReport: NewCustomReportNavigator,
  viewModel: ChooseReportTypeViewModel = chooseReportTypeViewModel(page),
) {
  LaunchedEffect(Unit) {
    viewModel.shouldNavigateEvent.collectLatest { event -> toReport(event.id) }
  }

  val dialog by viewModel.dialog.collectAsStateWithLifecycle()
  val savedReports by viewModel.savedReports.collectAsStateWithLifecycle()

  ChooseReportTypeScaffold(
    dialog = dialog,
    savedReports = savedReports,
    onAction = { action ->
      when (action) {
        is Create if action.type == Custom -> toNewCustomReport(page)
        is Create -> viewModel.createReport(action.type)
        is AddSavedReport -> viewModel.addSavedReport(action.id)
        ShowDisabledDialog -> viewModel.show(UnsupportedType)
        DismissDialog -> viewModel.hideDialogs()
      }
    },
  )
}

@Composable
private fun chooseReportTypeViewModel(page: DashboardPageId) =
  assistedMetroViewModel<ChooseReportTypeViewModel, ChooseReportTypeViewModel.Factory>(
    key = page.value,
    createViewModel = { create(page) },
  )

@Composable
internal fun ChooseReportTypeScaffold(
  dialog: ChooseReportTypeDialog?,
  savedReports: ImmutableList<SavedCustomReport>,
  onAction: ChooseReportTypeActionHandler,
  modifier: Modifier = Modifier,
) {
  val lazyListState = rememberLazyListState()
  val hazeState = rememberHazedTopBarState()
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        modifier = Modifier.hazedTopBar(hazeState, lazyListState),
        colors = colors.transparentTopAppBarColors(),
        title = { Text(Strings.reportsChooseTypeTitle) },
      )
    },
  ) { innerPadding ->
    ChooseReportTypeContent(
      modifier = Modifier.hazedTopBarContent(hazeState, innerPadding),
      dialog = dialog,
      savedReports = savedReports,
      contentPadding = hazedTopBarContentPadding(hazeState, innerPadding),
      lazyListState = lazyListState,
      onAction = onAction,
    )
  }
}

@Composable
private fun ChooseReportTypeContent(
  dialog: ChooseReportTypeDialog?,
  savedReports: ImmutableList<SavedCustomReport>,
  onAction: ChooseReportTypeActionHandler,
  contentPadding: PaddingValues,
  lazyListState: LazyListState,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier.fillMaxWidth().background(colors.pageBackground).scrollbar(lazyListState),
      state = lazyListState,
      verticalArrangement = Arrangement.spacedBy(8.dp),
      contentPadding = contentPadding,
    ) {
      if (savedReports.isNotEmpty()) {
        item {
          Text(
            modifier = Modifier.padding(horizontal = 8.dp),
            text = Strings.reportsChooseTypeSavedReports,
            color = colors.pageTextSubdued,
            style = typography.titleSmall,
          )
        }

        items(savedReports, key = { it.id.value }) { report ->
          SavedReport(
            modifier = Modifier.fillMaxWidth(),
            report = report,
            onAction = onAction,
          )
        }
      }

      items(WidgetType.known) { type ->
        WidgetType(
          modifier = Modifier.fillMaxWidth(),
          type = type,
          onAction = onAction,
        )
      }

      item { BottomSpacing() }
    }

    ChooseReportTypeDialogs(dialog, onAction)
  }
}

@Composable
private fun SavedReport(
  report: SavedCustomReport,
  onAction: ChooseReportTypeActionHandler,
  modifier: Modifier = Modifier,
) =
  Text(
    modifier =
      modifier
        .clip(CardShape)
        .background(colors.tableBackground, CardShape)
        .border(Hairline, colors.pillBorderDark, CardShape)
        .clickable { onAction(AddSavedReport(report.id)) }
        .padding(16.dp),
    text = report.name,
    color = colors.pageText,
    style = typography.titleMedium,
  )

@Composable
private fun WidgetType(
  type: WidgetType,
  onAction: ChooseReportTypeActionHandler,
  modifier: Modifier = Modifier,
) {
  val enabled = type.isEnabled()
  Column(
    modifier =
      modifier
        .clip(CardShape)
        .background(colors.tableBackground.disabledIf(!enabled), CardShape)
        .border(Hairline, colors.pillBorderDark, CardShape)
        .clickable(enabled) { onAction(Create(type)) }
        .padding(8.dp),
  ) {
    Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      Text(
        modifier = Modifier.weight(1f),
        text = type.string(),
        color = colors.pageText.disabledIf(!enabled),
        style = typography.titleLarge,
      )

      if (!enabled) {
        NormalIconButton(
          imageVector = MaterialIcons.Warning,
          contentDescription = Strings.reportsChooseTypeDisabled,
          onClick = { onAction(ShowDisabledDialog) },
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
    NetWorth -> PREVIEW_NET_WORTH_DATA
    CashFlow -> PREVIEW_CASH_FLOW_DATA
    Spending -> JUL_2025
    Custom -> PREVIEW_CUSTOM_DATA
    Markdown -> PREVIEW_SHORT_TEXT_DATA
    Summary -> PER_TRANSACTION_DATA
    Calendar -> THREE_MONTHS
    BudgetAnalysis -> TODO("https://github.com/jonapoul/aktual/issues/839")
    Formula -> PREVIEW_FORMULA_DATA
    Crossover -> PREVIEW_CROSSOVER_DATA
    Sankey -> PREVIEW_SANKEY_DATA
    BalanceForecast -> PREVIEW_BALANCE_FORECAST_DATA
    AgeOfMoney -> PREVIEW_AGE_OF_MONEY_DATA
    MonteCarlo -> PREVIEW_MONTE_CARLO_DATA
    Unknown -> error("No sample data for $this")
  }

@Stable
private fun WidgetType.isEnabled(): Boolean =
  when (this) {
    BudgetAnalysis,
    Custom,
    Unknown -> false

    NetWorth,
    CashFlow,
    Spending,
    Markdown,
    Summary,
    Calendar,
    Formula,
    Crossover,
    Sankey,
    BalanceForecast,
    MonteCarlo,
    AgeOfMoney -> true
  }

private val REPORT_HEIGHT = 250.dp

@PortraitPreview
@Composable
private fun PreviewChooseReportTypeScaffold(
  @PreviewParameter(ChooseReportTypeScaffoldParameters::class)
  params: ColoredParams<ChooseReportTypeScaffoldParams>,
) =
  PreviewWithColoredParams(params) {
    ChooseReportTypeScaffold(dialog = dialog, savedReports = savedReports, onAction = {})
  }

private data class ChooseReportTypeScaffoldParams(
  val dialog: ChooseReportTypeDialog?,
  val savedReports: ImmutableList<SavedCustomReport> = persistentListOf(),
)

private class ChooseReportTypeScaffoldParameters :
  ColoredParameterProvider<ChooseReportTypeScaffoldParams>(
    ChooseReportTypeScaffoldParams(dialog = null),
    ChooseReportTypeScaffoldParams(dialog = UnsupportedType),
    ChooseReportTypeScaffoldParams(
      dialog = null,
      savedReports =
        persistentListOf(
          SavedCustomReport(CustomReportId("groceries"), "Groceries by month"),
          SavedCustomReport(CustomReportId("spending"), "Spending by category"),
        ),
    ),
  )
