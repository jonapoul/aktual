package actual.budget.reports.ui

import actual.account.model.LoginToken
import actual.budget.model.BudgetId
import actual.budget.model.WidgetType
import actual.budget.reports.ui.charts.PreviewCalendar
import actual.budget.reports.ui.charts.PreviewCashFlow
import actual.budget.reports.ui.charts.PreviewCustom
import actual.budget.reports.ui.charts.PreviewNetWorth
import actual.budget.reports.ui.charts.PreviewSpending
import actual.budget.reports.ui.charts.PreviewSummary
import actual.budget.reports.ui.charts.PreviewText
import actual.budget.reports.ui.charts.ReportChart
import actual.budget.reports.vm.ChartData
import actual.budget.reports.vm.ChooseReportTypeViewModel
import actual.core.ui.ActualTypography
import actual.core.ui.CardShape
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewScreen
import actual.core.ui.ScreenPreview
import actual.core.ui.Theme
import actual.core.ui.WavyBackground
import actual.core.ui.defaultHazeStyle
import actual.core.ui.metroViewModel
import actual.core.ui.transparentTopAppBarColors
import actual.l10n.Strings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.collectLatest
import my.nanihadesuka.compose.LazyColumnScrollbar

@Composable
fun ChooseReportTypeScreen(
  nav: ChooseReportTypeNavigator,
  budgetId: BudgetId,
  token: LoginToken,
  viewModel: ChooseReportTypeViewModel = metroViewModel(budgetId),
) {
  BackHandler { nav.back() }

  LaunchedEffect(Unit) {
    viewModel.shouldNavigateEvent.collectLatest { event ->
      nav.toReport(token, budgetId, widgetId = event.id)
    }
  }

  ChooseReportTypeScaffold(
    onAction = { action ->
      when (action) {
        is ChooseReportTypeAction.Create -> viewModel.createReport(action.type)
      }
    },
  )
}

@Composable
private fun ChooseReportTypeScaffold(
  onAction: ChooseReportTypeActionListener,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = Scaffold(
  modifier = modifier,
  topBar = {
    TopAppBar(
      colors = theme.transparentTopAppBarColors(),
      title = { Text(Strings.reportsChooseTypeTitle) },
    )
  },
) { innerPadding ->
  Box {
    val hazeState = remember { HazeState() }

    WavyBackground(
      modifier = Modifier.hazeSource(hazeState),
    )

    Content(
      modifier = Modifier.padding(innerPadding),
      onAction = onAction,
      hazeState = hazeState,
      theme = theme,
    )
  }
}

@Composable
private fun Content(
  hazeState: HazeState,
  onAction: ChooseReportTypeActionListener,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  val scrollState = rememberLazyListState()
  val hazeStyle = defaultHazeStyle(theme)
  LazyColumnScrollbar(
    modifier = modifier,
    state = scrollState,
  ) {
    LazyColumn(
      modifier = Modifier.fillMaxWidth(),
      state = scrollState,
      verticalArrangement = Arrangement.spacedBy(8.dp),
      contentPadding = PaddingValues(8.dp),
    ) {
      items(WIDGET_TYPES) { type ->
        WidgetType(
          modifier = Modifier.fillMaxWidth(),
          type = type,
          onAction = onAction,
          theme = theme,
          hazeState = hazeState,
          hazeStyle = hazeStyle,
        )
      }
    }
  }
}

@Composable
private fun WidgetType(
  type: WidgetType,
  onAction: ChooseReportTypeActionListener,
  hazeState: HazeState,
  hazeStyle: HazeStyle,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = Column(
  modifier = modifier
    .background(Color.Transparent, CardShape)
    .hazeEffect(hazeState, hazeStyle)
    .clickable { onAction(ChooseReportTypeAction.Create(type)) }
    .padding(8.dp),
) {
  Text(
    modifier = Modifier.padding(8.dp),
    text = type.string(),
    color = theme.pageText,
    style = ActualTypography.titleLarge,
  )

  ReportChart(
    modifier = Modifier
      .fillMaxWidth()
      .height(REPORT_HEIGHT),
    data = type.sampleData(),
    compact = true,
    includeHeader = false,
    theme = theme,
    onAction = {}, // no-op
  )
}

@Composable
@ReadOnlyComposable
private fun WidgetType.string() = when (this) {
  WidgetType.NetWorth -> Strings.reportsChooseTypeNetWorth
  WidgetType.CashFlow -> Strings.reportsChooseTypeCashFlow
  WidgetType.Spending -> Strings.reportsChooseTypeSpending
  WidgetType.Custom -> Strings.reportsChooseTypeCustom
  WidgetType.Markdown -> Strings.reportsChooseTypeMarkdown
  WidgetType.Summary -> Strings.reportsChooseTypeSummary
  WidgetType.Calendar -> Strings.reportsChooseTypeCalendar
}

@Stable
private fun WidgetType.sampleData(): ChartData = when (this) {
  WidgetType.NetWorth -> PreviewNetWorth.DATA
  WidgetType.CashFlow -> PreviewCashFlow.DATA
  WidgetType.Spending -> PreviewSpending.JUL_2025
  WidgetType.Custom -> PreviewCustom.DATA
  WidgetType.Markdown -> PreviewText.SHORT_DATA
  WidgetType.Summary -> PreviewSummary.PER_TRANSACTION_DATA
  WidgetType.Calendar -> PreviewCalendar.THREE_MONTHS
}

@Composable
private fun metroViewModel(
  budgetId: BudgetId,
) = metroViewModel<ChooseReportTypeViewModel, ChooseReportTypeViewModel.Factory> {
  create(budgetId)
}

@Immutable
private sealed interface ChooseReportTypeAction {
  data class Create(val type: WidgetType) : ChooseReportTypeAction
}

@Immutable
private fun interface ChooseReportTypeActionListener {
  operator fun invoke(action: ChooseReportTypeAction)
}

private val WIDGET_TYPES = WidgetType.entries.toImmutableList()
private val REPORT_HEIGHT = 250.dp

@ScreenPreview
@Composable
private fun PreviewLoaded() = PreviewScreen {
  ChooseReportTypeScaffold(
    onAction = {},
  )
}
